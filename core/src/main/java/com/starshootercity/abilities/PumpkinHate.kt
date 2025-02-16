package com.starshootercity.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.abilities.Ability.AbilityRunner
import com.starshootercity.packetsenders.NMSInvoker
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class PumpkinHate : VisibleAbility, Listener {
    // Используем Set для быстрой проверки наличия игрока
    private val ignoringPlayers: MutableMap<Player?, MutableSet<Player?>> = HashMap<Player?, MutableSet<Player?>>()

    private var tickCounter = 0


    @EventHandler
    fun onServerTickEnd(event: ServerTickEndEvent?) {
        tickCounter++
        if (tickCounter < 10) {
            return
        }
        tickCounter = 0
        // Получаем список всех онлайн-игроков один раз
        val onlinePlayers: MutableList<Player> = ArrayList<Player>(Bukkit.getOnlinePlayers())

        // Для каждого игрока с данной способностью ("pumpkin hater")
        for (pumpkinHater in onlinePlayers) {
            runForAbility(pumpkinHater, AbilityRunner { hater: Player ->
                // Обрабатываем всех остальных игроков
                for (pumpkinWearer in onlinePlayers) {
                    if (pumpkinWearer == hater) continue

                    val helmet = pumpkinWearer.inventory.helmet
                    if (helmet != null && helmet.type == Material.CARVED_PUMPKIN) {
                        // Инициализируем набор, если его ещё нет
                        val ignoredSet = ignoringPlayers.computeIfAbsent(hater) { key: Player? -> HashSet<Player?>() }
                        // Добавляем, если ещё не добавлен
                        ignoredSet.add(pumpkinWearer)

                        val data: Byte = getData(pumpkinWearer)
                        nmsInvoker.sendEntityData(hater, pumpkinWearer, data)

                        hater.hidePlayer(origins, pumpkinWearer)
                        hater.sendEquipmentChange(pumpkinWearer, EquipmentSlot.HEAD, AIR_ITEMSTACK)
                    } else {
                        // Если игрок не носит тыкву – удаляем из набора игнорируемых и обновляем видимость
                        val ignoredSet = ignoringPlayers.computeIfAbsent(hater) { key: Player? -> HashSet<Player?>() }
                        ignoredSet.remove(pumpkinWearer)
                        hater.showPlayer(origins, pumpkinWearer)
                        AbilityRegister.updateEntity(hater, pumpkinWearer)
                    }
                }
            })
        }
    }

    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        runForAbility(event.getPlayer(), AbilityRunner { player: Player? ->
            if (event.item.type == Material.PUMPKIN_PIE) {
                event.isCancelled = true
                // Обновляем количество предметов (при достижении 0 сервер сам удалит предмет)
                event.item.amount = event.item.amount - 1

                player!!.addPotionEffect(PotionEffect(PotionEffectType.HUNGER, 300, 2, false, true))
                player.addPotionEffect(
                    PotionEffect(
                        NMSInvoker.getNauseaEffect(), 300, 1, false, true
                    )
                )
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
         * Метод возвращает битовую маску состояний игрока.
         */
        private fun getData(pumpkinWearer: Player): Byte {
            var data = 0x20
            if (pumpkinWearer.fireTicks > 0) {
                data = data or 0x01
            }
            if (pumpkinWearer.isSneaking) {
                data = data or 0x02
            }
            if (pumpkinWearer.isSprinting) {
                data = data or 0x08
            }
            if (pumpkinWearer.isSwimming) {
                data = data or 0x10
            }
            if (pumpkinWearer.isGlowing) {
                data = data or 0x40
            }
            if (pumpkinWearer.isGliding) {
                data = data or 0x80
            }
            return data.toByte()
        }
    }
}
