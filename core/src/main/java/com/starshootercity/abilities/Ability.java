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
            } else if (other != null) other.run(player);
        }
    }

    default boolean hasAbility(Player player) {

        for (OriginsAddon.KeyStateGetter keyStateGetter : AddonLoader.abilityOverrideChecks) {
            OriginsAddon.State state = keyStateGetter.get(player, getKey());
            if (state == OriginsAddon.State.DENY) return false;
            else if (state == OriginsAddon.State.ALLOW) return true;
        }

        if (OriginsReborn.isWorldGuardHookInitialized()) {
            if (WorldGuardHook.isAbilityDisabled(player.getLocation(), this)) return false;

            ConfigurationSection section = OriginsReborn.getInstance().getConfig().getConfigurationSection("prevent-abilities-in");
            if (section != null) {
                Location loc = BukkitAdapter.adapt(player.getLocation());
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionQuery query = container.createQuery();
                ApplicableRegionSet set = query.getApplicableRegions(loc);
                for (ProtectedRegion region : set) {
                    for (String sectionKey : section.getKeys(false)) {
                        if (!section.getStringList(sectionKey).contains(getKey().toString()) && !section.getStringList(sectionKey).contains("all"))
                            continue;
                        if (region.getId().equalsIgnoreCase(sectionKey)) {
                            return false;
                        }
                    }
                }
            }
        }

        List<Origin> origins = OriginSwapper.getOrigins(player);
        boolean hasAbility = false;
        for (Origin origin : origins) {
            if (origin.hasAbility(getKey())) hasAbility = true;
        }
        if (abilityMap.get(getKey()) instanceof DependantAbility dependantAbility) {
            return hasAbility && ((dependantAbility.getDependencyType() == DependantAbility.DependencyType.REGULAR) == dependantAbility.getDependency().isEnabled(player));
        }
        return hasAbility;
    }

    interface AbilityRunner {
        void run(Player player);
    }
}
