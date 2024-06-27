package com.starshootercity.abilities;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.events.PlayerLeftClickEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Phantomize implements DependencyAbility, Listener {
    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isEnabled(player) && player.getFoodLevel() <= 6) {
                phantomizedPlayers.put(player, false);
                PhantomizeToggleEvent phantomizeToggleEvent = new PhantomizeToggleEvent(player, false);
                phantomizeToggleEvent.callEvent();
            }
        }
    }

    private final Map<Player, Boolean> phantomizedPlayers = new HashMap<>();

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:phantomize");
    }

    @Override
    public boolean isEnabled(Player player) {
        return phantomizedPlayers.getOrDefault(player, false);
    }

    @EventHandler
    public void onLeftClick(PlayerLeftClickEvent event) {
        if (event.hasBlock()) return;
        if (event.getPlayer().getFoodLevel() <= 6) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) return;
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            boolean enabling = !phantomizedPlayers.getOrDefault(event.getPlayer(), false);
            phantomizedPlayers.put(event.getPlayer(), enabling);
            PhantomizeToggleEvent phantomizeToggleEvent = new PhantomizeToggleEvent(event.getPlayer(), enabling);
            phantomizeToggleEvent.callEvent();
        });
    }

    @SuppressWarnings("unused")
    public static class PhantomizeToggleEvent extends PlayerEvent {
        private final boolean enabling;

        public PhantomizeToggleEvent(Player who, boolean enabling) {
            super(who);
            this.enabling = enabling;
        }

        public boolean isEnabling() {
            return enabling;
        }

        private static final HandlerList HANDLERS = new HandlerList();

        @Override
        public @NotNull HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }
}
