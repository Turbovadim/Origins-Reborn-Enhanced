package com.starshootercity;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
                        .uri(URI.create(OriginsReborn.getInstance().getConfig().getString("resource-pack.link", "https://minhaskamal.github.io/DownGit/#/home?url=https://github.com/cometcake575/Origins-Reborn/blob/main/src/main/Origins%20Pack.zip")))
                        .computeHashAndBuild().get();
                event.getPlayer().sendResourcePacks(packInfo);
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
