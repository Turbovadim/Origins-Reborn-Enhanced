package ru.turbovadim.abilities

import ru.turbovadim.OriginsRebornEnhanced.Companion.NMSInvoker
import ru.turbovadim.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

class DamageFromPotions : Ability, Listener {
    override fun getKey(): Key {
        return Key.key("origins:damage_from_potions")
    }

    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        if (event.item.type != Material.POTION) return
        runForAbility(event.player, AbilityRunner { player ->
            NMSInvoker.dealFreezeDamage(player, 2)
        })
    }

}
