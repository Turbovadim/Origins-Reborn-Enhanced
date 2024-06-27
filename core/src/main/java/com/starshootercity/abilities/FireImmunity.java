package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FireImmunity implements VisibleAbility, Listener {
    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        AbilityRegister.runForAbility(event.getEntity(), getKey(), () -> {
            List<EntityDamageEvent.DamageCause> causes = new ArrayList<>() {{
                add(EntityDamageEvent.DamageCause.FIRE);
                add(EntityDamageEvent.DamageCause.FIRE_TICK);
                add(EntityDamageEvent.DamageCause.LAVA);
                add(EntityDamageEvent.DamageCause.HOT_FLOOR);
            }};

            if (causes.contains(event.getCause())) {
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
