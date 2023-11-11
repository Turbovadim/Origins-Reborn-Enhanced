package com.starshootercity.origins;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import com.starshootercity.OriginSwapper;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("unused")
public class Enderian implements Listener {
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.getPlayer().getPersistentDataContainer().set(enderianDroppingKey, PersistentDataType.BOOLEAN, true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> event.getPlayer().getPersistentDataContainer().set(enderianDroppingKey, PersistentDataType.BOOLEAN, false));
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (Boolean.TRUE.equals(event.getPlayer().getPersistentDataContainer().get(enderianDroppingKey, PersistentDataType.BOOLEAN))) {
            event.getPlayer().getPersistentDataContainer().set(enderianDroppingKey, PersistentDataType.BOOLEAN, false);
            return;
        }
        if (event.getClickedBlock() != null) return;
        if (event.getAction().isRightClick()) return;
        if (event.getItem() != null) return;
        OriginSwapper.runForOrigin(event.getPlayer(), "Enderian", () -> event.getPlayer().launchProjectile(EnderPearl.class));
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        OriginSwapper.runForOrigin(event.getPlayer(), "Enderian", () -> {
            event.getPlayer().getPersistentDataContainer().set(teleportingKey, PersistentDataType.BOOLEAN, true);
            Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> event.getPlayer().getPersistentDataContainer().set(teleportingKey, PersistentDataType.BOOLEAN, false));
        });
    }

    NamespacedKey teleportingKey = new
            NamespacedKey(OriginsReborn.getInstance(), "teleporting");
    NamespacedKey enderianDroppingKey = new NamespacedKey(OriginsReborn.getInstance(), "dropping");

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(teleportingKey, PersistentDataType.BOOLEAN))) {
                event.setCancelled(true);
                player.getPersistentDataContainer().set(teleportingKey, PersistentDataType.BOOLEAN, false);
            }
        }
    }

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            OriginSwapper.runForOrigin(player, "Enderian", () -> {
                double temp = player.getLocation().getBlock().getTemperature();
                boolean height = player.getWorld().getHighestBlockAt(player.getLocation()).getY() < player.getY();
                if (((CraftPlayer) player).getHandle().wasTouchingWater || (!player.getWorld().isClearWeather() && height && temp > 0.15 && temp < 0.95)) {
                    DamageSource source = ((CraftPlayer) player).getHandle().damageSources().freeze();
                    ((CraftPlayer) player).getHandle().hurt(source, 2);
                }
            });
        }
    }
}
