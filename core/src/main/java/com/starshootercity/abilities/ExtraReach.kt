package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import net.kyori.adventure.key.Key
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier

class ExtraReach : VisibleAbility, MultiAbility {
    override fun getKey(): Key {
        return Key.key("origins:extra_reach")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor("You can reach blocks and entities further away.", LineComponent.LineType.DESCRIPTION)
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Slender Body", LineComponent.LineType.TITLE)
    }

    override fun getAbilities(): MutableList<Ability> {
        return listOf<Ability>(
            ExtraReachBlocks.Companion.extraReachBlocks,
            ExtraReachEntities.Companion.extraReachEntities
        ).toMutableList()
    }

    class ExtraReachEntities : AttributeModifierAbility {
        override fun getAttribute(): Attribute {
            return NMSInvoker.getEntityInteractionRangeAttribute()!!
        }

        override fun getAmount(): Double {
            return 1.5
        }

        override fun getOperation(): AttributeModifier.Operation {
            return AttributeModifier.Operation.ADD_NUMBER
        }

        override fun getKey(): Key {
            return Key.key("origins:extra_reach_entities")
        }

        companion object {
            var extraReachEntities: ExtraReachEntities = ExtraReachEntities()
        }
    }

    class ExtraReachBlocks : AttributeModifierAbility {
        override fun getAttribute(): Attribute {
            return NMSInvoker.getBlockInteractionRangeAttribute()!!
        }

        override fun getAmount(): Double {
            return 1.5
        }

        override fun getOperation(): AttributeModifier.Operation {
            return AttributeModifier.Operation.ADD_NUMBER
        }

        override fun getKey(): Key {
            return Key.key("origins:extra_reach_blocks")
        }

        companion object {
            var extraReachBlocks: ExtraReachBlocks = ExtraReachBlocks()
        }
    }
}
