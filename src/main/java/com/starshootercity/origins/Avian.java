package com.starshootercity.origins;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Avian implements Listener {

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String origin = OriginSwapper.getOrigin(player);
            if (origin == null) continue;
            OriginSwapper.runForOrigin(player, "Avian", () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, -1, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 0, false, false));
            });
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (Tag.BEDS.isTagged(event.getClickedBlock().getType())) {
            OriginSwapper.runForOrigin(event.getPlayer(), "Avian", () -> {
                if (event.getClickedBlock().getY() < 128) {String overworld = OriginsReborn.getInstance().getConfig().getString("worlds.world");
                    if (overworld == null) {
                        overworld = "world";
                        OriginsReborn.getInstance().getConfig().set("worlds.world", "world");
                        OriginsReborn.getInstance().saveConfig();
                    }
                    boolean isInOverworld = event.getPlayer().getWorld() == Bukkit.getWorld(overworld);

                    if (!isInOverworld) return;
                    if (event.getClickedBlock().getWorld().isDayTime() && event.getClickedBlock().getWorld().isClearWeather()) return;
                    event.setCancelled(true);
                    event.getPlayer().swingMainHand();
                    event.getPlayer().sendActionBar(Component.text("You can only sleep above Y 128"));
                }
            });
        }
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
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        OriginSwapper.runForOrigin(event.getPlayer(), "Avian", () -> {
            if (meat.contains(event.getItem().getType())) {
                event.setCancelled(true);
                ItemStack item = event.getItem();
                item.setAmount(item.getAmount() - 1);
                event.getPlayer().getInventory().setItem(event.getHand(), item);
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 600, 2, false, true));
            }
        });
    }
}
