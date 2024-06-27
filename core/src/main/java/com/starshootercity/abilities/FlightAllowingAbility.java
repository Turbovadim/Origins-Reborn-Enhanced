package com.starshootercity.abilities;

import net.kyori.adventure.util.TriState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface FlightAllowingAbility extends Ability {
    boolean canFly(Player player);
    float getFlightSpeed(Player player);
    default @NotNull TriState getFlyingFallDamage(Player player) {
        return TriState.FALSE;
    }
}
