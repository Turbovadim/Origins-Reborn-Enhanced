package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScareCreepers implements VisibleAbility, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:scare_creepers");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("Creepers are scared of you and will only explode if you attack them first.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Catlike Appearance", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Creeper creeper) {
            fixCreeper(creeper);
        }
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof Creeper creeper) {
                fixCreeper(creeper);
            }
        }
    }

    public void fixCreeper(Creeper creeper) {
        Bukkit.getMobGoals().addGoal(creeper, 0, new AvoidEntityGoal<>(
                (PathfinderMob) ((CraftEntity) creeper).getHandle(),
                Player.class,
                6,
                1,
                1.2,
                livingEntity -> {
                    if (livingEntity.getBukkitEntity() instanceof org.bukkit.entity.Player player) {
                        if (AbilityRegister.hasAbility(player, getKey())) {
                            String data = creeper.getPersistentDataContainer().get(hitByPlayerKey, PersistentDataType.STRING);
                            if (data == null) {
                                return true;
                            }
                            return !data.equals(player.getName());
                        }
                    }
                    return false;
                }
        ).asPaperVanillaGoal());
    }

    private final NamespacedKey hitByPlayerKey = new NamespacedKey(OriginsReborn.getInstance(), "hit-by-player");

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.CREEPER) {
            org.bukkit.entity.Player player;
            if (event.getDamager() instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof org.bukkit.entity.Player shooter) player = shooter;
                else return;
            } else if (event.getDamager() instanceof org.bukkit.entity.Player damager) player = damager;
            else return;
            AbilityRegister.runForAbility(player, getKey(), () -> event.getEntity().getPersistentDataContainer().set(hitByPlayerKey, PersistentDataType.STRING, player.getName()));
        }
    }

    @EventHandler
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        if (event.getEntity().getType() == EntityType.CREEPER) {
            if (event.getTarget() instanceof org.bukkit.entity.Player player) {
                AbilityRegister.runForAbility(player, getKey(), () -> {
                    String data = event.getEntity().getPersistentDataContainer().get(hitByPlayerKey, PersistentDataType.STRING);
                    if (data == null) {
                        event.setCancelled(true);
                        return;
                    }
                    if (!data.equals(player.getName())) {
                        event.setCancelled(true);
                    }
                });
            }
        }
    }
}
