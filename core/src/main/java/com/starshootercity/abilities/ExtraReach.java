package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExtraReach implements VisibleAbility, MultiAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:extra_reach");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You can reach blocks and entities further away.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Slender Body", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    @Override
    public List<Ability> getAbilities() {
        return List.of(ExtraReachBlocks.extraReachBlocks, ExtraReachEntities.extraReachEntities);
    }

    public static class ExtraReachEntities implements AttributeModifierAbility {
        public static ExtraReachEntities extraReachEntities = new ExtraReachEntities();

        @Override
        public @NotNull Attribute getAttribute() {
            return OriginsReborn.getNMSInvoker().getEntityInteractionRangeAttribute();
        }

        @Override
        public double getAmount() {
            return 1.5;
        }

        @Override
        public AttributeModifier.@NotNull Operation getOperation() {
            return AttributeModifier.Operation.ADD_NUMBER;
        }

        @Override
        public @NotNull Key getKey() {
            return Key.key("origins:extra_reach_entities");
        }
    }

    public static class ExtraReachBlocks implements AttributeModifierAbility {
        public static ExtraReachBlocks extraReachBlocks = new ExtraReachBlocks();

        @Override
        public @NotNull Attribute getAttribute() {
            return OriginsReborn.getNMSInvoker().getBlockInteractionRangeAttribute();
        }

        @Override
        public double getAmount() {
            return 1.5;
        }

        @Override
        public AttributeModifier.@NotNull Operation getOperation() {
            return AttributeModifier.Operation.ADD_NUMBER;
        }

        @Override
        public @NotNull Key getKey() {
            return Key.key("origins:extra_reach_blocks");
        }
    }
}
