package com.starshootercity.abilities.incomplete;

import com.starshootercity.OriginSwapper;
import com.starshootercity.abilities.DependantAbility;
import com.starshootercity.abilities.VisibleAbility;
import net.kyori.adventure.key.Key;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Phasing implements DependantAbility, VisibleAbility, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:phasing");
    }

    @Override
    public @NotNull Key getDependencyKey() {
        return Key.key("origins:phantomized");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("While phantomized, you can walk through solid material, except Obsidian.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Phasing", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
