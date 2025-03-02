package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import net.kyori.adventure.key.Key
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier

class NaturalArmor : AttributeModifierAbility, VisibleAbility {
    override fun getKey(): Key {
        return Key.key("origins:natural_armor")
    }

    override fun getAttribute(): Attribute {
        return NMSInvoker.getArmorAttribute()
    }

    override fun getAmount(): Double {
        return 8.0
    }

    override fun getOperation(): AttributeModifier.Operation {
        return AttributeModifier.Operation.ADD_NUMBER
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "Even without wearing armor, your skin provides natural protection.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Sturdy Skin", LineComponent.LineType.TITLE)
    }
}
