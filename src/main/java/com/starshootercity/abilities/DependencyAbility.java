package com.starshootercity.abilities;

import org.bukkit.entity.Player;

public interface DependencyAbility extends Ability {
    boolean isEnabled(Player player);
}
