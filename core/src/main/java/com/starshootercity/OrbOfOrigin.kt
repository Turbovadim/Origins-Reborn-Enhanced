package com.starshootercity

import com.starshootercity.AddonLoader.getOrigins
import com.starshootercity.AddonLoader.getTextFor
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.events.PlayerSwapOriginEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

class OrbOfOrigin : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.currentItem == null || event.currentItem!!.itemMeta == null) return
        val meta = event.currentItem!!.itemMeta
        if (meta.persistentDataContainer
                .has<Byte?, Boolean?>(orbKey, OriginSwapper.BooleanPDT.BOOLEAN) && !meta.persistentDataContainer
                .has<Byte?, Boolean?>(
                    updatedKey, OriginSwapper.BooleanPDT.BOOLEAN
                )
        ) {
            event.currentItem!!.setItemMeta(orb.itemMeta)
        }
    }

    @EventHandler  // itemStack is null on some versions
    fun onPrepareItemCraft(event: PrepareItemCraftEvent) {
        val recipe = event.recipe
        if (recipe != null) {
            if (recipe.result.type == Material.CONDUIT) {
                for (itemStack in event.inventory.matrix) {
                    if (itemStack.itemMeta != null) {
                        if (itemStack.itemMeta.persistentDataContainer
                                .has<Byte?, Boolean?>(orbKey, OriginSwapper.BooleanPDT.BOOLEAN)
                        ) {
                            event.inventory.result = null
                        }
                    }
                }
            }
        }
    }

    init {
        Bukkit.removeRecipe(orbKey)
        val config = instance.config
        if (config.getBoolean("orb-of-origin.enable-recipe")) {
            val shapedRecipe = ShapedRecipe(orbKey, orb).apply {
                shape("012", "345", "678")
            }

            val recipeData: List<String> = config.getList("orb-of-origin.recipe")
                ?.flatMap { row ->
                    (row as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                } ?: emptyList()

            recipeData.forEachIndexed { index, materialName ->
                val material = Material.matchMaterial(materialName) ?: Material.AIR
                if (material != Material.AIR) {
                    shapedRecipe.setIngredient(index.toString()[0], material)
                }
            }
            Bukkit.addRecipe(shapedRecipe)
        }
    }


    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // Если блок, по которому кликнули, существует и интерактивный – выходим.
        event.clickedBlock?.takeIf { it.type.isInteractable }?.let { return }

        if (!event.action.isRightClick) return

        val player = event.player
        val item = event.item ?: return
        val meta = item.itemMeta ?: return

        // Если предмет не является Орбом Происхождения – выходим.
        if (!meta.persistentDataContainer.has(orbKey, OriginSwapper.BooleanPDT.BOOLEAN)) return

        // Проверяем кулдаун
        OriginSwapper.orbCooldown[player]?.let { lastUse ->
            if (System.currentTimeMillis() - lastUse < 500) return
        }

        // Определяем, в какой руке находится орб (если в основной руке тоже орб, считаем, что он там)
        val hand = if (player.inventory.itemInMainHand.itemMeta
                ?.persistentDataContainer
                ?.has(orbKey, OriginSwapper.BooleanPDT.BOOLEAN) == true
        ) EquipmentSlot.HAND else EquipmentSlot.OFF_HAND

        when (hand) {
            EquipmentSlot.HAND -> player.swingMainHand()
            else -> player.swingOffHand()
        }

        // Если в настройках включено потребление орба, уменьшаем количество предметов
        if (instance.config.getBoolean("orb-of-origin.consume")) {
            item.amount--
            player.inventory.setItemInMainHand(item)
        }

        var opened = false
        for (layer in AddonLoader.layers) {
            OriginSwapper.setOrigin(
                player,
                null,
                PlayerSwapOriginEvent.SwapReason.ORB_OF_ORIGIN,
                false,
                layer!!
            )
            if (opened) continue

            // Если для данного слоя настроен рандомный выбор, открываем окно с рандомной ориентацией
            if (instance.config.getBoolean("orb-of-origin.random.$layer")) {
                OriginSwapper.selectRandomOrigin(player, PlayerSwapOriginEvent.SwapReason.ORB_OF_ORIGIN, layer)
                val origin = OriginSwapper.getOrigin(player, layer)
                val index = getOrigins(layer).indexOf(origin)
                OriginSwapper.openOriginSwapper(
                    player,
                    PlayerSwapOriginEvent.SwapReason.ORB_OF_ORIGIN,
                    index,
                    0,
                    false,
                    true,
                    layer
                )
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(instance) {
                    OriginSwapper.openOriginSwapper(
                        player,
                        PlayerSwapOriginEvent.SwapReason.ORB_OF_ORIGIN,
                        0,
                        0,
                        layer
                    )
                }
            }
            opened = true
        }
    }


    companion object {
        @JvmField
        var orbKey: NamespacedKey = NamespacedKey(instance, "orb-of-origin")
        var updatedKey: NamespacedKey = NamespacedKey(instance, "updated-orb")

        @JvmField
        val orb: ItemStack = object : ItemStack(Material.NAUTILUS_SHELL) {
            init {
                var meta = itemMeta
                meta.persistentDataContainer.set<Byte?, Boolean?>(orbKey, OriginSwapper.BooleanPDT.BOOLEAN, true)
                meta.persistentDataContainer
                    .set<Byte?, Boolean?>(updatedKey, OriginSwapper.BooleanPDT.BOOLEAN, true)
                meta = NMSInvoker.setCustomModelData(meta, 1)
                meta.displayName(
                    Component.text(getTextFor("item.origins.orb_of_origin", "Orb of Origin"))
                        .color(NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false)
                )
                setItemMeta(meta)
            }
        }
    }
}
