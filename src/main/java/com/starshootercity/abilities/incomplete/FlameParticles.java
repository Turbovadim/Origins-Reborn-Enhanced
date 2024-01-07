package com.starshootercity.abilities.incomplete;

import com.starshootercity.abilities.Ability;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class FlameParticles implements Ability {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:flame_particles");
    }
}
