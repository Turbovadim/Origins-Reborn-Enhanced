package com.starshootercity.commands;

import com.starshootercity.OrbOfOrigin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
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
                return true;
            }
            case "set" -> {
                return true;
            }
            case "orb" -> {
                if (sender instanceof Player player) {
                    ItemStack orb = new ItemStack(Material.NAUTILUS_SHELL);
                    ItemMeta meta = orb.getItemMeta();
                    meta.getPersistentDataContainer().set(OrbOfOrigin.orbKey, PersistentDataType.BOOLEAN, true);
                    meta.displayName(Component.text("Orb of Origin").color(NamedTextColor.BLUE));
                    orb.setItemMeta(meta);
                    player.getInventory().addItem(orb);
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
            case 1 -> new ArrayList<>() {{
                add("swap");
                add("set");
                add("orb");
            }};
            default -> new ArrayList<>();
        };
        StringUtil.copyPartialMatches(args[args.length - 1], data, result);
        return result;
    }
}
