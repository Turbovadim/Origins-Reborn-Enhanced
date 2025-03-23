package ru.turbovadim.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import ru.turbovadim.OriginSwapper.LineData.Companion.makeLineFor
import ru.turbovadim.OriginSwapper.LineData.LineComponent
import ru.turbovadim.OriginsRebornEnhanced
import ru.turbovadim.OriginsRebornEnhanced.Companion.NMSInvoker
import ru.turbovadim.OriginsRebornEnhanced.Companion.instance
import ru.turbovadim.abilities.Ability.AbilityRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.endera.enderalib.utils.async.ioDispatcher

class PumpkinHate : VisibleAbility, Listener {

    @EventHandler
    fun onServerTickEnd(event: ServerTickEndEvent) {
        if (event.tickNumber % 10 != 0) return

        CoroutineScope(ioDispatcher).launch {
            val onlinePlayers = Bukkit.getOnlinePlayers().toList()
            val pumpkinWearers = onlinePlayers.filter { it.inventory.helmet?.type == Material.CARVED_PUMPKIN }.toSet()
            val nonPumpkinWearers = onlinePlayers.filter { it !in pumpkinWearers }

            onlinePlayers.forEach { pumpkinHater ->
                runForAbilityAsync(pumpkinHater) { hater ->
                    withContext(OriginsRebornEnhanced.bukkitDispatcher) {
                        pumpkinWearers.filter { it != hater }.forEach { pumpkinWearer ->
                            hater.hidePlayer(instance, pumpkinWearer)
                        }
                        nonPumpkinWearers.filter { it != hater }.forEach { other ->
                            hater.showPlayer(instance, other)
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        runForAbility(event.player, AbilityRunner { player ->
            if (event.item.type == Material.PUMPKIN_PIE) {
                event.isCancelled = true
                event.item.amount -= 1

                player.addPotionEffect(PotionEffect(PotionEffectType.HUNGER, 300, 2, false, true))
                player.addPotionEffect(PotionEffect(NMSInvoker.nauseaEffect, 300, 1, false, true))
                player.addPotionEffect(PotionEffect(PotionEffectType.POISON, 1200, 1, false, true))
            }
        })
    }

    override val description: MutableList<LineComponent> = makeLineFor(
        "You are afraid of pumpkins. For a good reason.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent> = makeLineFor(
        "Scared of Gourds",
        LineComponent.LineType.TITLE
    )

    override fun getKey(): Key {
        return Key.key("origins:pumpkin_hate")
    }
}
