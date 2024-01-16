package com.starshootercity.abilities.impossible;

import com.starshootercity.OriginSwapper;
import com.starshootercity.abilities.VisibleAbility;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LikeAir implements VisibleAbility {
    // Currently thought to be impossible on 1.20.4
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:like_air");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("Modifiers to your walking speed also apply while you're airborne.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Like Air", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
