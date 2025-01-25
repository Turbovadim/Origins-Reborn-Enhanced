package com.starshootercity.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.starshootercity.abilities.Ability;
import org.bukkit.Location;

public class WorldGuardHook {

    private static RegionContainer rc;


    public static boolean isAbilityDisabled(Location location, Ability ability) {
        try {
            RegionManager manager = rc.get(BukkitAdapter.adapt(location.getWorld()));
            if (manager == null) return false;
            ApplicableRegionSet set = manager.getApplicableRegions(new BlockVector3(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
            for (ProtectedRegion r : set) {
                String data = r.getFlag(flag);
                if (data != null) {
                    for (String s : data.split(",")) {
                        if (s.equals(ability.getKey().toString())) return true;
                    }
                }
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    private static StringFlag flag;

    public static boolean tryInitialize() {

        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        flag = new StringFlag("origins-reborn:disabled-abilities");
        registry.register(flag);

        return true;
    }

    public static void completeInitialize() {
        rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }
}
