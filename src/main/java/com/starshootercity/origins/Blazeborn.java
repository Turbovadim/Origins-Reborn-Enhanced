package com.starshootercity.origins;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OldOriginSwapper;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class Blazeborn implements Listener {

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            OldOriginSwapper.runForOrigin(player, "Blazeborn", () -> {
                double temp = player.getLocation().getBlock().getTemperature();
                boolean height = player.getWorld().getHighestBlockAt(player.getLocation()).getY() < player.getY();
                if (((CraftPlayer) player).getHandle().wasTouchingWater || (!player.getWorld().isClearWeather() && height && temp > 0.15 && temp < 0.95)) {
                    DamageSource source = ((CraftPlayer) player).getHandle().damageSources().freeze();
                    ((CraftPlayer) player).getHandle().hurt(source, 2);
                }
                player.removePotionEffect(PotionEffectType.HUNGER);
                player.removePotionEffect(PotionEffectType.POISON);
            });
        }
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player trueDamager;
        if (event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player player) {
                trueDamager = player;
            } else return;
        } else if (event.getDamager() instanceof Player player) {
            trueDamager = player;
        } else return;
        OldOriginSwapper.runForOrigin(trueDamager, "Blazeborn", () -> {
            if (trueDamager.getFireTicks() > 0) {
                event.setDamage(event.getDamage() * 2);
            }
        });
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            OldOriginSwapper.runForOrigin(player, "Blazeborn", () -> {
                if (new ArrayList<>() {{
                    add(EntityDamageEvent.DamageCause.FIRE);
                    add(EntityDamageEvent.DamageCause.FIRE_TICK);
                    add(EntityDamageEvent.DamageCause.LAVA);
                    add(EntityDamageEvent.DamageCause.HOT_FLOOR);
                }}.contains(event.getCause())) event.setCancelled(true);
            });
        }
    }
}
