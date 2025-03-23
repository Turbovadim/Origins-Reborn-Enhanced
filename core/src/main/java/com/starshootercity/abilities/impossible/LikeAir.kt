package com.starshootercity.abilities.impossible;

import com.starshootercity.OriginSwapper;
import com.starshootercity.abilities.VisibleAbility;
import net.kyori.adventure.key.Key;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LikeAir implements VisibleAbility, Listener {
    // Currently thought to be impossible on 1.20.6
    // If added then add an automated notice upon an operator joining allowing them to click to either
    // disable notifications about it or to automatically add it to avian, if the avian origin exists and is missing it
    // Auto disable these notifications if avian is ever detected with it so the notification never shows if someone removes it
    // intentionally later - and use 'disable avian update notifications' flag to never check if true
    // should be faster for both speed and general walking
    // Starting after version 2.2.14 this ability is in the default avian.json file however in earlier versions it will not have saved
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
