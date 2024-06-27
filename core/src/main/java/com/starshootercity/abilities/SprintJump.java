package com.starshootercity.abilities;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SprintJump implements VisibleAbility, Listener {
    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AbilityRegister.runForAbility(player, getKey(),
                    () -> {
                        if (player.isSprinting()) {
                            player.addPotionEffect(new PotionEffect(OriginsReborn.getNMSInvoker().getJumpBoostEffect(), 5, 1, false, false));
                        }
                    });
        }
    }
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:sprint_jump");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You are able to jump higher by jumping while sprinting.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Strong Ankles", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
