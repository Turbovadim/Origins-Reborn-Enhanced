package com.starshootercity.origins;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftCreeper;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.function.Predicate;

public class Feline implements Listener {
    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            OriginSwapper.runForOrigin(player, "Feline",
                    () -> {
                        if (player.isSprinting()) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 5, 1, false, false));
                        }
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 1, false, false));
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
