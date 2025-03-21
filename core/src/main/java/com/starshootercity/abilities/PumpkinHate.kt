package com.starshootercity.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.abilities.Ability.AbilityRunner
import com.starshootercity.packetsenders.NMSInvoker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
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
                    withContext(OriginsReborn.bukkitDispatcher) {
                        pumpkinWearers.filter { it != hater }.forEach { pumpkinWearer ->
                            hater.hidePlayer(origins, pumpkinWearer)
                        }
                        nonPumpkinWearers.filter { it != hater }.forEach { other ->
                            hater.showPlayer(origins, other)
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
                player.addPotionEffect(PotionEffect(NMSInvoker.getNauseaEffect(), 300, 1, false, true))
                player.addPotionEffect(PotionEffect(PotionEffectType.POISON, 1200, 1, false, true))
            }
        })
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "You are afraid of pumpkins. For a good reason.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor(
            "Scared of Gourds",
            LineComponent.LineType.TITLE
        )
    }

    override fun getKey(): Key {
        return Key.key("origins:pumpkin_hate")
    }

    companion object {
        var origins: OriginsReborn = instance
        val nmsInvoker: NMSInvoker = NMSInvoker
        private val AIR_ITEMSTACK = ItemStack(Material.AIR)

        /**
         * Returns a bit mask representing the player’s current state.
         */
        private fun getData(pumpkinWearer: Player): Byte {
            var data = 0x20
            if (pumpkinWearer.fireTicks > 0) data = data or 0x01
            if (pumpkinWearer.isSneaking) data = data or 0x02
            if (pumpkinWearer.isSprinting) data = data or 0x08
            if (pumpkinWearer.isSwimming) data = data or 0x10
            if (pumpkinWearer.isGlowing) data = data or 0x40
            if (pumpkinWearer.isGliding) data = data or 0x80
            return data.toByte()
        }
    }
}
