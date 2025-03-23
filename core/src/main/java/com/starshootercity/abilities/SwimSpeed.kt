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

class SwimSpeed : Listener, VisibleAbility {
    var storedEffects = HashMap<Player, SavedPotionEffect>()

    @EventHandler
    fun onServerTickEnd(event: ServerTickEndEvent) {
        if (event.tickNumber %6 != 0) return
        val currentTick = Bukkit.getCurrentTick()
        val dolphinGrace = PotionEffectType.DOLPHINS_GRACE

        for (player in Bukkit.getOnlinePlayers()) {
            runForAbility(player, AbilityRunner { p ->
                if (NMSInvoker.isUnderWater(p)) {
                    val effect = p.getPotionEffect(dolphinGrace)
                    val ambient = effect?.isAmbient == true
                    val showParticles = effect?.hasParticles() == true

                    if (effect != null && !isInfinite(effect)) {
                        storedEffects[p] = SavedPotionEffect(effect, currentTick)
                        p.removePotionEffect(dolphinGrace)
                    }
                    p.addPotionEffect(
                        PotionEffect(
                            dolphinGrace,
                            infiniteDuration(),
                            -1,
                            ambient,
                            showParticles
                        )
                    )
                } else {
                    if (p.hasPotionEffect(dolphinGrace)) {
                        p.getPotionEffect(dolphinGrace)?.let { effect ->
                            if (isInfinite(effect)) {
                                p.removePotionEffect(dolphinGrace)
                            }
                        }
                    }
                    storedEffects.remove(p)?.let { saved ->
                        val original = saved.effect ?: return@let
                        val remainingTime = original.duration - (currentTick - saved.currentTime)
                        if (remainingTime > 0) {
                            p.addPotionEffect(
                                PotionEffect(
                                    original.type,
                                    remainingTime,
                                    original.amplifier,
                                    original.isAmbient,
                                    original.hasParticles()
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
        return Key.key("origins:swim_speed")
    }

    override val description: MutableList<LineComponent> = makeLineFor("Your underwater speed is increased.", LineComponent.LineType.DESCRIPTION)

    override val title: MutableList<LineComponent> = makeLineFor("Fins", LineComponent.LineType.TITLE)
}
