package com.starshootercity.abilities;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.NotNull;

public interface AttributeModifierAbility extends Ability {
    @NotNull Attribute getAttribute();
    double getAmount();
    AttributeModifier.@NotNull Operation getOperation();
}
