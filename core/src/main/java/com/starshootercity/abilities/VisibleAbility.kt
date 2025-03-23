package com.starshootercity.abilities

import com.starshootercity.AddonLoader.getTextFor
import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent

interface VisibleAbility : Ability {
    val description: List<LineComponent?>
    val title: List<LineComponent?>

    val usedDescription: List<LineComponent?>
        get() {
            val keyText = getKey().toString().replace(":", ".")
            val text = getTextFor("power.$keyText.description")
            return if (text != null) {
                makeLineFor(text, LineComponent.LineType.DESCRIPTION)
            } else {
                description
            }
        }

    val usedTitle: List<LineComponent?>
        get() {
            val keyText = getKey().toString().replace(":", ".")
            val text = getTextFor("power.$keyText.name")
            return if (text != null) {
                makeLineFor(text, LineComponent.LineType.TITLE)
            } else {
                title
            }
        }
}
