package com.starshootercity;

import com.starshootercity.packetsenders.OriginsRebornResourcePackInfo;
import com.viaversion.viaversion.api.Via;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PackApplier implements Listener {
    private static final Map<Class<? extends OriginsAddon>, OriginsRebornResourcePackInfo> addonPacks = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (OriginsReborn.getInstance().getConfig().getBoolean("resource-pack.enabled")) {
            if (ShortcutUtils.isBedrockPlayer(event.getPlayer().getUniqueId())) return;
            Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> sendPacks(event.getPlayer()), 120);
        }
    }

    public static void sendPacks(Player player) {
        OriginsReborn.getNMSInvoker().sendResourcePacks(player, getPackURL(player), addonPacks);
    }

    public static String getPackURL(Player player) {
        String ver = getVersion(player);
        return switch (ver) {
            case "1.19.1-1.19.2", "1.19.1", "1.19.2" -> "https://github.com/cometcake575/Origins-Reborn/raw/main/packs/1.19.1-1.19.2.zip";
            case "1.19.3" -> "https://github.com/cometcake575/Origins-Reborn/raw/main/packs/1.19.3.zip";
            case "1.19.4" -> "https://github.com/cometcake575/Origins-Reborn/raw/main/packs/1.19.4.zip";
            case "1.20", "1.20.1", "1.20-1.20.1" -> "https://github.com/cometcake575/Origins-Reborn/raw/main/packs/1.20-1.20.1.zip";
            case "1.20.2" -> "https://github.com/cometcake575/Origins-Reborn/raw/main/packs/1.20.2.zip";
            case "1.20.3", "1.20.4", "1.20.3-1.20.4" -> "https://github.com/cometcake575/Origins-Reborn/raw/main/packs/1.20.3-1.20.4.zip";
            case "1.20.5", "1.20.6", "1.20.5-1.20.6" -> "https://github.com/cometcake575/Origins-Reborn/raw/main/packs/1.20.5-1.20.6.zip";
            case "1.21", "1.21.1", "1.21-1.21.1" -> "https://github.com/cometcake575/Origins-Reborn/raw/main/packs/1.21.zip";
            default -> "https://github.com/cometcake575/Origins-Reborn/raw/main/src/main/Origins%20Pack.zip";
        };
    }

    public static String getVersion(Player player) {
        try {
            return Via.getAPI().getPlayerProtocolVersion(player.getUniqueId()).getName();
        } catch (NoClassDefFoundError e) {
            return Bukkit.getBukkitVersion().split("-")[0];
        }
    }

/*
    @Subscribe
    public void onGeyserLoadResourcePacks(GeyserLoadResourcePacksEvent event) {
        event.resourcePacks().add(new File(OriginsReborn.getInstance().getDataFolder(), "bedrock-packs/bedrock.mcpack").toPath());
    }

    public PackApplier() {
        OriginsReborn.getInstance().saveResource("bedrock.mcpack", false);
    }

 */

    public static void addResourcePack(OriginsAddon addon, @NotNull OriginsRebornResourcePackInfo info) {
        addonPacks.put(addon.getClass(), info);
    }
}
