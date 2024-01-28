package com.starshootercity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShortcutUtils {
    public static void giveItemWithDrops(Player player, ItemStack... itemStacks) {
        for (ItemStack i : player.getInventory().addItem(itemStacks).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), i);
        }
    }

    public static @Nullable LivingEntity getLivingDamageSource(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity entity) return entity;
        else if (event.getDamager() instanceof LivingEntity entity) return entity;
        return null;
    }
}
