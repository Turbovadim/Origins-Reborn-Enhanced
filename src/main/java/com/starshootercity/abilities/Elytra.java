package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Elytra implements VisibleAbility, Listener {
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
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            if (!event.getPlayer().isOnGround() && !event.getPlayer().isGliding()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> event.getPlayer().setGliding(true));
            }
        });
    }

    @EventHandler
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        AbilityRegister.runForAbility(event.getEntity(), getKey(), () -> {
            if (!event.getEntity().isOnGround() && !event.isGliding()) {
                event.setCancelled(true);
            }
        });
    }
}
