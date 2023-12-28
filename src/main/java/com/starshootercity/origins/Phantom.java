package com.starshootercity.origins;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import com.starshootercity.OldOriginSwapper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("unused")
public class Phantom implements Listener {

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            OldOriginSwapper.runForOrigin(player, "Phantom",
                    () -> {
                        player.setInvisible(player.isSneaking() || player.getPotionEffect(PotionEffectType.INVISIBILITY) != null);
                        boolean height = player.getWorld().getHighestBlockAt(player.getLocation()).getY() < player.getY();
                        String overworld = OriginsReborn.getInstance().getConfig().getString("worlds.world");
                        if (overworld == null) {
                            overworld = "world";
                            OriginsReborn.getInstance().getConfig().set("worlds.world", "world");
                            OriginsReborn.getInstance().saveConfig();
                        }
                        boolean isInOverworld = player.getWorld() == Bukkit.getWorld(overworld);
                        boolean day = player.getWorld().isDayTime();
                        if (!player.isSneaking() && height && isInOverworld && day && player.getWorld().isClearWeather()) {
                            player.setFireTicks(Math.max(player.getFireTicks(), 60));
                        }
                        if (player.isSneaking()) player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 1, 9, false, false));
                    },
                    () -> player.setInvisible(player.getPotionEffect(PotionEffectType.INVISIBILITY) != null));
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.getPlayer().getPersistentDataContainer().set(phantomDroppingKey, PersistentDataType.BOOLEAN, true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> event.getPlayer().getPersistentDataContainer().set(phantomDroppingKey, PersistentDataType.BOOLEAN, false));
    }

    NamespacedKey phantomDroppingKey = new NamespacedKey(OriginsReborn.getInstance(), "dropping");

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (Boolean.TRUE.equals(event.getPlayer().getPersistentDataContainer().get(phantomDroppingKey, PersistentDataType.BOOLEAN))) {
            event.getPlayer().getPersistentDataContainer().set(phantomDroppingKey, PersistentDataType.BOOLEAN, false);
            return;
        }
        if (event.getClickedBlock() == null) return;
        if (event.getAction().isRightClick()) return;
        if (!event.getPlayer().isSneaking()) return;
        OldOriginSwapper.runForOrigin(event.getPlayer(), "Phantom", () -> {
            Block block = event.getClickedBlock();
            double distance = block.getLocation().distance(event.getPlayer().getLocation());
            if (distance > 2.2) return;
            if (block.getY() != event.getPlayer().getLocation().getBlock().getY()+1) return;
            int count = 0;
            if (!event.getClickedBlock().isSolid()) return;
            while (block.isSolid() || block.getRelative(BlockFace.DOWN).isSolid()) {
                block = block.getRelative(event.getBlockFace().getOppositeFace());
                count += 1;
                if (block.getType() == Material.OBSIDIAN || block.getType() == Material.BEDROCK) return;
                if (count > 3) return;
            }
            Location loc = block.getLocation();
            loc.subtract(-0.5, 1, -0.5);
            loc.setPitch(event.getPlayer().getPitch());
            loc.setYaw(event.getPlayer().getYaw());
            event.getPlayer().teleport(loc);
        });
    }
}
