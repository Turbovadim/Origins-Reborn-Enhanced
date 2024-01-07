package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MoreExhaustion implements VisibleAbility, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:more_exhaustion");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You exhaust much quicker than others, thus requiring you to eat more.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Large Appetite", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    @EventHandler
    public void onEntityExhaustion(EntityExhaustionEvent event) {
        AbilityRegister.runForAbility(event.getEntity(), getKey(), () -> event.setExhaustion(event.getExhaustion() * 1.6f));
    }
}
