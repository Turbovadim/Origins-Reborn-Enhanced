package com.starshootercity.abilities

import org.bukkit.entity.Player

interface DependencyAbility : Ability {
    fun isEnabled(player: Player): Boolean
}
