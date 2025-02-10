package com.starshootercity;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.starshootercity.abilities.*;
import com.starshootercity.commands.OriginCommand;
import com.starshootercity.events.PlayerSwapOriginEvent;
import com.starshootercity.geysermc.GeyserSwapper;
import com.starshootercity.packetsenders.NMSInvoker;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class OriginSwapper implements Listener {
    private final static NamespacedKey displayKey = new NamespacedKey(OriginsReborn.getInstance(), "displayed-item");
    private final static NamespacedKey layerKey = new NamespacedKey(OriginsReborn.getInstance(), "layer");
    private final static NamespacedKey confirmKey = new NamespacedKey(OriginsReborn.getInstance(), "confirm-select");
    private final static NamespacedKey costsCurrencyKey = new NamespacedKey(OriginsReborn.getInstance(), "costs-currency");
    private final static NamespacedKey originKey = new NamespacedKey(OriginsReborn.getInstance(), "origin-name");
    private final static NamespacedKey swapTypeKey = new NamespacedKey(OriginsReborn.getInstance(), "swap-type");
    private final static NamespacedKey pageSetKey = new NamespacedKey(OriginsReborn.getInstance(), "page-set");
    private final static NamespacedKey pageScrollKey = new NamespacedKey(OriginsReborn.getInstance(), "page-scroll");
    private final static NamespacedKey costKey = new NamespacedKey(OriginsReborn.getInstance(), "enable-cost");
    private final static NamespacedKey displayOnlyKey = new NamespacedKey(OriginsReborn.getInstance(), "display-only");
    private final static NamespacedKey closeKey = new NamespacedKey(OriginsReborn.getInstance(), "close");
    private final static Random random = new Random();

    public static ConfigOptions options = ConfigOptions.getInstance();
    public static OriginsReborn origins = OriginsReborn.getInstance();
    public static NMSInvoker nmsInvoker = OriginsReborn.getNMSInvoker();

    public static String getInverse(String string) {
        StringBuilder result = new StringBuilder();
        for (char c : string.toCharArray()) {
            result.append(getInverse(c));
        }
        return result.toString();
    }

    /**
     * @deprecated Origins-Reborn now has a 'layer' system, allowing for multiple origins to be set at once
     */
    @Deprecated
    public static void openOriginSwapper(Player player, PlayerSwapOriginEvent.SwapReason reason, int slot, int scrollAmount, boolean cost, boolean displayOnly) {
        openOriginSwapper(player, reason, slot, scrollAmount, cost, displayOnly, "origin");
    }

    /**
     * @deprecated Origins-Reborn now has a 'layer' system, allowing for multiple origins to be set at once
     */
    @Deprecated
    public static void openOriginSwapper(Player player, PlayerSwapOriginEvent.SwapReason reason, int slot, int scrollAmount) {
        openOriginSwapper(player, reason, slot, scrollAmount, "origin");
    }

    /**
     * @deprecated Origins-Reborn now has a 'layer' system, allowing for multiple origins to be set at once
     */
    @Deprecated
    public static void openOriginSwapper(Player player, PlayerSwapOriginEvent.SwapReason reason, int slot, int scrollAmount, boolean cost) {
        openOriginSwapper(player, reason, slot, scrollAmount, cost, "origin");
    }

    public static void openOriginSwapper(Player player, PlayerSwapOriginEvent.SwapReason reason, int slot, int scrollAmount, String layer) {
        openOriginSwapper(player, reason, slot, scrollAmount, false, false, layer);
    }
    public static void openOriginSwapper(Player player, PlayerSwapOriginEvent.SwapReason reason, int slot, int scrollAmount, boolean cost, String layer) {
        openOriginSwapper(player, reason, slot, scrollAmount, cost, false, layer);
    }
    public static void openOriginSwapper(Player player, PlayerSwapOriginEvent.SwapReason reason, int slot, int scrollAmount, boolean cost, boolean displayOnly, String layer) {
        if (shouldDisallowSelection(player, reason)) return;
        if (reason == PlayerSwapOriginEvent.SwapReason.INITIAL) {
            String def = options.getDefaultOrigin();
            Origin defaultOrigin = AddonLoader.getOriginByFilename(def);
            if (defaultOrigin != null) {
                setOrigin(player, defaultOrigin, reason, false, layer);
                return;
            }
        }
        lastSwapReasons.put(player, reason);
        boolean enableRandom = options.isRandomOptionEnabled();

        if (GeyserSwapper.checkBedrockSwap(player, reason, cost, displayOnly, layer)) {
            if (AddonLoader.getOrigins(layer).isEmpty()) return;
            List<Origin> origins = new ArrayList<>(AddonLoader.getOrigins(layer));
            if (!displayOnly) origins.removeIf(origin -> origin.isUnchoosable(player) || origin.hasPermission() && !player.hasPermission(origin.getPermission()));
            while (slot > origins.size() || slot == origins.size() && !enableRandom) {
                slot -= origins.size() + (enableRandom ? 1 : 0);
            }
            while (slot < 0) {
                slot += origins.size() + (enableRandom ? 1 : 0);
            }
            ItemStack icon;
            String name;
            String nameForDisplay;
            char impact;
            int amount = options.getSwapCommandVaultDefaultCost();

            LineData data;
            if (slot == origins.size()) {
                List<String> excludedOrigins = options.getRandomOptionExclude();

                List<String> excludedOriginNames = new ArrayList<>();
                for (String s : excludedOrigins) {
                    Origin origin = AddonLoader.getOriginByFilename(s);
                    if (origin == null) continue;
                    excludedOriginNames.add(AddonLoader.getTextFor("origin." + origin.getAddon().getNamespace() + "." + s.replace(" ", "_").toLowerCase() + ".name", origin.getName()));
                }
                icon = OrbOfOrigin.orb.clone();
                name = AddonLoader.getTextFor("origin.origins.random.name", "Random");
                nameForDisplay = AddonLoader.getTextFor("origin.origins.random.name", "Random");
                impact = '\uE002';
                StringBuilder names = new StringBuilder("%s\n\n".formatted(AddonLoader.getTextFor("origin.origins.random.description", "You'll be assigned one of the following:")));
                for (Origin origin : origins) {
                    if (!excludedOriginNames.contains(origin.getName())) {
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
                nameForDisplay = origin.getNameForDisplay();
                impact = origin.getImpact();
                data = new LineData(origin);
                if (origin.getCost() != null) {
                    amount = origin.getCost();
                }
            }
            StringBuilder compressedName = new StringBuilder("\uF001");
            for (char c : nameForDisplay.toCharArray()) {
                compressedName.append(c);
                compressedName.append('\uF000');
            }
            Component background = applyFont(ShortcutUtils.getColored(options.getScreenTitleBackground()), Key.key("minecraft:default"));
            Component component = applyFont(Component.text("\uF000\uE000\uF001\uE001\uF002" + impact),
                    Key.key("minecraft:origin_selector"))
                    .color(NamedTextColor.WHITE)
                    .append(background)
                    .append(applyFont(Component.text(compressedName.toString()),
                            Key.key("minecraft:origin_title_text")
                    ).color(NamedTextColor.WHITE))
                    .append(applyFont(Component.text(getInverse(nameForDisplay) + "\uF000"),
                            Key.key("minecraft:reverse_text")
                    ).color(NamedTextColor.WHITE));
            for (Component c : data.getLines(scrollAmount)) {
                component = component.append(c);
            }
            Component prefix = applyFont(ShortcutUtils.getColored(options.getScreenTitlePrefix()), Key.key("minecraft:default"));
            Component suffix = applyFont(ShortcutUtils.getColored(options.getScreenTitleSuffix()), Key.key("minecraft:default"));
            Inventory swapperInventory = Bukkit.createInventory(null, 54,
                    prefix.append(component).append(suffix)
            );
            ItemMeta meta = icon.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(originKey, PersistentDataType.STRING, name.toLowerCase());
            if (meta instanceof SkullMeta skullMeta) {
                skullMeta.setOwningPlayer(player);
            }
            container.set(displayKey, BooleanPDT.BOOLEAN, true);
            container.set(swapTypeKey, PersistentDataType.STRING, reason.getReason());
            container.set(layerKey, PersistentDataType.STRING, layer);
            icon.setItemMeta(meta);
            swapperInventory.setItem(1, icon);
            ItemStack confirm = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemStack invisibleConfirm = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta confirmMeta = confirm.getItemMeta();
            PersistentDataContainer confirmContainer = confirmMeta.getPersistentDataContainer();
            ItemMeta invisibleConfirmMeta = invisibleConfirm.getItemMeta();
            PersistentDataContainer invisibleConfirmContainer = invisibleConfirmMeta.getPersistentDataContainer();

            confirmMeta.displayName(Component.text("Confirm")
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            confirmMeta = nmsInvoker.setCustomModelData(confirmMeta, 5);
            if (!displayOnly) confirmContainer.set(confirmKey, BooleanPDT.BOOLEAN, true);
            else confirmContainer.set(closeKey, BooleanPDT.BOOLEAN, true);

            invisibleConfirmMeta.displayName(Component.text("Confirm")
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            invisibleConfirmMeta = nmsInvoker.setCustomModelData(invisibleConfirmMeta, 6);
            if (!displayOnly) invisibleConfirmContainer.set(confirmKey, BooleanPDT.BOOLEAN, true);
            else invisibleConfirmContainer.set(closeKey, BooleanPDT.BOOLEAN, true);

            if (amount != 0 && cost && !player.hasPermission(options.getSwapCommandVaultBypassPermission())) {
                boolean go = true;
                if (OriginsReborn.getInstance().getConfig().getBoolean("swap-command.vault.permanent-purchases")) {
                    go = !getUsedOriginFileConfiguration().getStringList(player.getUniqueId().toString()).contains(name);
                }
                if (go) {
                    String symbol = options.getSwapCommandVaultCurrencySymbol();
                    List<Component> costsCurrency = List.of(
                            Component.text((OriginsReborn.getInstance().getEconomy().has(player, amount) ? "This will cost %s%s of your balance!" : "You need at least %s%s in your balance to do this!").formatted(symbol, amount))
                    );
                    confirmMeta.lore(costsCurrency);
                    invisibleConfirmMeta.lore(costsCurrency);
                    confirmContainer.set(costsCurrencyKey, PersistentDataType.INTEGER, amount);
                    invisibleConfirmContainer.set(costsCurrencyKey, PersistentDataType.INTEGER, amount);
                }
            }

            ItemStack up = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemStack down = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta upMeta = up.getItemMeta();
            PersistentDataContainer upContainer = upMeta.getPersistentDataContainer();
            ItemMeta downMeta = down.getItemMeta();
            PersistentDataContainer downContainer = downMeta.getPersistentDataContainer();

            int scrollSize = options.getOriginSelectionScrollAmount();

            upMeta.displayName(Component.text("Up")
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            if (scrollAmount != 0) {
                upContainer.set(pageSetKey, PersistentDataType.INTEGER, slot);
                upContainer.set(pageScrollKey, PersistentDataType.INTEGER, Math.max(scrollAmount - scrollSize, 0));
            }
            upMeta = nmsInvoker.setCustomModelData(upMeta, 3 + (scrollAmount == 0 ? 6 : 0));
            upContainer.set(costKey, BooleanPDT.BOOLEAN, cost);
            upContainer.set(displayOnlyKey, BooleanPDT.BOOLEAN, displayOnly);


            int size = data.lines.size() - scrollAmount - 6;
            boolean canGoDown = size > 0;

            downMeta.displayName(Component.text("Down")
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            if (canGoDown) {
                downContainer.set(pageSetKey, PersistentDataType.INTEGER, slot);
                downContainer.set(pageScrollKey, PersistentDataType.INTEGER, Math.min(scrollAmount + scrollSize, scrollAmount + size));
            }
            downMeta = nmsInvoker.setCustomModelData(downMeta, 4 + (!canGoDown ? 6 : 0));
            downContainer.set(costKey, BooleanPDT.BOOLEAN, cost);
            downContainer.set(displayOnlyKey, BooleanPDT.BOOLEAN, displayOnly);


            up.setItemMeta(upMeta);
            down.setItemMeta(downMeta);
            swapperInventory.setItem(52, up);
            swapperInventory.setItem(53, down);


            if (!displayOnly) {
                ItemStack left = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                ItemStack right = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                ItemMeta leftMeta = left.getItemMeta();
                PersistentDataContainer leftContainer = leftMeta.getPersistentDataContainer();
                ItemMeta rightMeta = right.getItemMeta();
                PersistentDataContainer rightContainer = rightMeta.getPersistentDataContainer();


                leftMeta.displayName(Component.text("Previous origin")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false));
                leftContainer.set(pageSetKey, PersistentDataType.INTEGER, slot - 1);
                leftContainer.set(pageScrollKey, PersistentDataType.INTEGER, 0);
                leftMeta = nmsInvoker.setCustomModelData(leftMeta, 1);
                leftContainer.set(costKey, BooleanPDT.BOOLEAN, cost);
                leftContainer.set(displayOnlyKey, BooleanPDT.BOOLEAN, false);

                rightMeta.displayName(Component.text("Next origin")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false));
                rightContainer.set(pageSetKey, PersistentDataType.INTEGER, slot + 1);
                rightContainer.set(pageScrollKey, PersistentDataType.INTEGER, 0);
                rightMeta = nmsInvoker.setCustomModelData(rightMeta, 2);
                rightContainer.set(costKey, BooleanPDT.BOOLEAN, cost);
                rightContainer.set(displayOnlyKey, BooleanPDT.BOOLEAN, false);


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
    }

    public static Component applyFont(Component component, Key font) {
        return OriginsReborn.getNMSInvoker().applyFont(component, font);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getWhoClicked().getOpenInventory().getItem(1);
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;
            PersistentDataContainer itemContainer = meta.getPersistentDataContainer();
            if (itemContainer.has(displayKey, BooleanPDT.BOOLEAN)) {
                event.setCancelled(true);
            }
            String layer = itemContainer.getOrDefault(layerKey, PersistentDataType.STRING, "origin");
            if (event.getWhoClicked() instanceof Player player) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem == null || currentItem.getItemMeta() == null) return;
                ItemMeta currentItemMeta = currentItem.getItemMeta();
                PersistentDataContainer currentItemContainer = currentItemMeta.getPersistentDataContainer();
                Integer page = currentItemContainer.get(pageSetKey, PersistentDataType.INTEGER);
                if (page != null) {
                    boolean cost = currentItemContainer.getOrDefault(costKey, BooleanPDT.BOOLEAN, false);
                    boolean allowUnchoosable = currentItemContainer.getOrDefault(displayOnlyKey, BooleanPDT.BOOLEAN, false);
                    Integer scroll = currentItemContainer.get(pageScrollKey, PersistentDataType.INTEGER);
                    if (scroll == null) return;
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1, 1);
                    openOriginSwapper(player, getReason(item), page, scroll, cost, allowUnchoosable, layer);
                }
                if (currentItemContainer.has(confirmKey, BooleanPDT.BOOLEAN)) {
                    int amount = options.getSwapCommandVaultCost();
                    if (!player.hasPermission(options.getSwapCommandVaultBypassPermission()) && currentItemContainer.has(costsCurrencyKey, PersistentDataType.INTEGER)) {
                        amount = currentItemContainer.getOrDefault(costsCurrencyKey, PersistentDataType.INTEGER, amount);
                        if (!OriginsReborn.getInstance().getEconomy().has(player, amount)) {
                            return;
                        } else {
                            origins.getEconomy().withdrawPlayer(player, amount);
                        }
                    }
                    String originName = item.getItemMeta().getPersistentDataContainer().get(originKey, PersistentDataType.STRING);
                    if (originName == null) return;
                    Origin origin;
                    if (originName.equalsIgnoreCase("random")) {
                        List<String> excludedOrigins = options.getRandomOptionExclude();

                        List<Origin> origins = new ArrayList<>(AddonLoader.getOrigins(layer));
                        origins.removeIf(origin1 -> excludedOrigins.contains(origin1.getName()));
                        origins.removeIf(origin1 -> origin1.isUnchoosable(player));
                        if (origins.isEmpty()) {
                            origin = AddonLoader.getFirstOrigin(layer);
                        } else {
                            origin = origins.get(random.nextInt(origins.size()));
                        }
                    } else {
                        origin = AddonLoader.getOrigin(originName);
                    }
                    PlayerSwapOriginEvent.SwapReason reason = getReason(item);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1, 1);
                    player.closeInventory();

                    if (reason == PlayerSwapOriginEvent.SwapReason.ORB_OF_ORIGIN) orbCooldown.put(player, System.currentTimeMillis());
                    boolean resetPlayer = shouldResetPlayer(reason);
                    if (origin.isUnchoosable(player)) {
                        openOriginSwapper(player, reason, 0, 0, layer);
                        return;
                    }
                    OriginsReborn.getCooldowns().setCooldown(player, OriginCommand.key);
                    setOrigin(player, origin, reason, resetPlayer, layer);
                } else if (currentItemContainer.has(closeKey, BooleanPDT.BOOLEAN)) event.getWhoClicked().closeInventory();
            }
        }
    }

    public static boolean shouldResetPlayer(PlayerSwapOriginEvent.SwapReason reason) {
        return switch (reason) {
            case COMMAND -> options.isSwapCommandResetPlayer();
            case ORB_OF_ORIGIN -> options.isOrbOfOriginResetPlayer();
            default -> false;
        };
    }

    public static int getWidth(String s) {
        int result = 0;
        for (char c : s.toCharArray()) {
            result += WidthGetter.getWidth(c);
        }
        return result;
    }

    public static String getInverse(char c) {
        return switch (WidthGetter.getWidth(c)) {
            case 0 -> "";
            case 2 -> "\uF001";
            case 3 -> "\uF002";
            case 4 -> "\uF003";
            case 5 -> "\uF004";
            case 6 -> "\uF005";
            case 7 -> "\uF006";
            case 8 -> "\uF007";
            case 9 -> "\uF008";
            case 10 -> "\uF009";
            case 11 -> "\uF008\uF001";
            case 12 -> "\uF009\uF001";
            case 13 -> "\uF009\uF002";
            case 14 -> "\uF009\uF003";
            case 15 -> "\uF009\uF004";
            case 16 -> "\uF009\uF005";
            case 17 -> "\uF009\uF006";
            default -> throw new IllegalStateException("Unexpected value: " + c);
        };
    }

    public static Map<Player, Long> orbCooldown = new HashMap<>();

    public static void resetPlayer(Player player, boolean full) {
        resetAttributes(player);
        player.closeInventory();
        OriginsReborn.getNMSInvoker().setWorldBorderOverlay(player, false);
        player.setCooldown(Material.SHIELD, 0);
        player.setAllowFlight(false);
        player.setFlying(false);
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            AbilityRegister.updateEntity(player, otherPlayer);
        }
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getAmplifier() == -1 || ShortcutUtils.isInfinite(effect)) player.removePotionEffect(effect.getType());
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
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        World world = getRespawnWorld(getOrigins(player));
        player.teleport(world.getSpawnLocation());
        OriginsReborn.getNMSInvoker().resetRespawnLocation(player);
    }

    public static @NotNull World getRespawnWorld(@NotNull List<Origin> origin) {
        List<Ability> abilities = new ArrayList<>();
        for (Origin o : origin) abilities.addAll(o.getAbilities());
        for (Ability ability : abilities) {
            if (ability instanceof DefaultSpawnAbility defaultSpawnAbility) {
                World world = defaultSpawnAbility.getWorld();
                if (world != null) return world;
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
        // Кэшируем экземпляр плагина и NMSInvoker для сокращения количества вызовов

        // Перебираем все способности из abilityMap
        for (Ability ability : AbilityRegister.abilityMap.values()) {
            // Обрабатываем только способности, реализующие AttributeModifierAbility
            if (!(ability instanceof AttributeModifierAbility attributeAbility)) {
                continue;
            }

            // Получаем экземпляр атрибута игрока для данной способности.
            AttributeInstance instance;
            try {
                instance = player.getAttribute(attributeAbility.getAttribute());
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (instance == null) {
                continue;
            }

            // Получаем строковое представление ключа способности и сразу форматируем его (заменяя ":" на "-")
            String abilityKeyStr = attributeAbility.getKey().asString();
            NamespacedKey key = new NamespacedKey(origins, abilityKeyStr.replace(":", "-"));

            // Вычисляем требуемое значение модификатора один раз для данной способности
            double requiredAmount = attributeAbility.getTotalAmount(player);
            // Определяем, активна ли способность для игрока
            boolean hasAbility = ability.hasAbility(player);

            // Получаем текущий модификатор по ключу, чтобы избежать лишних вызовов
            AttributeModifier currentModifier = nmsInvoker.getAttributeModifier(instance, key);

            if (hasAbility) {
                // Если модификатор уже существует и его значение совпадает с требуемым, пропускаем способность
                if (currentModifier != null) {
                    if (currentModifier.getAmount() == requiredAmount) {
                        continue;
                    } else {
                        instance.removeModifier(currentModifier);
                    }
                }
                // Добавляем новый (или обновляем существующий) модификатор
                nmsInvoker.addAttributeModifier(instance, key, abilityKeyStr, requiredAmount, attributeAbility.getActualOperation());
            } else {
                // Если способность не применяется, удаляем существующий модификатор (если он есть)
                if (currentModifier != null) {
                    instance.removeModifier(currentModifier);
                }
            }
        }
    }


    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> resetAttributes(event.getPlayer()), 5);
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadOrigins(event.getPlayer());
        resetAttributes(event.getPlayer());
        lastJoinedTick.put(event.getPlayer(), Bukkit.getCurrentTick());
        for (String layer : AddonLoader.layers) {
            if (event.getPlayer().getOpenInventory().getType() == InventoryType.CHEST) {
                continue;
            }
            Origin origin = getOrigin(event.getPlayer(), layer);
            if (origin != null) {
                if (origin.getTeam() == null) return;
                origin.getTeam().addPlayer(event.getPlayer());
            } else {
                if (AddonLoader.getDefaultOrigin(layer) != null) {
                    setOrigin(event.getPlayer(), AddonLoader.getDefaultOrigin(layer), PlayerSwapOriginEvent.SwapReason.INITIAL, false, layer);
                } else if (OriginsReborn.getInstance().getConfig().getBoolean("origin-selection.randomise.%s".formatted(layer))) {
                    selectRandomOrigin(event.getPlayer(), PlayerSwapOriginEvent.SwapReason.INITIAL, layer);
                } else if (ShortcutUtils.isBedrockPlayer(event.getPlayer().getUniqueId())) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> GeyserSwapper.openOriginSwapper(event.getPlayer(), PlayerSwapOriginEvent.SwapReason.INITIAL, false, false, layer), OriginsReborn.getInstance().getConfig().getInt("geyser.join-form-delay", 20));
                } else {
                    openOriginSwapper(event.getPlayer(), PlayerSwapOriginEvent.SwapReason.INITIAL, 0, 0, layer);
                }
            }
        }
    }

    public static void resetAttributes(Player player) {
        final double[] health = {player.getHealth()};
        for (Attribute attribute : Attribute.values()) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) continue;
            for (AttributeModifier modifier : instance.getModifiers()) {
                instance.removeModifier(modifier);
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> {
            AttributeInstance mh = player.getAttribute(OriginsReborn.getNMSInvoker().getMaxHealthAttribute());
            if (mh == null) return;
            double maxHealth = mh.getValue();
            health[0] = Math.min(maxHealth, health[0]);
            player.setHealth(health[0]);
        }, 10);
    }

    private static final Map<Player, PlayerSwapOriginEvent.SwapReason> lastSwapReasons = new HashMap<>();

    private static final Map<Player, Integer> lastJoinedTick = new HashMap<>();


//    loler

//    @EventHandler
//    public void onServerTickEnd(ServerTickEndEvent event) {
//        // Получаем настройки из ConfigOptions
//        int delay = options.getOriginSelectionDelayBeforeRequired();
//
//        // Проходим по всем онлайн-игрокам
//        for (Player player : Bukkit.getOnlinePlayers()) {
//            // Если для игрока не зафиксирован tick захода, устанавливаем его
//            lastJoinedTick.putIfAbsent(player, event.getTickNumber());
//
//            // Если игрок ещё не ждал необходимую задержку, переходим к следующему
//            if (Bukkit.getCurrentTick() - delay < lastJoinedTick.get(player)) {
//                continue;
//            }
//
//            // Получаем причину последней попытки смены origin
//            PlayerSwapOriginEvent.SwapReason reason = lastSwapReasons.getOrDefault(player, PlayerSwapOriginEvent.SwapReason.INITIAL);
//
//            // Если не разрешено выбирать origin в данном мире/ситуации
//            if (shouldDisallowSelection(player, reason)) {
//                player.setAllowFlight(AbilityRegister.canFly(player, true));
//                AbilityRegister.updateFlight(player, true);
//                resetAttributes(player);
//                continue;
//            }
//
//            // Если режим полёта не отключён в настройках
//            if (!options.isMiscSettingsDisableFlightStuff()) {
//                player.setAllowFlight(AbilityRegister.canFly(player, false));
//                AbilityRegister.updateFlight(player, false);
//            }
//
//            // Обновляем невидимость и атрибуты
//            player.setInvisible(AbilityRegister.isInvisible(player));
//            applyAttributeChanges(player);
//
//            // Получаем первый не выбранный слой
//            String layer = AddonLoader.getFirstUnselectedLayer(player);
//            if (layer == null) {
//                continue;
//            }
//
//            // Если игрок не находится в инвентаре сундука
//            if (player.getOpenInventory().getType() != InventoryType.CHEST) {
//                // Если для данного слоя задан дефолтный origin – устанавливаем его
//                if (AddonLoader.getDefaultOrigin(layer) != null) {
//                    setOrigin(player, AddonLoader.getDefaultOrigin(layer), PlayerSwapOriginEvent.SwapReason.INITIAL, false, layer);
//                }
//                // Если режим случайного выбора не включён и игрок не Bedrock,
//                // открываем меню выбора origin
//                if (!options.isOriginSelectionRandomise(layer) && !ShortcutUtils.isBedrockPlayer(player.getUniqueId())) {
//                    openOriginSwapper(player, reason, 0, 0, layer);
//                }
//            }
//        }
//    }

    public void startScheduledTask() {
        // Запускаем задачу, которая будет выполняться каждые 5 тиков (5L)
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllPlayers();
            }
        }.runTaskTimer(origins, 0L, 10L);
    }

    private void updateAllPlayers() {
        // Получаем задержку из настроек
        int delay = options.getOriginSelectionDelayBeforeRequired();

        // Проходим по всем онлайн-игрокам
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Если для игрока не зафиксирован тик захода, устанавливаем его
            lastJoinedTick.putIfAbsent(player, Bukkit.getCurrentTick());

            // Если игрок ещё не ждал необходимую задержку, пропускаем его
            if (Bukkit.getCurrentTick() - delay < lastJoinedTick.get(player)) {
                continue;
            }

            // Получаем причину последней попытки смены origin
            PlayerSwapOriginEvent.SwapReason reason = lastSwapReasons.getOrDefault(player, PlayerSwapOriginEvent.SwapReason.INITIAL);

            // Если не разрешено выбирать origin в данном мире/ситуации
            if (shouldDisallowSelection(player, reason)) {
                player.setAllowFlight(AbilityRegister.canFly(player, true));
                AbilityRegister.updateFlight(player, true);
                resetAttributes(player);
                continue;
            }

            // Если режим полёта не отключён в настройках
            if (!options.isMiscSettingsDisableFlightStuff()) {
                player.setAllowFlight(AbilityRegister.canFly(player, false));
                AbilityRegister.updateFlight(player, false);
            }

            // Обновляем невидимость и атрибуты
            player.setInvisible(AbilityRegister.isInvisible(player));
            applyAttributeChanges(player);

            // Получаем первый не выбранный слой
            String layer = AddonLoader.getFirstUnselectedLayer(player);
            if (layer == null) {
                continue;
            }

            // Если игрок не находится в инвентаре сундука
            if (player.getOpenInventory().getType() != InventoryType.CHEST) {
                // Если для данного слоя задан дефолтный origin – устанавливаем его
                if (AddonLoader.getDefaultOrigin(layer) != null) {
                    setOrigin(player, AddonLoader.getDefaultOrigin(layer), PlayerSwapOriginEvent.SwapReason.INITIAL, false, layer);
                }
                // Если режим случайного выбора не включён и игрок не Bedrock,
                // открываем меню выбора origin
                if (!options.isOriginSelectionRandomise(layer) && !ShortcutUtils.isBedrockPlayer(player.getUniqueId())) {
                    openOriginSwapper(player, reason, 0, 0, layer);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (hasNotSelectedAllOrigins(event.getPlayer())) event.setCancelled(true);
    }

    public static boolean shouldDisallowSelection(Player player, PlayerSwapOriginEvent.SwapReason reason) {
        try {
            return !AuthMeApi.getInstance().isAuthenticated(player);
        } catch (NoClassDefFoundError ignored) {}
        String worldId = player.getWorld().getName();
        return !AddonLoader.shouldOpenSwapMenu(player, reason) || options.getWorldsDisabledWorlds().contains(worldId);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (invulnerableMode.equalsIgnoreCase("INITIAL") && hasNotSelectedAllOrigins(player)) event.setCancelled(true);
            else if (invulnerableMode.equalsIgnoreCase("ON")) {
                ItemStack item = player.getOpenInventory().getTopInventory().getItem(1);
                if (item != null && item.getItemMeta() != null) {
                    if (item.getItemMeta().getPersistentDataContainer().has(originKey, PersistentDataType.STRING)) event.setCancelled(true);
                }
            }
        }
    }

    public boolean hasNotSelectedAllOrigins(Player player) {
        for (String layer : AddonLoader.layers) {
            if (getOrigin(player, layer) == null) return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerSwapOrigin(PlayerSwapOriginEvent event) {
        if (event.getNewOrigin() == null) return;

        String name = "default";
        if (OriginsReborn.getInstance().getConfig().contains("commands-on-origin.%s".formatted(name))) {
            for (String s : OriginsReborn.getInstance().getConfig().getStringList("commands-on-origin.%s".formatted(name))) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", event.getPlayer().getName()).replace("%uuid%", event.getPlayer().getUniqueId().toString()));
            }
        }

        name = event.getNewOrigin().getActualName().replace(" ", "_").toLowerCase();
        if (OriginsReborn.getInstance().getConfig().contains("commands-on-origin.%s".formatted(name))) {
            for (String s : OriginsReborn.getInstance().getConfig().getStringList("commands-on-origin.%s".formatted(name))) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", event.getPlayer().getName()).replace("%uuid%", event.getPlayer().getUniqueId().toString()));
            }
        }

        if (!options.isOriginSelectionAutoSpawnTeleport()) return;

        if (event.getReason() == PlayerSwapOriginEvent.SwapReason.INITIAL || event.getReason() == PlayerSwapOriginEvent.SwapReason.DIED) {
            Location loc = OriginsReborn.getNMSInvoker().getRespawnLocation(event.getPlayer());
            event.getPlayer().teleport(Objects.requireNonNullElseGet(loc, () -> getRespawnWorld(Collections.singletonList(event.getNewOrigin())).getSpawnLocation()));
        }
    }

    public static void selectRandomOrigin(Player player, PlayerSwapOriginEvent.SwapReason reason, String layer) {
        Origin origin = AddonLoader.getRandomOrigin(layer);
        setOrigin(player, origin, reason, shouldResetPlayer(reason), layer);
        openOriginSwapper(player, reason, AddonLoader.getOrigins(layer).indexOf(origin), 0, false, true, layer);
    }

    private final Map<Player, Set<PlayerRespawnEvent.RespawnFlag>> lastRespawnReasons = new HashMap<>();

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (OriginsReborn.getNMSInvoker().getRespawnLocation(event.getPlayer()) == null) {
            World world = getRespawnWorld(getOrigins(event.getPlayer()));
            event.setRespawnLocation(world.getSpawnLocation());
        }

        lastRespawnReasons.put(event.getPlayer(), event.getRespawnFlags());
    }

    @EventHandler
    public void onPlayerPostRespawn(PlayerPostRespawnEvent event) {
        if (lastRespawnReasons.get(event.getPlayer()).contains(PlayerRespawnEvent.RespawnFlag.END_PORTAL)) return;
        if (options.isOriginSelectionDeathOriginChange()) {
            for (String layer : AddonLoader.layers) {
                setOrigin(event.getPlayer(), null, PlayerSwapOriginEvent.SwapReason.DIED, false, layer);
                if (OriginsReborn.getInstance().getConfig().getBoolean("origin-selection.randomise.%s".formatted(layer))) {
                    selectRandomOrigin(event.getPlayer(), PlayerSwapOriginEvent.SwapReason.INITIAL, layer);
                } else openOriginSwapper(event.getPlayer(), PlayerSwapOriginEvent.SwapReason.INITIAL, 0, 0, layer);
            }
        }
    }

    public PlayerSwapOriginEvent.SwapReason getReason(ItemStack icon) {
        return PlayerSwapOriginEvent.SwapReason.get(icon.getItemMeta().getPersistentDataContainer().get(swapTypeKey, PersistentDataType.STRING));
    }

    /**
     * @deprecated Origins-Reborn now has a 'layer' system, allowing for multiple origins to be set at once
     */
    @Deprecated
    public static @Nullable Origin getOrigin(Player player) {
        return getOrigin(player, "origin");
    }

    public static @Nullable Origin getOrigin(Player player, String layer) {
        if (player.getPersistentDataContainer().has(originKey, PersistentDataType.STRING)) {
            return getStoredOrigin(player, layer);
        }
        PersistentDataContainer pdc = player.getPersistentDataContainer().get(originKey, PersistentDataType.TAG_CONTAINER);
        if (pdc == null) return null;
        String name = pdc.get(AddonLoader.layerKeys.get(layer), PersistentDataType.STRING);
        if (name == null) return null;
        return AddonLoader.getOrigin(name);
    }

    public static @Nullable Origin getStoredOrigin(Player player, String layer) {
        String oldOrigin = originFileConfiguration.getString(player.getUniqueId().toString(), "null");
        if (!oldOrigin.equals("null") && layer.equals("origin")) {
            if (!oldOrigin.contains("MemorySection")) {
                originFileConfiguration.set(player.getUniqueId() + "." + layer, oldOrigin);
                saveOrigins();
            }
        }
        String name = originFileConfiguration.getString(player.getUniqueId() + "." + layer, "null");
        return AddonLoader.getOrigin(name);
    }

    public static void loadOrigins(Player player) {
        player.getPersistentDataContainer().remove(originKey);
        for (String layer : AddonLoader.layers) {
            Origin origin = getStoredOrigin(player, layer);
            if (origin == null) continue;
            PersistentDataContainer pdc = player.getPersistentDataContainer().get(originKey, PersistentDataType.TAG_CONTAINER);
            if (pdc == null) pdc = player.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
            pdc.set(AddonLoader.layerKeys.get(layer), PersistentDataType.STRING, origin.getName().toLowerCase());
            player.getPersistentDataContainer().set(originKey, PersistentDataType.TAG_CONTAINER, pdc);
        }
    }

    public static List<Origin> getOrigins(Player player) {
        List<Origin> origins = new ArrayList<>();
        for (String layer : AddonLoader.layers) {
            Origin o = getOrigin(player, layer);
            if (o != null) origins.add(o);
        }
        return origins;
    }

    public static FileConfiguration getUsedOriginFileConfiguration() {
        return usedOriginFileConfiguration;
    }

    /**
     * @deprecated Origins-Reborn now has a 'layer' system, allowing for multiple origins to be set at once
     */
    @Deprecated
    public static void setOrigin(Player player, @Nullable Origin origin, PlayerSwapOriginEvent.SwapReason reason, boolean resetPlayer) {
        setOrigin(player, origin, reason, resetPlayer, "origin");
    }

    @SuppressWarnings("deprecation")
    public static void setOrigin(Player player, @Nullable Origin origin, PlayerSwapOriginEvent.SwapReason reason, boolean resetPlayer, String layer) {
        PlayerSwapOriginEvent swapOriginEvent = new PlayerSwapOriginEvent(player, reason, resetPlayer, getOrigin(player, layer), origin);
        if (!swapOriginEvent.callEvent()) return;
        if (swapOriginEvent.getNewOrigin() == null) {
            originFileConfiguration.set(player.getUniqueId() + "." + layer, null);
            saveOrigins();
            resetPlayer(player, swapOriginEvent.isResetPlayer());
            return;
        }
        if (swapOriginEvent.getNewOrigin().getTeam() != null) {
            swapOriginEvent.getNewOrigin().getTeam().addPlayer(player);
        }
        OriginsReborn.getCooldowns().resetCooldowns(player);
        originFileConfiguration.set(player.getUniqueId() + "." + layer, swapOriginEvent.getNewOrigin().getName().toLowerCase());
        saveOrigins();
        List<String> usedOrigins = new ArrayList<>(usedOriginFileConfiguration.getStringList(player.getUniqueId().toString()));
        usedOrigins.add(swapOriginEvent.getNewOrigin().getName().toLowerCase());
        usedOriginFileConfiguration.set(player.getUniqueId().toString(), usedOrigins);
        saveUsedOrigins();
        resetPlayer(player, swapOriginEvent.isResetPlayer());
        loadOrigins(player);
    }

    private static File originFile;
    private static FileConfiguration originFileConfiguration;

    private static File usedOriginFile;
    private static FileConfiguration usedOriginFileConfiguration;

    public static FileConfiguration getOriginFileConfiguration() {
        return originFileConfiguration;
    }

    private final String invulnerableMode;

    public OriginSwapper() {
        invulnerableMode = OriginsReborn.getInstance().getConfig().getString("origin-selection.invulnerable-mode", "OFF");

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

        usedOriginFile = new File(OriginsReborn.getInstance().getDataFolder(), "used-origins.yml");
        if (!usedOriginFile.exists()) {
            boolean ignored = usedOriginFile.getParentFile().mkdirs();
            OriginsReborn.getInstance().saveResource("used-origins.yml", false);
        }
        usedOriginFileConfiguration = new YamlConfiguration();
        try {
            usedOriginFileConfiguration.load(usedOriginFile);
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

    public static void saveUsedOrigins() {
        try {
            usedOriginFileConfiguration.save(usedOriginFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class LineData {
        // TODO Deprecate this and replace it with 'description' and 'title' methods inside VisibleAbility which returns the specified value as a fallback
        public static List<LineComponent> makeLineFor(String text, LineComponent.LineType type) {
            List<LineComponent> list = new ArrayList<>();

            // Разбиваем исходный текст на две части по первому символу новой строки
            String[] lines = text.split("\n", 2);
            String firstLine = lines[0];
            StringBuilder otherPart = new StringBuilder();
            if (lines.length > 1) {
                otherPart.append(lines[1]);
            }

            // Если строка содержит пробелы и её ширина превышает 140, пытаемся разбить её по словам
            if (firstLine.indexOf(' ') >= 0 && getWidth(firstLine) > 140) {
                String[] tokens = firstLine.split(" ");
                StringBuilder firstPart = new StringBuilder(tokens[0]);
                int currentWidth = getWidth(firstPart.toString());
                int spaceWidth = getWidth(" ");
                // Обрабатываем последующие слова
                for (int i = 1; i < tokens.length; i++) {
                    int tokenWidth = getWidth(tokens[i]);
                    // Если добавление следующего слова (с пробелом) не превышает 140, добавляем его
                    if (currentWidth + spaceWidth + tokenWidth <= 140) {
                        firstPart.append(' ').append(tokens[i]);
                        currentWidth += spaceWidth + tokenWidth;
                    } else {
                        // Иначе оставшиеся слова добавляем в otherPart
                        if (!otherPart.isEmpty()) {
                            otherPart.append(' ');
                        }
                        otherPart.append(tokens[i]);
                    }
                }
                firstLine = firstPart.toString();
            }

            // Если тип строки DESCRIPTION, добавляем префикс \uF00A
            if (type == LineComponent.LineType.DESCRIPTION) {
                firstLine = '\uF00A' + firstLine;
            }

            // Формируем строки для вывода:
            // Для каждого символа в firstLine добавляем его в результат, затем вставляем разделитель \uF000,
            // а в rawResult пропускаем символ \uF00A
            StringBuilder result = new StringBuilder();
            StringBuilder rawResult = new StringBuilder();
            for (int i = 0, len = firstLine.length(); i < len; i++) {
                char c = firstLine.charAt(i);
                result.append(c);
                if (c != '\uF00A') {
                    rawResult.append(c);
                }
                result.append('\uF000');
            }
            rawResult.append(' ');

            // Собираем компонент с нужными цветом и текстом, добавляя к нему инвертированный текст
            String finalText = firstLine;
            Component comp = Component.text(result.toString())
                    .color(type == LineComponent.LineType.TITLE
                            ? NamedTextColor.WHITE
                            : TextColor.fromHexString("#CACACA"))
                    .append(Component.text(getInverse(finalText)));
            list.add(new LineComponent(comp, type, rawResult.toString()));

            // Рекурсивно обрабатываем оставшуюся часть, если она не пуста
            if (!otherPart.isEmpty()) {
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
            private final String rawText;
            private final boolean empty;

            public boolean isEmpty() {
                return empty;
            }

            public LineType getType() {
                return type;
            }

            public LineComponent(Component component, LineType type, String rawText) {
                this.component = component;
                this.type = type;
                this.rawText = rawText;
                this.empty = false;
            }

            public LineComponent() {
                this.type = LineType.DESCRIPTION;
                this.component = Component.empty();
                this.rawText = "";
                this.empty = true;
            }

            public String getRawText() {
                return rawText;
            }

            public Component getComponent(int lineNumber) {
                @Subst("minecraft:text_line_0") String formatted = "minecraft:%stext_line_%s".formatted(type == LineType.DESCRIPTION ? "" : "title_", lineNumber);
                return applyFont(component, Key.key(formatted));
            }
        }
        private final List<LineComponent> lines;

        public LineData(Origin origin) {
            lines = new ArrayList<>();
            lines.addAll(makeLineFor(origin.getDescription(), LineComponent.LineType.DESCRIPTION));
            List<VisibleAbility> visibleAbilities = origin.getVisibleAbilities();
            int size = visibleAbilities.size();
            int count = 0;
            if (size > 0) lines.add(new LineComponent());
            for (VisibleAbility visibleAbility : visibleAbilities) {
                count++;
                lines.addAll(visibleAbility.getUsedTitle());
                lines.addAll(visibleAbility.getUsedDescription());
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

        public List<LineComponent> getRawLines() {
            return lines;
        }
    }

    public static class BooleanPDT implements PersistentDataType<Byte, Boolean> {
        public static BooleanPDT BOOLEAN = new BooleanPDT();

        @Override
        public @NotNull Class<Byte> getPrimitiveType() {
            return Byte.class;
        }

        @Override
        public @NotNull Class<Boolean> getComplexType() {
            return Boolean.class;
        }

        @Override
        public @NotNull Byte toPrimitive(@NotNull Boolean aBoolean, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
            return (byte) (aBoolean ? 1 : 0);
        }

        @Override
        public @NotNull Boolean fromPrimitive(@NotNull Byte aByte, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
            return aByte >= 1;
        }
    }
}
