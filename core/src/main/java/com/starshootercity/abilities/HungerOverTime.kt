package com.starshootercity.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class HungerOverTime : DependantAbility, VisibleAbility, Listener {
    override fun getKey(): Key {
        return Key.key("origins:hunger_over_time")
    }

    override val description: MutableList<LineComponent> = makeLineFor(
        "Being phantomized causes you to become hungry.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent> = makeLineFor(
        "Fast Metabolism",
        LineComponent.LineType.TITLE
    )

    override val dependencyKey: Key = Key.key("origins:phantomize")

    @EventHandler
    fun onServerTickEnd(event: ServerTickEndEvent) {
        if (event.tickNumber % 20 != 0) return

        Bukkit.getOnlinePlayers().forEach { player ->
            runForAbility(player, AbilityRunner { it.exhaustion += 0.812f })
        }
    }

}
