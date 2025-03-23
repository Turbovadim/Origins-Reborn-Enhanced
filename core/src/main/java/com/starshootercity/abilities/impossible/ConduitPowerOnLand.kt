package com.starshootercity.abilities.impossible

import com.starshootercity.abilities.Ability
import net.kyori.adventure.key.Key

class ConduitPowerOnLand : Ability {
    // Requires a modification to Paper which has been suggested on GitHub, will update if implemented
    override fun getKey(): Key {
        return Key.key("origins:conduit_power_on_land")
    }
}
