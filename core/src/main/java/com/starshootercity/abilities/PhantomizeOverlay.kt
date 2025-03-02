package com.starshootercity.abilities

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.abilities.Phantomize.PhantomizeToggleEvent
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PhantomizeOverlay : DependantAbility, Listener {
    override fun getKey(): Key {
        return Key.key("origins:phantomize_overlay")
    }

    override fun getDependencyKey(): Key {
        return Key.key("origins:phantomize")
    }

    @EventHandler
    fun onPhantomizeToggle(event: PhantomizeToggleEvent) {
        updatePhantomizeOverlay(event.getPlayer())
    }

    @EventHandler
    fun onPlayerPostRespawn(event: PlayerPostRespawnEvent) {
        updatePhantomizeOverlay(event.getPlayer())
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        updatePhantomizeOverlay(event.getPlayer())
    }

    private fun updatePhantomizeOverlay(player: Player?) {
        NMSInvoker.setWorldBorderOverlay(player, dependency.isEnabled(player))
    }
}
