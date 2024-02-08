package com.starshootercity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

@SuppressWarnings("unused")
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

    public static JSONObject openJSONFile(File file) {
        try (Scanner scanner = new Scanner(file)) {
            StringBuilder data = new StringBuilder();
            while (scanner.hasNextLine()) {
                data.append(scanner.nextLine());
            }
            return new JSONObject(data.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
