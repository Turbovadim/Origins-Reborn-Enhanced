package com.starshootercity.abilities.impossible;

import com.starshootercity.OriginSwapper;
import com.starshootercity.abilities.VisibleAbility;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Translucent implements VisibleAbility {
    // Currently thought to be impossible without having unintended effects on 1.20.4
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:translucent");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("Your skin is translucent.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Translucent", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
