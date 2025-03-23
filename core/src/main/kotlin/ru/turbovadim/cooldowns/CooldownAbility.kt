package ru.turbovadim.cooldowns

import ru.turbovadim.OriginsRebornEnhanced.Companion.getCooldowns
import ru.turbovadim.OriginsRebornEnhanced.Companion.instance
import ru.turbovadim.abilities.Ability
import ru.turbovadim.cooldowns.Cooldowns.CooldownInfo
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import ru.turbovadim.OriginsRebornEnhanced

@JvmDefaultWithCompatibility
@Suppress("unused") // Some functions here are unused but are useful in addons
interface CooldownAbility : Ability {
    val cooldownKey: NamespacedKey
        get() = NamespacedKey(instance, getKey().asString().replace(":", "-"))

    fun setCooldown(player: Player) {
        if (OriginsRebornEnhanced.mainConfig.cooldowns.disableAllCooldowns) return
        getCooldowns().setCooldown(player, cooldownKey)
    }

    fun setCooldown(player: Player, amount: Int) {
        if (OriginsRebornEnhanced.mainConfig.cooldowns.disableAllCooldowns) return
        getCooldowns().setCooldown(player, cooldownKey, amount, cooldownInfo.isStatic)
    }

    fun hasCooldown(player: Player): Boolean {
        if (OriginsRebornEnhanced.mainConfig.cooldowns.disableAllCooldowns) return false
        return getCooldowns().hasCooldown(player, cooldownKey)
    }

    fun getCooldown(player: Player): Long {
        return getCooldowns().getCooldown(player, cooldownKey)
    }

    val cooldownInfo: CooldownInfo
}
