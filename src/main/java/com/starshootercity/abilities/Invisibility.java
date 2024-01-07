package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Invisibility implements DependantAbility, VisibleAbility, VisibilityChangingAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:invisibility");
    }

    @Override
    public @NotNull Key getDependencyKey() {
        return Key.key("origins:phantomize");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("While phantomized, you are invisible.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Invisibility", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    @Override
    public boolean isInvisible(Player player) {
        return getDependency().isEnabled(player);
    }
}
