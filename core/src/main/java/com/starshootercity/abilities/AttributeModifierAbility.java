package com.starshootercity.abilities;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface AttributeModifierAbility extends Ability {
    @NotNull Attribute getAttribute();
    double getAmount();
    @SuppressWarnings("unused")
    default double getChangedAmount(Player player) {
        return 0;
    }
    AttributeModifier.@NotNull Operation getOperation();
    default double getTotalAmount(Player player) {
        return getAmount() + getChangedAmount(player);
    }
}
