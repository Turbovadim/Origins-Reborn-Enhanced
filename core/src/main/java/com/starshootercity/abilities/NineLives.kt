package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import net.kyori.adventure.key.Key
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier

class NineLives : AttributeModifierAbility, VisibleAbility {
    override fun getKey(): Key {
        return Key.key("origins:nine_lives")
    }

    override val description: MutableList<LineComponent?> = makeLineFor(
        "You have 1 less heart of health than humans.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent?> = makeLineFor(
        "Nine Lives",
        LineComponent.LineType.TITLE
    )

    override val attribute: Attribute = NMSInvoker.getMaxHealthAttribute()

    override val amount: Double = -2.0

    override val operation: AttributeModifier.Operation = AttributeModifier.Operation.ADD_NUMBER
}
