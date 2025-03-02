package com.starshootercity.abilities

import com.starshootercity.OriginSwapper
import com.starshootercity.OriginsReborn.Companion.instance
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

class AirFromPotions : Ability, Listener {
    override fun getKey(): Key {
        return Key.key("origins:air_from_potions")
    }

    var dehydrationKey: NamespacedKey = NamespacedKey(instance, "dehydrating")

    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        if (event.item.type != Material.POTION) return

        val player = event.player
        val pdc = player.persistentDataContainer

        pdc.set(dehydrationKey, OriginSwapper.BooleanPDT.BOOLEAN, true)
        player.remainingAir = (player.remainingAir + 60).coerceAtMost(player.maximumAir)
        pdc.set(dehydrationKey, OriginSwapper.BooleanPDT.BOOLEAN, false)
    }

}
