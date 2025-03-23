package com.starshootercity.skript;

import com.starshootercity.OriginSwapper;
import com.starshootercity.abilities.VisibleAbility;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NamedSkriptAbility extends SkriptAbility implements VisibleAbility {

    private final String title;
    private final String description;

    public NamedSkriptAbility(Key key, String title, String description) {
        super(key);
        this.title = title;
        this.description = description;
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor(description, OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor(title, OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
