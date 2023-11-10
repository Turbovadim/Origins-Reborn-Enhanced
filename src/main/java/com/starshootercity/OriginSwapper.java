package com.starshootercity;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.origins.Shulk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OriginSwapper implements Listener, CommandExecutor {
    Map<String, Team> originTeams = new HashMap<>();
    public OriginSwapper() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (String origin : origins.keySet()) {
            Team oldTeam = scoreboard.getTeam(origin);
            if (oldTeam != null) oldTeam.unregister();
            Team team = scoreboard.registerNewTeam(origin);
            team.prefix(Component.text("[")
                            .color(NamedTextColor.DARK_GRAY)
                    .append(Component.text("%s".formatted(origin))
                            .color(NamedTextColor.GRAY))
                    .append(Component.text("] ")
                            .color(NamedTextColor.DARK_GRAY)));
            originTeams.put(origin, team);
        }
    }
    Map<String, Material> origins = new HashMap<>() {{
        put("Enderian", Material.ENDER_PEARL);
        put("Merling", Material.COD);
        put("Phantom", Material.PHANTOM_MEMBRANE);
        put("Elytrian", Material.ELYTRA);
        put("Blazeborn", Material.BLAZE_POWDER);
        put("Avian", Material.FEATHER);
        put("Arachnid", Material.COBWEB);
        put("Shulk", Material.SHULKER_SHELL);
        put("Feline", Material.ORANGE_WOOL);
    }};

    Map<String, List<Component>> information = new HashMap<>() {{
        put("Enderian", new ArrayList<>() {{
            add(Component.text("+ Can teleport without ender pearls")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ 2 extra hearts")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ No damage from teleportation")
                    .color(NamedTextColor.GREEN));
            add(Component.text("- Take damage when in contact with water")
                    .color(NamedTextColor.RED));
        }});
        put("Merling", new ArrayList<>() {{
            add(Component.text("+ Can breathe underwater")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ Infinite Dolphin's Grace")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ Infinite Conduit Power")
                    .color(NamedTextColor.GREEN));
            add(Component.text("- Limited breath out of water")
                    .color(NamedTextColor.RED));
        }});
        put("Phantom", new ArrayList<>() {{
            add(Component.text("+ Can walk through up to 3 solid blocks other than bedrock and obsidian by crouching next to them and clicking")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ Invisible while sneaking")
                    .color(NamedTextColor.GREEN));
            add(Component.text("- Lose extra hunger while sneaking")
                    .color(NamedTextColor.RED));
            add(Component.text("- Burn in daylight when not sneaking")
                    .color(NamedTextColor.RED));
            add(Component.text("- Only 7 hearts")
                    .color(NamedTextColor.RED));
        }});
        put("Elytrian", new ArrayList<>() {{
            add(Component.text("+ Permanent elytra")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ Double damage while gliding")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ Can be launched upwards by pressing shift while gliding (30 second cooldown)")
                    .color(NamedTextColor.GREEN));
            add(Component.text("- Cannot equip a chestplate")
                    .color(NamedTextColor.RED));
            add(Component.text("- Cannot wear armour stronger than chainmail")
                    .color(NamedTextColor.RED));
            add(Component.text("- Take double kinetic damage")
                    .color(NamedTextColor.RED));
            add(Component.text("- Slowness and weakness when there is not air above you")
                    .color(NamedTextColor.RED));
        }});
        put("Blazeborn", new ArrayList<>() {{
            add(Component.text("+ Immunity to fire and lava")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ Deal double damage whilst on fire")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ Immunity to poison and hunger")
                    .color(NamedTextColor.GREEN));
            add(Component.text("- Start in the nether")
                    .color(NamedTextColor.RED));
            add(Component.text("- Take damage when in contact with water")
                    .color(NamedTextColor.RED));
        }});
        put("Avian", new ArrayList<>() {{
            add(Component.text("+ Permanent slow falling")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ 25% speed boost")
                    .color(NamedTextColor.GREEN));
            add(Component.text("- Cannot sleep below 128 blocks")
                    .color(NamedTextColor.RED));
            add(Component.text("- Eating meat gives food poisoning")
                    .color(NamedTextColor.RED));
        }});
        put("Arachnid", new ArrayList<>() {{
            add(Component.text("+ Can climb when next to walls")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ Spawn a cobweb when attacking")
                    .color(NamedTextColor.GREEN));
            add(Component.text("- Only 3 hearts of health")
                    .color(NamedTextColor.RED));
            add(Component.text("- Food other than meat does not restore hunger or saturation")
                    .color(NamedTextColor.RED));
        }});
        put("Shulk", new ArrayList<>() {{
            add(Component.text("+ 9 extra inventory slots by right clicking on a chestplate slot,")
                    .color(NamedTextColor.GREEN));
            add(Component.text("not dropped on death")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ -25% all damage taken")
                    .color(NamedTextColor.GREEN));
            add(Component.text("- Cannot use shields")
                    .color(NamedTextColor.RED));
            add(Component.text("- Saturation and hunger decrease three times as fast")
                    .color(NamedTextColor.RED));
        }});
        put("Feline", new ArrayList<>() {{
            add(Component.text("+ Immune to fall damage")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ Jump boost when sprinting")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ Infinite night vision")
                    .color(NamedTextColor.GREEN));
            add(Component.text("+ Creepers only ignite if attacked")
                    .color(NamedTextColor.GREEN));
            add(Component.text("- Slower mining speed")
                    .color(NamedTextColor.RED));
            add(Component.text("- Only 9 hearts of health")
                    .color(NamedTextColor.RED));
        }});
    }};


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            openOriginSwapper(player, false);
        } else sender.sendMessage(Component.text("This command can only be run by a player")
                .color(NamedTextColor.RED));
        return true;
    }

    public static NamespacedKey key = new NamespacedKey(OrigamiOrigins.getInstance(), "Origin");

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (getOrigin(event.getPlayer()) == null) {
            openOriginSwapper(event.getPlayer(), true);
        } else {
            originTeams.get(getOrigin(event.getPlayer())).addPlayer(event.getPlayer());
        }
    }

    public static String getOrigin(Player player) {
        String origin = player.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (origin == null) {
            origin = OrigamiOrigins.getInstance().getConfig().getString("origins.%s".formatted(player.getUniqueId().toString()));
            if (origin == null) return null;
            player.getPersistentDataContainer().set(key, PersistentDataType.STRING, origin);
        }
        return origin;
    }

    public static void runForOrigin(Player player, String origin, Runnable task) {
        runForOrigin(player, origin, task, () -> {});
    }

    public static void runForOrigin(Player player, String origin, Runnable task, Runnable other) {
        String actualOrigin = getOrigin(player);
        if (actualOrigin == null || !actualOrigin.equals(origin)) {
            other.run();
        } else task.run();
    }

    public void setOrigin(Player player, String origin) {
        OrigamiOrigins.getInstance().getConfig().set("origins.%s".formatted(player.getUniqueId().toString()), origin);
        player.getPersistentDataContainer().set(key, PersistentDataType.STRING, origin);
        for (Team team : originTeams.values()) {
            team.removePlayer(player);
        }
        originTeams.get(origin).addPlayer(player);
        OrigamiOrigins.getInstance().saveConfig();
    }

    public void openOriginSwapper(Player player, boolean firstTime) {
        Inventory originSwapper = Bukkit.createInventory(null, InventoryType.DISPENSER, Component.text("%s Origin".formatted(firstTime ? "Choose" : "Swap"))
                .color(NamedTextColor.BLACK));
        for (String origin : origins.keySet()) {
            originSwapper.addItem(makeOriginItem(origins.get(origin), origin, firstTime));
        }
        player.openInventory(originSwapper);
    }


    public ItemStack makeOriginItem(Material type, String origin, boolean firstTime) {
        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, origin);
        meta.displayName(Component.text(origin)
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GRAY));
        meta.getPersistentDataContainer().set(new NamespacedKey(OrigamiOrigins.getInstance(), "firsttime"), PersistentDataType.BOOLEAN, firstTime);
        int price = OrigamiOrigins.getInstance().getConfig().getInt("change-cost");
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.addAll(information.get(origin));
        lore.add(Component.text(""));
        lore.add(Component.text("Warning:")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Switching origins will wipe your inventory and cost $%s".formatted(price))
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null) return;
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta.getPersistentDataContainer().has(key)) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                Integer cost = meta.getPersistentDataContainer().get(new NamespacedKey(OrigamiOrigins.getInstance(), "cost"), PersistentDataType.INTEGER);
                if (cost != null) {
                    if (OrigamiOrigins.economy != null && !OrigamiOrigins.economy.has(player, cost)) return;
                    if (OrigamiOrigins.economy != null) OrigamiOrigins.economy.withdrawPlayer(player, cost);
                }
                player.getInventory().clear();
                player.getEnderChest().clear();
                player.setSaturation(5);
                AttributeInstance instance = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                double maxHealth;
                String origin = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                setOrigin(player, origin);
                if (origin == null) return;
                if (instance == null) maxHealth = 1;
                else {
                    instance.setBaseValue(getMaxHealth(origin));
                    maxHealth = instance.getBaseValue();
                }
                player.setHealth(maxHealth);
                Shulk.getInventoriesConfig().set(player.getUniqueId().toString(), null);
                player.setFallDistance(0);
                player.setCooldown(Material.SHIELD, 0);
                player.setAllowFlight(false);
                player.setFlying(false);
                player.setRemainingAir(player.getMaximumAir());
                player.setFoodLevel(20);
                player.setFireTicks(0);
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                player.setBedSpawnLocation(null);
                player.closeInventory();
                String nether = OrigamiOrigins.getInstance().getConfig().getString("worlds.world_nether");
                String overworld = OrigamiOrigins.getInstance().getConfig().getString("worlds.world");
                if (nether == null) {
                    nether = "world_nether";
                    OrigamiOrigins.getInstance().getConfig().set("worlds.world_nether", "world_nether");
                    OrigamiOrigins.getInstance().saveConfig();
                }
                if (overworld == null) {
                    overworld = "world";
                    OrigamiOrigins.getInstance().getConfig().set("worlds.world", "world");
                    OrigamiOrigins.getInstance().saveConfig();
                }
                String worldToUse = origin.equals("Blazeborn") ? nether : overworld;
                World world = Bukkit.getWorld(worldToUse);
                if (world == null) {
                    OrigamiOrigins.getInstance().getLogger().warning("World '%s' could not be found".formatted(worldToUse));
                    return;
                }
                player.teleport(world.getSpawnLocation());
                player.setBedSpawnLocation(world.getSpawnLocation(), true);
            }
        }
    }

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String origin = getOrigin(player);
            if (origin == null) continue;
            AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth == null) continue;
            maxHealth.setBaseValue(getMaxHealth(origin));
            AttributeInstance speed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (speed == null) continue;
            speed.setBaseValue(getSpeed(origin));
        }
    }
    public double getMaxHealth(String origin) {
        return switch (origin) {
            case "Enderian" -> 24;
            case "Phantom" -> 14;
            case "Arachnid" -> 6;
            case "Feline" -> 18;
            default -> 20;
        };
    }
    public double getSpeed(String origin) {
        return switch (origin) {
            case "Avian" -> 0.125;
            default -> 0.1;
        };
    }
}
