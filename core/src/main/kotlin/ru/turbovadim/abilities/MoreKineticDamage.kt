package ru.turbovadim.abilities

import ru.turbovadim.OriginSwapper.LineData.Companion.makeLineFor
import ru.turbovadim.OriginSwapper.LineData.LineComponent
import net.kyori.adventure.key.Key
import org.bukkit.event.Listener

class MoreKineticDamage : VisibleAbility, Listener {
    override fun getKey(): Key {
        return Key.key("origins:more_kinetic_damage")
    }

    override val description: MutableList<LineComponent> = makeLineFor(
        "You take more damage from falling and flying into blocks.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent> = makeLineFor(
        "Brittle Bones",
        LineComponent.LineType.TITLE
    )
}
