package com.starshootercity.abilities

import net.kyori.adventure.key.Key
import org.bukkit.Particle

class FlameParticles : ParticleAbility {
    override fun getKey(): Key {
        return Key.key("origins:flame_particles")
    }

    override fun getParticle(): Particle {
        return Particle.FLAME
    }

    override fun getFrequency(): Int {
        return 4
    }
}
