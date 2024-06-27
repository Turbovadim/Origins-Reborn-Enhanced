package com.starshootercity.abilities;

import com.starshootercity.AddonLoader;
import com.starshootercity.OriginSwapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface VisibleAbility extends Ability {
    @NotNull List<OriginSwapper.LineData.LineComponent> getDescription();
    @NotNull List<OriginSwapper.LineData.LineComponent> getTitle();

    default List<OriginSwapper.LineData.LineComponent> getUsedDescription() {
        String s = AddonLoader.getTextFor("power." + getKey().toString().replace(":", ".") + ".description");
        if (s != null) return OriginSwapper.LineData.makeLineFor(s, OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
        else return getDescription();
    }

    default List<OriginSwapper.LineData.LineComponent> getUsedTitle() {
        String s = AddonLoader.getTextFor("power." + getKey().toString().replace(":", ".") + ".name");
        if (s != null)
            return OriginSwapper.LineData.makeLineFor(s, OriginSwapper.LineData.LineComponent.LineType.TITLE);
        else return getTitle();
    }
}
