package com.starshootercity.abilities;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.key.Key;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
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
        WorldBorder border = new WorldBorder();
        border.setWarningBlocks(getDependency().isEnabled(player) ? (int) player.getWorld().getWorldBorder().getSize() * 2 : player.getWorld().getWorldBorder().getWarningDistance());
        ClientboundSetBorderWarningDistancePacket warningDistancePacket = new ClientboundSetBorderWarningDistancePacket(border);
        ((CraftPlayer) player).getHandle().connection.send(warningDistancePacket);
    }
}
