package com.starshootercity.abilities

import com.starshootercity.AddonLoader.getTextFor
import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class FreshAir : VisibleAbility, Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        if (!event.action.isRightClick) return

        val player = event.player
        val inventory = player.inventory
        if (player.isSneaking &&
            inventory.itemInOffHand.type == Material.AIR &&
            inventory.itemInMainHand.type == Material.AIR
        ) return

        if (!Tag.BEDS.isTagged(clickedBlock.type)) return

        runForAbility(player, AbilityRunner { p ->
            if (clickedBlock.y >= 86) return@AbilityRunner

            val config = instance.config
            val overworld = config.getString("worlds.world") ?: "world".also {
                config.set("worlds.world", it)
                instance.saveConfig()
            }

            val world = Bukkit.getWorld(overworld) ?: return@AbilityRunner
            if (p.world != world) return@AbilityRunner

            val blockWorld = clickedBlock.world
            if (blockWorld.isDayTime && blockWorld.isClearWeather) return@AbilityRunner

            event.isCancelled = true
            p.swingMainHand()
            p.sendActionBar(
                Component.text(
                    getTextFor("origins.avian_sleep_fail", "You need fresh air to sleep")
                )
            )
        })
    }


    override fun getKey(): Key {
        return Key.key("origins:fresh_air")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "When sleeping, your bed needs to be at an altitude of at least ${instance.getConfig().getInt("extra-settings.fresh-air-required-sleep-height", 86)} blocks, so you can breathe fresh air.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Fresh Air", LineComponent.LineType.TITLE)
    }
}
