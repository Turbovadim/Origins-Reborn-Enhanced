package ru.turbovadim.skript

import ru.turbovadim.OriginSwapper.LineData.Companion.makeLineFor
import ru.turbovadim.OriginSwapper.LineData.LineComponent
import ru.turbovadim.abilities.VisibleAbility
import net.kyori.adventure.key.Key

class NamedSkriptAbility(
    key: Key,
    title2: String,
    description2: String
) : SkriptAbility(key), VisibleAbility {

    override val description: MutableList<LineComponent> = makeLineFor(description2, LineComponent.LineType.DESCRIPTION)

    override val title: MutableList<LineComponent> = makeLineFor(title2, LineComponent.LineType.TITLE)

}
