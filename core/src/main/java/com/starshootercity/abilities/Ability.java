package com.starshootercity.abilities;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.starshootercity.*;
import com.starshootercity.util.WorldGuardHook;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.starshootercity.abilities.AbilityRegister.abilityMap;

public interface Ability {

    @NotNull Key getKey();

    default void runForAbility(Entity entity, @NotNull AbilityRunner runner) {
        runForAbility(entity, runner, null);
    }

    default void runForAbility(Entity entity, @Nullable AbilityRunner has, @Nullable AbilityRunner other) {
        if (entity instanceof Player player) {
            if (hasAbility(player)) {
                if (has != null) has.run(player);
            } else if (other != null) {
                other.run(player);
            }
        }
    }

    default boolean hasAbility(Player player) {
        // Проверка переопределения способностей через аддоны
        for (OriginsAddon.KeyStateGetter keyStateGetter : AddonLoader.abilityOverrideChecks) {
            assert keyStateGetter != null;
            OriginsAddon.State state = keyStateGetter.get(player, getKey());
            if (state == OriginsAddon.State.DENY) return false;
            else if (state == OriginsAddon.State.ALLOW) return true;
        }

        // Проверка через WorldGuard
        if (OriginsReborn.Companion.isWorldGuardHookInitialized()) {
            if (WorldGuardHook.isAbilityDisabled(player.getLocation(), this)) return false;

            ConfigurationSection section = OriginsReborn.getInstance().getConfig()
                    .getConfigurationSection("prevent-abilities-in");
            if (section != null) {
                Location loc = BukkitAdapter.adapt(player.getLocation());
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionQuery query = container.createQuery();
                ApplicableRegionSet regions = query.getApplicableRegions(loc);
                String keyStr = getKey().toString();
                for (ProtectedRegion region : regions) {
                    for (String sectionKey : section.getKeys(false)) {
                        List<String> abilities = section.getStringList(sectionKey);
                        if (!abilities.contains(keyStr) && !abilities.contains("all")) continue;
                        if (region.getId().equalsIgnoreCase(sectionKey)) {
                            return false;
                        }
                    }
                }
            }
        }

        // Проверка по списку Origin’ов игрока
        List<Origin> origins = OriginSwapper.getOrigins(player);
        boolean hasAbility = origins.stream().anyMatch(origin -> origin.hasAbility(getKey()));

        if (abilityMap.get(getKey()) instanceof DependantAbility dependantAbility) {
            boolean dependencyEnabled = dependantAbility.getDependency().isEnabled(player);
            boolean expected = (dependantAbility.getDependencyType() == DependantAbility.DependencyType.REGULAR);
            return hasAbility && (dependencyEnabled == expected);
        }
        return hasAbility;
    }

    interface AbilityRunner {
        void run(Player player);
    }
}
