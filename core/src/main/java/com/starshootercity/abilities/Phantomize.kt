package com.starshootercity.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import com.starshootercity.abilities.Ability.AbilityRunner
import com.starshootercity.events.PlayerLeftClickEvent
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import java.util.*

class Phantomize : DependencyAbility, Listener {

    @EventHandler
    fun onServerTickEnd(event: ServerTickEndEvent?) {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (isEnabled(player) && player.foodLevel <= 6) {
                phantomizedPlayers[player.uniqueId] = false
                PhantomizeToggleEvent(player, false).callEvent()
            }
        }
    }


    private val phantomizedPlayers: MutableMap<UUID?, Boolean?> = HashMap<UUID?, Boolean?>()

    override fun getKey(): Key {
        return Key.key("origins:phantomize")
    }

    override fun isEnabled(player: Player): Boolean {
        return phantomizedPlayers.getOrDefault(player.uniqueId, false)!!
    }

    @EventHandler
    fun onLeftClick(event: PlayerLeftClickEvent) {
        val player = event.player
        if (event.hasBlock() || player.foodLevel <= 6 || player.inventory.itemInMainHand.type != Material.AIR) return
        runForAbility(player, AbilityRunner { p ->
            val enabling = phantomizedPlayers.getOrDefault(p.uniqueId, false)!!
            phantomizedPlayers[p.uniqueId] = enabling
            PhantomizeToggleEvent(p, enabling).callEvent()
        })
    }


    @Suppress("unused")
    class PhantomizeToggleEvent(who: Player, val isEnabling: Boolean) : PlayerEvent(who) {
        override fun getHandlers(): HandlerList {
            return handlerList
        }

        companion object {
            val handlerList: HandlerList = HandlerList()
        }
    }
}
