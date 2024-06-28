package com.starshootercity.packetsenders;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class OriginsRebornBlockDamageAbortEvent extends BlockEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemStack itemstack;

    public OriginsRebornBlockDamageAbortEvent(@NotNull Player player, @NotNull Block block, @NotNull ItemStack itemInHand) {
        super(block);
        this.player = player;
        this.itemstack = itemInHand;
    }

    public @NotNull Player getPlayer() {
        return this.player;
    }

    public @NotNull ItemStack getItemInHand() {
        return this.itemstack;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
