package com.starshootercity.abilities;

import net.kyori.adventure.key.Key;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.jetbrains.annotations.NotNull;

public class DamageFromPotions implements Ability, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:damage_from_potions");
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            if (event.getItem().getType() == Material.POTION) {
                Player player = ((CraftPlayer) event.getPlayer()).getHandle();
                player.hurt(player.damageSources().freeze(), 2);
            }
        });
    }
}
