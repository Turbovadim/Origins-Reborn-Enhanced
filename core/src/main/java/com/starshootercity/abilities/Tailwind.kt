package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import net.kyori.adventure.key.Key
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier

class Tailwind : AttributeModifierAbility, VisibleAbility {
    override fun getKey(): Key {
        return Key.key("origins:tailwind")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor("You are a little bit quicker on foot than others.", LineComponent.LineType.DESCRIPTION)
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Tailwind", LineComponent.LineType.TITLE)
    }

    override fun getAttribute(): Attribute {
        return NMSInvoker.getMovementSpeedAttribute()
    }

    override fun getAmount(): Double {
        return 0.2
    }

    override fun getOperation(): AttributeModifier.Operation {
        return AttributeModifier.Operation.MULTIPLY_SCALAR_1
    }
}
