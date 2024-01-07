package com.starshootercity.abilities;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderDependencyAbility implements DependencyAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:blank_dependency");
    }

    @Override
    public boolean isEnabled(Player player) {
        return false;
    }
}
