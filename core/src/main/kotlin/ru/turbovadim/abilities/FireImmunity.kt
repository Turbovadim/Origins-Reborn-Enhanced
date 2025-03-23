package ru.turbovadim.abilities

import ru.turbovadim.OriginSwapper.LineData.Companion.makeLineFor
import ru.turbovadim.OriginSwapper.LineData.LineComponent
import ru.turbovadim.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class FireImmunity : VisibleAbility, Listener {

    @EventHandler
    fun onEntityDamageEvent(event: EntityDamageEvent) {
        val fireCauses = setOf(
            EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.LAVA,
            EntityDamageEvent.DamageCause.HOT_FLOOR
        )

        if (event.cause in fireCauses) {
            runForAbility(event.entity, AbilityRunner { _ ->
                event.isCancelled = true
            })
        }
    }

    override fun getKey(): Key {
        return Key.key("origins:fire_immunity")
    }

    override val description: MutableList<LineComponent> = makeLineFor(
        "You are immune to all types of fire damage.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent> = makeLineFor(
        "Fire Immunity",
        LineComponent.LineType.TITLE
    )
}
