package com.starshootercity.skript

import com.starshootercity.abilities.Ability
import net.kyori.adventure.key.Key

open class SkriptAbility(private val key: Key) : Ability {
    override fun getKey(): Key {
        return key
    }
}
