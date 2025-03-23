package com.starshootercity.abilities.custom

import com.starshootercity.abilities.Ability
import com.starshootercity.abilities.custom.ToggleableAbilities.isEnabled
import com.starshootercity.abilities.custom.ToggleableAbilities.registerAbility

interface ToggleableAbility : Ability {
    fun shouldRegister(): Boolean {
        registerAbility(this)
        return isEnabled(this)
    }
}
