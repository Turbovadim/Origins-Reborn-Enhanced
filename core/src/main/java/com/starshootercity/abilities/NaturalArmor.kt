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

    override val attribute: Attribute = NMSInvoker.armorAttribute

    override val amount: Double = 8.0

    override val operation: AttributeModifier.Operation = AttributeModifier.Operation.ADD_NUMBER

    override val description: MutableList<LineComponent> = makeLineFor(
        "Even without wearing armor, your skin provides natural protection.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent> = makeLineFor(
        "Sturdy Skin",
        LineComponent.LineType.TITLE
    )
}
