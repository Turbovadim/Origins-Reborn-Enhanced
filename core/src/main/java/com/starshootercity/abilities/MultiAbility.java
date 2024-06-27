package com.starshootercity.abilities;

import java.util.List;

public interface MultiAbility extends Ability {
    List<Ability> getAbilities();
}
