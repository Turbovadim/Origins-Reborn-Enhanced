package com.starshootercity.commands;

import com.starshootercity.*;
import com.starshootercity.cooldowns.Cooldowns;
import com.starshootercity.events.PlayerSwapOriginEvent;
import com.starshootercity.util.CompressionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OriginCommand implements CommandExecutor, TabCompleter {
    public static NamespacedKey key = OriginsReborn.getCooldowns().registerCooldown(new NamespacedKey(OriginsReborn.getInstance(), "swap-command-cooldown"), new Cooldowns.CooldownInfo(0));

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) return false;
        switch (args[0].toLowerCase()) {
            case "swap" -> {
                if (sender instanceof Player player) {
                    if (OriginsReborn.getCooldowns().hasCooldown(player, key)) {
                        long cooldown = OriginsReborn.getCooldowns().getCooldown(player, key);
                        //player.sendMessage(Component.text("You are on cooldown for %s".formatted(ShortcutUtils.getFormattedTime(cooldown))).color(NamedTextColor.RED));
                        player.sendMessage(Component.text("You are on cooldown.").color(NamedTextColor.RED));
                        return true;
                    }
                    if (OriginsReborn.getInstance().getConfig().getBoolean("swap-command.enabled")) {
                        if (AddonLoader.allowOriginSwapCommand(player)) {
                            OriginSwapper.openOriginSwapper(player, PlayerSwapOriginEvent.SwapReason.COMMAND, 0, 0, OriginsReborn.getInstance().isVaultEnabled());
                        } else {
                            sender.sendMessage(Component.text(OriginsReborn.getInstance().getConfig().getString("messages.no-swap-command-permissions", "§cYou don't have permission to do this!")));
                        }
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
                AddonLoader.reloadAddons();
                OriginsReborn.getInstance().reloadConfig();
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
                Origin origin = AddonLoader.originNameMap.get(args[2].replace("_", " "));
                if (origin == null) return false;
                OriginSwapper.setOrigin(player, origin, PlayerSwapOriginEvent.SwapReason.COMMAND, false);
                return true;
            }
            case "orb" -> {
                Player player;
                if (sender instanceof Player p) {
                    player = p;
                    if (!player.hasPermission("originsreborn.admin")) {
                        sender.sendMessage(Component.text("You don't have permission to do this!").color(NamedTextColor.RED));
                        return true;
                    }
                } else if (!(args.length == 2 && (player = Bukkit.getPlayer(args[1])) != null)) {
                    sender.sendMessage(Component.text("This command can only be run by a player").color(NamedTextColor.RED));
                    return true;
                }
                player.getInventory().addItem(OrbOfOrigin.orb);
                return true;
            }
            case "check" -> {
                if (sender instanceof Player player) {
                    OriginSwapper.openOriginSwapper(player, PlayerSwapOriginEvent.SwapReason.COMMAND, AddonLoader.origins.indexOf(OriginSwapper.getOrigin(player)), 0, false, true);
                } else {
                    sender.sendMessage(Component.text("This command can only be run by a player").color(NamedTextColor.RED));
                }
                return true;
            }
            case "export" -> {
                if (args.length != 3) return false;
                File output = new File(OriginsReborn.getInstance().getDataFolder(), "export/" + args[2] + ".orbarch");
                List<File> files = AddonLoader.originFiles.get(args[1]);
                if (files == null) return false;
                try {
                    CompressionUtils.compressFiles(files, output);
                    sender.sendMessage(Component.text("Exported origins to '~/plugins/Origins-Reborn/export/%s.orbarch'".formatted(args[2])).color(NamedTextColor.AQUA));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
            case "import" -> {
                if (args.length != 2) return false;
                File input = new File(OriginsReborn.getInstance().getDataFolder(), "import/" + args[1]);
                File output = new File(OriginsReborn.getInstance().getDataFolder(), "origins");
                if (!input.exists() || !output.exists()) return false;
                try {
                    CompressionUtils.decompressFiles(input, output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
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
                List<String> r = new ArrayList<>();
                r.add("check");
                if (sender instanceof Player player && AddonLoader.allowOriginSwapCommand(player)) {
                    r.add("swap");
                }
                if (!sender.hasPermission("originsreborn.admin")) yield r;
                r.add("reload");
                r.add("set");
                r.add("orb");
                r.add("export");
                r.add("import");
                yield r;
            }
            case 2 -> {
                switch (args[0]) {
                    case "set", "orb" -> {
                        yield new ArrayList<>() {{
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                add(player.getName());
                            }
                        }};
                    }
                    case "export" -> {
                        yield new ArrayList<>(AddonLoader.originFiles.keySet());
                    }
                    case "import" -> {
                        File input = new File(OriginsReborn.getInstance().getDataFolder(), "import");
                        File[] files = input.listFiles();

                        if (files == null) yield List.of();
                        List<String> fileNames = new ArrayList<>();
                        for (File file : files) {
                            fileNames.add(file.getName());
                        }
                        yield fileNames;
                    }
                    default -> {
                        yield new ArrayList<>();
                    }
                }
            }
            case 3 -> {
                if (args[0].equals("set")) {
                    yield new ArrayList<>() {{
                        for (Origin origin : AddonLoader.origins) {
                            add(origin.getName().toLowerCase().replace(" ", "_"));
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