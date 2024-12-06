package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import com.starshootercity.commands.FlightToggleCommand;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Elytra implements VisibleAbility, FlightAllowingAbility, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:elytra");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You have Elytra wings without needing to equip any.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Winged", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        runForAbility(event.getEntity(), player -> {
            if (!player.isOnGround() && !event.isGliding()) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    public boolean canFly(Player player) {
        return !player.isGliding();
    }

    @Override
    public float getFlightSpeed(Player player) {
        return player.getFlySpeed();
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (FlightToggleCommand.canFly(event.getPlayer())) return;
        runForAbility(event.getPlayer(), player -> {
            if (event.isFlying()) {
                event.setCancelled(true);
                player.setGliding(true);
            }
        });
    }

    @Override
    public @NotNull TriState getFlyingFallDamage(Player player) {
        return TriState.TRUE;
    }
}
