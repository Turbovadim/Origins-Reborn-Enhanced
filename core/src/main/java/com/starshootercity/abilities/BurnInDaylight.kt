package com.starshootercity.abilities

import com.destroystokyo.paper.MaterialTags
import com.destroystokyo.paper.event.server.ServerTickEndEvent
import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.abilities.DependantAbility.DependencyType
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

open class BurnInDaylight : VisibleAbility, DependantAbility, Listener {
    override fun getDependencyType(): DependencyType {
        return DependencyType.INVERSE
    }

    @EventHandler
    fun onServerTickEnd(event: ServerTickEndEvent) {
        if (event.tickNumber % 15 != 0) return

        Bukkit.getOnlinePlayers().forEach { player ->
            runForAbility(player) { player ->
                val world = player.world
                val loc = player.location
                val playerY = loc.y
                var block = world.getHighestBlockAt(loc)

                while ((MaterialTags.GLASS.isTagged(block) || MaterialTags.GLASS_PANES.isTagged(block)) && block.y >= playerY) {
                    block = block.getRelative(BlockFace.DOWN)
                }

                val isBelowPlayer = block.y < playerY
                if (isBelowPlayer && world.environment == World.Environment.NORMAL && world.isDayTime && !player.isInWaterOrRainOrBubbleColumn) {
                    player.fireTicks = player.fireTicks.coerceAtLeast(60)
                }
            }
        }
    }


    override fun getKey(): Key {
        return Key.key("origins:burn_in_daylight")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "You begin to burn in daylight if you are not invisible.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Photoallergic", LineComponent.LineType.TITLE)
    }

    override fun getDependencyKey(): Key {
        return Key.key("origins:phantomize")
    }
}
