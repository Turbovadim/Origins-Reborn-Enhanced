package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.potion.PotionEffectType

class Hotblooded : VisibleAbility, Listener {

    @EventHandler
    fun onEntityPotionEffect(event: EntityPotionEffectEvent) {
        runForAbility(event.entity, AbilityRunner { player ->
            event.newEffect?.let { effect ->
                if (effect.type == PotionEffectType.POISON || effect.type == PotionEffectType.HUNGER) {
                    event.isCancelled = true
                }
            }
        })
    }

    override fun getKey(): Key {
        return Key.key("origins:hotblooded")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "Due to your hot body, venoms burn up, making you immune to poison and hunger status effects.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Hotblooded", LineComponent.LineType.TITLE)
    }
}
