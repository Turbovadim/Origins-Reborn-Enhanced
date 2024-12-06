package com.starshootercity.abilities.custom;

import com.starshootercity.abilities.Ability;

public interface ToggleableAbility extends Ability {
    default boolean shouldRegister() {
        ToggleableAbilities.registerAbility(this);
        return ToggleableAbilities.isEnabled(this);
    }
}
