package com.starshootercity

/**
 * Класс для хранения и обновления значений опций из конфигурационного файла.
 * Реализован по паттерну Singleton — гарантируется наличие только одного экземпляра.
 */
class ConfigsOptions private constructor() {
    // Поля настроек с не-null значениями
    var defaultOrigin: String = "NONE"
        private set
    var isRandomOptionEnabled: Boolean = false
        private set
    var randomOptionExclude: MutableList<String> = mutableListOf()
        private set

    var isSwapCommandResetPlayer: Boolean = false
        private set
    var swapCommandVaultDefaultCost: Int = 1000
        private set
    var swapCommandVaultCost: Int = 1000
        private set
    var isSwapCommandVaultPermanentPurchases: Boolean = false
        private set
    var swapCommandVaultBypassPermission: String = "originsreborn.costbypass"
        private set
    var swapCommandVaultCurrencySymbol: String = "$"
        private set

    var screenTitleBackground: String = ""
        private set
    var screenTitlePrefix: String = ""
        private set
    var screenTitleSuffix: String = ""
        private set
    var originSelectionScrollAmount: Int = 1
        private set
    var originSelectionInvulnerableMode: String = "OFF"
        private set

    var isOriginSelectionAutoSpawnTeleport: Boolean = false
        private set
    var isOriginSelectionDeathOriginChange: Boolean = false
        private set

    var worldsWorld: String = "world"
        private set
    var worldsDisabledWorlds: MutableList<String> = mutableListOf()
        private set

    var geyserJoinFormDelay: Int = 20
        private set
    var isMiscSettingsDisableFlightStuff: Boolean = false
        private set

    var originSelectionDelayBeforeRequired: Int = 0
        private set

    var isOrbOfOriginResetPlayer: Boolean = false
        private set

    // Приватный конструктор предотвращает создание экземпляров извне
    init {
        update()
    }

    /**
     * Обновляет все значения из конфигурации плагина.
     * Вызывайте этот метод при старте плагина и при перезагрузке конфигурации.
     */
    fun update() {
        val config = OriginsReborn.instance.getConfig()

        defaultOrigin = config.getString("origin-selection.default_origin", "NONE") ?: "NONE"
        isRandomOptionEnabled = config.getBoolean("origin-selection.random-option.enabled", false)
        randomOptionExclude = config.getStringList("origin-selection.random-option.exclude").toMutableList()

        isSwapCommandResetPlayer = config.getBoolean("swap-command.reset-player")
        swapCommandVaultDefaultCost = config.getInt("swap-command.vault.default-cost", 1000)
        swapCommandVaultCost = config.getInt("swap-command.vault.cost", 1000)
        isSwapCommandVaultPermanentPurchases = config.getBoolean("swap-command.vault.permanent-purchases", false)
        swapCommandVaultBypassPermission =
            config.getString("swap-command.vault.bypass-permission", "originsreborn.costbypass") ?: "originsreborn.costbypass"
        swapCommandVaultCurrencySymbol = config.getString("swap-command.vault.currency-symbol", "$") ?: "$"

        screenTitleBackground = config.getString("origin-selection.screen-title.background", "") ?: ""
        screenTitlePrefix = config.getString("origin-selection.screen-title.prefix", "") ?: ""
        screenTitleSuffix = config.getString("origin-selection.screen-title.suffix", "") ?: ""
        originSelectionScrollAmount = config.getInt("origin-selection.scroll-amount", 1)
        originSelectionInvulnerableMode = config.getString("origin-selection.invulnerable-mode", "OFF") ?: "OFF"

        isOriginSelectionAutoSpawnTeleport = config.getBoolean("origin-selection.auto-spawn-teleport", false)
        isOriginSelectionDeathOriginChange = config.getBoolean("origin-selection.death-origin-change", false)

        worldsWorld = config.getString("worlds.world", "world") ?: "world"
        worldsDisabledWorlds = config.getStringList("worlds.disabled-worlds").toMutableList()

        geyserJoinFormDelay = config.getInt("geyser.join-form-delay", 20)
        isMiscSettingsDisableFlightStuff = config.getBoolean("misc-settings.disable-flight-stuff", false)

        originSelectionDelayBeforeRequired = config.getInt("origin-selection.delay-before-required", 0)

        isOrbOfOriginResetPlayer = config.getBoolean("orb-of-origin.reset-player")
    }

    /**
     * Проверяет, включён ли режим случайного выбора для данного слоя.
     *
     * @param layer название слоя (например, "origin")
     * @return true, если для данного слоя включена опция случайного выбора, иначе false.
     */
    fun isOriginSelectionRandomise(layer: String): Boolean {
        return OriginsReborn.instance.config.getBoolean("origin-selection.randomize.$layer", false)
    }

    companion object {
        // Единственный экземпляр класса
        @JvmStatic
        val instance: ConfigsOptions by lazy { ConfigsOptions() }
    }
}
