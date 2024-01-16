package com.starshootercity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PackApplier implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (OriginsReborn.getInstance().getConfig().getBoolean("none"));
    }
}
