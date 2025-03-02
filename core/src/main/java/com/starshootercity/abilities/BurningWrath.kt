package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class BurningWrath : VisibleAbility, Listener {

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        runForAbility(event.damager, AbilityRunner { player: Player ->
            if (player.fireTicks > 0) event.setDamage(event.damage + 3)
        })
    }

    override fun getKey(): Key {
        return Key.key("origins:burning_wrath")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "When on fire, you deal additional damage with your attacks.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Burning Wrath", LineComponent.LineType.TITLE)
    }
}
