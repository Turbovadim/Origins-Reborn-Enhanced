package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.jetbrains.annotations.NotNull;

public class AirFromPotions implements Ability, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:air_from_potions");
    }

    NamespacedKey dehydrationKey = new NamespacedKey(OriginsReborn.getInstance(), "dehydrating");

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.POTION) {
            event.getPlayer().getPersistentDataContainer().set(dehydrationKey, OriginSwapper.BooleanPDT.BOOLEAN, true);
            event.getPlayer().setRemainingAir(Math.min(event.getPlayer().getRemainingAir() + 60, event.getPlayer().getMaximumAir()));
            event.getPlayer().getPersistentDataContainer().set(dehydrationKey, OriginSwapper.BooleanPDT.BOOLEAN, false);
        }
    }
}
