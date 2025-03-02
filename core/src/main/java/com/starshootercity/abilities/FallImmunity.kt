package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class FallImmunity : VisibleAbility, Listener {

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (event.cause != EntityDamageEvent.DamageCause.FALL) return

        runForAbility(event.entity, AbilityRunner { player ->
            event.isCancelled = true
        })
    }

    override fun getKey(): Key {
        return Key.key("origins:fall_immunity")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "You never take fall damage, no matter from which height you fall.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor(
            "Acrobatics",
            LineComponent.LineType.TITLE
        )
    }
}
