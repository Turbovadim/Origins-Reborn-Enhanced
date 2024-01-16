package com.starshootercity.abilities;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
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

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.getPlayer().getPersistentDataContainer().set(phantomizeDroppingKey, PersistentDataType.BOOLEAN, true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> event.getPlayer().getPersistentDataContainer().set(phantomizeDroppingKey, PersistentDataType.BOOLEAN, false));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getFoodLevel() <= 6) return;
        if (Boolean.TRUE.equals(event.getPlayer().getPersistentDataContainer().get(phantomizeDroppingKey, PersistentDataType.BOOLEAN))) {
            event.getPlayer().getPersistentDataContainer().set(phantomizeDroppingKey, PersistentDataType.BOOLEAN, false);
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_AIR) {
            if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) return;
            AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
                boolean enabling = !phantomizedPlayers.getOrDefault(event.getPlayer(), false);
                phantomizedPlayers.put(event.getPlayer(), enabling);
                PhantomizeToggleEvent phantomizeToggleEvent = new PhantomizeToggleEvent(event.getPlayer(), enabling);
                phantomizeToggleEvent.callEvent();
            });
        }
    }

    NamespacedKey phantomizeDroppingKey = new NamespacedKey(OriginsReborn.getInstance(), "phantomize-dropping");

    private final Map<Player, Boolean> phantomizedPlayers = new HashMap<>();

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:phantomize");
    }

    @Override
    public boolean isEnabled(Player player) {
        return phantomizedPlayers.getOrDefault(player, false);
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
