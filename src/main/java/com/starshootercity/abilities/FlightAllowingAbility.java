package com.starshootercity.abilities;

import org.bukkit.entity.Player;

public interface FlightAllowingAbility extends Ability {
    boolean canFly(Player player);
    float getFlightSpeed(Player player);
}
