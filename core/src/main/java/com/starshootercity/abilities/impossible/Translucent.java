package com.starshootercity.abilities.impossible;

import com.starshootercity.OriginSwapper;
import com.starshootercity.abilities.VisibleAbility;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Translucent implements VisibleAbility {
    // Currently thought to be impossible without having unintended effects on 1.20.6
    // If added then add an automated notice upon an operator joining allowing them to click to either
    // disable notifications about it or to automatically add it to phantom, if the avian origin exists and is missing it
    // Auto disable these notifications if phantom is ever detected with it so the notification never shows if someone removes it
    // Starting after version 2.2.14 this ability is in the default phantom.json file however in earlier versions it will not have saved
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
