package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
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
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        AbilityRegister.runForAbility(event.getEntity(), getKey(), () -> {
            if (!event.getEntity().isOnGround() && !event.isGliding()) {
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
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            if (event.isFlying()) {
                event.setCancelled(true);
                event.getPlayer().setGliding(true);
            }
        });
    }

    @Override
    public @NotNull TriState getFlyingFallDamage(Player player) {
        return TriState.TRUE;
    }
}
