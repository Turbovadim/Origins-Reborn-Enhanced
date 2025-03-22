package com.starshootercity.abilities

import com.starshootercity.AddonLoader.getTextFor
import com.starshootercity.OriginSwapper
import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.ShortcutUtils.isBedrockPlayer
import com.starshootercity.abilities.Ability.AbilityRunner
import com.starshootercity.events.PlayerLeftClickEvent
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import java.io.File
import java.io.IOException

class ShulkerInventory : VisibleAbility, Listener {
    override fun getKey(): Key {
        return Key.key("origins:shulker_inventory")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "You have access to an additional 9 slots of inventory, which keep the items on death.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Hoarder", LineComponent.LineType.TITLE)
    }

    var openedBoxKey: NamespacedKey = NamespacedKey(instance, "openedbox")

    init {
        inventories = File(instance.dataFolder, "inventories.yml")
        if (!inventories.exists()) {
            inventories.getParentFile().mkdirs()
            instance.saveResource("inventories.yml", false)
        }

        inventoriesConfig = AutosavingYamlConfiguration()
        try {
            inventoriesConfig!!.load(inventories)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InvalidConfigurationException) {
            throw RuntimeException(e)
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        event.player.persistentDataContainer
            .set(openedBoxKey, OriginSwapper.BooleanPDT.BOOLEAN, false)
    }

    @EventHandler
    fun onPlayerLeftClick(event: PlayerLeftClickEvent) {
        val player = event.getPlayer()
        if (event.hasBlock() || event.hasItem()) return
        if (!isBedrockPlayer(player.uniqueId)) return

        runForAbility(player, AbilityRunner { p ->
            val inventory = Bukkit.createInventory(
                p,
                InventoryType.DISPENSER,
                Component.text(getTextFor("container.shulker_inventory_power", "Shulker Inventory"))
            )
            p.openInventory(inventory)
            for (i in 0..8) {
                inventoriesConfig?.getItemStack("${p.uniqueId}.$i")?.let { item ->
                    inventory.setItem(i, item)
                }
            }
            p.persistentDataContainer.set(openedBoxKey, OriginSwapper.BooleanPDT.BOOLEAN, true)
        })
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        runForAbility(player, AbilityRunner { p ->
            if (event.isRightClick && event.slotType == InventoryType.SlotType.ARMOR && event.slot == 38) {
                event.isCancelled = true
                val inventory = Bukkit.createInventory(
                    p,
                    InventoryType.DISPENSER,
                    Component.text(getTextFor("container.shulker_inventory_power", "Shulker Inventory"))
                )
                p.openInventory(inventory)
                for (i in 0..8) {
                    inventoriesConfig?.getItemStack("${p.uniqueId}.$i")?.let { item ->
                        inventory.setItem(i, item)
                    }
                }
                p.persistentDataContainer.set(openedBoxKey, OriginSwapper.BooleanPDT.BOOLEAN, true)
                return@AbilityRunner
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(instance, Runnable {
                if (p.persistentDataContainer.get<Byte, Boolean>(openedBoxKey, OriginSwapper.BooleanPDT.BOOLEAN) == true) {
                    for (i in 0..8) {
                        inventoriesConfig?.set("${p.uniqueId}.$i", p.openInventory.getItem(i))
                    }
                }
            })
        })
    }


    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, Runnable {
            if (player.persistentDataContainer.get(openedBoxKey, OriginSwapper.BooleanPDT.BOOLEAN) == true) {
                for (i in 0..8) {
                    inventoriesConfig?.set("${player.uniqueId}.$i", player.openInventory.getItem(i))
                }
            }
        })
    }

    private class AutosavingYamlConfiguration : YamlConfiguration() {
        override fun set(path: String, value: Any?) {
            super.set(path, value)
            try {
                save(inventories)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    companion object {
        private lateinit var inventories: File
        var inventoriesConfig: FileConfiguration? = null
    }
}