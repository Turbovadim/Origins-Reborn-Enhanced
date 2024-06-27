package com.starshootercity.abilities;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class PhantomizeOverlay implements DependantAbility, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:phantomize_overlay");
    }

    @Override
    public @NotNull Key getDependencyKey() {
        return Key.key("origins:phantomize");
    }

    @EventHandler
    public void onPhantomizeToggle(Phantomize.PhantomizeToggleEvent event) {
        updatePhantomizeOverlay(event.getPlayer());
    }

    @EventHandler
    public void onPlayerPostRespawn(PlayerPostRespawnEvent event) {
        updatePhantomizeOverlay(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updatePhantomizeOverlay(event.getPlayer());
    }

    private void updatePhantomizeOverlay(Player player) {
        if (getDependency().isEnabled(player)) {
            WorldBorder border = Bukkit.createWorldBorder();
            border.setCenter(player.getWorld().getWorldBorder().getCenter());
            border.setSize(player.getWorld().getWorldBorder().getSize());
            border.setWarningDistance(player.getWorld().getWorldBorder().getWarningDistance()*2);
        } else player.setWorldBorder(null);
    }
}
