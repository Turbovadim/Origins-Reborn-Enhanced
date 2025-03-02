package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.persistence.PersistentDataType
import java.util.function.Predicate

class ScareCreepers : VisibleAbility, Listener {
    override fun getKey(): Key {
        return Key.key("origins:scare_creepers")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "Creepers are scared of you and will only explode if you attack them first.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Catlike Appearance", LineComponent.LineType.TITLE)
    }

    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        val creeper = event.entity
        if (creeper is Creeper) {
            fixCreeper(creeper)
        }
    }

    @EventHandler
    fun onEntitiesLoad(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            if (entity is Creeper) {
                fixCreeper(entity)
            }
        }
    }

    fun fixCreeper(creeper: Creeper) {
        val afraidGoal = NMSInvoker.getCreeperAfraidGoal(
            creeper,
            Predicate { player -> hasAbility(player) },
            Predicate { livingEntity ->
                creeper.persistentDataContainer.get(hitByPlayerKey, PersistentDataType.STRING)
                    ?.let { stored -> stored == livingEntity?.uniqueId?.toString() } == true
            }
        )
        Bukkit.getMobGoals().addGoal(creeper, 0, afraidGoal)
    }

    private val hitByPlayerKey = NamespacedKey(instance, "hit-by-player")

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.entity.type != EntityType.CREEPER) return

        val player: Player = when (val damager = event.damager) {
            is Projectile -> {
                val shooter = damager.shooter
                shooter as? Player ?: return
            }
            is Player -> damager
            else -> return
        }

        runForAbility(player, AbilityRunner { p ->
            p.persistentDataContainer.set(hitByPlayerKey, PersistentDataType.STRING, p.uniqueId.toString())
        })
    }


    @EventHandler
    fun onEntityTargetLivingEntity(event: EntityTargetLivingEntityEvent) {
        if (event.getEntity().type == EntityType.CREEPER) {
            runForAbility(event.target, AbilityRunner { player: Player? ->
                val data = event.getEntity().persistentDataContainer
                    .get<String?, String?>(hitByPlayerKey, PersistentDataType.STRING)
                if (data == null) {
                    event.isCancelled = true
                    return@AbilityRunner
                }
                if (data != player!!.uniqueId.toString()) {
                    event.isCancelled = true
                }
            })
        }
    }
}
