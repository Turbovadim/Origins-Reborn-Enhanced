package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import net.kyori.adventure.key.Key
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier

class Fragile : AttributeModifierAbility, VisibleAbility {
    override fun getKey(): Key {
        return Key.key("origins:fragile")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor("You have 3 less hearts of health than humans.", LineComponent.LineType.DESCRIPTION)
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Fragile", LineComponent.LineType.TITLE)
    }

    override fun getAttribute(): Attribute {
        return NMSInvoker.getMaxHealthAttribute()
    }

    override fun getAmount(): Double {
        return -6.0
    }

    override fun getOperation(): AttributeModifier.Operation {
        return AttributeModifier.Operation.ADD_NUMBER
    }
}
