package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class AerialCombatant : VisibleAbility, Listener {
    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        runForAbility(event.damager, AbilityRunner { player: Player ->
            if (player.isGliding) event.setDamage(event.damage * 2)
        })
    }

    override fun getKey(): Key {
        return Key.key("origins:aerial_combatant")
    }

    override val description: MutableList<LineComponent> = makeLineFor(
            "You deal substantially more damage while in Elytra flight.",
            LineComponent.LineType.DESCRIPTION
        )

    override val title: MutableList<LineComponent> = makeLineFor("Aerial Combatant", LineComponent.LineType.TITLE)

}
