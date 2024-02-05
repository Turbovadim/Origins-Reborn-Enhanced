package com.starshootercity;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PackApplier implements Listener {
    private static final Map<Class<? extends OriginsAddon>, ResourcePackInfo> addonPacks = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> {
            if (isBedrockPlayer(event.getPlayer())) return;
            try {
                if (OriginsReborn.getInstance().getConfig().getBoolean("resource-pack.enabled")) {
                    ResourcePackInfo packInfo = ResourcePackInfo.resourcePackInfo()
                            .uri(URI.create(OriginsReborn.getInstance().getConfig().getString("resource-pack.link", "https://github.com/cometcake575/Origins-Reborn/raw/main/src/main/Origins%20Pack.zip")))
                            .computeHashAndBuild().get();
                    List<ResourcePackInfo> packs = new ArrayList<>();
                    packs.add(packInfo);
                    packs.addAll(addonPacks.values());
                    event.getPlayer().sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                            .packs(packs)
                            .required(true)
                            .build()
                    );
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 30);
    }

    private boolean isBedrockPlayer(Player player) {
        try {
            if (GeyserApi.api().isBedrockPlayer(player.getUniqueId())) {
                GeyserConnection connection = GeyserApi.api().connectionByUuid(player.getUniqueId());
                return connection != null;
            } else return false;
        } catch (NoClassDefFoundError e) {
            return false;
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

    public static void addResourcePack(OriginsAddon addon, @Nullable ResourcePackInfo info) {
        if (info == null) return;
        addonPacks.put(addon.getClass(), info);
    }
}
