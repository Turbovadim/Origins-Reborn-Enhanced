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

class WaterVision : VisibleAbility, Listener {

    var storedEffects: MutableMap<Player, SavedPotionEffect> = HashMap()

    @EventHandler
    fun onServerTickEnd(event: ServerTickEndEvent?) {
        val currentTick = Bukkit.getCurrentTick()
        Bukkit.getOnlinePlayers().forEach { player ->
            runForAbility(player, AbilityRunner { player ->
                if (NMSInvoker.isUnderWater(player)) {
                    val currentEffect = player.getPotionEffect(PotionEffectType.NIGHT_VISION)
                    val ambient = currentEffect?.isAmbient == true
                    val showParticles = currentEffect?.hasParticles() == true

                    if (currentEffect != null && !isInfinite(currentEffect)) {
                        storedEffects[player] = SavedPotionEffect(currentEffect, currentTick)
                        player.removePotionEffect(PotionEffectType.NIGHT_VISION)
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
                    storedEffects.remove(player)?.let { saved ->
                        val potionEffect = saved.effect ?: return@let
                        val remainingDuration = potionEffect.duration - (currentTick - saved.currentTime)
                        if (remainingDuration > 0) {
                            player.addPotionEffect(
                                PotionEffect(
                                    potionEffect.type,
                                    remainingDuration,
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
        return Key.key("origins:water_vision")
    }

    override val description: MutableList<LineComponent> = makeLineFor("Your vision underwater is perfect.", LineComponent.LineType.DESCRIPTION)

    override val title: MutableList<LineComponent> = makeLineFor("Wet Eyes", LineComponent.LineType.TITLE)
}
