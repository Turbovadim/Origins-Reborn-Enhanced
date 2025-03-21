package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.GameEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.GenericGameEvent

class VelvetPaws : VisibleAbility, Listener {
    override fun getKey(): Key {
        return Key.key("origins:velvet_paws")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "Your footsteps don't cause any vibrations which could otherwise be picked up by nearby lifeforms.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Velvet Paws", LineComponent.LineType.TITLE)
    }

    @EventHandler
    fun onGenericGameEvent(event: GenericGameEvent) {
        if (event.event == GameEvent.STEP) {
            runForAbility(event.entity!!, AbilityRunner { player: Player? -> event.isCancelled = true })
        }
    }
}
