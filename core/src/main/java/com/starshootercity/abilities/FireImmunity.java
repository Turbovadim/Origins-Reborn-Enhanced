package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class FireImmunity implements VisibleAbility, Listener {
    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        runForAbility(event.getEntity(), player -> {
            if (Set.of(
                    EntityDamageEvent.DamageCause.FIRE,
                    EntityDamageEvent.DamageCause.FIRE_TICK,
                    EntityDamageEvent.DamageCause.LAVA,
                    EntityDamageEvent.DamageCause.HOT_FLOOR
            ).contains(event.getCause())) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:fire_immunity");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You are immune to all types of fire damage.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Fire Immunity", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
