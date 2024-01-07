package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface VisibleAbility extends Ability {
    @NotNull List<OriginSwapper.LineData.LineComponent> getDescription();
    @NotNull List<OriginSwapper.LineData.LineComponent> getTitle();
}
