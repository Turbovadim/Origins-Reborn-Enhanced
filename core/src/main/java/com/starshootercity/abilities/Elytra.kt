package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.abilities.Ability.AbilityRunner
import com.starshootercity.commands.FlightToggleCommand
import net.kyori.adventure.key.Key
import net.kyori.adventure.util.TriState
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerToggleFlightEvent

class Elytra : VisibleAbility, FlightAllowingAbility, Listener {
    override fun getKey(): Key {
        return Key.key("origins:elytra")
    }

    override val description: MutableList<LineComponent?> = makeLineFor(
        "You have Elytra wings without needing to equip any.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent?> = makeLineFor(
        "Winged",
        LineComponent.LineType.TITLE
    )

    @EventHandler
    fun onEntityToggleGlide(event: EntityToggleGlideEvent) {
        runForAbility(event.entity, AbilityRunner { player ->
            if (!player.isOnGround && !event.isGliding) {
                event.isCancelled = true
            }
        })
    }


    override fun canFly(player: Player): Boolean {
        return true
    }

    override fun getFlightSpeed(player: Player): Float {
        return player.flySpeed
    }

    @EventHandler
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        val player = event.player
        if (FlightToggleCommand.canFly(player)) return

        runForAbility(player, AbilityRunner { p ->
            if (event.isFlying) {
                event.isCancelled = true
                p.isGliding = !p.isGliding
            }
        })
    }

    override fun getFlyingFallDamage(player: Player): TriState {
        return TriState.TRUE
    }
}
