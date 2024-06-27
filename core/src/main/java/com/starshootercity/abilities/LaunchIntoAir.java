package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LaunchIntoAir implements VisibleAbility, Listener {
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
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            if (event.getPlayer().isGliding()) {
                if (OriginsReborn.getCooldowns().hasCooldown(event.getPlayer(), key)) return;
                OriginsReborn.getCooldowns().setCooldown(event.getPlayer(), key);
                event.getPlayer().setVelocity(event.getPlayer().getVelocity().add(new Vector(0, 2, 0)));
            }
        });
    }

    private final NamespacedKey key = OriginsReborn.getCooldowns().registerCooldown(new NamespacedKey(OriginsReborn.getInstance(), "launch-into-air"), 600);
}
