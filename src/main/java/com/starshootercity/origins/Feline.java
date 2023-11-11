package com.starshootercity.origins;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
            OriginSwapper.runForOrigin(player, "Blazeborn", () -> {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) event.setCancelled(true);
            });
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Creeper) {
            if (event.getTarget() instanceof Player) {
                if (event.getReason() != EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
