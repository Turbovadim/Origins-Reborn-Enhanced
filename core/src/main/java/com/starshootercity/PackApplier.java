package com.starshootercity;

import com.starshootercity.packetsenders.OriginsRebornResourcePackInfo;
import org.bukkit.Bukkit;
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
            Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> OriginsReborn.getNMSInvoker().sendResourcePacks(event.getPlayer(), OriginsReborn.getNMSInvoker().getPackURL(), addonPacks), 120);
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
