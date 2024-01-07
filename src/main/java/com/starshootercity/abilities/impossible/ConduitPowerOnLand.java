package com.starshootercity.abilities.impossible;

import com.starshootercity.abilities.Ability;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class ConduitPowerOnLand implements Ability {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:conduit_power_on_land");
    }
}
