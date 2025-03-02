package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import com.starshootercity.cooldowns.CooldownAbility;
import com.starshootercity.cooldowns.Cooldowns;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LaunchIntoAir implements VisibleAbility, Listener, CooldownAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:launch_into_air");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("Every 30 seconds, you are able to launch about 20 blocks up into the air.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Gift of the Winds", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    @EventHandler
    public void onSneakToggle(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        runForAbility(event.getPlayer(), player -> {
            if (player.isGliding()) {
                if (hasCooldown(player)) return;
                setCooldown(player);
                player.setVelocity(player.getVelocity().add(new Vector(0, 2, 0)));
            }
        });
    }

    @Override
    public Cooldowns.@NotNull CooldownInfo getCooldownInfo() {
        return new Cooldowns.CooldownInfo(600, "launch");
    }
}
