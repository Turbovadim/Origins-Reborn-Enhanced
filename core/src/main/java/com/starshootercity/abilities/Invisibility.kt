package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player

class Invisibility : DependantAbility, VisibleAbility, VisibilityChangingAbility {

    override fun getKey(): Key {
        return Key.key("origins:invisibility")
    }

    override val dependencyKey: Key = Key.key("origins:phantomize")

    override val description: MutableList<LineComponent?> = makeLineFor(
        "While phantomized, you are invisible.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent?> = makeLineFor(
        "Invisibility",
        LineComponent.LineType.TITLE
    )

    override fun isInvisible(player: Player): Boolean {
        return dependency.isEnabled(player)
    }
}
