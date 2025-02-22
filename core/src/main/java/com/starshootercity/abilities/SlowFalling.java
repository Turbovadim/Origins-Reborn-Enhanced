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
        runForAbility(event.getPlayer(), player -> {
            if (player.isSneaking()) {
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
            } else player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, ShortcutUtils.infiniteDuration(), 0, false, false));
        });
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:slow_falling");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("Вы падаете на землю так же мягко, как перо, если только вы не приседаете.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Перышко", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
