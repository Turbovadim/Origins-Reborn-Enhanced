package com.starshootercity.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import com.starshootercity.OriginSwapper
import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityAirChangeEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.math.max
import kotlin.math.min

class WaterBreathing : Listener, VisibleAbility {

    @EventHandler
    fun onEntityAirChange(event: EntityAirChangeEvent) {
        runForAbility(event.entity, AbilityRunner { player ->
            val pdc = player.persistentDataContainer
            if (pdc.get(airKey, OriginSwapper.BooleanPDT.BOOLEAN) == true ||
                pdc.get(dehydrationKey, OriginSwapper.BooleanPDT.BOOLEAN) == true) {
                return@AbilityRunner
            }

            val underwater = NMSInvoker.isUnderWater(player)
            val waterBreathing = hasWaterBreathing(player)

            if (player.remainingAir - event.amount > 0) {
                if (!underwater && !waterBreathing) return@AbilityRunner
            } else if (underwater || waterBreathing) {
                return@AbilityRunner
            }

            event.isCancelled = true
        })
    }


    @EventHandler
    fun onEntityPotionEffect(event: EntityPotionEffectEvent) {
        if (event.cause != EntityPotionEffectEvent.Cause.TURTLE_HELMET) return
        runForAbility(event.getEntity(), AbilityRunner { player: Player? -> event.isCancelled = true })
    }

    fun hasWaterBreathing(player: Player): Boolean {
        return player.hasPotionEffect(PotionEffectType.CONDUIT_POWER) || player.hasPotionEffect(PotionEffectType.WATER_BREATHING)
    }

    var airKey: NamespacedKey = NamespacedKey(instance, "fullair")
    var dehydrationKey: NamespacedKey = NamespacedKey(instance, "dehydrating")
    var damageKey: NamespacedKey = NamespacedKey(instance, "ignore-item-damage")

    @EventHandler
    fun onServerTickEnd(event: ServerTickEndEvent?) {
        Bukkit.getOnlinePlayers().forEach { player ->
            runForAbility(
                player,
                AbilityRunner { player ->
                    val underwater = NMSInvoker.isUnderWater(player)
                    val waterBreathing = hasWaterBreathing(player)
                    val inRain = player.isInRain

                    if (underwater || waterBreathing || inRain) {
                        player.inventory.helmet
                            ?.takeIf { underwater && it.type == Material.TURTLE_HELMET }
                            ?.let {
                                player.addPotionEffect(
                                    PotionEffect(
                                        PotionEffectType.WATER_BREATHING,
                                        200,
                                        0,
                                        false,
                                        false,
                                        true
                                    )
                                )
                            }

                        if (player.persistentDataContainer.get(airKey, OriginSwapper.BooleanPDT.BOOLEAN) == true) {
                            player.remainingAir = -50
                            return@AbilityRunner
                        }

                        player.remainingAir = min(
                            max((player.remainingAir + 4).toDouble(), 4.0),
                            player.maximumAir.toDouble()
                        ).toInt()

                        if (player.remainingAir == player.maximumAir) {
                            player.remainingAir = -50
                            player.persistentDataContainer.set(airKey, OriginSwapper.BooleanPDT.BOOLEAN, true)
                        }
                    } else {
                        if (player.persistentDataContainer.get(airKey, OriginSwapper.BooleanPDT.BOOLEAN) == true) {
                            player.remainingAir = player.maximumAir
                            player.persistentDataContainer.set(airKey, OriginSwapper.BooleanPDT.BOOLEAN, false)
                        }
                        decreaseAir(player)
                        if (player.remainingAir < -25) {
                            player.persistentDataContainer.set(dehydrationKey, OriginSwapper.BooleanPDT.BOOLEAN, true)
                            player.remainingAir = -5
                            player.persistentDataContainer.set(dehydrationKey, OriginSwapper.BooleanPDT.BOOLEAN, false)
                            player.persistentDataContainer.set(damageKey, PersistentDataType.INTEGER, Bukkit.getCurrentTick())
                            NMSInvoker.dealDrowningDamage(player, 2)
                        }
                    }
                },
                AbilityRunner { player ->
                    if (player.persistentDataContainer.has(airKey, OriginSwapper.BooleanPDT.BOOLEAN)) {
                        player.remainingAir = player.maximumAir
                        player.persistentDataContainer.remove(airKey)
                    }
                }
            )
        }
    }


    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (event.player.persistentDataContainer.getOrDefault(damageKey, PersistentDataType.INTEGER, 0) >= Bukkit.getCurrentTick()) {
            event.deathMessage(event.player.displayName().append(Component.text(" didn't manage to keep wet")))
        }
    }

    private val random = Random()

    fun decreaseAir(player: Player) {
        val respirationLevel = player.inventory.helmet
            ?.itemMeta
            ?.getEnchantLevel(NMSInvoker.getRespirationEnchantment()) ?: 0
        if (respirationLevel > 0 && random.nextInt(respirationLevel + 1) > 0) return
        player.remainingAir--
    }


    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.getPlayer().persistentDataContainer.set<Int?, Int?>(damageKey, PersistentDataType.INTEGER, -1)
    }

    override fun getKey(): Key {
        return Key.key("origins:water_breathing")
    }

    override val description: MutableList<LineComponent?> = makeLineFor("You can breathe underwater, but not on land.", LineComponent.LineType.DESCRIPTION)

    override val title: MutableList<LineComponent?> = makeLineFor("Gills", LineComponent.LineType.TITLE)
}
