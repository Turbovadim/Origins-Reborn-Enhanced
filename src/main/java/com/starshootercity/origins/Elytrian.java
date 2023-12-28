package com.starshootercity.origins;

import com.destroystokyo.paper.MaterialTags;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import com.starshootercity.OldOriginSwapper;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Elytrian implements Listener {
    NamespacedKey cannotDropKey = new NamespacedKey(OriginsReborn.getInstance(), "cannotdrop");
    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(cannotDropKey, PersistentDataType.BOOLEAN, true);
        elytra.setItemMeta(meta);
        for (Player player : Bukkit.getOnlinePlayers()) {
            OldOriginSwapper.runForOrigin(player, "Elytrian",
                    () -> {
                        if (player.getLocation().getBlock().getRelative(BlockFace.UP, 2).isSolid()) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 5, 2, false, false));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5, 1, false, false));
                        }
                        ItemStack chestplate = player.getEquipment().getChestplate();
                        if (chestplate != null) {
                            if (chestplate.getType() == Material.ELYTRA) return;
                        }
                        player.getEquipment().setChestplate(elytra);
                    });
        }
    }

    NamespacedKey boostUsedKey = new NamespacedKey(OriginsReborn.getInstance(), "boostused");

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            OldOriginSwapper.runForOrigin(event.getPlayer(), "Elytrian", () -> {
                if (!event.getPlayer().isGliding()) return;
                Long time = event.getPlayer().getPersistentDataContainer().get(boostUsedKey, PersistentDataType.LONG);
                long deltaTime;
                if (time == null) deltaTime = Long.MAX_VALUE;
                else {
                    deltaTime = Instant.now().getEpochSecond() - time;
                }
                if (deltaTime > 30) {
                    event.getPlayer().getPersistentDataContainer().set(boostUsedKey, PersistentDataType.LONG, Instant.now().getEpochSecond());
                    Vector vector = event.getPlayer().getVelocity();
                    vector.add(new Vector(0, 3, 0));
                    event.getPlayer().setVelocity(vector);
                }
            });
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (Boolean.TRUE.equals(event.getEntity().getItemStack().getItemMeta().getPersistentDataContainer().get(cannotDropKey, PersistentDataType.BOOLEAN))) {
            event.setCancelled(true);
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
        OldOriginSwapper.runForOrigin(trueDamager, "Elytrian", () -> {
            if (trueDamager.isGliding()) event.setDamage(event.getDamage() * 2);
        });
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
        OldOriginSwapper.runForOrigin(player, "Elytrian", () -> {
            List<Material> allowedTypes = new ArrayList<>() {{
                add(Material.CHAINMAIL_HELMET);
                add(Material.CHAINMAIL_LEGGINGS);
                add(Material.CHAINMAIL_BOOTS);
                add(Material.LEATHER_HELMET);
                add(Material.LEATHER_LEGGINGS);
                add(Material.LEATHER_BOOTS);
                add(Material.GOLDEN_HELMET);
                add(Material.GOLDEN_LEGGINGS);
                add(Material.GOLDEN_BOOTS);
            }};
            if (allowedTypes.contains(armor.getType())) return;
            event.setCancelled(true);
        });
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            OldOriginSwapper.runForOrigin(player, "Elytrian", () -> {
                if (event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
                    event.setDamage(event.getDamage() * 2);
                }
            });
        }
    }
}
