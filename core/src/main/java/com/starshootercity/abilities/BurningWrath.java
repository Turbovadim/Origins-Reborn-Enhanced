package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BurningWrath implements VisibleAbility, Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        AbilityRegister.runForAbility(event.getDamager(), getKey(), () -> {
            if (event.getDamager().getFireTicks() > 0) event.setDamage(event.getDamage() + 3);
        });
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:burning_wrath");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("When on fire, you deal additional damage with your attacks.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Burning Wrath", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
