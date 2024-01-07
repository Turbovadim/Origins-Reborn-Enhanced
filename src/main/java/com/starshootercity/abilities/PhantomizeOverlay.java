package com.starshootercity.abilities;

import net.kyori.adventure.key.Key;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
        WorldBorder border = new WorldBorder();
        border.setWarningBlocks(event.isEnabling() ? (int) event.getPlayer().getWorld().getWorldBorder().getSize() * 2 : event.getPlayer().getWorld().getWorldBorder().getWarningDistance());
        ClientboundSetBorderWarningDistancePacket warningDistancePacket = new ClientboundSetBorderWarningDistancePacket(border);
        ((CraftPlayer) event.getPlayer()).getHandle().connection.send(warningDistancePacket);
    }
}
