package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.World

class NetherSpawn : DefaultSpawnAbility, VisibleAbility {
    override fun getKey(): Key {
        return Key.key("origins:nether_spawn")
    }

    override val world: World?
        get() {
            val nether = OriginsReborn.mainConfig.worlds.worldNether
            return Bukkit.getWorld(nether) ?: Bukkit.getWorld("world_nether")
        }


    override val description: MutableList<LineComponent?> = makeLineFor("Your natural spawn will be in the Nether.", LineComponent.LineType.DESCRIPTION)

    override val title: MutableList<LineComponent?> = makeLineFor("Nether Inhabitant", LineComponent.LineType.TITLE)
}
