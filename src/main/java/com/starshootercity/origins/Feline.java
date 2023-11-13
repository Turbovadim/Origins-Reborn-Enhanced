package com.starshootercity.origins;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Feline implements Listener {
    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            OriginSwapper.runForOrigin(player, "Feline",
                    () -> {
                        if (player.isSprinting()) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 5, 1, false, false));
                        }
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, -1, 0, false, false));
                    });
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            OriginSwapper.runForOrigin(player, "Feline", () -> {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) event.setCancelled(true);
            });
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Creeper creeper) {
            if (event.getTarget() instanceof Player player) {
                OriginSwapper.runForOrigin(player, "Feline", () -> {
                    List<Creeper> attacked = attackedCreepers.get(player);
                    if (attacked == null) {
                        event.setCancelled(true);
                        return;
                    }
                    if (!attacked.contains(creeper)) event.setCancelled(true);
                });
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Creeper creeper) {
            Player trueDamager;
            if (event.getDamager() instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Player player) {
                    trueDamager = player;
                } else return;
            } else if (event.getDamager() instanceof Player player) {
                trueDamager = player;
            } else return;
            attackedCreepers.computeIfAbsent(trueDamager, k -> new ArrayList<>());
            attackedCreepers.get(trueDamager).add(creeper);
        }
    }

    Map<Player, List<Creeper>> attackedCreepers = new HashMap<>();
}
