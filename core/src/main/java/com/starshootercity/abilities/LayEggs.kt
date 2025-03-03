package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.TimeSkipEvent
import org.bukkit.inventory.ItemStack

class LayEggs : VisibleAbility, Listener {

    @EventHandler
    fun onTimeSkip(event: TimeSkipEvent) {
        if (event.skipReason != TimeSkipEvent.SkipReason.NIGHT_SKIP) return

        Bukkit.getOnlinePlayers()
            .filter { it.isDeeplySleeping }
            .forEach { player ->
                runForAbility(player, AbilityRunner {
                    player.world.dropItem(player.location, ItemStack(Material.EGG))
                    player.world.playSound(player.location, Sound.ENTITY_CHICKEN_EGG, SoundCategory.PLAYERS, 1f, 1f)
                })
            }
    }

    override fun getKey(): Key {
        return Key.key("origins:lay_eggs")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "Whenever you wake up in the morning, you will lay an egg.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Oviparous", LineComponent.LineType.TITLE)
    }
}
