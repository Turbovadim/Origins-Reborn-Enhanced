package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.ShortcutUtils.infiniteDuration
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SlowFalling : VisibleAbility, Listener {

    val potionEffect = PotionEffect(
        PotionEffectType.SLOW_FALLING,
        infiniteDuration(),
        0,
        false,
        false
    )

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        runForAbility(event.player, AbilityRunner { player ->
            if (player.isSneaking) {
                player.removePotionEffect(PotionEffectType.SLOW_FALLING)
            } else {
                player.addPotionEffect(potionEffect)
            }
        })
    }

    override fun getKey(): Key {
        return Key.key("origins:slow_falling")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "You fall as gently to the ground as a feather would, unless you sneak.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Featherweight", LineComponent.LineType.TITLE)
    }
}
