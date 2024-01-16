package com.starshootercity;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.util.concurrent.ExecutionException;

public class PackApplier implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            if (OriginsReborn.getInstance().getConfig().getBoolean("resource-pack.enabled")) {
                ResourcePackInfo packInfo = ResourcePackInfo.resourcePackInfo()
                        .uri(URI.create(OriginsReborn.getInstance().getConfig().getString("resource-pack.link", "https://github.com/cometcake575/Origins-Reborn/raw/main/src/main/Origins%20Pack.zip")))
                        .computeHashAndBuild().get();
                event.getPlayer().sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                        .packs(packInfo)
                        .required(true)
                        .build()
                );
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
