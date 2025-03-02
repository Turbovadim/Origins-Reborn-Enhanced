package com.starshootercity.abilities

import net.kyori.adventure.key.Key
import org.bukkit.Particle

class EnderParticles : ParticleAbility {
    override fun getKey(): Key {
        return Key.key("origins:ender_particles")
    }

    override fun getParticle(): Particle {
        return Particle.PORTAL
    }

    override fun getFrequency(): Int {
        return 4
    }
}
