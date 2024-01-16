package com.starshootercity.abilities;

import net.kyori.adventure.key.Key;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

public class FlameParticles implements ParticleAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:flame_particles");
    }

    @Override
    public Particle getParticle() {
        return Particle.FLAME;
    }

    @Override
    public int getFrequency() {
        return 4;
    }
}
