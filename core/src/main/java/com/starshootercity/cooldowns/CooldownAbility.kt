package com.starshootercity.cooldowns

import com.starshootercity.OriginsReborn.Companion.getCooldowns
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.abilities.Ability
import com.starshootercity.cooldowns.Cooldowns.CooldownInfo
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

@JvmDefaultWithCompatibility
@Suppress("unused") // Some functions here are unused but are useful in addons
interface CooldownAbility : Ability {
    val cooldownKey: NamespacedKey
        get() = NamespacedKey(instance, getKey().asString().replace(":", "-"))

    fun setCooldown(player: Player) {
        if (instance.getConfig().getBoolean("cooldowns.disable-all-cooldowns")) return
        getCooldowns().setCooldown(player, cooldownKey)
    }

    fun setCooldown(player: Player, amount: Int) {
        if (instance.getConfig().getBoolean("cooldowns.disable-all-cooldowns")) return
        getCooldowns().setCooldown(player, cooldownKey, amount, cooldownInfo.isStatic)
    }

    fun hasCooldown(player: Player): Boolean {
        if (instance.getConfig().getBoolean("cooldowns.disable-all-cooldowns")) return false
        return getCooldowns().hasCooldown(player, cooldownKey)
    }

    fun getCooldown(player: Player): Long {
        return getCooldowns().getCooldown(player, cooldownKey)
    }

    val cooldownInfo: CooldownInfo
}
