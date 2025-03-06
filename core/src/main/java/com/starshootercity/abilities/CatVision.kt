package com.starshootercity.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.SavedPotionEffect
import com.starshootercity.ShortcutUtils.infiniteDuration
import com.starshootercity.ShortcutUtils.isInfinite
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

open class CatVision : VisibleAbility, Listener {
    val storedEffects = mutableMapOf<Player, SavedPotionEffect>()

    @EventHandler
    fun onServerTickEnd(event: ServerTickEndEvent?) {
        for (player in Bukkit.getOnlinePlayers()) {
            runForAbility(player, AbilityRunner { player ->
                if (!NMSInvoker.isUnderWater(player)) {
                    val currentEffect = player.getPotionEffect(PotionEffectType.NIGHT_VISION)
                    val ambient = currentEffect?.isAmbient == true
                    val showParticles = currentEffect?.hasParticles() == true

                    currentEffect?.let {
                        if (!isInfinite(it)) {
                            storedEffects[player] = SavedPotionEffect(it, Bukkit.getCurrentTick())
                            player.removePotionEffect(PotionEffectType.NIGHT_VISION)
                        }
                    }
                    player.addPotionEffect(
                        PotionEffect(
                            PotionEffectType.NIGHT_VISION,
                            infiniteDuration(),
                            -1,
                            ambient,
                            showParticles
                        )
                    )
                } else {
                    player.getPotionEffect(PotionEffectType.NIGHT_VISION)?.let { effect ->
                        if (isInfinite(effect)) {
                            player.removePotionEffect(PotionEffectType.NIGHT_VISION)
                        }
                    }
                    storedEffects.remove(player)?.let { savedEffect ->
                        val potionEffect = savedEffect.effect!!
                        val timeLeft = potionEffect.duration - (Bukkit.getCurrentTick() - savedEffect.currentTime)
                        if (timeLeft > 0) {
                            player.addPotionEffect(
                                PotionEffect(
                                    potionEffect.type,
                                    timeLeft,
                                    potionEffect.amplifier,
                                    potionEffect.isAmbient,
                                    potionEffect.hasParticles()
                                )
                            )
                        }
                    }
                }
            })
        }
    }

    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        if (event.item.type == Material.MILK_BUCKET) {
            storedEffects.remove(event.getPlayer())
        }
    }

    override fun getKey(): Key {
        return Key.key("origins:cat_vision")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor("You can slightly see in the dark when not in water.", LineComponent.LineType.DESCRIPTION)
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Nocturnal", LineComponent.LineType.TITLE)
    }
}
