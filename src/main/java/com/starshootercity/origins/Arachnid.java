package com.starshootercity.origins;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import com.starshootercity.OldOriginSwapper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Arachnid implements Listener {
    NamespacedKey stoppedClimbingKey = new NamespacedKey(OriginsReborn.getInstance(), "stoppedclimbing");
    NamespacedKey startedClimbingKey = new NamespacedKey(OriginsReborn.getInstance(), "startedclimbing");

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                player.setAllowFlight(true);
                continue;
            }
            OldOriginSwapper.runForOrigin(player, "Arachnid", () -> {
                boolean anySolid = false;
                boolean anysolidAbove = false;
                for (BlockFace face : new ArrayList<BlockFace>() {{
                    add(BlockFace.WEST);
                    add(BlockFace.EAST);
                    add(BlockFace.NORTH);
                    add(BlockFace.SOUTH);
                }}) {
                    anySolid = player.getLocation().getBlock().getRelative(face).isSolid();
                    anysolidAbove = player.getLocation().getBlock().getRelative(BlockFace.UP).getRelative(face).isSolid();
                    if (anySolid) break;
                }
                player.setAllowFlight(anySolid);
                if (player.getAllowFlight() && anysolidAbove) {
                    if (!Boolean.TRUE.equals(player.getPersistentDataContainer().get(stoppedClimbingKey, PersistentDataType.BOOLEAN))) {
                        if (!player.isOnGround()) player.setFlying(true);
                    } else {
                        if (player.isOnGround()) player.getPersistentDataContainer().set(stoppedClimbingKey, PersistentDataType.BOOLEAN, false);
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!event.isFlying()) {
            Long time = event.getPlayer().getPersistentDataContainer().get(startedClimbingKey, PersistentDataType.LONG);
            if (time != null) {
                if (Instant.now().getEpochSecond() - time < 2) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        event.getPlayer().getPersistentDataContainer().set(stoppedClimbingKey, PersistentDataType.BOOLEAN, !event.isFlying());
    }
    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        event.getPlayer().getPersistentDataContainer().set(startedClimbingKey, PersistentDataType.LONG, Instant.now().getEpochSecond());
    }

    List<Material> meat = new ArrayList<>() {{
        add(Material.PORKCHOP);
        add(Material.COOKED_PORKCHOP);
        add(Material.BEEF);
        add(Material.COOKED_BEEF);
        add(Material.CHICKEN);
        add(Material.COOKED_CHICKEN);
        add(Material.RABBIT);
        add(Material.COOKED_RABBIT);
        add(Material.MUTTON);
        add(Material.COOKED_MUTTON);
    }};

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            OldOriginSwapper.runForOrigin(player, "Arachnid", () -> {
                if (!event.getEntity().getLocation().getBlock().isSolid()) {
                    event.getEntity().getLocation().getBlock().setType(Material.COBWEB);
                }
            });
        }
    }


    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        OldOriginSwapper.runForOrigin(event.getPlayer(), "Arachnid", () -> {
            if (!meat.contains(event.getItem().getType())) {
                event.setCancelled(true);
                ItemStack item = event.getItem();
                item.setAmount(item.getAmount() - 1);
                event.getPlayer().getInventory().setItem(event.getHand(), item);
            }
        });
    }
}
