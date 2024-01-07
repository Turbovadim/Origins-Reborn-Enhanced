package com.starshootercity.abilities;

import org.bukkit.entity.Player;

public interface VisibilityChangingAbility extends Ability {
    boolean isInvisible(Player player);
}
