package com.starshootercity

import com.starshootercity.abilities.Ability
import com.starshootercity.abilities.AbilityRegister
import com.starshootercity.abilities.MultiAbility
import com.starshootercity.abilities.VisibleAbility
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.util.*

class Origin(
    private val name: String,
    val icon: ItemStack,
    val position: Int,
    @get:org.jetbrains.annotations.Range(from = 0, to = 3) impactParam: Int,
    val displayName: String,
    private val abilities: List<Key>,
    private val description: String,
    val addon: OriginsAddon,
    private val unchoosable: Boolean,
    val priority: Int,
    val permission: String?,
    val cost: Int?, // may be null
    private val max: Int,
    val layer: String
) {
    val team: Team? = if (OriginsReborn.instance.config.getBoolean("display.enable-prefixes")) {
        val scoreboard: Scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        scoreboard.getTeam(name)?.unregister()
        val newTeam = scoreboard.registerNewTeam(name)
        newTeam.displayName(Component.text("[")
            .color(NamedTextColor.DARK_GRAY)
            .append(Component.text(name).color(NamedTextColor.WHITE))
            .append(Component.text("] ").color(NamedTextColor.DARK_GRAY))
        )
        newTeam
    } else {
        null
    }

    val impact: Char = when (impactParam) {
        0 -> '\uE002'
        1 -> '\uE003'
        2 -> '\uE004'
        else -> '\uE005'
    }

//    fun getCost(): Int? = cost

    fun isUnchoosable(player: Player): Boolean {
        if (unchoosable) return true
        val instance = OriginsReborn.instance
        val mode = instance.config.getString("restrictions.reusing-origins", "NONE")
        val same = instance.config.getBoolean("restrictions.prevent-same-origins")
        if (max != -1) {
            var num = 0
            for (p in OriginSwapper.originFileConfiguration.getKeys(false)) {
                if (OriginSwapper.originFileConfiguration.getString(p, "").equals(getName().lowercase(Locale.getDefault()))) {
                    num++
                }
            }
            if (num >= max) return true
        }
        if (same) {
            for (p in OriginSwapper.originFileConfiguration.getKeys(false)) {
                if (OriginSwapper.originFileConfiguration.getString(p, "").equals(getName().lowercase(Locale.getDefault()))) {
                    return true
                }
            }
        }
        return when (mode) {
            "PERPLAYER" -> OriginSwapper.usedOriginFileConfiguration
                .getStringList(player.uniqueId.toString())
                .contains(getName().lowercase(Locale.getDefault()))
            "ALL" -> {
                for (p in OriginSwapper.usedOriginFileConfiguration.getKeys(false)) {
                    if (OriginSwapper.usedOriginFileConfiguration.getStringList(p)
                            .contains(getName().lowercase(Locale.getDefault()))
                    ) {
                        return true
                    }
                }
                false
            }
            else -> false
        }
    }

//    fun getPriority(): Int = priority

//    fun getTeam(): Team? = team

//    fun getPermission(): String? = permission
//
    fun hasPermission(): Boolean = permission != null

//    fun getLayer(): String = layer

    fun getNameForDisplay(): String = displayName

//    fun getAddon(): OriginsAddon = addon

    fun getVisibleAbilities(): List<VisibleAbility> {
        val result = mutableListOf<VisibleAbility>()
        for (key in abilities) {
            val ability = AbilityRegister.abilityMap[key]
            if (ability is VisibleAbility) result.add(ability)
        }
        return result
    }

    fun getAbilities(): List<Ability> {
        val originAbilities = mutableListOf<Ability>()
        for (key in abilities) {
            val a = AbilityRegister.abilityMap[key]
            originAbilities.add(a!!)
            if (a is MultiAbility) originAbilities.addAll(a.getAbilities())
        }
        return originAbilities
    }

    fun hasAbility(key: Key): Boolean {
        for (ability in AbilityRegister.multiAbilityMap.getOrDefault(key, listOf())) {
            if (abilities.contains(ability.getKey())) return true
        }
        return abilities.contains(key)
    }

//    fun getImpact(): Char = impact

//    fun getPosition(): Int = position

    fun getName(): String {
        return AddonLoader.getTextFor(
            "origin.${addon.getNamespace()}.${name.replace(" ", "_").lowercase(Locale.getDefault())}.name",
            name
        )
    }

    fun getActualName(): String = name

    fun getDescription(): String {
        return AddonLoader.getTextFor(
            "origin.${addon.getNamespace()}.${name.replace(" ", "_").lowercase(Locale.getDefault())}.description",
            description
        )
    }

//    fun getIcon(): ItemStack = icon

    fun getResourceURL(): String {
        val keyValue = icon.type.key.value()
        val folder = if (icon.type.isBlock) "block" else "item"
        return "https://assets.mcasset.cloud/1.20.4/assets/minecraft/textures/$folder/$keyValue.png"
    }
}
