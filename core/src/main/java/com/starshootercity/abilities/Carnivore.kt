package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Carnivore : VisibleAbility, Listener {
    var meat: MutableList<Material?> = object : ArrayList<Material?>() {
        init {
            add(Material.PORKCHOP)
            add(Material.COOKED_PORKCHOP)
            add(Material.BEEF)
            add(Material.COOKED_BEEF)
            add(Material.CHICKEN)
            add(Material.COOKED_CHICKEN)
            add(Material.RABBIT)
            add(Material.COOKED_RABBIT)
            add(Material.MUTTON)
            add(Material.COOKED_MUTTON)
            add(Material.RABBIT_STEW)
            add(Material.COD)
            add(Material.COOKED_COD)
            add(Material.TROPICAL_FISH)
            add(Material.SALMON)
            add(Material.COOKED_SALMON)
            add(Material.PUFFERFISH)
            if (NMSInvoker.ominousBottle != null) add(NMSInvoker.ominousBottle)
        }
    }

    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        if (event.item.type == Material.POTION) return

        runForAbility(event.player, AbilityRunner { player ->
            if (event.item.type !in meat) {
                event.isCancelled = true
                event.item.amount--
                player.addPotionEffect(
                    PotionEffect(PotionEffectType.POISON, 300, 1, false, true)
                )
            }
        })
    }


    override val description: MutableList<LineComponent> = makeLineFor(
        "Your diet is restricted to meat, you can't eat vegetables.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent> = makeLineFor(
        "Carnivore",
        LineComponent.LineType.TITLE
    )

    override fun getKey(): Key {
        return Key.key("origins:carnivore")
    }
}
