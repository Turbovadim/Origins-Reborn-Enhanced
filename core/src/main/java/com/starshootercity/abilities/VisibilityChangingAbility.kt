package com.starshootercity.abilities

import org.bukkit.entity.Player

interface VisibilityChangingAbility : Ability {
    fun isInvisible(player: Player): Boolean
}
