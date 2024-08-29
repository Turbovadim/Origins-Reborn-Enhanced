package com.starshootercity.abilities;

import com.starshootercity.AddonLoader;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ShulkerInventory implements VisibleAbility, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:shulker_inventory");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You have access to an additional 9 slots of inventory, which keep the items on death.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Hoarder", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    private static File inventories;
    private static FileConfiguration inventoriesConfig;
    public ShulkerInventory() {
        inventories = new File(OriginsReborn.getInstance().getDataFolder(), "inventories.yml");
        if (!inventories.exists()) {
            boolean ignore = inventories.getParentFile().mkdirs();
            OriginsReborn.getInstance().saveResource("inventories.yml", false);
        }

        inventoriesConfig = new AutosavingYamlConfiguration();
        try {
            inventoriesConfig.load(inventories);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    NamespacedKey openedBoxKey = new NamespacedKey(OriginsReborn.getInstance(), "openedbox");

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        event.getPlayer().getPersistentDataContainer().set(openedBoxKey, OriginSwapper.BooleanPDT.BOOLEAN, false);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            AbilityRegister.runForAbility(player, getKey(), () -> {
                if (event.isRightClick()) {
                    if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                        if (event.getSlot() == 38) {
                            Inventory inventory = Bukkit.createInventory(player, InventoryType.DISPENSER, Component.text(AddonLoader.getTextFor("container.shulker_inventory_power", "Shulker Inventory")));
                            player.openInventory(inventory);
                            for (int i = 0; i < 9; i++) {
                                ItemStack item = getInventoriesConfig().getItemStack("%s.%s".formatted(player.getUniqueId().toString(), i));
                                if (item != null) inventory.setItem(i, item);
                            }
                            player.getPersistentDataContainer().set(openedBoxKey, OriginSwapper.BooleanPDT.BOOLEAN, true);
                            return;
                        }
                    }
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> {
                    if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(openedBoxKey, OriginSwapper.BooleanPDT.BOOLEAN))) {
                        for (int i = 0; i < 9; i++) {
                            getInventoriesConfig().set("%s.%s".formatted(player.getUniqueId().toString(), i), player.getOpenInventory().getItem(i));
                        }
                    }
                });
            });
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> {
            if (Boolean.TRUE.equals(event.getWhoClicked().getPersistentDataContainer().get(openedBoxKey, OriginSwapper.BooleanPDT.BOOLEAN))) {
                for (int i = 0; i < 9; i++) {
                    getInventoriesConfig().set("%s.%s".formatted(event.getWhoClicked().getUniqueId().toString(), i), event.getWhoClicked().getOpenInventory().getItem(i));
                }
            }
        });
    }

    public static FileConfiguration getInventoriesConfig() {
        return inventoriesConfig;
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