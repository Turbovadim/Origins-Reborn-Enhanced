package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import net.kyori.adventure.key.Key
import org.bukkit.event.Listener

class MoreKineticDamage : VisibleAbility, Listener {
    override fun getKey(): Key {
        return Key.key("origins:more_kinetic_damage")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "You take more damage from falling and flying into blocks.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Brittle Bones", LineComponent.LineType.TITLE)
    }
}
