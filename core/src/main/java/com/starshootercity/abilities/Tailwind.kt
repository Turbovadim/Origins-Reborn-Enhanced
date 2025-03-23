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

    override val description: MutableList<LineComponent> = makeLineFor("You are a little bit quicker on foot than others.", LineComponent.LineType.DESCRIPTION)

    override val title: MutableList<LineComponent> = makeLineFor("Tailwind", LineComponent.LineType.TITLE)

    override val attribute: Attribute = NMSInvoker.movementSpeedAttribute

    override val amount: Double = 0.2

    override val operation: AttributeModifier.Operation = AttributeModifier.Operation.MULTIPLY_SCALAR_1
}
