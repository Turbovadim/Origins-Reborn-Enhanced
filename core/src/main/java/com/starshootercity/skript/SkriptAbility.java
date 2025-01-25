package com.starshootercity.skript;

import com.starshootercity.abilities.Ability;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class SkriptAbility implements Ability {

    private final Key key;

    public SkriptAbility(Key key) {
        this.key = key;
    }

    @Override
    public @NotNull Key getKey() {
        return key;
    }
}
