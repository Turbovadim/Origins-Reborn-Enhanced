package com.starshootercity.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.SavedPotionEffect
import com.starshootercity.ShortcutUtils.infiniteDuration
import com.starshootercity.ShortcutUtils.isInfinite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.endera.enderalib.utils.async.ioDispatcher

open class CatVision : VisibleAbility, Listener {
    val storedEffects = mutableMapOf<Player, SavedPotionEffect>()

    @EventHandler
    fun onServerTickEnd(event: ServerTickEndEvent?) {
        CoroutineScope(ioDispatcher).launch {
            for (player in Bukkit.getOnlinePlayers().toList()) {
                runForAbilityAsync(player) { player ->
                    if (!NMSInvoker.isUnderWater(player)) {
                        val currentEffect = withContext(OriginsReborn.bukkitDispatcher) {
                            player.getPotionEffect(PotionEffectType.NIGHT_VISION)
                        }
                        val ambient = currentEffect?.isAmbient == true
                        val showParticles = currentEffect?.hasParticles() == true

                        currentEffect?.let {
                            if (!isInfinite(it)) {
                                storedEffects[player] = SavedPotionEffect(it, Bukkit.getCurrentTick())
                                withContext(OriginsReborn.bukkitDispatcher) {
                                    player.removePotionEffect(PotionEffectType.NIGHT_VISION)
                                }
                            }
                        }
                        withContext(OriginsReborn.bukkitDispatcher) {
                            player.addPotionEffect(
                                PotionEffect(
                                    PotionEffectType.NIGHT_VISION,
                                    infiniteDuration(),
                                    -1,
                                    ambient,
                                    showParticles

                                )
                            )
                        }
                    } else {
                        withContext(OriginsReborn.bukkitDispatcher) {
                            player.getPotionEffect(PotionEffectType.NIGHT_VISION)?.let { effect ->
                                if (isInfinite(effect)) {
                                    player.removePotionEffect(PotionEffectType.NIGHT_VISION)
                                }
                            }
                            storedEffects.remove(player)?.let { savedEffect ->
                                val potionEffect = savedEffect.effect!!
                                val timeLeft =
                                    potionEffect.duration - (Bukkit.getCurrentTick() - savedEffect.currentTime)
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
                    }
                }
            }
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

    override val description: MutableList<LineComponent?> = makeLineFor(
        "You can slightly see in the dark when not in water.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent?> = makeLineFor(
        "Nocturnal",
        LineComponent.LineType.TITLE
    )
}
