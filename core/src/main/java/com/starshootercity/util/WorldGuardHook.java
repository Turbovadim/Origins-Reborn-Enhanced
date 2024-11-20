package com.starshootercity.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;

public class WorldGuardHook {
    private static boolean initialized = false;

    private static RegionContainer rc;

    public static boolean isFlagDenied(Location location, WGFlag... flags) {
        if (!isInitialized()) return false;
        RegionManager manager = rc.get(BukkitAdapter.adapt(location.getWorld()));
        if (manager == null) return false;
        ApplicableRegionSet set = manager.getApplicableRegions(new BlockVector3(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        for (ProtectedRegion r : set) {
            for (WGFlag flag : flags) {
                StateFlag.State s = r.getFlag(getFlagFor(flag));
                if (s == null) continue;
                if (s.equals(StateFlag.State.DENY)) return true;
            }
        }
        return false;
    }

    public static void tryInitialize() {
        try {
            rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
            initialized = true;
        } catch (NoClassDefFoundError e) {
            initialized = false;
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static StateFlag getFlagFor(WGFlag flag) {
        return switch (flag) {
            case BLOCK_BREAK -> Flags.BLOCK_BREAK;
            case BLOCK_PLACE -> Flags.BLOCK_PLACE;
            case PASSTHROUGH -> Flags.PASSTHROUGH;
            case BUILD -> Flags.BUILD;
            case USE -> Flags.USE;
            case INTERACT -> Flags.INTERACT;
            case DAMAGE_ANIMALS -> Flags.DAMAGE_ANIMALS;
            case PVP -> Flags.PVP;
            case SLEEP -> Flags.SLEEP;
            case RESPAWN_ANCHORS -> Flags.RESPAWN_ANCHORS;
            case TNT -> Flags.TNT;
            case CHEST_ACCESS -> Flags.CHEST_ACCESS;
            case PLACE_VEHICLE -> Flags.PLACE_VEHICLE;
            case DESTROY_VEHICLE -> Flags.DESTROY_VEHICLE;
            case LIGHTER -> Flags.LIGHTER;
            case RIDE -> Flags.RIDE;
            case POTION_SPLASH -> Flags.POTION_SPLASH;
            case ITEM_FRAME_ROTATE -> Flags.ITEM_FRAME_ROTATE;
            case TRAMPLE_BLOCKS -> Flags.TRAMPLE_BLOCKS;
            case FIREWORK_DAMAGE -> Flags.FIREWORK_DAMAGE;
            case USE_ANVIL -> Flags.USE_ANVIL;
            case USE_DRIPLEAF -> Flags.USE_DRIPLEAF;
            case ITEM_PICKUP -> Flags.ITEM_PICKUP;
            case ITEM_DROP -> Flags.ITEM_DROP;
            case EXP_DROPS -> Flags.EXP_DROPS;
            case MOB_DAMAGE -> Flags.MOB_DAMAGE;
            case CREEPER_EXPLOSION -> Flags.CREEPER_EXPLOSION;
            case ENDERDRAGON_BLOCK_DAMAGE -> Flags.ENDERDRAGON_BLOCK_DAMAGE;
            case GHAST_FIREBALL -> Flags.GHAST_FIREBALL;
            case OTHER_EXPLOSION -> Flags.OTHER_EXPLOSION;
            case WITHER_DAMAGE -> Flags.WITHER_DAMAGE;
            case ENDER_BUILD -> Flags.ENDER_BUILD;
            case SNOWMAN_TRAILS -> Flags.SNOWMAN_TRAILS;
            case RAVAGER_RAVAGE -> Flags.RAVAGER_RAVAGE;
            case ENTITY_PAINTING_DESTROY -> Flags.ENTITY_PAINTING_DESTROY;
            case MOB_SPAWNING -> Flags.MOB_SPAWNING;
            case PISTONS -> Flags.PISTONS;
            case FIRE_SPREAD -> Flags.FIRE_SPREAD;
            case LAVA_FIRE -> Flags.LAVA_FIRE;
            case LIGHTNING -> Flags.LIGHTNING;
            case SNOW_FALL -> Flags.SNOW_FALL;
            case SNOW_MELT -> Flags.SNOW_MELT;
            case ICE_FORM -> Flags.ICE_FORM;
            case ICE_MELT -> Flags.ICE_MELT;
            case FROSTED_ICE_MELT -> Flags.FROSTED_ICE_MELT;
            case FROSTED_ICE_FORM -> Flags.FROSTED_ICE_FORM;
            case MUSHROOMS -> Flags.MUSHROOMS;
            case LEAF_DECAY -> Flags.LEAF_DECAY;
            case GRASS_SPREAD -> Flags.GRASS_SPREAD;
            case MYCELIUM_SPREAD -> Flags.MYCELIUM_SPREAD;
            case VINE_GROWTH -> Flags.VINE_GROWTH;
            case ROCK_GROWTH -> Flags.ROCK_GROWTH;
            case SCULK_GROWTH -> Flags.SCULK_GROWTH;
            case CROP_GROWTH -> Flags.CROP_GROWTH;
            case SOIL_DRY -> Flags.SOIL_DRY;
            case CORAL_FADE -> Flags.CORAL_FADE;
            case COPPER_FADE -> Flags.COPPER_FADE;
            case WATER_FLOW -> Flags.WATER_FLOW;
            case LAVA_FLOW -> Flags.LAVA_FLOW;
            case SEND_CHAT -> Flags.SEND_CHAT;
            case RECEIVE_CHAT -> Flags.RECEIVE_CHAT;
            case INVINCIBILITY -> Flags.INVINCIBILITY;
            case FALL_DAMAGE -> Flags.FALL_DAMAGE;
            case HEALTH_REGEN -> Flags.HEALTH_REGEN;
            case HUNGER_DRAIN -> Flags.HUNGER_DRAIN;
            case ENTRY -> Flags.ENTRY;
            case EXIT -> Flags.EXIT;
            case EXIT_VIA_TELEPORT -> Flags.EXIT_VIA_TELEPORT;
            case ENDERPEARL -> Flags.ENDERPEARL;
            case CHORUS_TELEPORT -> Flags.CHORUS_TELEPORT;
        };
    }

    public enum WGFlag {
        BLOCK_BREAK,
        BLOCK_PLACE,
        PASSTHROUGH,
        BUILD,
        USE,
        INTERACT,
        DAMAGE_ANIMALS,
        PVP,
        SLEEP,
        RESPAWN_ANCHORS,
        TNT,
        CHEST_ACCESS,
        PLACE_VEHICLE,
        DESTROY_VEHICLE,
        LIGHTER,
        RIDE,
        POTION_SPLASH,
        ITEM_FRAME_ROTATE,
        TRAMPLE_BLOCKS,
        FIREWORK_DAMAGE,
        USE_ANVIL,
        USE_DRIPLEAF,
        ITEM_PICKUP,
        ITEM_DROP,
        EXP_DROPS,
        MOB_DAMAGE,
        CREEPER_EXPLOSION,
        ENDERDRAGON_BLOCK_DAMAGE,
        GHAST_FIREBALL,
        OTHER_EXPLOSION,
        WITHER_DAMAGE,
        ENDER_BUILD,
        SNOWMAN_TRAILS,
        RAVAGER_RAVAGE,
        ENTITY_PAINTING_DESTROY,
        MOB_SPAWNING,
        PISTONS,
        FIRE_SPREAD,
        LAVA_FIRE,
        LIGHTNING,
        SNOW_FALL,
        SNOW_MELT,
        ICE_FORM,
        ICE_MELT,
        FROSTED_ICE_MELT,
        FROSTED_ICE_FORM,
        MUSHROOMS,
        LEAF_DECAY,
        GRASS_SPREAD,
        MYCELIUM_SPREAD,
        VINE_GROWTH,
        ROCK_GROWTH,
        SCULK_GROWTH,
        CROP_GROWTH,
        SOIL_DRY,
        CORAL_FADE,
        COPPER_FADE,
        WATER_FLOW,
        LAVA_FLOW,
        SEND_CHAT,
        RECEIVE_CHAT,
        INVINCIBILITY,
        FALL_DAMAGE,
        HEALTH_REGEN,
        HUNGER_DRAIN,
        ENTRY,
        EXIT,
        EXIT_VIA_TELEPORT,
        ENDERPEARL,
        CHORUS_TELEPORT
    }
}
