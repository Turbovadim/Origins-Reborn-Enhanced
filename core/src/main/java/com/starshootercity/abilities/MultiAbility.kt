package com.starshootercity.abilities

interface MultiAbility : Ability {
    val abilities: MutableList<Ability>
}
