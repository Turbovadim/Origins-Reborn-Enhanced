package com.starshootercity

import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.geysermc.api.Geyser
import org.geysermc.floodgate.api.FloodgateApi
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*

@Suppress("unused")
object ShortcutUtils {
    @JvmStatic
    fun giveItemWithDrops(player: Player, vararg itemStacks: ItemStack?) {
        // Фильтруем null-значения, чтобы избежать исключений
        val validItems = itemStacks.filterNotNull().toTypedArray()
        // Добавляем предметы в инвентарь и получаем оставшиеся, если не поместились
        player.inventory.addItem(*validItems).values.forEach { leftover ->
            player.world.dropItemNaturally(player.location, leftover)
        }
    }


    fun getLivingDamageSource(event: EntityDamageByEntityEvent): LivingEntity? {
        val damageDealer = event.damager
        return when (damageDealer) {
            is Projectile -> damageDealer.shooter as? LivingEntity
            is LivingEntity -> damageDealer
            else -> null
        }
    }


    fun openJSONFile(file: File): JSONObject {
        try {
            Scanner(file).use { scanner ->
                val data = StringBuilder()
                while (scanner.hasNextLine()) {
                    data.append(scanner.nextLine())
                }
                return try {
                    JSONObject(data.toString())
                } catch (e: JSONException) {
                    JSONObject()
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun isBedrockPlayer(uuid: UUID): Boolean {
        return try {
            FloodgateApi.getInstance().isFloodgatePlayer(uuid)
        } catch (e: NoClassDefFoundError) {
            try {
                Geyser.api().isBedrockPlayer(uuid)
            } catch (ex: NoClassDefFoundError) {
                false
            }
        }
    }

    fun getColored(f: String): Component {
        var component: Component = Component.empty()
        val iterator = substringsBetween(f, "<", ">").iterator()
        for (s in f.split("<#\\w{6}>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (s.isEmpty()) continue
            component = component.append(
                if (iterator.hasNext()) Component.text(s).color(
                    TextColor.fromHexString(
                        iterator.next()!!
                    )
                ) else Component.text(s)
            )
        }
        return component
    }

    fun substringsBetween(s: String, start: String, end: String): MutableList<String?> {
        val starti = s.indexOf(start)
        if (starti == -1) return mutableListOf<String?>()
        val startPart = s.substring(starti)
        val endi = startPart.indexOf(end)
        if (endi == -1) return mutableListOf<String?>()
        val data: MutableList<String?> = ArrayList<String?>()
        data.add(startPart.substring(0, endi))
        data.addAll(substringsBetween(startPart.substring(endi), start, end))
        return data
    }

    @JvmStatic
    fun isInfinite(effect: PotionEffect): Boolean {
        return if (NMSInvoker.supportsInfiniteDuration()) {
            (effect.duration == -1)
        } else (effect.duration >= 20000)
    }

    @JvmStatic
    fun infiniteDuration(): Int {
        return if (NMSInvoker.supportsInfiniteDuration()) {
            -1
        } else 50000
    }
}
