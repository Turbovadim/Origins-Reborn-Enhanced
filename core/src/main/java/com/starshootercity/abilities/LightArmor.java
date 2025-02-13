package com.starshootercity.abilities;

import com.destroystokyo.paper.MaterialTags;
import com.starshootercity.OriginSwapper;
import com.starshootercity.ShortcutUtils;
import com.starshootercity.events.PlayerSwapOriginEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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

    private final List<Material> allowedTypes = List.of(
        Material.CHAINMAIL_HELMET,
        Material.CHAINMAIL_CHESTPLATE,
        Material.CHAINMAIL_LEGGINGS,
        Material.CHAINMAIL_BOOTS,
        Material.LEATHER_HELMET,
        Material.LEATHER_CHESTPLATE,
        Material.LEATHER_LEGGINGS,
        Material.LEATHER_BOOTS,
        Material.GOLDEN_HELMET,
        Material.GOLDEN_CHESTPLATE,
        Material.GOLDEN_LEGGINGS,
        Material.GOLDEN_BOOTS
    );

    @EventHandler
    public void onPlayerSwapOrigin(PlayerSwapOriginEvent event) {
        if (event.newOrigin == null) return;
        if (event.newOrigin.hasAbility(getKey())) {
            ItemStack helmet = event.getPlayer().getInventory().getHelmet();
            ItemStack chestplate = event.getPlayer().getInventory().getChestplate();
            ItemStack leggings = event.getPlayer().getInventory().getLeggings();
            ItemStack boots = event.getPlayer().getInventory().getBoots();
            if (helmet == null || chestplate == null || leggings == null || boots == null) return;
            if (!allowedTypes.contains(helmet.getType())) {
                event.getPlayer().getInventory().setHelmet(null);
                ShortcutUtils.giveItemWithDrops(event.getPlayer(), helmet);
            }
            if (!allowedTypes.contains(chestplate.getType())) {
                event.getPlayer().getInventory().setChestplate(null);
                ShortcutUtils.giveItemWithDrops(event.getPlayer(), chestplate);
            }
            if (!allowedTypes.contains(leggings.getType())) {
                event.getPlayer().getInventory().setLeggings(null);
                ShortcutUtils.giveItemWithDrops(event.getPlayer(), leggings);
            }
            if (!allowedTypes.contains(boots.getType())) {
                event.getPlayer().getInventory().setBoots(null);
                ShortcutUtils.giveItemWithDrops(event.getPlayer(), boots);
            }
        }
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
            if (event.getCursor() != null) {
                if (isArmor(event.getCursor().getType())) {
                    if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                        checkArmorEvent(event, player, event.getCursor());
                    }
                }
            }
            if (event.isShiftClick()) {
                if (event.getCurrentItem() == null) return;
                if (event.getInventory().getType() != InventoryType.CRAFTING) return;
                if (MaterialTags.HELMETS.isTagged(event.getCurrentItem().getType()) && player.getEquipment().getHelmet() == null) {
                    checkArmorEvent(event, player, event.getCurrentItem());
                }
                if (MaterialTags.CHESTPLATES.isTagged(event.getCurrentItem().getType()) && player.getEquipment().getChestplate() == null) {
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

    public void checkArmorEvent(Cancellable event, Player p, ItemStack armor) {
        runForAbility(p, player -> {
            if (allowedTypes.contains(armor.getType())) return;
            event.setCancelled(true);
        });
    }

    public boolean isArmor(Material material) {
        return MaterialTags.HELMETS.isTagged(material) || MaterialTags.CHESTPLATES.isTagged(material) || MaterialTags.LEGGINGS.isTagged(material) || MaterialTags.BOOTS.isTagged(material);
    }
}
