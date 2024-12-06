package com.starshootercity.packetsenders;

import io.netty.channel.*;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketListenerV1_17_1 {
    public void removePlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    public void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "PACKET READ: " + ChatColor.RED + packet.toString());
                System.out.println("PACKET READ: " + packet);
                if (packet instanceof ServerboundPlayerActionPacket p) {
                    if (p.getAction().equals(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK)) {
                        System.out.println("BLOCK DAMAGE ABORT EVENT DETECTED");
                        Block block = player.getTargetBlock(8);
                        if (block != null) new OriginsRebornBlockDamageAbortEvent(player, block, player.getInventory().getItemInMainHand()).callEvent();
                    }
                }
                super.channelRead(channelHandlerContext, packet);
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().connection.connection.channel.pipeline();
        pipeline.addBefore("origins_reborn_handler", player.getName(), channelDuplexHandler);
    }
}
