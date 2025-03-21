package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.instance
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.World

class NetherSpawn : DefaultSpawnAbility, VisibleAbility {
    override fun getKey(): Key {
        return Key.key("origins:nether_spawn")
    }

    override fun getWorld(): World? {
        val config = instance.config
        val nether = config.getString("worlds.world_nether") ?: "world_nether".also {
            config.set("worlds.world_nether", it)
            instance.saveConfig()
        }
        return Bukkit.getWorld(nether)
    }


    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor("Your natural spawn will be in the Nether.", LineComponent.LineType.DESCRIPTION)
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Nether Inhabitant", LineComponent.LineType.TITLE)
    }
}
