package com.starshootercity.abilities.incomplete;

import com.starshootercity.abilities.Ability;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class EnderParticles implements Ability {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:ender_particles");
    }
}
