package ru.turbovadim.packetsenders

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockEvent
import org.bukkit.inventory.ItemStack

class OriginsRebornBlockDamageAbortEvent(
    val player: Player,
    block: Block,
    val itemInHand: ItemStack
) : BlockEvent(block) {

    override fun getHandlers(): HandlerList = handlers

    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlers
    }
}
