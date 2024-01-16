package com.starshootercity.abilities;

import com.destroystokyo.paper.MaterialTags;
import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LightArmor implements VisibleAbility, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:light_armor");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You can not wear any heavy armor (armor with protection values higher than chainmail).", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Need for Mobility", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventorySlots().contains(38)) {
            if (event.getWhoClicked() instanceof Player player) {
                checkArmorEvent(event, player, event.getOldCursor());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (Tag.ITEMS_TRIMMABLE_ARMOR.isTagged(event.getCursor().getType())) {
                if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                    checkArmorEvent(event, player, event.getCursor());
                }
            }
            if (event.isShiftClick()) {
                if (event.getCurrentItem() == null) return;
                if (event.getInventory().getType() != InventoryType.CRAFTING) return;
                if (MaterialTags.HELMETS.isTagged(event.getCurrentItem().getType()) && player.getEquipment().getHelmet() == null) {
                    checkArmorEvent(event, player, event.getCurrentItem());
                }
                if (MaterialTags.CHESTPLATES.isTagged(event.getCurrentItem().getType()) && player.getEquipment().getLeggings() == null) {
                    checkArmorEvent(event, player, event.getCurrentItem());
                }
                if (MaterialTags.LEGGINGS.isTagged(event.getCurrentItem().getType()) && player.getEquipment().getLeggings() == null) {
                    checkArmorEvent(event, player, event.getCurrentItem());
                }
                if (MaterialTags.BOOTS.isTagged(event.getCurrentItem().getType()) && player.getEquipment().getBoots() == null) {
                    checkArmorEvent(event, player, event.getCurrentItem());
                }
            }
            if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
                if (event.getHotbarButton() == -1) {
                    if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                        checkArmorEvent(event, player, player.getInventory().getItemInOffHand());
                    }
                }
            }
            if (event.getClick() == ClickType.NUMBER_KEY) {
                if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                    ItemStack item = player.getInventory().getItem(event.getHotbarButton());
                    if (item != null) {
                        checkArmorEvent(event, player, item);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()) {
            if (event.getItem() == null) return;
            if (MaterialTags.HELMETS.isTagged(event.getItem().getType())) {
                checkArmorEvent(event, event.getPlayer(), event.getItem());
            }
            if (MaterialTags.CHESTPLATES.isTagged(event.getItem().getType())) {
                checkArmorEvent(event, event.getPlayer(), event.getItem());
            }
            if (MaterialTags.LEGGINGS.isTagged(event.getItem().getType())) {
                checkArmorEvent(event, event.getPlayer(), event.getItem());
            }
            if (MaterialTags.BOOTS.isTagged(event.getItem().getType())) {
                checkArmorEvent(event, event.getPlayer(), event.getItem());
            }
        }
    }
    @EventHandler
    public void onBlockDispenseArmor(BlockDispenseArmorEvent event) {
        if (event.getTargetEntity() instanceof Player player) {
            checkArmorEvent(event, player, event.getItem());
        }
    }

    public void checkArmorEvent(Cancellable event, Player player, ItemStack armor) {
        AbilityRegister.runForAbility(player, getKey(), () -> {
            List<Material> allowedTypes = new ArrayList<>() {{
                add(Material.CHAINMAIL_HELMET);
                add(Material.CHAINMAIL_CHESTPLATE);
                add(Material.CHAINMAIL_LEGGINGS);
                add(Material.CHAINMAIL_BOOTS);
                add(Material.LEATHER_HELMET);
                add(Material.LEATHER_CHESTPLATE);
                add(Material.LEATHER_LEGGINGS);
                add(Material.LEATHER_BOOTS);
                add(Material.GOLDEN_HELMET);
                add(Material.GOLDEN_CHESTPLATE);
                add(Material.GOLDEN_LEGGINGS);
                add(Material.GOLDEN_BOOTS);
            }};
            if (allowedTypes.contains(armor.getType())) return;
            event.setCancelled(true);
        });
    }
}
