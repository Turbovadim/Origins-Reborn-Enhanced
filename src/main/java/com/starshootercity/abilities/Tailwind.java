package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Tailwind implements SpeedModifierAbility, VisibleAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:tailwind");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You are a little bit quicker on foot than others.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Tailwind", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    @Override
    public double getSpeedIncrease() {
        return 0.2;
    }
}
