package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NineLives implements HealthModifierAbility, VisibleAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:nine_lives");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You have 1 less heart of health than humans.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Nine Lives", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    @Override
    public int getHealth() {
        return 18;
    }
}
