package com.starshootercity.origins;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OrigamiOrigins;
import com.starshootercity.OriginSwapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class Shulk implements Listener {
    private static File inventories;
    private static FileConfiguration inventoriesConfig;
    public Shulk() {
        inventories = new File(OrigamiOrigins.getInstance().getDataFolder(), "inventories.yml");
        if (!inventories.exists()) {
            boolean ignore = inventories.getParentFile().mkdirs();
            OrigamiOrigins.getInstance().saveResource("inventories.yml", false);
        }

        inventoriesConfig = new AutosavingYamlConfiguration();
        try {
            inventoriesConfig.load(inventories);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            OriginSwapper.runForOrigin(player, "Shulk", () -> event.setDamage(event.getDamage() * 0.75));
        }
    }

    NamespacedKey openedBoxKey = new NamespacedKey(OrigamiOrigins.getInstance(), "openedbox");

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        event.getPlayer().getPersistentDataContainer().set(openedBoxKey, PersistentDataType.BOOLEAN, false);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (event.isRightClick()) {
                if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                    if (event.getSlot() == 38) {
                        Inventory inventory = Bukkit.createInventory(player, InventoryType.DISPENSER, Component.text("Storage Pouch"));
                        player.openInventory(inventory);
                        for (int i = 0; i < 9; i++) {
                            ItemStack item = getInventoriesConfig().getItemStack("%s.%s".formatted(player.getUniqueId().toString(), i));
                            if (item != null) inventory.setItem(i, item);
                        }
                        player.getPersistentDataContainer().set(openedBoxKey, PersistentDataType.BOOLEAN, true);
                        return;
                    }
                }
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(OrigamiOrigins.getInstance(), () -> {
                if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(openedBoxKey, PersistentDataType.BOOLEAN))) {
                    for (int i = 0; i < 9; i++) {
                        getInventoriesConfig().set("%s.%s".formatted(player.getUniqueId().toString(), i), player.getOpenInventory().getItem(i));
                    }
                }
            });
        }
    }

    public static FileConfiguration getInventoriesConfig() {
        return inventoriesConfig;
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            OriginSwapper.runForOrigin(player, "Shulk", () -> {
                event.setFoodLevel(Math.max(player.getFoodLevel() - ((player.getFoodLevel() - event.getFoodLevel()) * 3), 0));
            });
        }
    }

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            OriginSwapper.runForOrigin(player, "Shulk", () -> {
                player.setCooldown(Material.SHIELD, 1000);
            });
        }
    }

    private static class AutosavingYamlConfiguration extends YamlConfiguration {
        @Override
        public void set(@NotNull String path, @Nullable Object value) {
            super.set(path, value);
            try {
                save(inventories);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
