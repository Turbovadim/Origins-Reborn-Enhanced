package com.starshootercity.commands;

import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlightToggleCommand implements CommandExecutor {
    public static boolean canFly(Player player) {
        return player.getPersistentDataContainer().has(key, OriginSwapper.BooleanPDT.BOOLEAN);
    }

    public static void setCanFly(Player player, boolean b) {
        if (b) player.getPersistentDataContainer().set(key, OriginSwapper.BooleanPDT.BOOLEAN, true);
        else player.getPersistentDataContainer().remove(key);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (canFly(player)) {
                setCanFly(player, false);
                player.sendMessage(Component.text("Disabled flight").color(NamedTextColor.AQUA));
            } else {
                setCanFly(player, true);
                player.sendMessage(Component.text("Enabled flight").color(NamedTextColor.AQUA));
            }
        } else sender.sendMessage(Component.text("This command can only be used by players!").color(NamedTextColor.RED));
        return true;
    }

    public FlightToggleCommand() {
         key = new NamespacedKey(OriginsReborn.getInstance(), "flight");
    }

    private static NamespacedKey key;
}
