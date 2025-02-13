package com.starshootercity

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import com.starshootercity.AddonLoader.getDefaultOrigin
import com.starshootercity.AddonLoader.getFirstOrigin
import com.starshootercity.AddonLoader.getFirstUnselectedLayer
import com.starshootercity.AddonLoader.getOrigin
import com.starshootercity.AddonLoader.getOriginByFilename
import com.starshootercity.AddonLoader.getOrigins
import com.starshootercity.AddonLoader.getRandomOrigin
import com.starshootercity.AddonLoader.getTextFor
import com.starshootercity.AddonLoader.shouldOpenSwapMenu
import com.starshootercity.OriginSwapper.LineData.LineComponent.LineType
import com.starshootercity.OriginsReborn.Companion.getCooldowns
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.abilities.*
import com.starshootercity.commands.OriginCommand
import com.starshootercity.events.PlayerSwapOriginEvent
import com.starshootercity.events.PlayerSwapOriginEvent.SwapReason
import com.starshootercity.geysermc.GeyserSwapper
import com.starshootercity.packetsenders.NMSInvoker
import fr.xephi.authme.api.v3.AuthMeApi
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.intellij.lang.annotations.Subst
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.max
import kotlin.math.min

class OriginSwapper : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val item = event.whoClicked.openInventory.getItem(1)
        if (item != null) {
            val meta = item.itemMeta
            if (meta == null) return
            val itemContainer = meta.persistentDataContainer
            if (itemContainer.has<Byte?, Boolean?>(displayKey, BooleanPDT.Companion.BOOLEAN)) {
                event.isCancelled = true
            }
            val layer = itemContainer.getOrDefault<String?, String>(layerKey, PersistentDataType.STRING, "origin")
            val player = event.whoClicked
            if (player is Player) {
                val currentItem = event.getCurrentItem()
                if (currentItem == null || currentItem.itemMeta == null) return
                val currentItemMeta = currentItem.itemMeta
                val currentItemContainer = currentItemMeta.persistentDataContainer
                val page = currentItemContainer.get<Int?, Int?>(pageSetKey, PersistentDataType.INTEGER)
                if (page != null) {
                    val cost = currentItemContainer.getOrDefault<Byte?, Boolean?>(costKey, BooleanPDT.Companion.BOOLEAN, false)
                    val allowUnchoosable = currentItemContainer.getOrDefault<Byte?, Boolean?>(
                        displayOnlyKey,
                        BooleanPDT.Companion.BOOLEAN,
                        false
                    )
                    val scroll = currentItemContainer.get<Int?, Int?>(pageScrollKey, PersistentDataType.INTEGER)
                    if (scroll == null) return
                    player.playSound(player.location, Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1f, 1f)
                    openOriginSwapper(player, getReason(item), page, scroll, cost, allowUnchoosable, layer)
                }
                if (currentItemContainer.has<Byte?, Boolean?>(confirmKey, BooleanPDT.Companion.BOOLEAN)) {
                    var amount: Int = options.swapCommandVaultCost
                    if (!player.hasPermission(options.swapCommandVaultBypassPermission) && currentItemContainer.has<Int?, Int?>(
                            costsCurrencyKey, PersistentDataType.INTEGER
                        )
                    ) {
                        amount = currentItemContainer.getOrDefault<Int?, Int?>(
                            costsCurrencyKey,
                            PersistentDataType.INTEGER,
                            amount
                        )
                        if (!instance.economy!!.has(player, amount.toDouble())) {
                            return
                        } else {
                            origins.economy!!.withdrawPlayer(player, amount.toDouble())
                        }
                    }
                    val originName = item.itemMeta.persistentDataContainer
                        .get<String?, String?>(originKey, PersistentDataType.STRING)
                    if (originName == null) return
                    val origin = if (originName.equals("random", ignoreCase = true)) {
                        val excludedOrigins: MutableList<String> = options.randomOptionExclude

                        val origins: MutableList<Origin?> = ArrayList<Origin?>(getOrigins(layer))
                        origins.removeIf { origin1: Origin? -> excludedOrigins.contains(origin1!!.getName()) }
                        origins.removeIf { origin1: Origin? -> origin1!!.isUnchoosable(player) }
                        if (origins.isEmpty()) {
                            getFirstOrigin(layer)
                        } else {
                            origins[random.nextInt(origins.size)]
                        }
                    } else {
                        getOrigin(originName)
                    }
                    val reason = getReason(item)
                    player.playSound(player.location, Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1f, 1f)
                    player.closeInventory()

                    if (reason == SwapReason.ORB_OF_ORIGIN) orbCooldown.put(player, System.currentTimeMillis())
                    val resetPlayer: Boolean = shouldResetPlayer(reason)
                    if (origin!!.isUnchoosable(player)) {
                        openOriginSwapper(player, reason, 0, 0, layer)
                        return
                    }
                    getCooldowns().setCooldown(player, OriginCommand.key)
                    setOrigin(player, origin, reason, resetPlayer, layer)
                } else if (currentItemContainer.has<Byte?, Boolean?>(
                        closeKey,
                        BooleanPDT.Companion.BOOLEAN
                    )
                ) event.whoClicked.closeInventory()
            }
        }
    }

    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, Runnable { resetAttributes(event.getPlayer()) }, 5)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.getPlayer()
        loadOrigins(player)
        resetAttributes(player)
        lastJoinedTick.put(player, Bukkit.getCurrentTick())
        if (player.openInventory.type == InventoryType.CHEST) {
            return
        }
        val config: FileConfiguration = origins.getConfig()
        for (layer in AddonLoader.layers) {
            val origin: Origin? = getOrigin(player, layer!!)

            if (origin != null) {
                if (origin.team == null) {
                    return
                }
                origin.team.addPlayer(player)
            } else {
                val defaultOrigin = getDefaultOrigin(layer)
                if (defaultOrigin != null) {
                    setOrigin(player, defaultOrigin, SwapReason.INITIAL, false, layer)
                } else if (config.getBoolean("origin-selection.randomise.$layer")) {
                    selectRandomOrigin(player, SwapReason.INITIAL, layer)
                } else if (ShortcutUtils.isBedrockPlayer(player.uniqueId)) {
                    val delay = config.getInt("geyser.join-form-delay", 20)
                    Bukkit.getScheduler().scheduleSyncDelayedTask(
                        origins,
                        Runnable { GeyserSwapper.openOriginSwapper(player, SwapReason.INITIAL, false, false, layer) },
                        delay.toLong()
                    )
                } else {
                    openOriginSwapper(player, SwapReason.INITIAL, 0, 0, layer)
                }
            }
        }
    }


    fun startScheduledTask() {
        // Запускаем задачу, которая будет выполняться каждые 5 тиков (5L)
        object : BukkitRunnable() {
            override fun run() {
                updateAllPlayers()
            }
        }.runTaskTimer(origins, 0L, 10L)
    }

    private fun updateAllPlayers() {
        val delay: Int = options.originSelectionDelayBeforeRequired

        for (player in Bukkit.getOnlinePlayers()) {
            lastJoinedTick.putIfAbsent(player, Bukkit.getCurrentTick())
            if (Bukkit.getCurrentTick() - delay < lastJoinedTick[player]!!) {
                continue
            }
            val reason = lastSwapReasons.getOrDefault(player, SwapReason.INITIAL)
            if (shouldDisallowSelection(player, reason)) {
                player.allowFlight = AbilityRegister.canFly(player, true)
                AbilityRegister.updateFlight(player, true)
                resetAttributes(player)
                continue
            }
            if (!options.isMiscSettingsDisableFlightStuff) {
                player.allowFlight = AbilityRegister.canFly(player, false)
                AbilityRegister.updateFlight(player, false)
            }

            player.isInvisible = AbilityRegister.isInvisible(player)
            applyAttributeChanges(player)
            val layer = getFirstUnselectedLayer(player)
            if (layer == null) {
                continue
            }
            if (player.openInventory.type != InventoryType.CHEST) {
                if (getDefaultOrigin(layer) != null) {
                    setOrigin(player, getDefaultOrigin(layer), SwapReason.INITIAL, false, layer)
                }
                if (!options.isOriginSelectionRandomise(layer) && !ShortcutUtils.isBedrockPlayer(player.uniqueId)) {
                    openOriginSwapper(player, reason, 0, 0, layer)
                }
            }
        }
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (hasNotSelectedAllOrigins(event.getPlayer())) event.isCancelled = true
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        val player = event.getEntity()
        if (player is Player) {
            if (invulnerableMode.equals(
                    "INITIAL",
                    ignoreCase = true
                ) && hasNotSelectedAllOrigins(player)
            ) event.isCancelled = true
            else if (invulnerableMode.equals("ON", ignoreCase = true)) {
                val item: ItemStack? = player.openInventory.topInventory.getItem(1)
                if (item != null && item.itemMeta != null) {
                    if (item.itemMeta.persistentDataContainer
                            .has<String?, String?>(originKey, PersistentDataType.STRING)
                    ) event.isCancelled = true
                }
            }
        }
    }

    fun hasNotSelectedAllOrigins(player: Player): Boolean {
        for (layer in AddonLoader.layers) {
            if (getOrigin(player, layer!!) == null) return true
        }
        return false
    }

    @EventHandler
    fun onPlayerSwapOrigin(event: PlayerSwapOriginEvent) {
        val player = event.getPlayer()
        val newOrigin = event.newOrigin ?: return

        // Выполняем команды для дефолтного источника (default)
        executeCommands("default", player)

        // Выполняем команды для нового источника
        val originName = newOrigin.getActualName().replace(" ", "_").lowercase(Locale.getDefault())
        executeCommands(originName, player)

        if (!options.isOriginSelectionAutoSpawnTeleport) return

        if (event.reason == SwapReason.INITIAL || event.reason == SwapReason.DIED) {
            // Если локация из nmsInvoker не найдена, используем спаун-локацию из respawnWorld
            val loc = nmsInvoker.getRespawnLocation(player)
                ?: getRespawnWorld(listOf(newOrigin)).spawnLocation
            player.teleport(loc)
        }
    }

    private fun executeCommands(originName: String, player: Player) {
        val configPath = "commands-on-origin.$originName"
        if (instance.getConfig().contains(configPath)) {
            instance.getConfig().getStringList(configPath).forEach { command ->
                Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    command.replace("%player%", player.name)
                        .replace("%uuid%", player.uniqueId.toString())
                )
            }
        }
    }


    private val lastRespawnReasons: MutableMap<Player?, MutableSet<PlayerRespawnEvent.RespawnFlag?>?> =
        HashMap<Player?, MutableSet<PlayerRespawnEvent.RespawnFlag?>?>()

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        if (nmsInvoker.getRespawnLocation(event.getPlayer()) == null) {
            val world: World = getRespawnWorld(getOrigins(event.getPlayer()))
            event.setRespawnLocation(world.spawnLocation)
        }

        lastRespawnReasons.put(event.getPlayer(), event.respawnFlags)
    }

    @EventHandler
    fun onPlayerPostRespawn(event: PlayerPostRespawnEvent) {
        if (lastRespawnReasons[event.getPlayer()]!!.contains(PlayerRespawnEvent.RespawnFlag.END_PORTAL)) return
        if (options.isOriginSelectionDeathOriginChange) {
            for (layer in AddonLoader.layers) {
                setOrigin(event.getPlayer(), null, SwapReason.DIED, false, layer!!)
                if (instance.getConfig().getBoolean("origin-selection.randomise.$layer")) {
                    selectRandomOrigin(event.getPlayer(), SwapReason.INITIAL, layer)
                } else openOriginSwapper(event.getPlayer(), SwapReason.INITIAL, 0, 0, layer)
            }
        }
    }

    fun getReason(icon: ItemStack): SwapReason {
        return SwapReason.get(
            icon.itemMeta.persistentDataContainer
                .get<String?, String?>(swapTypeKey, PersistentDataType.STRING)
        )
    }

    private val invulnerableMode: String = instance.getConfig().getString("origin-selection.invulnerable-mode", "OFF")!!

    init {

        originFile = File(instance.dataFolder, "selected-origins.yml")
        if (!originFile.exists()) {
            val ignored: Boolean = originFile.getParentFile().mkdirs()
            instance.saveResource("selected-origins.yml", false)
        }
        originFileConfiguration = YamlConfiguration()
        try {
            originFileConfiguration.load(originFile)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InvalidConfigurationException) {
            throw RuntimeException(e)
        }

        usedOriginFile = File(instance.getDataFolder(), "used-origins.yml")
        if (!usedOriginFile.exists()) {
            val ignored: Boolean = usedOriginFile.getParentFile().mkdirs()
            instance.saveResource("used-origins.yml", false)
        }
        usedOriginFileConfiguration = YamlConfiguration()
        try {
            usedOriginFileConfiguration.load(usedOriginFile)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InvalidConfigurationException) {
            throw RuntimeException(e)
        }
    }

    class LineData {
        class LineComponent {
            enum class LineType {
                TITLE,
                DESCRIPTION
            }

            private val component: Component?
            @JvmField
            val type: LineType?
            @JvmField
            val rawText: String?
            val isEmpty: Boolean

            constructor(component: Component?, type: LineType?, rawText: String?) {
                this.component = component
                this.type = type
                this.rawText = rawText
                this.isEmpty = false
            }

            constructor() {
                this.type = LineType.DESCRIPTION
                this.component = Component.empty()
                this.rawText = ""
                this.isEmpty = true
            }

            fun getComponent(lineNumber: Int): Component {
                @Subst("minecraft:text_line_0")
                val formatted = "minecraft:${if (type == LineType.DESCRIPTION) "" else "title_"}text_line_$lineNumber"
                return applyFont(component, Key.key(formatted))
            }
        }

        val rawLines: MutableList<LineComponent?>

        constructor(origin: Origin) {
            this.rawLines = ArrayList<LineComponent?>()
            rawLines.addAll(makeLineFor(origin.getDescription(), LineType.DESCRIPTION))
            val visibleAbilities: List<VisibleAbility> = origin.getVisibleAbilities()
            val size = visibleAbilities.size
            var count = 0
            if (size > 0) rawLines.add(LineComponent())
            for (visibleAbility in visibleAbilities) {
                count++
                rawLines.addAll(visibleAbility.getUsedTitle())
                rawLines.addAll(visibleAbility.getUsedDescription())
                if (count < size) rawLines.add(LineComponent())
            }
        }

        constructor(lines: MutableList<LineComponent?>) {
            this.rawLines = lines
        }

        fun getLines(startingPoint: Int): MutableList<Component> {
            val resultLines: MutableList<Component> = ArrayList<Component>()
            var i = startingPoint
            while (i < startingPoint + 6 && i < rawLines.size) {
                resultLines.add(rawLines[i]!!.getComponent(i - startingPoint))
                i++
            }
            return resultLines
        }

        companion object {
            // TODO Deprecate this and replace it with 'description' and 'title' methods inside VisibleAbility which returns the specified value as a fallback
            @JvmStatic
            fun makeLineFor(text: String, type: LineType?): MutableList<LineComponent?> {
                val list: MutableList<LineComponent?> = ArrayList<LineComponent?>()

                val lines = text.split("\n".toRegex(), limit = 2).toTypedArray()
                var firstLine = lines[0]
                val otherPart = StringBuilder()
                if (lines.size > 1) {
                    otherPart.append(lines[1])
                }

                if (firstLine.indexOf(' ') >= 0 && getWidth(firstLine) > 140) {
                    val tokens: Array<String?> =
                        firstLine.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val firstPart = StringBuilder(tokens[0])
                    var currentWidth: Int = getWidth(firstPart.toString())
                    val spaceWidth: Int = getWidth(" ")
                    for (i in 1..<tokens.size) {
                        val tokenWidth: Int = getWidth(tokens[i]!!)
                        if (currentWidth + spaceWidth + tokenWidth <= 140) {
                            firstPart.append(' ').append(tokens[i])
                            currentWidth += spaceWidth + tokenWidth
                        } else {
                            if (!otherPart.isEmpty()) {
                                otherPart.append(' ')
                            }
                            otherPart.append(tokens[i])
                        }
                    }
                    firstLine = firstPart.toString()
                }

                if (type == LineType.DESCRIPTION) {
                    firstLine = '\uF00A'.toString() + firstLine
                }
                val result = StringBuilder()
                val rawResult = StringBuilder()
                var i = 0
                val len = firstLine.length
                while (i < len) {
                    val c = firstLine[i]
                    result.append(c)
                    if (c != '\uF00A') {
                        rawResult.append(c)
                    }
                    result.append('\uF000')
                    i++
                }
                rawResult.append(' ')

                // Собираем компонент с нужными цветом и текстом, добавляя к нему инвертированный текст
                val finalText = firstLine
                val comp: Component = Component.text(result.toString())
                    .color(
                        if (type == LineType.TITLE)
                            NamedTextColor.WHITE
                        else
                            TextColor.fromHexString("#CACACA")
                    )
                    .append(Component.text(getInverse(finalText)))
                list.add(LineComponent(comp, type, rawResult.toString()))

                // Рекурсивно обрабатываем оставшуюся часть, если она не пуста
                if (!otherPart.isEmpty()) {
                    list.addAll(makeLineFor(otherPart.toString(), type))
                }
                return list
            }
        }
    }

    class BooleanPDT : PersistentDataType<Byte, Boolean> {
        override fun getPrimitiveType(): Class<Byte> = Byte::class.javaObjectType

        override fun getComplexType(): Class<Boolean> = Boolean::class.java

        override fun toPrimitive(complex: Boolean, context: PersistentDataAdapterContext): Byte =
            if (complex) 1.toByte() else 0.toByte()

        override fun fromPrimitive(primitive: Byte, context: PersistentDataAdapterContext): Boolean =
            primitive >= 1

        companion object {
            @JvmField
            val BOOLEAN = BooleanPDT()
        }
    }

    companion object {
        private val displayKey = NamespacedKey(instance, "displayed-item")
        private val layerKey = NamespacedKey(instance, "layer")
        private val confirmKey = NamespacedKey(instance, "confirm-select")
        private val costsCurrencyKey = NamespacedKey(instance, "costs-currency")
        private val originKey = NamespacedKey(instance, "origin-name")
        private val swapTypeKey = NamespacedKey(instance, "swap-type")
        private val pageSetKey = NamespacedKey(instance, "page-set")
        private val pageScrollKey = NamespacedKey(instance, "page-scroll")
        private val costKey = NamespacedKey(instance, "enable-cost")
        private val displayOnlyKey = NamespacedKey(instance, "display-only")
        private val closeKey = NamespacedKey(instance, "close")
        private val random = Random()

        var options: ConfigOptions = ConfigOptions.instance
        var origins: OriginsReborn = instance
        var nmsInvoker: NMSInvoker = OriginsReborn.NMSInvoker

        fun getInverse(string: String): String {
            val result = StringBuilder()
            for (c in string.toCharArray()) {
                result.append(getInverse(c))
            }
            return result.toString()
        }

        @Deprecated("Origins-Reborn now has a 'layer' system, allowing for multiple origins to be set at once")
        fun openOriginSwapper(
            player: Player,
            reason: SwapReason,
            slot: Int,
            scrollAmount: Int,
            cost: Boolean,
            displayOnly: Boolean
        ) {
            openOriginSwapper(player, reason, slot, scrollAmount, cost, displayOnly, "origin")
        }

        @Deprecated("Origins-Reborn now has a 'layer' system, allowing for multiple origins to be set at once")
        fun openOriginSwapper(player: Player, reason: SwapReason, slot: Int, scrollAmount: Int) {
            openOriginSwapper(player, reason, slot, scrollAmount, "origin")
        }

        @Deprecated("Origins-Reborn now has a 'layer' system, allowing for multiple origins to be set at once")
        fun openOriginSwapper(player: Player, reason: SwapReason, slot: Int, scrollAmount: Int, cost: Boolean) {
            openOriginSwapper(player, reason, slot, scrollAmount, cost, "origin")
        }

        fun openOriginSwapper(player: Player, reason: SwapReason, slot: Int, scrollAmount: Int, layer: String) {
            openOriginSwapper(player, reason, slot, scrollAmount, false, false, layer)
        }

        @JvmStatic
        fun openOriginSwapper(
            player: Player,
            reason: SwapReason,
            slot: Int,
            scrollAmount: Int,
            cost: Boolean,
            layer: String
        ) {
            openOriginSwapper(player, reason, slot, scrollAmount, cost, false, layer)
        }

        @JvmStatic
        fun openOriginSwapper(
            player: Player,
            reason: SwapReason,
            slot: Int,
            scrollAmount: Int,
            cost: Boolean,
            displayOnly: Boolean,
            layer: String
        ) {
            var slot = slot
            if (shouldDisallowSelection(player, reason)) return
            if (reason == SwapReason.INITIAL) {
                val def: String? = options.defaultOrigin
                val defaultOrigin = getOriginByFilename(def)
                if (defaultOrigin != null) {
                    setOrigin(player, defaultOrigin, reason, false, layer)
                    return
                }
            }
            lastSwapReasons.put(player, reason)
            val enableRandom: Boolean = options.isRandomOptionEnabled

            if (GeyserSwapper.checkBedrockSwap(player, reason, cost, displayOnly, layer)) {
                if (getOrigins(layer).isEmpty()) return
                val origins: MutableList<Origin> = ArrayList<Origin>(getOrigins(layer))
                if (!displayOnly) origins.removeIf { origin: Origin? ->
                    origin!!.isUnchoosable(player) || origin.hasPermission() && !player.hasPermission(
                        origin.permission!!
                    )
                }
                while (slot > origins.size || slot == origins.size && !enableRandom) {
                    slot -= origins.size + (if (enableRandom) 1 else 0)
                }
                while (slot < 0) {
                    slot += origins.size + (if (enableRandom) 1 else 0)
                }
                val icon: ItemStack?
                val name: String?
                val nameForDisplay: String?
                val impact: Char
                var amount: Int = options.swapCommandVaultDefaultCost

                val data: LineData?
                if (slot == origins.size) {
                    val excludedOrigins: MutableList<String> = options.randomOptionExclude

                    val excludedOriginNames: MutableList<String?> = ArrayList<String?>()
                    for (s in excludedOrigins) {
                        val origin = getOriginByFilename(s)
                        if (origin == null) continue
                        excludedOriginNames.add(
                            getTextFor(
                                "origin." + origin.addon.getNamespace() + "." + s.replace(" ", "_").lowercase(
                                    Locale.getDefault()
                                ) + ".name", origin.getName()
                            )
                        )
                    }
                    icon = OrbOfOrigin.orb.clone()
                    name = getTextFor("origin.origins.random.name", "Random")
                    nameForDisplay = getTextFor("origin.origins.random.name", "Random")
                    impact = '\uE002'
                    val names = StringBuilder("${getTextFor("origin.origins.random.description", "You'll be assigned one of the following:")}\n\n")

                    for (origin in origins) {
                        if (!excludedOriginNames.contains(origin.getName())) {
                            names.append(origin.getName()).append("\n")
                        }
                    }
                    data = LineData(
                        LineData.Companion.makeLineFor(
                            names.toString(),
                            LineType.DESCRIPTION
                        )
                    )
                } else {
                    val origin = origins[slot]
                    icon = origin.icon
                    name = origin.getName()
                    nameForDisplay = origin.getNameForDisplay()
                    impact = origin.impact
                    data = LineData(origin)
                    if (origin.cost != null) {
                        amount = origin.cost
                    }
                }
                val compressedName = StringBuilder("\uF001")
                for (c in nameForDisplay.toCharArray()) {
                    compressedName.append(c)
                    compressedName.append('\uF000')
                }
                val background: Component = applyFont(
                    ShortcutUtils.getColored(options.screenTitleBackground),
                    Key.key("minecraft:default")
                )
                var component: Component = applyFont(
                    Component.text("\uF000\uE000\uF001\uE001\uF002" + impact),
                    Key.key("minecraft:origin_selector")
                )
                    .color(NamedTextColor.WHITE)
                    .append(background)
                    .append(
                        applyFont(
                            Component.text(compressedName.toString()),
                            Key.key("minecraft:origin_title_text")
                        ).color(NamedTextColor.WHITE)
                    )
                    .append(
                        applyFont(
                            Component.text(getInverse(nameForDisplay) + "\uF000"),
                            Key.key("minecraft:reverse_text")
                        ).color(NamedTextColor.WHITE)
                    )
                for (c in data.getLines(scrollAmount)) {
                    component = component.append(c)
                }
                val prefix: Component =
                    applyFont(ShortcutUtils.getColored(options.screenTitlePrefix), Key.key("minecraft:default"))
                val suffix: Component =
                    applyFont(ShortcutUtils.getColored(options.screenTitleSuffix), Key.key("minecraft:default"))
                val swapperInventory = Bukkit.createInventory(
                    null, 54,
                    prefix.append(component).append(suffix)
                )
                val meta = icon.itemMeta
                val container = meta.persistentDataContainer
                container.set<String?, String?>(
                    originKey,
                    PersistentDataType.STRING,
                    name.lowercase(Locale.getDefault())
                )
                if (meta is SkullMeta) {
                    meta.owningPlayer = player
                }
                container.set<Byte?, Boolean?>(displayKey, BooleanPDT.Companion.BOOLEAN, true)
                container.set<String?, String?>(swapTypeKey, PersistentDataType.STRING, reason.reason)
                container.set<String?, String?>(layerKey, PersistentDataType.STRING, layer)
                icon.setItemMeta(meta)
                swapperInventory.setItem(1, icon)
                val confirm = ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                val invisibleConfirm = ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                var confirmMeta = confirm.itemMeta
                val confirmContainer = confirmMeta.persistentDataContainer
                var invisibleConfirmMeta = invisibleConfirm.itemMeta
                val invisibleConfirmContainer = invisibleConfirmMeta.persistentDataContainer

                confirmMeta.displayName(
                    Component.text("Confirm")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false)
                )
                confirmMeta = nmsInvoker.setCustomModelData(confirmMeta, 5)
                if (!displayOnly) confirmContainer.set<Byte?, Boolean?>(confirmKey, BooleanPDT.Companion.BOOLEAN, true)
                else confirmContainer.set<Byte?, Boolean?>(closeKey, BooleanPDT.Companion.BOOLEAN, true)

                invisibleConfirmMeta.displayName(
                    Component.text("Confirm")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false)
                )
                invisibleConfirmMeta = nmsInvoker.setCustomModelData(invisibleConfirmMeta, 6)
                if (!displayOnly) invisibleConfirmContainer.set<Byte?, Boolean?>(
                    confirmKey,
                    BooleanPDT.Companion.BOOLEAN,
                    true
                )
                else invisibleConfirmContainer.set<Byte?, Boolean?>(closeKey, BooleanPDT.Companion.BOOLEAN, true)

                if (amount != 0 && cost && !player.hasPermission(options.swapCommandVaultBypassPermission)) {
                    var go = true
                    if (instance.getConfig().getBoolean("swap-command.vault.permanent-purchases")) {
                        go = !usedOriginFileConfiguration.getStringList(player.uniqueId.toString()).contains(name)
                    }
                    if (go) {
                        val symbol: String? = options.swapCommandVaultCurrencySymbol
                        val costsCurrency = listOf<Component?>(
                            Component.text(
                                (if (instance.economy!!.has(
                                        player,
                                        amount.toDouble()
                                    )
                                ) "This will cost $symbol$amount of your balance!" else "You need at least %s%s in your balance to do this!")
                            )
                        )
                        confirmMeta.lore(costsCurrency)
                        invisibleConfirmMeta.lore(costsCurrency)
                        confirmContainer.set<Int?, Int?>(costsCurrencyKey, PersistentDataType.INTEGER, amount)
                        invisibleConfirmContainer.set<Int?, Int?>(costsCurrencyKey, PersistentDataType.INTEGER, amount)
                    }
                }

                val up = ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                val down = ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                var upMeta = up.itemMeta
                val upContainer = upMeta.persistentDataContainer
                var downMeta = down.itemMeta
                val downContainer = downMeta.persistentDataContainer

                val scrollSize: Int = options.originSelectionScrollAmount

                upMeta.displayName(
                    Component.text("Up")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false)
                )
                if (scrollAmount != 0) {
                    upContainer.set(pageSetKey, PersistentDataType.INTEGER, slot)
                    upContainer.set(
                        pageScrollKey,
                        PersistentDataType.INTEGER,
                        max(scrollAmount - scrollSize, 0)
                    )
                }
                upMeta = nmsInvoker.setCustomModelData(upMeta, 3 + (if (scrollAmount == 0) 6 else 0))
                upContainer.set<Byte?, Boolean?>(costKey, BooleanPDT.Companion.BOOLEAN, cost)
                upContainer.set<Byte?, Boolean?>(displayOnlyKey, BooleanPDT.Companion.BOOLEAN, displayOnly)


                val size = data.rawLines.size - scrollAmount - 6
                val canGoDown = size > 0

                downMeta.displayName(
                    Component.text("Down")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false)
                )
                if (canGoDown) {
                    downContainer.set(pageSetKey, PersistentDataType.INTEGER, slot)
                    downContainer.set(
                        pageScrollKey,
                        PersistentDataType.INTEGER,
                        min(scrollAmount + scrollSize, scrollAmount + size)
                    )
                }
                downMeta = nmsInvoker.setCustomModelData(downMeta, 4 + (if (!canGoDown) 6 else 0))
                downContainer.set<Byte?, Boolean?>(costKey, BooleanPDT.Companion.BOOLEAN, cost)
                downContainer.set<Byte?, Boolean?>(displayOnlyKey, BooleanPDT.Companion.BOOLEAN, displayOnly)


                up.setItemMeta(upMeta)
                down.setItemMeta(downMeta)
                swapperInventory.setItem(52, up)
                swapperInventory.setItem(53, down)


                if (!displayOnly) {
                    val left = ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                    val right = ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                    var leftMeta = left.itemMeta
                    val leftContainer = leftMeta.persistentDataContainer
                    var rightMeta = right.itemMeta
                    val rightContainer = rightMeta.persistentDataContainer


                    leftMeta.displayName(
                        Component.text("Previous origin")
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)
                    )
                    leftContainer.set<Int?, Int?>(pageSetKey, PersistentDataType.INTEGER, slot - 1)
                    leftContainer.set<Int?, Int?>(pageScrollKey, PersistentDataType.INTEGER, 0)
                    leftMeta = nmsInvoker.setCustomModelData(leftMeta, 1)
                    leftContainer.set<Byte?, Boolean?>(costKey, BooleanPDT.Companion.BOOLEAN, cost)
                    leftContainer.set<Byte?, Boolean?>(displayOnlyKey, BooleanPDT.Companion.BOOLEAN, false)

                    rightMeta.displayName(
                        Component.text("Next origin")
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)
                    )
                    rightContainer.set<Int?, Int?>(pageSetKey, PersistentDataType.INTEGER, slot + 1)
                    rightContainer.set<Int?, Int?>(pageScrollKey, PersistentDataType.INTEGER, 0)
                    rightMeta = nmsInvoker.setCustomModelData(rightMeta, 2)
                    rightContainer.set<Byte?, Boolean?>(costKey, BooleanPDT.Companion.BOOLEAN, cost)
                    rightContainer.set<Byte?, Boolean?>(displayOnlyKey, BooleanPDT.Companion.BOOLEAN, false)


                    left.setItemMeta(leftMeta)
                    right.setItemMeta(rightMeta)

                    swapperInventory.setItem(47, left)
                    swapperInventory.setItem(51, right)
                }

                confirm.setItemMeta(confirmMeta)
                invisibleConfirm.setItemMeta(invisibleConfirmMeta)
                swapperInventory.setItem(48, confirm)
                swapperInventory.setItem(49, invisibleConfirm)
                swapperInventory.setItem(50, invisibleConfirm)
                player.openInventory(swapperInventory)
            }
        }

        fun applyFont(component: Component?, font: Key?): Component {
            return nmsInvoker.applyFont(component, font)
        }

        @JvmStatic
        fun shouldResetPlayer(reason: SwapReason): Boolean {
            return when (reason) {
                SwapReason.COMMAND -> options.isSwapCommandResetPlayer
                SwapReason.ORB_OF_ORIGIN -> options.isOrbOfOriginResetPlayer
                else -> false
            }
        }

        fun getWidth(s: String): Int {
            var result = 0
            for (c in s.toCharArray()) {
                result += WidthGetter.getWidth(c)
            }
            return result
        }

        fun getInverse(c: Char): String {
            return when (WidthGetter.getWidth(c)) {
                0 -> ""
                2 -> "\uF001"
                3 -> "\uF002"
                4 -> "\uF003"
                5 -> "\uF004"
                6 -> "\uF005"
                7 -> "\uF006"
                8 -> "\uF007"
                9 -> "\uF008"
                10 -> "\uF009"
                11 -> "\uF008\uF001"
                12 -> "\uF009\uF001"
                13 -> "\uF009\uF002"
                14 -> "\uF009\uF003"
                15 -> "\uF009\uF004"
                16 -> "\uF009\uF005"
                17 -> "\uF009\uF006"
                else -> throw IllegalStateException("Unexpected value: $c")
            }
        }

        @JvmField
        var orbCooldown: MutableMap<Player?, Long?> = HashMap<Player?, Long?>()

        fun resetPlayer(player: Player, full: Boolean) {
            resetAttributes(player)
            player.closeInventory()
            nmsInvoker.setWorldBorderOverlay(player, false)
            player.setCooldown(Material.SHIELD, 0)
            player.allowFlight = false
            player.isFlying = false
            for (otherPlayer in Bukkit.getOnlinePlayers()) {
                AbilityRegister.updateEntity(player, otherPlayer)
            }
            for (effect in player.activePotionEffects) {
                if (effect.amplifier == -1 || ShortcutUtils.isInfinite(effect)) player.removePotionEffect(effect.type)
            }
            if (!full) return
            player.inventory.clear()
            player.enderChest.clear()
            player.saturation = 5f
            player.fallDistance = 0f
            player.remainingAir = player.maximumAir
            player.foodLevel = 20
            player.fireTicks = 0
            player.health = getMaxHealth(player)
            for (effect in player.activePotionEffects) {
                player.removePotionEffect(effect.type)
            }
            val world: World = getRespawnWorld(getOrigins(player))
            player.teleport(world.spawnLocation)
            nmsInvoker.resetRespawnLocation(player)
        }

        fun getRespawnWorld(origin: List<Origin>): World {
            val abilities: MutableList<Ability?> = ArrayList<Ability?>()
            for (o in origin) abilities.addAll(o.getAbilities())
            for (ability in abilities) {
                if (ability is DefaultSpawnAbility) {
                    val world = ability.getWorld()
                    if (world != null) return world
                }
            }
            var overworld = instance.getConfig().getString("worlds.world")
            if (overworld == null) {
                overworld = "world"
                instance.getConfig().set("worlds.world", "world")
                instance.saveConfig()
            }
            val world = Bukkit.getWorld(overworld)
            if (world == null) return Bukkit.getWorlds()[0]
            return world
        }

        fun getMaxHealth(player: Player): Double {
            applyAttributeChanges(player)
            val instance = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
            if (instance == null) return 20.0
            return instance.value
        }

        fun applyAttributeChanges(player: Player) {
            for (ability in AbilityRegister.abilityMap.values) {
                if (ability !is AttributeModifierAbility) {
                    continue
                }

                val instance: AttributeInstance?
                try {
                    instance = player.getAttribute(ability.getAttribute())
                } catch (e: IllegalArgumentException) {
                    continue
                }
                if (instance == null) {
                    continue
                }

                val abilityKeyStr = ability.getKey().asString()
                val key = NamespacedKey(origins, abilityKeyStr.replace(":", "-"))

                val requiredAmount = ability.getTotalAmount(player)
                val hasAbility = ability.hasAbility(player)

                val currentModifier: AttributeModifier? = nmsInvoker.getAttributeModifier(instance, key)

                if (hasAbility) {
                    if (currentModifier != null) {
                        if (currentModifier.amount == requiredAmount) {
                            continue
                        } else {
                            instance.removeModifier(currentModifier)
                        }
                    }
                    nmsInvoker.addAttributeModifier(
                        instance,
                        key,
                        abilityKeyStr,
                        requiredAmount,
                        ability.actualOperation
                    )
                } else {
                    if (currentModifier != null) {
                        instance.removeModifier(currentModifier)
                    }
                }
            }
        }


        fun resetAttributes(player: Player) {
            val health = doubleArrayOf(player.health)
            for (attribute in Attribute.entries) {
                val instance = player.getAttribute(attribute)
                if (instance == null) continue
                for (modifier in instance.modifiers) {
                    instance.removeModifier(modifier)
                }
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(origins, Runnable {
                val mh = player.getAttribute(nmsInvoker.getMaxHealthAttribute())
                if (mh == null) return@Runnable
                val maxHealth = mh.value
                health[0] = min(maxHealth, health[0])
                player.health = health[0]
            }, 10)
        }

        private val lastSwapReasons: MutableMap<Player?, SwapReason> = HashMap<Player?, SwapReason>()

        private val lastJoinedTick: MutableMap<Player?, Int?> = HashMap<Player?, Int?>()


        fun shouldDisallowSelection(player: Player, reason: SwapReason): Boolean {
            try {
                return !AuthMeApi.getInstance().isAuthenticated(player)
            } catch (ignored: NoClassDefFoundError) {
            }
            val worldId = player.world.name
            return !shouldOpenSwapMenu(player, reason) || options.worldsDisabledWorlds.contains(worldId)
        }

        fun selectRandomOrigin(player: Player, reason: SwapReason, layer: String) {
            val origin = getRandomOrigin(layer)
            setOrigin(player, origin, reason, shouldResetPlayer(reason), layer)
            openOriginSwapper(player, reason, getOrigins(layer).indexOf(origin), 0, false, true, layer)
        }

        @Deprecated("Origins-Reborn now has a 'layer' system, allowing for multiple origins to be set at once")
        fun getOrigin(player: Player): Origin? {
            return getOrigin(player, "origin")
        }

        @JvmStatic
        fun getOrigin(player: Player, layer: String): Origin? {
            if (player.persistentDataContainer.has<String?, String?>(originKey, PersistentDataType.STRING)) {
                return getStoredOrigin(player, layer)
            }
            val pdc = player.persistentDataContainer.get<PersistentDataContainer?, PersistentDataContainer?>(
                originKey, PersistentDataType.TAG_CONTAINER
            )
            if (pdc == null) return null
            val name = pdc.get<String?, String?>(AddonLoader.layerKeys.get(layer)!!, PersistentDataType.STRING)
            if (name == null) return null
            return getOrigin(name)
        }

        fun getStoredOrigin(player: Player, layer: String): Origin? {
            val oldOrigin: String = originFileConfiguration.getString(player.uniqueId.toString(), "null")!!
            if (oldOrigin != "null" && layer == "origin") {
                if (!oldOrigin.contains("MemorySection")) {
                    originFileConfiguration.set(player.uniqueId.toString() + "." + layer, oldOrigin)
                    saveOrigins()
                }
            }
            val name: String =
                originFileConfiguration.getString(player.uniqueId.toString() + "." + layer, "null")!!
            return getOrigin(name)
        }

        fun loadOrigins(player: Player) {
            player.persistentDataContainer.remove(originKey)
            for (layer in AddonLoader.layers) {
                val origin: Origin? = getStoredOrigin(player, layer!!)
                if (origin == null) continue
                var pdc = player.persistentDataContainer.get<PersistentDataContainer?, PersistentDataContainer?>(
                    originKey, PersistentDataType.TAG_CONTAINER
                )
                if (pdc == null) pdc =
                    player.persistentDataContainer.adapterContext.newPersistentDataContainer()
                pdc.set<String?, String?>(
                    AddonLoader.layerKeys[layer]!!, PersistentDataType.STRING, origin.getName().lowercase(
                        Locale.getDefault()
                    )
                )
                player.persistentDataContainer.set<PersistentDataContainer?, PersistentDataContainer?>(
                    originKey,
                    PersistentDataType.TAG_CONTAINER,
                    pdc
                )
            }
        }

        @JvmStatic
        fun getOrigins(player: Player): MutableList<Origin> {
            val origins: MutableList<Origin> = ArrayList<Origin>()
            for (layer in AddonLoader.layers) {
                val o: Origin? = getOrigin(player, layer!!)
                if (o != null) origins.add(o)
            }
            return origins
        }

        @Deprecated("Origins-Reborn now has a 'layer' system, allowing for multiple origins to be set at once")
        fun setOrigin(player: Player, origin: Origin?, reason: SwapReason?, resetPlayer: Boolean) {
            setOrigin(player, origin, reason, resetPlayer, "origin")
        }

        @JvmStatic
        fun setOrigin(player: Player, origin: Origin?, reason: SwapReason?, resetPlayer: Boolean, layer: String) {
            val swapOriginEvent = PlayerSwapOriginEvent(player, reason, resetPlayer, getOrigin(player, layer), origin)
            if (!swapOriginEvent.callEvent()) return
            if (swapOriginEvent.newOrigin == null) {
                originFileConfiguration.set(player.uniqueId.toString() + "." + layer, null)
                saveOrigins()
                resetPlayer(player, swapOriginEvent.isResetPlayer)
                return
            }
            if (swapOriginEvent.newOrigin!!.team != null) {
                swapOriginEvent.newOrigin!!.team!!.addPlayer(player)
            }
            getCooldowns().resetCooldowns(player)
            originFileConfiguration.set(
                player.uniqueId.toString() + "." + layer, swapOriginEvent.newOrigin!!.getName().lowercase(
                    Locale.getDefault()
                )
            )
            saveOrigins()
            val usedOrigins: MutableList<String?> =
                ArrayList<String?>(usedOriginFileConfiguration.getStringList(player.uniqueId.toString()))
            usedOrigins.add(swapOriginEvent.newOrigin!!.getName().lowercase(Locale.getDefault()))
            usedOriginFileConfiguration.set(player.uniqueId.toString(), usedOrigins)
            saveUsedOrigins()
            resetPlayer(player, swapOriginEvent.isResetPlayer)
            loadOrigins(player)
        }

        private lateinit var originFile: File
        lateinit var originFileConfiguration: FileConfiguration

        private lateinit var usedOriginFile: File
        lateinit var usedOriginFileConfiguration: FileConfiguration

        fun saveOrigins() {
            try {
                originFileConfiguration.save(originFile)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        fun saveUsedOrigins() {
            try {
                usedOriginFileConfiguration.save(usedOriginFile)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }
}
