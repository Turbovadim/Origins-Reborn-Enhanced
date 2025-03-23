package com.starshootercity.abilities.impossible;

import com.starshootercity.abilities.Ability;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class ConduitPowerOnLand implements Ability {
    // Requires a modification to Paper which has been suggested on GitHub, will update if implemented
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:conduit_power_on_land");
    }
}
