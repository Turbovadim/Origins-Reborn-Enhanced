package com.starshootercity.abilities;

import net.kyori.adventure.key.Key;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class DamageFromSnowballs implements Ability, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:damage_from_snowballs");
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getType() != EntityType.SNOWBALL) return;
        if (event.getHitEntity() instanceof Player player) {
            AbilityRegister.runForAbility(player, getKey(), () -> {
                net.minecraft.world.entity.player.Player NMSPlayer = ((CraftPlayer) player).getHandle();
                NMSPlayer.hurt(NMSPlayer.damageSources().freeze(), 3);
                Vector vector = event.getEntity().getLocation().getDirection();
                player.knockback(0.5, -vector.getX(), -vector.getZ());
            });
        }
    }
}
