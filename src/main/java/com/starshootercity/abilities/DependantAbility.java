package com.starshootercity.abilities;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface DependantAbility extends Ability {
    @NotNull Key getDependencyKey();

    default @NotNull DependencyAbility getDependency() {
        return AbilityRegister.dependencyAbilityMap.getOrDefault(getDependencyKey(), new PlaceholderDependencyAbility());
    }

    default DependencyType getDependencyType() {
        return DependencyType.REGULAR;
    }

    enum DependencyType {
        REGULAR,
        INVERSE
    }
}
