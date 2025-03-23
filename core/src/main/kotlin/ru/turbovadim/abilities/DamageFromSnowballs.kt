package ru.turbovadim.abilities

import ru.turbovadim.OriginsRebornEnhanced.Companion.NMSInvoker
import ru.turbovadim.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent

class DamageFromSnowballs : Ability, Listener {
    override fun getKey(): Key {
        return Key.key("origins:damage_from_snowballs")
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        val projectile = event.entity
        if (projectile.type != EntityType.SNOWBALL) return

        val direction = projectile.location.direction
        val hitEntity = event.hitEntity
        if (hitEntity == null) return
        runForAbility(hitEntity) { player ->
            NMSInvoker.dealFreezeDamage(player, 3)
            NMSInvoker.knockback(player, 0.5, -direction.x, -direction.z)
        }
    }

}
