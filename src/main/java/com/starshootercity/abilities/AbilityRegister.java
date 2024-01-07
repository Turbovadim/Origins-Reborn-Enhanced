package com.starshootercity.abilities;

import com.starshootercity.Origin;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityRegister {
    public static Map<Key, Ability> abilityMap = new HashMap<>();
    public static Map<Key, DependencyAbility> dependencyAbilityMap = new HashMap<>();
    public static void registerAbility(Ability ability) {
        abilityMap.put(ability.getKey(), ability);
        if (ability instanceof DependencyAbility dependencyAbility) {
            dependencyAbilityMap.put(ability.getKey(), dependencyAbility);
        }
        if (ability instanceof Listener listener) {
            Bukkit.getPluginManager().registerEvents(listener, OriginsReborn.getInstance());
        }
    }

    public static void runForAbility(Entity entity, Key key, Runnable runnable) {
        runForAbility(entity, key, runnable, () -> {});
    }

    public static boolean hasAbility(Player player, Key key) {
        Origin origin = OriginSwapper.getOrigin(player);
        if (origin == null) {
            return false;
        }
        if (abilityMap.get(key) instanceof DependantAbility dependantAbility) {
            return origin.hasAbility(key) && ((dependantAbility.getDependencyType() == DependantAbility.DependencyType.REGULAR) == dependantAbility.getDependency().isEnabled(player));
        }
        return origin.hasAbility(key);
    }

    public static void runForAbility(Entity entity, Key key, Runnable runnable, Runnable other) {
        if (entity instanceof Player player) {
            if (hasAbility(player, key)) {
                runnable.run();
                return;
            }
        }
        other.run();
    }

    public static void runWithoutAbility(Entity entity, Key key, Runnable runnable) {
        runForAbility(entity, key, () -> {}, runnable);
    }


    public static boolean canFly(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return true;
        Origin origin = OriginSwapper.getOrigin(player);
        if (origin == null) return false;
        for (Ability ability : origin.getAbilities()) {
            if (ability instanceof FlightAllowingAbility flightAllowingAbility) {
                if (flightAllowingAbility.canFly(player)) return true;
            }
        }
        return false;
    }


    public static boolean isInvisible(Player player) {
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return true;
        Origin origin = OriginSwapper.getOrigin(player);
        if (origin == null) return false;
        for (Ability ability : origin.getAbilities()) {
            if (ability instanceof VisibilityChangingAbility visibilityChangingAbility) {
                if (visibilityChangingAbility.isInvisible(player)) return true;
            }
        }
        return false;
    }

    public static float getFlySpeed(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return 0.2f;
        Origin origin = OriginSwapper.getOrigin(player);
        if (origin == null) return 1f;
        float speed = 1f;
        for (Ability ability : origin.getAbilities()) {
            if (ability instanceof FlightAllowingAbility flightAllowingAbility) {
                if (flightAllowingAbility.canFly(player)) {
                    speed = Math.min(speed, flightAllowingAbility.getFlightSpeed(player));
                }
            }
        }
        return speed;
    }

    public static void updateEntity(Player player, Entity target) {
        byte data = 0;
        if (target.getFireTicks() > 0) {
            data += 0x01;
        }
        if (target.isSneaking()) {
            data += 0x02;
        }
        if (target.isGlowing()) {
            data += 0x40;
        }
        if (target instanceof Player targetPlayer) {
            if (targetPlayer.isSprinting()) {
                data += 0x08;
            }
            if (targetPlayer.isSwimming()) {
                data += 0x10;
            }
            if (targetPlayer.isInvisible()) {
                data += 0x20;
            }
            if (targetPlayer.isGliding()) {
                data += 0x80;
            }
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                player.sendEquipmentChange(targetPlayer, equipmentSlot, targetPlayer.getInventory().getItem(equipmentSlot));
            }
        }
        List<SynchedEntityData.DataValue<?>> eData = new ArrayList<>();
        eData.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), data));
        ClientboundSetEntityDataPacket metadata = new ClientboundSetEntityDataPacket(((CraftEntity) target).getHandle().getId(), eData);
        ((CraftPlayer) player).getHandle().connection.send(metadata);
    }
}
