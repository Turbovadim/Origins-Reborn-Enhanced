package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.bukkitDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.event.player.PlayerToggleSprintEvent
import org.endera.enderalib.utils.async.ioDispatcher

class LikeWater : VisibleAbility, FlightAllowingAbility, Listener {
    override fun getKey(): Key {
        return Key.key("origins:like_water")
    }

    override fun canFly(player: Player): Boolean {
        return player.isInWater && !player.isInBubbleColumn
    }

    override fun getFlightSpeed(player: Player?): Float {
        return 0.06f
    }

    @EventHandler
    fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
        runForAbility(event.getPlayer()) { player ->
            if (player.isInWater) player.isFlying = false
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        CoroutineScope(ioDispatcher).launch {
            val player = event.player
            if (!player.isInWater || player.isSwimming) return@launch

            val rising = event.to.y > event.from.y
            runForAbilityAsync(player) { p ->
                val isFlying = (p.isFlying || rising) && !p.isInBubbleColumn
                withContext(bukkitDispatcher) {
                    p.isFlying = isFlying
                }
            }
        }

    }

    @EventHandler
    fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) {
        if (!event.getPlayer().isFlying) return
        runForAbility(event.getPlayer()) { player ->
            if (player.isInWater) {
                player.isFlying = false
            }
        }
    }

    @EventHandler
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        runForAbility(event.getPlayer()) { player ->
            if (player!!.isInWater) event.isCancelled = true
        }
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "When underwater, you do not sink to the ground unless you want to.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Like Water", LineComponent.LineType.TITLE)
    }
}
