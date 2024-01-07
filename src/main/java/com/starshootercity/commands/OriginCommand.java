package com.starshootercity.commands;

import com.starshootercity.*;
import com.starshootercity.events.PlayerSwapOriginEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OriginCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) return false;
        switch (args[0].toLowerCase()) {
            case "swap" -> {
                if (sender instanceof Player player) {
                    if (OriginsReborn.getInstance().getConfig().getBoolean("swap-command.enabled")) {
                        OriginSwapper.openOriginSwapper(player, PlayerSwapOriginEvent.SwapReason.COMMAND, 0, 0, false);
                    } else {
                        sender.sendMessage(Component.text("This command has been disabled in the configuration").color(NamedTextColor.RED));
                    }
                } else {
                    sender.sendMessage(Component.text("This command can only be run by a player").color(NamedTextColor.RED));
                }
                return true;
            }
            case "reload" -> {
                if (sender instanceof Player player) {
                    if (!player.hasPermission("originsreborn.admin")) {
                        sender.sendMessage(Component.text("You don't have permission to do this!").color(NamedTextColor.RED));
                        return true;
                    }
                }
                OriginLoader.loadOrigins();
                return true;
            }
            case "set" -> {
                if (sender instanceof Player player) {
                    if (!player.hasPermission("originsreborn.admin")) {
                        sender.sendMessage(Component.text("You don't have permission to do this!").color(NamedTextColor.RED));
                        return true;
                    }
                }
                if (args.length < 3) return false;
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) return false;
                Origin origin = OriginLoader.originNameMap.get(args[2]);
                if (origin == null) return false;
                OriginSwapper.setOrigin(player, origin, PlayerSwapOriginEvent.SwapReason.COMMAND, false);
                return true;
            }
            case "orb" -> {
                if (sender instanceof Player player) {
                    if (player.hasPermission("originsreborn.admin")) {
                        player.getInventory().addItem(OrbOfOrigin.orb);
                    } else {
                        sender.sendMessage(Component.text("You don't have permission to do this!").color(NamedTextColor.RED));
                    }
                } else {
                    sender.sendMessage(Component.text("This command can only be run by a player").color(NamedTextColor.RED));
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        List<String> result = new ArrayList<>();
        List<String> data = switch (args.length) {
            case 1 -> {
                List<String> r = new ArrayList<>() {{
                    add("swap");
                }};
                if (sender instanceof Player player) {
                    if (!player.hasPermission("originsreborn.admin")) yield r;
                }
                r.add("reload");
                r.add("set");
                r.add("orb");
                yield r;
            }
            case 2 -> {
                if (args[0].equals("set")) {
                    yield new ArrayList<>() {{
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            add(player.getName());
                        }
                    }};
                } else yield new ArrayList<>();
            }
            case 3 -> {
                if (args[0].equals("set")) {
                    yield new ArrayList<>() {{
                        for (Origin origin : OriginLoader.origins) {
                            add(origin.getName().toLowerCase());
                        }
                    }};
                } else yield new ArrayList<>();
            }
            default -> new ArrayList<>();
        };
        StringUtil.copyPartialMatches(args[args.length - 1], data, result);
        return result;
    }
}
