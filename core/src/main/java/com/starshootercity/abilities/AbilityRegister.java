package com.starshootercity.abilities;

import com.starshootercity.*;
import com.starshootercity.commands.FlightToggleCommand;
import com.starshootercity.cooldowns.CooldownAbility;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityRegister {
    public static Map<Key, Ability> abilityMap = new HashMap<>();
    public static Map<Key, DependencyAbility> dependencyAbilityMap = new HashMap<>();
    public static Map<Key, List<MultiAbility>> multiAbilityMap = new HashMap<>();
    public static void registerAbility(Ability ability, JavaPlugin instance) {
        if (ability instanceof DependencyAbility dependencyAbility) {
            dependencyAbilityMap.put(ability.getKey(), dependencyAbility);
        }
        if (ability instanceof MultiAbility multiAbility) {
            for (Ability a : multiAbility.getAbilities()) {
                List<MultiAbility> abilities = multiAbilityMap.getOrDefault(a.getKey(), new ArrayList<>());
                abilities.add(multiAbility);
                multiAbilityMap.put(a.getKey(), abilities);
            }
        }
        if (ability instanceof CooldownAbility cooldownAbility) {
            OriginsReborn.getCooldowns().registerCooldown(cooldownAbility.getCooldownKey(), cooldownAbility.getCooldownInfo());
        }
        if (ability instanceof Listener listener) {
            Bukkit.getPluginManager().registerEvents(listener, instance);
        }
        abilityMap.put(ability.getKey(), ability);
    }

    public static void runForAbility(Entity entity, Key key, Runnable runnable) {
        runForAbility(entity, key, runnable, () -> {});
    }

    public static boolean hasAbility(Player player, Key key) {
        return hasAbility(player, key, false);
    }

    public static boolean hasAbility(Player player, Key key, boolean ignoreOverrides) {
        if (!ignoreOverrides) {
            for (OriginsAddon.KeyStateGetter keyStateGetter : AddonLoader.hasAbilityOverrideChecks) {
                OriginsAddon.State state = keyStateGetter.get(player, key);
                if (state == OriginsAddon.State.DENY) return false;
                else if (state == OriginsAddon.State.ALLOW) return true;
            }
        }
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
        if (entity == null) return;
        String worldId = entity.getWorld().getName();
        if (OriginsReborn.getInstance().getConfig().getStringList("worlds.disabled-worlds").contains(worldId)) return;
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
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || FlightToggleCommand.canFly.contains(player)) return true;
        for (Ability ability : AbilityRegister.abilityMap.values()) {
            if (ability instanceof FlightAllowingAbility flightAllowingAbility) {
                if (hasAbility(player, ability.getKey()) && flightAllowingAbility.canFly(player)) return true;
            }
        }
        return false;
    }


    public static boolean isInvisible(Player player) {
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return true;
        for (Ability ability : abilityMap.values()) {
            if (ability instanceof VisibilityChangingAbility visibilityChangingAbility) {
                if (hasAbility(player, ability.getKey()) && visibilityChangingAbility.isInvisible(player)) return true;
            }
        }
        return false;
    }

    public static void updateFlight(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || FlightToggleCommand.canFly.contains(player)) {
            player.setFlySpeed(0.1f);
            return;
        }
        TriState flyingFallDamage = TriState.FALSE;
        float speed = -1f;
        for (Ability ability : abilityMap.values()) {
            if (ability instanceof FlightAllowingAbility flightAllowingAbility) {
                if (hasAbility(player, ability.getKey()) && flightAllowingAbility.canFly(player)) {
                    float abilitySpeed = flightAllowingAbility.getFlightSpeed(player);
                    speed = speed == -1 ? abilitySpeed : Math.min(speed, abilitySpeed);
                    if (flightAllowingAbility.getFlyingFallDamage(player) == TriState.TRUE) {
                        flyingFallDamage = TriState.TRUE;
                    }
                }
            }
        }
        OriginsReborn.getNMSInvoker().setFlyingFallDamage(player, flyingFallDamage);
        player.setFlySpeed(speed == -1 ? 0 : speed);
    }

    public static void updateEntity(Player player, Entity target) {
        byte data = 0;
        if (target.getFireTicks() > 0) {
            data += 0x01;
        }
        if (target.isGlowing()) {
            data += 0x40;
        }
        if (target instanceof LivingEntity entity) {
            if (entity.isInvisible()) data += 0x20;
        }
        if (target instanceof Player targetPlayer) {
            if (targetPlayer.isSneaking()) {
                data += 0x02;
            }
            if (targetPlayer.isSprinting()) {
                data += 0x08;
            }
            if (targetPlayer.isSwimming()) {
                data += 0x10;
            }
            if (targetPlayer.isGliding()) {
                data += (byte) 0x80;
            }
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                try {
                    ItemStack item = targetPlayer.getInventory().getItem(equipmentSlot);
                    if (item != null) {
                        player.sendEquipmentChange(targetPlayer, equipmentSlot, item);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }

        OriginsReborn.getNMSInvoker().sendEntityData(player, target, data);
    }
}
