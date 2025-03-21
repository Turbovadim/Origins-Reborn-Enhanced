package com.starshootercity.abilities

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.starshootercity.AddonLoader
import com.starshootercity.OriginSwapper
import com.starshootercity.OriginsAddon
import com.starshootercity.OriginsReborn
import com.starshootercity.abilities.AbilityRegister.abilityMap
import com.starshootercity.util.WorldGuardHook
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.endera.enderalib.utils.async.ioDispatcher

interface Ability {

    fun getKey(): Key

    suspend fun runForAbilityAsync(entity: Entity, runner: AsyncAbilityRunner) {
        runForAbilityAsync(entity, runner, null)
    }

    suspend fun runForAbilityAsync(entity: Entity, has: AsyncAbilityRunner?, other: AsyncAbilityRunner?) {
        if (entity is Player) {
            if (hasAbilityAsync(entity)) {
                has?.run(entity)
            } else {
                other?.run(entity)
            }
        }
    }

    fun runForAbility(entity: Entity, runner: AbilityRunner) {
        runForAbility(entity, runner, null)
    }

    fun runForAbility(entity: Entity, has: AbilityRunner?, other: AbilityRunner?) {
        if (entity is Player) {
            if (hasAbility(entity)) {
                has?.run(entity)
            } else {
                other?.run(entity)
            }
        }
    }

    suspend fun hasAbilityAsync(player: Player): Boolean = withContext(ioDispatcher) {
        for (keyStateGetter in AddonLoader.abilityOverrideChecks) {
            val state = keyStateGetter?.get(player, getKey())
            when (state) {
                OriginsAddon.State.DENY -> return@withContext false
                OriginsAddon.State.ALLOW -> return@withContext true
                else -> return@withContext false
            }
        }

        if (OriginsReborn.Companion.isWorldGuardHookInitialized) {
            if (WorldGuardHook.isAbilityDisabled(player.location, this@Ability)) return@withContext false

            val section = OriginsReborn.instance.config.getConfigurationSection("prevent-abilities-in")
            if (section != null) {
                val loc = BukkitAdapter.adapt(player.location)
                val container = WorldGuard.getInstance().platform.regionContainer
                val query = container.createQuery()
                val regions = query.getApplicableRegions(loc)
                val keyStr = getKey().toString()
                for (region in regions) {
                    for (sectionKey in section.getKeys(false)) {
                        val abilities = section.getStringList(sectionKey)
                        if (!abilities.contains(keyStr) && !abilities.contains("all")) continue
                        if (region.id.equals(sectionKey, ignoreCase = true)) {
                            return@withContext false
                        }
                    }
                }
            }
        }

        val origins = OriginSwapper.getOrigins(player)
        var hasAbility = origins.any { it.hasAbility(getKey()) }

        if (abilityMap[getKey()] is DependantAbility) {
            val dependantAbility = abilityMap[getKey()] as DependantAbility
            val dependencyEnabled = dependantAbility.dependency.isEnabled(player)
            val expected = (dependantAbility.dependencyType == DependantAbility.DependencyType.REGULAR)
            hasAbility = hasAbility && (dependencyEnabled == expected)
        }
        return@withContext hasAbility
    }

    fun hasAbility(player: Player): Boolean = runBlocking {
        hasAbilityAsync(player)
    }

    fun interface AbilityRunner {
        fun run(player: Player)
    }

    fun interface AsyncAbilityRunner {
        suspend fun run(player: Player)
    }
}
