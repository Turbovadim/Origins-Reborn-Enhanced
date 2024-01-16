package com.starshootercity.abilities;

import net.kyori.adventure.key.Key;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

public class EnderParticles implements ParticleAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:ender_particles");
    }

    @Override
    public Particle getParticle() {
        return Particle.PORTAL;
    }

    @Override
    public int getFrequency() {
        return 4;
    }
}
