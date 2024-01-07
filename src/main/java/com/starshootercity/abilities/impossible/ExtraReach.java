package com.starshootercity.abilities.impossible;

import com.starshootercity.OriginSwapper;
import com.starshootercity.abilities.VisibleAbility;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExtraReach implements VisibleAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:extra_reach");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You can reach blocks and entities further away.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Slender Body", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
