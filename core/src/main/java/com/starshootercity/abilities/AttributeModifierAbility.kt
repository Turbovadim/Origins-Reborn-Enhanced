package com.starshootercity.abilities;

import net.objecthunter.exp4j.ExpressionBuilder;
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
        double total = getAmount() + getChangedAmount(player);
        if (total != 0) {
            String modifiedValue = AbilityRegister.attributeModifierAbilityFileConfig.getString("%s.value".formatted(getKey()), "x");
            try {
                return new ExpressionBuilder(modifiedValue).build().setVariable("x", total).evaluate();
            } catch (IllegalArgumentException ignored) {}
        }
        return total;
    }

    default AttributeModifier.@NotNull Operation getActualOperation() {
        return switch (AbilityRegister.attributeModifierAbilityFileConfig.getString("%s.operation".formatted(getKey()), "default").toLowerCase()) {
            case "add_scalar" -> AttributeModifier.Operation.ADD_SCALAR;
            case "add_number" -> AttributeModifier.Operation.ADD_NUMBER;
            case "multiply_scalar_1" -> AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            default -> getOperation();
        };
    }
}
