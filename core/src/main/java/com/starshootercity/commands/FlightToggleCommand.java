package com.starshootercity.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FlightToggleCommand implements CommandExecutor {
    public static List<Player> canFly = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (canFly.contains(player)) {
                canFly.remove(player);
                player.sendMessage(Component.text("Disabled flight").color(NamedTextColor.AQUA));
            } else {
                canFly.add(player);
                player.sendMessage(Component.text("Enabled flight").color(NamedTextColor.AQUA));
            }
        } else sender.sendMessage(Component.text("This command can only be used by players!").color(NamedTextColor.RED));
        return true;
    }
}
