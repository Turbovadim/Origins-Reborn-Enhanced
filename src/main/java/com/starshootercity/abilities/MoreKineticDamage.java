package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MoreKineticDamage implements VisibleAbility, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:more_kinetic_damage");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You take more damage from falling and flying into blocks.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Brittle Bones", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
