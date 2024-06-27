package com.starshootercity.abilities;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PumpkinHate implements VisibleAbility, Listener {
    private final Map<Player, List<Player>> ignoringPlayers = new HashMap<>();

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player pumpkinWearer : Bukkit.getOnlinePlayers()) {
            for (Player pumpkinHater : Bukkit.getOnlinePlayers()) {
                AbilityRegister.runForAbility(pumpkinHater, getKey(), () -> {
                    if (pumpkinWearer != pumpkinHater) {
                        if (!ignoringPlayers.containsKey(pumpkinHater)) {
                            ignoringPlayers.put(pumpkinHater, new ArrayList<>());
                        }
                        ItemStack helmet = pumpkinWearer.getInventory().getHelmet();
                        if (helmet != null && helmet.getType() == Material.CARVED_PUMPKIN) {
                            if (!ignoringPlayers.get(pumpkinHater).contains(pumpkinWearer)) {
                                ignoringPlayers.get(pumpkinHater).add(pumpkinWearer);
                            }

                            byte data = getData(pumpkinWearer);

                            OriginsReborn.getNMSInvoker().sendEntityData(pumpkinHater, pumpkinWearer, data);

                            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                                pumpkinHater.sendEquipmentChange(pumpkinWearer, equipmentSlot, new ItemStack(Material.AIR));
                            }
                        } else {
                            ignoringPlayers.get(pumpkinHater).remove(pumpkinWearer);
                            AbilityRegister.updateEntity(pumpkinHater, pumpkinWearer);
                        }
                    }
                });
            }
        }
    }

    private static byte getData(Player pumpkinWearer) {
        byte data = 0x20;
        if (pumpkinWearer.getFireTicks() > 0) {
            data += 0x01;
        }
        if (pumpkinWearer.isSneaking()) {
            data += 0x02;
        }
        if (pumpkinWearer.isSprinting()) {
            data += 0x08;
        }
        if (pumpkinWearer.isSwimming()) {
            data += 0x10;
        }
        if (pumpkinWearer.isGlowing()) {
            data += 0x40;
        }
        if (pumpkinWearer.isGliding()) {
            data += (byte) 0x80;
        }
        return data;
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            if (event.getItem().getType() == Material.PUMPKIN_PIE) {
                event.setCancelled(true);
                event.getItem().setAmount(event.getItem().getAmount() - 1);
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 300, 2, false, true));
                event.getPlayer().addPotionEffect(new PotionEffect(OriginsReborn.getNMSInvoker().getNauseaEffect(), 300, 1, false, true));
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 1200, 1, false, true));
            }
        });
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor(
                "You are afraid of pumpkins. For a good reason.",
                OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor(
                "Scared of Gourds",
                OriginSwapper.LineData.LineComponent.LineType.TITLE
        );
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:pumpkin_hate");
    }
}
