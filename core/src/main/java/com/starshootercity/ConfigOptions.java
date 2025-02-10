package com.starshootercity;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;

/**
 * Класс для хранения и обновления значений опций из конфигурационного файла.
 * Реализован по паттерну Singleton — гарантируется наличие только одного экземпляра.
 */
public class ConfigOptions {

    // Единственный экземпляр класса
    private static ConfigOptions instance;

    // Поля настроек
    private String defaultOrigin; // "origin-selection.default_origin" (по умолчанию "NONE")
    private boolean randomOptionEnabled; // "origin-selection.random-option.enabled"
    private List<String> randomOptionExclude; // "origin-selection.random-option.exclude"

    private boolean swapCommandResetPlayer;
    private int swapCommandVaultDefaultCost; // "swap-command.vault.default-cost"
    private int swapCommandVaultCost; // "swap-command.vault.cost"
    private boolean swapCommandVaultPermanentPurchases; // "swap-command.vault.permanent-purchases"
    private String swapCommandVaultBypassPermission; // "swap-command.vault.bypass-permission"
    private String swapCommandVaultCurrencySymbol; // "swap-command.vault.currency-symbol"

    private String screenTitleBackground; // "origin-selection.screen-title.background"
    private String screenTitlePrefix; // "origin-selection.screen-title.prefix"
    private String screenTitleSuffix; // "origin-selection.screen-title.suffix"
    private int originSelectionScrollAmount; // "origin-selection.scroll-amount"
    private String originSelectionInvulnerableMode; // "origin-selection.invulnerable-mode"

    private boolean originSelectionAutoSpawnTeleport; // "origin-selection.auto-spawn-teleport"
    private boolean originSelectionDeathOriginChange; // "origin-selection.death-origin-change"

    private String worldsWorld; // "worlds.world"
    private List<String> worldsDisabledWorlds; // "worlds.disabled-worlds"

    private int geyserJoinFormDelay; // "geyser.join-form-delay"
    private boolean miscSettingsDisableFlightStuff; // "misc-settings.disable-flight-stuff"

    // Новое поле: задержка перед обязательным выбором origin
    private int originSelectionDelayBeforeRequired; // "origin-selection.delay-before-required"

    private boolean orbOfOriginResetPlayer;

    // Приватный конструктор предотвращает создание экземпляров извне
    private ConfigOptions() {
        update();
    }

    /**
     * Возвращает единственный экземпляр ConfigOptions.
     * Если экземпляр ещё не создан, он будет создан и инициализирован.
     *
     * @return экземпляр ConfigOptions
     */
    public static synchronized ConfigOptions getInstance() {
        if (instance == null) {
            instance = new ConfigOptions();
        }
        return instance;
    }

    /**
     * Обновляет все значения из конфигурации плагина.
     * Вызывайте этот метод при старте плагина и при перезагрузке конфигурации.
     */
    public void update() {
        FileConfiguration config = OriginsReborn.getInstance().getConfig();

        defaultOrigin = config.getString("origin-selection.default_origin", "NONE");
        randomOptionEnabled = config.getBoolean("origin-selection.random-option.enabled", false);
        randomOptionExclude = config.getStringList("origin-selection.random-option.exclude");

        swapCommandResetPlayer = config.getBoolean("swap-command.reset-player");
        swapCommandVaultDefaultCost = config.getInt("swap-command.vault.default-cost", 1000);
        swapCommandVaultCost = config.getInt("swap-command.vault.cost", 1000);
        swapCommandVaultPermanentPurchases = config.getBoolean("swap-command.vault.permanent-purchases", false);
        swapCommandVaultBypassPermission = config.getString("swap-command.vault.bypass-permission", "originsreborn.costbypass");
        swapCommandVaultCurrencySymbol = config.getString("swap-command.vault.currency-symbol", "$");

        screenTitleBackground = config.getString("origin-selection.screen-title.background", "");
        screenTitlePrefix = config.getString("origin-selection.screen-title.prefix", "");
        screenTitleSuffix = config.getString("origin-selection.screen-title.suffix", "");
        originSelectionScrollAmount = config.getInt("origin-selection.scroll-amount", 1);
        originSelectionInvulnerableMode = config.getString("origin-selection.invulnerable-mode", "OFF");

        originSelectionAutoSpawnTeleport = config.getBoolean("origin-selection.auto-spawn-teleport", false);
        originSelectionDeathOriginChange = config.getBoolean("origin-selection.death-origin-change", false);

        worldsWorld = config.getString("worlds.world", "world");
        worldsDisabledWorlds = config.getStringList("worlds.disabled-worlds");

        geyserJoinFormDelay = config.getInt("geyser.join-form-delay", 20);
        miscSettingsDisableFlightStuff = config.getBoolean("misc-settings.disable-flight-stuff", false);

        // Чтение задержки перед выбором origin
        originSelectionDelayBeforeRequired = config.getInt("origin-selection.delay-before-required", 0);

        orbOfOriginResetPlayer = config.getBoolean("orb-of-origin.reset-player");
    }

    // Геттеры для доступа к настройкам

    public String getDefaultOrigin() {
        return defaultOrigin;
    }

    public boolean isRandomOptionEnabled() {
        return randomOptionEnabled;
    }

    public List<String> getRandomOptionExclude() {
        return randomOptionExclude;
    }

    public int getSwapCommandVaultDefaultCost() {
        return swapCommandVaultDefaultCost;
    }

    public int getSwapCommandVaultCost() {
        return swapCommandVaultCost;
    }

    public boolean isSwapCommandVaultPermanentPurchases() {
        return swapCommandVaultPermanentPurchases;
    }

    public boolean isSwapCommandResetPlayer() {
        return swapCommandResetPlayer;
    }

    public String getSwapCommandVaultBypassPermission() {
        return swapCommandVaultBypassPermission;
    }

    public String getSwapCommandVaultCurrencySymbol() {
        return swapCommandVaultCurrencySymbol;
    }

    public String getScreenTitleBackground() {
        return screenTitleBackground;
    }

    public String getScreenTitlePrefix() {
        return screenTitlePrefix;
    }

    public String getScreenTitleSuffix() {
        return screenTitleSuffix;
    }

    public int getOriginSelectionScrollAmount() {
        return originSelectionScrollAmount;
    }

    public String getOriginSelectionInvulnerableMode() {
        return originSelectionInvulnerableMode;
    }

    public boolean isOriginSelectionAutoSpawnTeleport() {
        return originSelectionAutoSpawnTeleport;
    }

    public boolean isOriginSelectionDeathOriginChange() {
        return originSelectionDeathOriginChange;
    }

    public String getWorldsWorld() {
        return worldsWorld;
    }

    public List<String> getWorldsDisabledWorlds() {
        return worldsDisabledWorlds;
    }

    public int getGeyserJoinFormDelay() {
        return geyserJoinFormDelay;
    }

    public boolean isMiscSettingsDisableFlightStuff() {
        return miscSettingsDisableFlightStuff;
    }

    public int getOriginSelectionDelayBeforeRequired() {
        return originSelectionDelayBeforeRequired;
    }

    public boolean isOrbOfOriginResetPlayer() {
        return orbOfOriginResetPlayer;
    }

    /**
     * Проверяет, включён ли режим случайного выбора для данного слоя.
     *
     * @param layer название слоя (например, "origin")
     * @return true, если для данного слоя включена опция случайного выбора, иначе false.
     */
    public boolean isOriginSelectionRandomise(String layer) {
        return OriginsReborn.getInstance().getConfig().getBoolean("origin-selection.randomise." + layer, false);
    }
}
