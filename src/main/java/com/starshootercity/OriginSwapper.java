package com.starshootercity;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.abilities.*;
import com.starshootercity.events.PlayerSwapOriginEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.world.level.border.WorldBorder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class OriginSwapper implements Listener {
    private final static NamespacedKey displayKey = new NamespacedKey(OriginsReborn.getInstance(), "display-item");
    private final static NamespacedKey confirmKey = new NamespacedKey(OriginsReborn.getInstance(), "confirm-select");
    private final static NamespacedKey costsCurrencyKey = new NamespacedKey(OriginsReborn.getInstance(), "costs-currency");
    private final static NamespacedKey originKey = new NamespacedKey(OriginsReborn.getInstance(), "origin-name");
    private final static NamespacedKey swapTypeKey = new NamespacedKey(OriginsReborn.getInstance(), "swap-type");
    private final static NamespacedKey pageSetKey = new NamespacedKey(OriginsReborn.getInstance(), "page-set");
    private final static NamespacedKey pageScrollKey = new NamespacedKey(OriginsReborn.getInstance(), "page-scroll");
    private final static NamespacedKey randomOriginKey = new NamespacedKey(OriginsReborn.getInstance(), "random-origin");
    private final static Random random = new Random();

    public static String getInverse(String string) {
        StringBuilder result = new StringBuilder();
        for (char c : string.toCharArray()) {
            result.append(getInverse(c));
        }
        return result.toString();
    }

    public static void openOriginSwapper(Player player, PlayerSwapOriginEvent.SwapReason reason, int slot, int scrollAmount, boolean forceRandom) {
        openOriginSwapper(player, reason, slot, scrollAmount, forceRandom, false);
    }
    public static void openOriginSwapper(Player player, PlayerSwapOriginEvent.SwapReason reason, int slot, int scrollAmount, boolean forceRandom, boolean cost) {
        if (OriginLoader.origins.size() == 0) return;
        List<Origin> origins = new ArrayList<>(OriginLoader.origins);
        origins.removeIf(Origin::isUnchoosable);
        boolean enableRandom = OriginsReborn.getInstance().getConfig().getBoolean("origin-selection.random-option.enabled");
        while (slot > origins.size() || slot == origins.size() && !enableRandom) {
            slot -= origins.size() + (enableRandom ? 1 : 0);
        }
        while (slot < 0) {
            slot += origins.size() + (enableRandom ? 1 : 0);
        }
        ItemStack icon;
        String name;
        char impact;
        LineData data;
        if (slot == origins.size()) {
            List<String> excludedOrigins = OriginsReborn.getInstance().getConfig().getStringList("origin-selection.random-option.exclude");
            icon = OrbOfOrigin.orb;
            name = "Random";
            impact = '\uE002';
            StringBuilder names = new StringBuilder("You'll be assigned one of the following:\n\n");
            for (Origin origin : origins) {
                if (!excludedOrigins.contains(origin.getName())) {
                    names.append(origin.getName()).append("\n");
                }
            }
            data = new LineData(LineData.makeLineFor(
                    names.toString(),
                    LineData.LineComponent.LineType.DESCRIPTION
            ));
        } else {
            Origin origin = origins.get(slot);
            icon = origin.getIcon();
            name = origin.getName();
            impact = origin.getImpact();
            data = new LineData(origin);
        }
        StringBuilder compressedName = new StringBuilder("\uF001");
        for (char c : name.toCharArray()) {
            compressedName.append(c);
            compressedName.append('\uF000');
        }
        Component component = Component.text("\uF000\uE000\uF001\uE001\uF002" + impact)
                .font(Key.key("minecraft:origin_selector"))
                .color(NamedTextColor.WHITE)
                .append(Component.text(compressedName.toString())
                        .font(Key.key("minecraft:origin_title_text"))
                        .color(NamedTextColor.WHITE)
                )
                .append(Component.text(getInverse(name) + "\uF000")
                        .font(Key.key("minecraft:reverse_text"))
                        .color(NamedTextColor.WHITE)
                );
        for (Component c : data.getLines(scrollAmount)) {
            component = component.append(c);
        }
        Inventory swapperInventory = Bukkit.createInventory(null, 54,
                component
        );
        ItemMeta meta = icon.getItemMeta();
        meta.getPersistentDataContainer().set(originKey, PersistentDataType.STRING, name.toLowerCase());
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }
        meta.getPersistentDataContainer().set(displayKey, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(swapTypeKey, PersistentDataType.STRING, reason.getReason());
        icon.setItemMeta(meta);
        swapperInventory.setItem(1, icon);
        ItemStack confirm = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemStack invisibleConfirm = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        ItemMeta invisibleConfirmMeta = invisibleConfirm.getItemMeta();

        confirmMeta.displayName(Component.text("Confirm")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        confirmMeta.setCustomModelData(5);
        confirmMeta.getPersistentDataContainer().set(confirmKey, PersistentDataType.BOOLEAN, true);

        invisibleConfirmMeta.displayName(Component.text("Confirm")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        invisibleConfirmMeta.setCustomModelData(6);
        invisibleConfirmMeta.getPersistentDataContainer().set(confirmKey, PersistentDataType.BOOLEAN, true);

        if (cost && !player.hasPermission(OriginsReborn.getInstance().getConfig().getString("swap-command.vault.bypass-permission", "originsreborn.costbypass"))) {
            String symbol = OriginsReborn.getInstance().getConfig().getString("swap-command.vault.currency-symbol", "$");
            int amount = OriginsReborn.getInstance().getConfig().getInt("swap-command.vault.cost", 1000);
            List<Component> costsCurrency = List.of(
                    Component.text((OriginsReborn.getInstance().getEconomy().has(player, amount) ? "This will cost %s%s of your balance!" : "You need at least %s%s in your balance to do this!").formatted(symbol, amount))
            );
            confirmMeta.lore(costsCurrency);
            invisibleConfirmMeta.lore(costsCurrency);
            confirmMeta.getPersistentDataContainer().set(costsCurrencyKey, PersistentDataType.BOOLEAN, true);
            invisibleConfirmMeta.getPersistentDataContainer().set(costsCurrencyKey, PersistentDataType.BOOLEAN, true);
        }

        ItemStack up = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemStack down = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta upMeta = up.getItemMeta();
        ItemMeta downMeta = down.getItemMeta();

        int scrollSize = OriginsReborn.getInstance().getConfig().getInt("origin-selection.scroll-amount", 1);

        upMeta.displayName(Component.text("Up")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        if (scrollAmount != 0) {
            upMeta.getPersistentDataContainer().set(pageSetKey, PersistentDataType.INTEGER, slot);
            upMeta.getPersistentDataContainer().set(pageScrollKey, PersistentDataType.INTEGER, Math.max(scrollAmount - scrollSize, 0));
            upMeta.getPersistentDataContainer().set(randomOriginKey, PersistentDataType.BOOLEAN, forceRandom);
        }
        upMeta.setCustomModelData(3 + (scrollAmount == 0 ? 6 : 0));


        int size = data.lines.size() - scrollAmount - 6;
        boolean canGoDown = size > 0;

        downMeta.displayName(Component.text("Down")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        if (canGoDown) {
            downMeta.getPersistentDataContainer().set(pageSetKey, PersistentDataType.INTEGER, slot);
            downMeta.getPersistentDataContainer().set(pageScrollKey, PersistentDataType.INTEGER, Math.min(scrollAmount + scrollSize, scrollAmount + size));
            downMeta.getPersistentDataContainer().set(randomOriginKey, PersistentDataType.BOOLEAN, forceRandom);
        }
        downMeta.setCustomModelData(4 + (!canGoDown ? 6 : 0));

        up.setItemMeta(upMeta);
        down.setItemMeta(downMeta);
        swapperInventory.setItem(52, up);
        swapperInventory.setItem(53, down);

        if (!forceRandom) {
            ItemStack left = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemStack right = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta leftMeta = left.getItemMeta();
            ItemMeta rightMeta = right.getItemMeta();

            leftMeta.displayName(Component.text("Previous origin")
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            leftMeta.getPersistentDataContainer().set(pageSetKey, PersistentDataType.INTEGER, slot - 1);
            leftMeta.getPersistentDataContainer().set(pageScrollKey, PersistentDataType.INTEGER, 0);
            leftMeta.setCustomModelData(1);


            rightMeta.displayName(Component.text("Next origin")
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            rightMeta.getPersistentDataContainer().set(pageSetKey, PersistentDataType.INTEGER, slot + 1);
            rightMeta.getPersistentDataContainer().set(pageScrollKey, PersistentDataType.INTEGER, 0);
            rightMeta.setCustomModelData(2);

            left.setItemMeta(leftMeta);
            right.setItemMeta(rightMeta);

            swapperInventory.setItem(47, left);
            swapperInventory.setItem(51, right);
        }

        confirm.setItemMeta(confirmMeta);
        invisibleConfirm.setItemMeta(invisibleConfirmMeta);
        swapperInventory.setItem(48, confirm);
        swapperInventory.setItem(49, invisibleConfirm);
        swapperInventory.setItem(50, invisibleConfirm);
        player.openInventory(swapperInventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getWhoClicked().getOpenInventory().getItem(1);
        if (item != null) {
            if (item.getItemMeta() == null) return;
            if (item.getItemMeta().getPersistentDataContainer().has(displayKey)) {
                event.setCancelled(true);
            }
            if (event.getWhoClicked() instanceof Player player) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem == null) return;
                Integer page = currentItem.getItemMeta().getPersistentDataContainer().get(pageSetKey, PersistentDataType.INTEGER);
                if (page != null) {
                    boolean forceRandom = currentItem.getItemMeta().getPersistentDataContainer().getOrDefault(randomOriginKey, PersistentDataType.BOOLEAN, false);
                    Integer scroll = currentItem.getItemMeta().getPersistentDataContainer().get(pageScrollKey, PersistentDataType.INTEGER);
                    if (scroll == null) return;
                    player.playSound(player, Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1, 1);
                    openOriginSwapper(player, getReason(item), page, scroll, forceRandom);
                }
                if (currentItem.getItemMeta().getPersistentDataContainer().has(confirmKey)) {
                    int amount = OriginsReborn.getInstance().getConfig().getInt("swap-command.vault.cost", 1000);
                    if (!player.hasPermission(OriginsReborn.getInstance().getConfig().getString("swap-command.vault.bypass-permission", "originsreborn.costbypass")) && Boolean.TRUE.equals(currentItem.getItemMeta().getPersistentDataContainer().get(costsCurrencyKey, PersistentDataType.BOOLEAN))) {
                        if (!OriginsReborn.getInstance().getEconomy().has(player, amount)) {
                            return;
                        } else {
                            OriginsReborn.getInstance().getEconomy().withdrawPlayer(player, amount);
                        }
                    }
                    String originName = item.getItemMeta().getPersistentDataContainer().get(originKey, PersistentDataType.STRING);
                    if (originName == null) return;
                    Origin origin;
                    if (originName.equalsIgnoreCase("random")) {
                        List<String> excludedOrigins = OriginsReborn.getInstance().getConfig().getStringList("origin-selection.random-option.exclude");
                        List<Origin> origins = new ArrayList<>(OriginLoader.origins);
                        origins.removeIf(origin1 -> excludedOrigins.contains(origin1.getName()));
                        origins.removeIf(Origin::isUnchoosable);
                        if (origins.isEmpty()) {
                            origin = OriginLoader.origins.get(0);
                        } else {
                            origin = origins.get(random.nextInt(origins.size()));
                        }
                    } else {
                        origin = OriginLoader.originNameMap.get(originName);
                    }
                    PlayerSwapOriginEvent.SwapReason reason = getReason(item);
                    player.playSound(player, Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1, 1);
                    player.closeInventory();

                    ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

                    if (reason == PlayerSwapOriginEvent.SwapReason.ORB_OF_ORIGIN) {
                        EquipmentSlot hand = null;
                        if (meta != null) {
                            if (meta.getPersistentDataContainer().has(OrbOfOrigin.orbKey)) {
                                hand = EquipmentSlot.HAND;
                            }
                        }
                        if (hand == null) {
                            ItemMeta offhandMeta = player.getInventory().getItemInOffHand().getItemMeta();
                            if (offhandMeta != null) {
                                if (offhandMeta.getPersistentDataContainer().has(OrbOfOrigin.orbKey)) {
                                    hand = EquipmentSlot.OFF_HAND;
                                }
                            }
                        }
                        if (hand == null) return;
                        orbCooldown.put(player, System.currentTimeMillis());
                        player.swingHand(hand);
                        if (OriginsReborn.getInstance().getConfig().getBoolean("orb-of-origin.consume")) {
                            player.getInventory().getItem(hand).setAmount(player.getInventory().getItem(hand).getAmount() - 1);
                        }
                    }
                    boolean resetPlayer = shouldResetPlayer(reason);
                    setOrigin(player, origin, reason, resetPlayer);
                }
            }
        }
    }

    public boolean shouldResetPlayer(PlayerSwapOriginEvent.SwapReason reason) {
        return switch (reason) {
            case COMMAND -> OriginsReborn.getInstance().getConfig().getBoolean("swap-command.reset-player");
            case ORB_OF_ORIGIN -> OriginsReborn.getInstance().getConfig().getBoolean("orb-of-origin.reset-player");
            default -> false;
        };
    }

    public static int getWidth(char c) {
        return switch (c) {
            case ':', '.', '\'', 'i', ';', '|', '!', ',', '\uF00A' -> 2;
            case 'l', '`' -> 3;
            case '(', '}', ')', '*', '[', ']', '{', '"', 'I', 't', ' ' -> 4;
            case '<', 'k', '>', 'f' -> 5;
            case '^', '/', 'X', 'W', 'g', 'h', '=', 'x', 'J', '\\', 'n', 'y', 'w', 'L', 'j', 'Z', '1', '?', '-', 'G', 'H', 'K', 'N', '0', '7', '8', 'O', 'V', 'p', 'Y', 'z', '+', 'A', '2', 'd', 'T', 'B', 'b', 'R', 'q', 'F', 'Q', 'a', '6', 'e', 'C', 'U', '3', 'S', '#', 'P', 'M', '9', 'v', '_', 'o', 'm', '&', 'u', 'c', 'D', 'E', '4', '5', 'r', 's', '$', '%' -> 6;
            case '@', '~' -> 7;
            default -> throw new IllegalStateException("Unexpected value: " + c);
        };
    }

    public static int getWidth(String s) {
        int result = 0;
        for (char c : s.toCharArray()) {
            result += getWidth(c);
        }
        return result;
    }

    public static char getInverse(char c) {
        return switch (getWidth(c)) {
            case 2 -> '\uF001';
            case 3 -> '\uF002';
            case 4 -> '\uF003';
            case 5 -> '\uF004';
            case 6 -> '\uF005';
            case 7 -> '\uF006';
            case 8 -> '\uF007';
            case 9 -> '\uF008';
            case 10 -> '\uF009';
            default -> throw new IllegalStateException("Unexpected value: " + c);
        };
    }

    public static Map<Player, Long> orbCooldown = new HashMap<>();

    public static void resetPlayer(Player player, boolean full) {
        resetAttributes(player);
        ClientboundSetBorderWarningDistancePacket warningDistancePacket = new ClientboundSetBorderWarningDistancePacket(new WorldBorder() {{
            setWarningBlocks(player.getWorld().getWorldBorder().getWarningDistance());
        }});
        ((CraftPlayer) player).getHandle().connection.send(warningDistancePacket);
        player.setCooldown(Material.SHIELD, 0);
        player.setAllowFlight(false);
        player.setFlying(false);
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            AbilityRegister.updateEntity(player, otherPlayer);
        }
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getAmplifier() == -1 || effect.getDuration() == PotionEffect.INFINITE_DURATION) player.removePotionEffect(effect.getType());
        }
        if (!full) return;
        player.getInventory().clear();
        player.getEnderChest().clear();
        player.setSaturation(5);
        player.setFallDistance(0);
        player.setRemainingAir(player.getMaximumAir());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setHealth(getMaxHealth(player));
        ShulkerInventory.getInventoriesConfig().set(player.getUniqueId().toString(), null);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        World world = getRespawnWorld(getOrigin(player));
        player.teleport(world.getSpawnLocation());
        player.setBedSpawnLocation(null);
    }

    public static @NotNull World getRespawnWorld(@Nullable Origin origin) {
        if (origin != null) {
            for (Ability ability : origin.getAbilities()) {
                if (ability instanceof DefaultSpawnAbility defaultSpawnAbility) {
                    World world = defaultSpawnAbility.getWorld();
                    if (world != null) return world;
                }
            }
        }
        String overworld = OriginsReborn.getInstance().getConfig().getString("worlds.world");
        if (overworld == null) {
            overworld = "world";
            OriginsReborn.getInstance().getConfig().set("worlds.world", "world");
            OriginsReborn.getInstance().saveConfig();
        }
        World world = Bukkit.getWorld(overworld);
        if (world == null) return Bukkit.getWorlds().get(0);
        return world;
    }

    public static double getMaxHealth(Player player) {
        applyAttributeChanges(player);
        AttributeInstance instance = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (instance == null) return 20;
        return instance.getValue();
    }

    public static void applyAttributeChanges(Player player) {
        Origin origin = getOrigin(player);
        if (origin == null) return;
        for (Ability ability : AbilityRegister.abilityMap.values()) {
            if (ability instanceof AttributeModifierAbility attributeModifierAbility) {
                AttributeInstance instance = player.getAttribute(attributeModifierAbility.getAttribute());
                if (instance == null) continue;
                UUID u = UUID.nameUUIDFromBytes(StringUtils.getBytes(ability.getKey().asString(), (Charset) null));
                if (origin.hasAbility(ability.getKey())) {
                    if (instance.getModifier(u) != null) continue;
                    instance.addModifier(new AttributeModifier(u, attributeModifierAbility.getKey().asString(), attributeModifierAbility.getAmount(), attributeModifierAbility.getOperation()));
                } else if (instance.getModifier(u) != null) instance.removeModifier(u);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        resetAttributes(event.getPlayer());
    }

    public static void resetAttributes(Player player) {
        for (Attribute attribute : Attribute.values()) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) continue;
            for (AttributeModifier modifier : instance.getModifiers()) {
                instance.removeModifier(modifier);
            }
        }
    }

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getOrigin(player) == null) {
                if (player.isDead()) continue;
                if (player.getOpenInventory().getType() != InventoryType.CHEST) {
                    if (OriginsReborn.getInstance().getConfig().getBoolean("origin-selection.randomise")) {
                        selectRandomOrigin(player, PlayerSwapOriginEvent.SwapReason.INITIAL);
                    } else openOriginSwapper(player, PlayerSwapOriginEvent.SwapReason.INITIAL, 0, 0, false);
                }
            } else {
                AbilityRegister.updateFlight(player);
                player.setAllowFlight(AbilityRegister.canFly(player));
                player.setInvisible(AbilityRegister.isInvisible(player));
                applyAttributeChanges(player);
            }
        }
    }

    public void selectRandomOrigin(Player player, PlayerSwapOriginEvent.SwapReason reason) {
        Origin origin = OriginLoader.origins.get(random.nextInt(OriginLoader.origins.size()));
        setOrigin(player, origin, reason, shouldResetPlayer(reason));
        openOriginSwapper(player, reason, OriginLoader.origins.indexOf(origin), 0, true);
    }

    private final Map<Player, PlayerRespawnEvent.RespawnReason> lastRespawnReasons = new HashMap<>();

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.getPlayer().getBedSpawnLocation() == null) {
            World world = getRespawnWorld(getOrigin(event.getPlayer()));
            event.setRespawnLocation(world.getSpawnLocation());
        }
        lastRespawnReasons.put(event.getPlayer(), event.getRespawnReason());
    }

    @EventHandler
    public void onPlayerPostRespawn(PlayerPostRespawnEvent event) {
        if (lastRespawnReasons.get(event.getPlayer()) != PlayerRespawnEvent.RespawnReason.DEATH) return;
        FileConfiguration config = OriginsReborn.getInstance().getConfig();
        if (config.getBoolean("origin-selection.death-origin-change")) {
            setOrigin(event.getPlayer(), null, PlayerSwapOriginEvent.SwapReason.DIED, false);
            if (OriginsReborn.getInstance().getConfig().getBoolean("origin-selection.randomise")) {
                selectRandomOrigin(event.getPlayer(), PlayerSwapOriginEvent.SwapReason.INITIAL);
            } else openOriginSwapper(event.getPlayer(), PlayerSwapOriginEvent.SwapReason.INITIAL, 0, 0, false);
        }
    }

    public PlayerSwapOriginEvent.SwapReason getReason(ItemStack icon) {
        return PlayerSwapOriginEvent.SwapReason.get(icon.getItemMeta().getPersistentDataContainer().get(swapTypeKey, PersistentDataType.STRING));
    }

    public static Origin getOrigin(Player player) {
        if (player.getPersistentDataContainer().has(originKey)) {
            return OriginLoader.originNameMap.get(player.getPersistentDataContainer().get(originKey, PersistentDataType.STRING));
        } else {
            String name = originFileConfiguration.getString(player.getUniqueId().toString());
            if (name == null) return null;
            player.getPersistentDataContainer().set(originKey, PersistentDataType.STRING, name);
            return OriginLoader.originNameMap.get(name);
        }
    }
    public static void setOrigin(Player player, @Nullable Origin origin, PlayerSwapOriginEvent.SwapReason reason, boolean resetPlayer) {
        PlayerSwapOriginEvent swapOriginEvent = new PlayerSwapOriginEvent(player, reason, resetPlayer, getOrigin(player), origin);
        if (!swapOriginEvent.callEvent()) return;
        if (swapOriginEvent.getNewOrigin() == null) {
            originFileConfiguration.set(player.getUniqueId().toString(), null);
            player.getPersistentDataContainer().remove(originKey);
            saveOrigins();
            resetPlayer(player, swapOriginEvent.isResetPlayer());
            return;
        }
        originFileConfiguration.set(player.getUniqueId().toString(), swapOriginEvent.getNewOrigin().getName().toLowerCase());
        player.getPersistentDataContainer().set(originKey, PersistentDataType.STRING, swapOriginEvent.getNewOrigin().getName().toLowerCase());
        saveOrigins();
        resetPlayer(player, swapOriginEvent.isResetPlayer());
    }


    private static File originFile;
    private static FileConfiguration originFileConfiguration;
    public OriginSwapper() {
        originFile = new File(OriginsReborn.getInstance().getDataFolder(), "selected-origins.yml");
        if (!originFile.exists()) {
            boolean ignored = originFile.getParentFile().mkdirs();
            OriginsReborn.getInstance().saveResource("selected-origins.yml", false);
        }
        originFileConfiguration = new YamlConfiguration();
        try {
            originFileConfiguration.load(originFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveOrigins() {
        try {
            originFileConfiguration.save(originFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class LineData {
        public static List<LineComponent> makeLineFor(String text, LineComponent.LineType type) {
            StringBuilder result = new StringBuilder();
            List<LineComponent> list = new ArrayList<>();
            List<String> splitLines = new ArrayList<>(Arrays.stream(text.split("\n", 2)).toList());
            StringBuilder otherPart = new StringBuilder();
            String firstLine = splitLines.remove(0);
            if (firstLine.contains(" ") && getWidth(firstLine) > 140) {
                List<String> split = new ArrayList<>(Arrays.stream(firstLine.split(" ")).toList());
                StringBuilder firstPart = new StringBuilder(split.get(0));
                split.remove(0);
                boolean canAdd = true;
                for (String s : split) {
                    if (canAdd && getWidth(firstPart + " " + s) <= 140) {
                        firstPart.append(" ");
                        firstPart.append(s);
                    } else {
                        canAdd = false;
                        if (otherPart.length() > 0) otherPart.append(" ");
                        otherPart.append(s);
                    }
                }
                firstLine = firstPart.toString();
            }
            for (String s : splitLines) {
                if (otherPart.length() > 0) otherPart.append("\n");
                otherPart.append(s);
            }
            if (type == LineComponent.LineType.DESCRIPTION) firstLine = '\uF00A' + firstLine;
            for (char c : firstLine.toCharArray()) {
                result.append(c);
                result.append('\uF000');
            }
            String finalText = firstLine;
            list.add(new LineComponent(
                        Component.text(result.toString())
                                .color(type == LineComponent.LineType.TITLE ? NamedTextColor.WHITE : TextColor.fromHexString("#CACACA"))
                                .append(Component.text(getInverse(finalText))),
                        type
                ));
            if (otherPart.length() != 0) {
                list.addAll(makeLineFor(otherPart.toString(), type));
            }
            return list;
        }
        public static class LineComponent {
            public enum LineType {
                TITLE,
                DESCRIPTION
            }
            private final Component component;
            private final LineType type;

            public LineComponent(Component component, LineType type) {
                this.component = component;
                this.type = type;
            }

            public LineComponent() {
                this.type = LineType.DESCRIPTION;
                this.component = Component.empty();
            }

            public Component getComponent(int lineNumber) {
                @Subst("minecraft:text_line_0") String formatted = "minecraft:%stext_line_%s".formatted(type == LineType.DESCRIPTION ? "" : "title_", lineNumber);
                return component.font(
                        Key.key(formatted)
                );
            }
        }
        private final List<LineComponent> lines;
        public LineData(Origin origin) {
            lines = new ArrayList<>();
            lines.addAll(origin.getLineData());
            List<VisibleAbility> visibleAbilities = origin.getVisibleAbilities();
            int size = visibleAbilities.size();
            int count = 0;
            if (size > 0) lines.add(new LineComponent());
            for (VisibleAbility visibleAbility : visibleAbilities) {
                count++;
                lines.addAll(visibleAbility.getTitle());
                lines.addAll(visibleAbility.getDescription());
                if (count < size) lines.add(new LineComponent());
            }
        }
        public LineData(List<LineComponent> lines) {
            this.lines = lines;
        }

        public List<Component> getLines(int startingPoint) {
            List<Component> resultLines = new ArrayList<>();
            for (int i = startingPoint; i < startingPoint + 6 && i < lines.size(); i++) {
                resultLines.add(lines.get(i).getComponent(i - startingPoint));
            }
            return resultLines;
        }
    }
}
