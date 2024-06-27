package com.starshootercity.abilities;

import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public interface DefaultSpawnAbility extends Ability {
    @Nullable World getWorld();
}
