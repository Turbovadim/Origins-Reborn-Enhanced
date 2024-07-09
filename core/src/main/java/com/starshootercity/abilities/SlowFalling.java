package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import com.starshootercity.ShortcutUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SlowFalling implements VisibleAbility, Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            if (event.getPlayer().isSneaking()) {
                event.getPlayer().removePotionEffect(PotionEffectType.SLOW_FALLING);
            } else event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, ShortcutUtils.infiniteDuration(), 0, false, false));
        });
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:slow_falling");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You fall as gently to the ground as a feather would, unless you sneak.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Featherweight", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
