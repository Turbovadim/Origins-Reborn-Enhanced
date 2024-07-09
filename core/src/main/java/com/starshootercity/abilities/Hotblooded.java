package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Hotblooded implements VisibleAbility, Listener {
    @EventHandler
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        AbilityRegister.runForAbility(event.getEntity(), getKey(), () -> {
            if (event.getNewEffect() != null) {
                if (event.getNewEffect().getType().equals(PotionEffectType.POISON) || event.getNewEffect().getType().equals(PotionEffectType.HUNGER)) {
                    event.setCancelled(true);
                }
            }
        });
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:hotblooded");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("Due to your hot body, venoms burn up, making you immune to poison and hunger status effects.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Hotblooded", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
