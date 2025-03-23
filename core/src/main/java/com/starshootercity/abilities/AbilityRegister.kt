package com.starshootercity.abilities;

import com.starshootercity.OriginsReborn;
import com.starshootercity.commands.FlightToggleCommand;
import com.starshootercity.cooldowns.CooldownAbility;
import com.starshootercity.packetsenders.NMSInvoker;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AbilityRegister {
    public static Map<Key, Ability> abilityMap = new HashMap<>();
    public static Map<Key, DependencyAbility> dependencyAbilityMap = new HashMap<>();
    public static Map<Key, List<MultiAbility>> multiAbilityMap = new HashMap<>();

    public static OriginsReborn origins = OriginsReborn.getInstance();
    public static NMSInvoker nmsInvoker = OriginsReborn.getNMSInvoker();

    public static void registerAbility(Ability ability, JavaPlugin instance) {
        if (ability instanceof DependencyAbility dependencyAbility) {
            dependencyAbilityMap.put(ability.getKey(), dependencyAbility);
        }

        if (ability instanceof MultiAbility multiAbility) {
            for (Ability a : multiAbility.getAbilities()) {
                multiAbilityMap.computeIfAbsent(a.getKey(), k -> new ArrayList<>()).add(multiAbility);
            }
        }

        if (ability instanceof CooldownAbility cooldownAbility) {
            OriginsReborn.getCooldowns().registerCooldown(instance, cooldownAbility.getCooldownKey(), Objects.requireNonNull(cooldownAbility.getCooldownInfo()));
        }

        if (ability instanceof Listener listener) {
            Bukkit.getPluginManager().registerEvents(listener, instance);
        }

        if (ability instanceof AttributeModifierAbility ama) {
            String formattedValueKey = "%s.value".formatted(ama.getKey());
            String formattedOperationKey = "%s.operation".formatted(ama.getKey());
            boolean changed = false;

            if (!attributeModifierAbilityFileConfig.contains(ama.getKey().toString())) {
                attributeModifierAbilityFileConfig.set(formattedValueKey, "x");
                attributeModifierAbilityFileConfig.set(formattedOperationKey, "default");
                changed = true;
            }

            if ("default".equals(attributeModifierAbilityFileConfig.get(formattedValueKey, "default"))) {
                attributeModifierAbilityFileConfig.set(formattedValueKey, "x");
                changed = true;
            }

            if (changed) {
                try {
                    attributeModifierAbilityFileConfig.save(attributeModifierAbilityFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        abilityMap.put(ability.getKey(), ability);
    }


    /**
     * @deprecated Testing abilities is now contained in the Ability interface
     */
    @Deprecated
    public static void runForAbility(Entity entity, Key key, Runnable runnable) {
        runForAbility(entity, key, runnable, () -> {});
    }

    /**
     * @deprecated Testing abilities is now contained in the Ability interface
     */
    @Deprecated
    public static boolean hasAbility(Player player, Key key) {
        return hasAbility(player, key, false);
    }

    /**
     * @deprecated Testing abilities is now contained in the Ability interface
     */
    @Deprecated
    public static boolean hasAbility(Player player, Key key, boolean ignoreOverrides) {
        if (!abilityMap.containsKey(key)) return false;
        return abilityMap.get(key).hasAbility(player);
    }

    /**
     * @deprecated Testing abilities is now contained in the Ability interface
     */
    @Deprecated
    public static void runForAbility(Entity entity, Key key, Runnable runnable, Runnable other) {
        if (entity == null) return;
        String worldId = entity.getWorld().getName();
        if (OriginsReborn.mainConfig.getWorlds().getDisabledWorlds().contains(worldId)) return;
        if (entity instanceof Player player) {
            if (hasAbility(player, key)) {
                runnable.run();
                return;
            }
        }
        other.run();
    }

    /**
     * @deprecated Testing abilities is now contained in the Ability interface
     */
    @Deprecated
    public static void runWithoutAbility(Entity entity, Key key, Runnable runnable) {
        runForAbility(entity, key, () -> {}, runnable);
    }


    public static boolean canFly(Player player, boolean disabledWorld) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || FlightToggleCommand.canFly(player)) return true;
        if (disabledWorld) return false;
        for (Ability ability : AbilityRegister.abilityMap.values()) {
            if (ability instanceof FlightAllowingAbility flightAllowingAbility) {
                if (ability.hasAbility(player) && flightAllowingAbility.canFly(player)) return true;
            }
        }
        return false;
    }


    public static boolean isInvisible(Player player) {
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return true;
        for (Ability ability : abilityMap.values()) {
            if (ability instanceof VisibilityChangingAbility visibilityChangingAbility) {
                if (ability.hasAbility(player) && visibilityChangingAbility.isInvisible(player)) return true;
            }
        }
        return false;
    }

    public static void updateFlight(Player player, boolean inDisabledWorld) {
        if (
            player.getGameMode() == GameMode.CREATIVE ||
            player.getGameMode() == GameMode.SPECTATOR
        ) {
            return;
        }
        if (FlightToggleCommand.canFly(player)) {
            player.setFlySpeed(0.1f);
            return;
        }
        if (inDisabledWorld) return;

        TriState flyingFallDamage = TriState.FALSE;
        float speed = -1f; // Используем -1 как индикатор отсутствия способности

        // Перебираем все способности из abilityMap
        for (Ability ability : abilityMap.values()) {
            if (!(ability instanceof FlightAllowingAbility flightAllowingAbility)) continue;
            // Если у игрока нет способности или он не может летать по данной способности, пропускаем её
            if (!ability.hasAbility(player) || !flightAllowingAbility.canFly(player)) continue;

            // Получаем скорость для данной способности и выбираем минимальную (если их несколько)
            float abilitySpeed = flightAllowingAbility.getFlightSpeed(player);
            speed = (speed < 0f) ? abilitySpeed : Math.min(speed, abilitySpeed);

            // Если хотя бы одна способность требует TRUE для урона при падении — запоминаем это
            if (flightAllowingAbility.getFlyingFallDamage(player) == TriState.TRUE) {
                flyingFallDamage = TriState.TRUE;
            }
        }

        // Устанавливаем урон при падении и скорости полёта
        nmsInvoker.setFlyingFallDamage(player, flyingFallDamage);
        player.setFlySpeed(speed < 0f ? 0 : speed);
    }


    public static void updateEntity(Player player, Entity target) {
        byte data = 0;

        if (target.getFireTicks() > 0) {
            data |= 0x01;
        }
        if (target.isGlowing()) {
            data |= 0x40;
        }
        if (target instanceof LivingEntity living && living.isInvisible()) {
            data |= 0x20;
        }
        if (target instanceof Player targetPlayer) {
            if (targetPlayer.isSneaking()) {
                data |= 0x02;
            }
            if (targetPlayer.isSprinting()) {
                data |= 0x08;
            }
            if (targetPlayer.isSwimming()) {
                data |= 0x10;
            }
            if (targetPlayer.isGliding()) {
                data |= (byte) 0x80;
            }

            var inventory = targetPlayer.getInventory();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                try {
                    ItemStack item = inventory.getItem(slot);
                    if (item != null) {
                        player.sendEquipmentChange(targetPlayer, slot, item);
                    }
                } catch (IllegalArgumentException ignored) {
                    // Если слот не поддерживается, пропускаем его
                }
            }
        }

        nmsInvoker.sendEntityData(player, target, data);
    }

    public static FileConfiguration attributeModifierAbilityFileConfig;

    private static File attributeModifierAbilityFile;

    public static void setupAMAF() {
        attributeModifierAbilityFile = new File(origins.getDataFolder(), "attribute-modifier-ability-config.yml");
        if (!attributeModifierAbilityFile.exists()) {
            boolean ignored = attributeModifierAbilityFile.getParentFile().mkdirs();
            origins.saveResource("attribute-modifier-ability-config.yml", false);
        }

        attributeModifierAbilityFileConfig = new YamlConfiguration();

        try {
            attributeModifierAbilityFileConfig.load(attributeModifierAbilityFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
