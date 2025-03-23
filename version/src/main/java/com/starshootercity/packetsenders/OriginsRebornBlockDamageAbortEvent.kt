package com.starshootercity.packetsenders

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockEvent
import org.bukkit.inventory.ItemStack

class OriginsRebornBlockDamageAbortEvent(
    private val player: Player,
    block: Block,
    private val itemInHand: ItemStack
) : BlockEvent(block) {

    fun getPlayer(): Player = player

    fun getItemInHand(): ItemStack = itemInHand

    override fun getHandlers(): HandlerList = handlers

    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlers
    }
}
