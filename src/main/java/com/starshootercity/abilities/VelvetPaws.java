package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.GameEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.GenericGameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VelvetPaws implements VisibleAbility, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:velvet_paws");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("Your footsteps don't cause any vibrations which could otherwise be picked up by nearby lifeforms.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Velvet Paws", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    @EventHandler
    public void onGenericGameEvent(GenericGameEvent event) {
        if (event.getEvent() == GameEvent.STEP) {
            AbilityRegister.runForAbility(event.getEntity(), getKey(), () -> event.setCancelled(true));
        }
    }
}
