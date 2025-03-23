package com.starshootercity

import com.starshootercity.OriginsAddon.KeyStateGetter
import com.starshootercity.OriginsAddon.SwapStateGetter
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.events.PlayerSwapOriginEvent.SwapReason
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.json.JSONObject
import java.io.*
import java.util.*
import java.util.zip.ZipInputStream

object AddonLoader {
    private val origins: MutableList<Origin?> = ArrayList<Origin?>()
    private val originNameMap: MutableMap<String?, Origin?> = HashMap<String?, Origin?>()
    private val originFileNameMap: MutableMap<String?, Origin?> = HashMap<String?, Origin?>()
    val registeredAddons: MutableList<OriginsAddon> = ArrayList<OriginsAddon>()
    @JvmField
    var originFiles: MutableMap<String?, MutableList<File?>?> = HashMap<String?, MutableList<File?>?>()
    @JvmField
    var layers: MutableList<String?> = ArrayList<String?>()
    @JvmField
    var layerKeys: MutableMap<String?, NamespacedKey?> = HashMap<String?, NamespacedKey?>()

    private val random = Random()

    @JvmStatic
    suspend fun getFirstUnselectedLayer(player: Player): String? {
        for (layer in layers) {
            if (OriginSwapper.getOrigin(player, layer!!) == null) return layer
        }
        return null
    }

    @JvmStatic
    fun getOrigin(name: String): Origin? {
        return originNameMap[name]
    }

    @JvmStatic
    fun getOriginByFilename(name: String): Origin? {
        return originFileNameMap[name]
    }

    @JvmStatic
    fun getOrigins(layer: String): MutableList<Origin> {
        val o: MutableList<Origin> = ArrayList<Origin>(origins)
        o.removeIf { or -> or.layer != layer }
        return o
    }

    @JvmStatic
    fun getFirstOrigin(layer: String): Origin? {
        return getOrigins(layer)[0]
    }

    @JvmStatic
    fun getRandomOrigin(layer: String): Origin? {
        val o = getOrigins(layer)
        return o[random.nextInt(o.size)]
    }

    fun register(addon: OriginsAddon) {
        if (registeredAddons.contains(addon)) {
            registeredAddons.remove(addon)
            origins.removeIf { origin: Origin? -> origin!!.addon.getNamespace() == addon.getNamespace() }
        }
        registeredAddons.add(addon)
        loadOriginsFor(addon)
        // TODO() ПРОТЕСТИРОВАТЬ ЯЗЫК
//        prepareLanguagesFor(addon)
        if (addon.shouldAllowOriginSwapCommand() != null) allowOriginSwapChecks.add(addon.shouldAllowOriginSwapCommand()!!)
        if (addon.shouldOpenSwapMenu() != null) openSwapMenuChecks.add(addon.shouldOpenSwapMenu()!!)
        if (addon.getAbilityOverride() != null) abilityOverrideChecks.add(addon.getAbilityOverride())
        sortOrigins()
    }

    @JvmStatic
    fun shouldOpenSwapMenu(player: Player, reason: SwapReason): Boolean {
        for (getter in openSwapMenuChecks) {
            val v = getter.get(player, reason)
            if (v == OriginsAddon.State.DENY) return false
        }
        return true
    }

    @JvmStatic
    fun allowOriginSwapCommand(player: Player): Boolean {
        var allowed = false
        for (getter in allowOriginSwapChecks) {
            val v = getter.get(player, SwapReason.COMMAND)
            if (v == OriginsAddon.State.DENY) return false
            else if (v == OriginsAddon.State.ALLOW) allowed = true
        }
        return allowed || player.hasPermission(
            OriginsReborn.mainConfig.swapCommand.permission
        )
    }

    val allowOriginSwapChecks: MutableList<SwapStateGetter> = ArrayList<SwapStateGetter>()
    val openSwapMenuChecks: MutableList<SwapStateGetter> = ArrayList<SwapStateGetter>()
    @JvmField
    val abilityOverrideChecks: MutableList<KeyStateGetter?> = ArrayList<KeyStateGetter?>()

    private val languageData: MutableMap<String?, String?> = HashMap<String?, String?>()

    @JvmStatic
    fun getTextFor(key: String?, fallback: String) = languageData[key] ?: fallback

    @JvmStatic
    fun getTextFor(key: String?) = languageData[key]


    @JvmStatic
    fun reloadAddons() {
        origins.clear()
        originNameMap.clear()
        languageData.clear()
        originFiles.clear()
        for (addon in registeredAddons) {
            loadOriginsFor(addon)
            //prepareLanguagesFor(addon);
        }
        sortOrigins()
    }

    fun sortOrigins() {
        origins.sortWith(compareBy({ it!!.impact }, { it!!.position }))
    }

    private const val BUFFER_SIZE = 4096

    /**
     * Извлекает файл из ZipInputStream по указанному пути.
     * Перед созданием файла гарантируется, что его родительские директории существуют.
     */
    @Throws(IOException::class)
    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        val outFile = File(filePath)
        // Создаём родительские директории, если их ещё нет
        outFile.parentFile.mkdirs()
        BufferedOutputStream(FileOutputStream(outFile)).use { bos ->
            val bytesIn = ByteArray(BUFFER_SIZE)
            var read: Int
            while (zipIn.read(bytesIn).also { read = it } != -1) {
                bos.write(bytesIn, 0, read)
            }
        }
    }

    /**
     * Подготавливает языковые файлы для плагина.
     * Из архива извлекаются файлы из папки lang с расширением .json,
     * затем из них подгружается язык, указанный в конфигурации.
     */
//    private fun prepareLanguagesFor(addon: OriginsAddon) {
//        val langFolder = File(addon.dataFolder, "lang")
//        if (!langFolder.exists() && !langFolder.mkdirs()) {
//            instance.logger.warning("Не удалось создать папку с языковыми файлами: ${langFolder.absolutePath}")
//            return
//        }
//        if (!langFolder.exists()) {
//            try {
//                ZipInputStream(FileInputStream(addon.getFile())).use { zipIn ->
//                    var entry = zipIn.nextEntry
//                    while (entry != null) {
//                        if (entry.name.startsWith("lang/") && entry.name.endsWith(".json")) {
//                            // Формируем корректный путь для извлечения файла
//                            val outputFile = File(langFolder.parentFile, entry.name)
//                            extractFile(zipIn, outputFile.absolutePath)
//                        }
//                        entry = zipIn.nextEntry
//                    }
//                }
//            } catch (e: IOException) {
//                throw RuntimeException("Ошибка при извлечении языковых файлов", e)
//            }
//        }
//
//
//        // Загружаем языковые данные для указанного языка
//        val lang = instance.config.getString("display.language", "en_us")!!
//        println(lang)
//        langFolder.listFiles()?.forEach { file ->
//            if (file.name.equals("$lang.json", ignoreCase = true)) {
//                val jsonObject = ShortcutUtils.openJSONFile(file)
//                jsonObject.keySet().forEach { key ->
//                    languageData[key] = jsonObject.getString(key)
//                }
//            }
//        }
//    }


    private fun loadOriginsFor(addon: OriginsAddon) {
        val addonFiles: MutableList<File?> = ArrayList<File?>()
        originFiles.put(addon.getNamespace(), addonFiles)
        val originFolder = File(addon.dataFolder, "origins")
        if (!originFolder.exists()) {
            originFolder.mkdirs()
            try {
                ZipInputStream(FileInputStream(addon.getFile())).use { inputStream ->
                    var entry = inputStream.getNextEntry()
                    while (entry != null) {
                        if (entry.getName().startsWith("origins/") && entry.getName().endsWith(".json")) {
                            extractFile(
                                inputStream,
                                originFolder.getParentFile().absolutePath + "/" + entry.getName()
                            )
                        }
                        entry = inputStream.getNextEntry()
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        val files = originFolder.listFiles()
        if (files == null) return
        for (file in files) {
            if (!file.getName().endsWith(".json")) continue
            addonFiles.add(file)
            loadOrigin(file, addon)
        }
    }

    private fun sortLayers() {
        layers.sortBy { instance.config.getInt("origin-selection.layers.$it") }
    }

    fun registerLayer(layer: String, priority: Int, addon: OriginsAddon) {
        if (layers.contains(layer)) return
        layers.add(layer)
        layerKeys[layer] = NamespacedKey(addon, layer.lowercase(Locale.getDefault()).replace(" ", "_"))

        val config = instance.config

//        if (!config.contains("origin-selection.default-origin.$layer")) {
//            config.set("origin-selection.default-origin.$layer", "NONE")
//            NMSInvoker.setComments(
//                "origin-selection.default-origin",
//                listOf(
//                    "Default origin, automatically gives players this origin rather than opening the GUI when the player has no origin",
//                    "Should be the name of the origin file without the ending, e.g. for 'origin_name.json' the value should be 'origin_name'",
//                    "Disabled if set to an invalid name such as \"NONE\""
//                )
//            )
//            instance.saveConfig()
//        }
//
//        if (!config.contains("origin-selection.layer-orders.$layer")) {
//            config.set("origin-selection.layer-orders.$layer", priority)
//            NMSInvoker.setComments(
//                "origin-section.layer-orders",
//                listOf("Priorities for different origin 'layers' to be selected in, higher priority layers are selected first.")
//            )
//            instance.saveConfig()
//        }
//
//        if (!config.contains("orb-of-origin.random.$layer")) {
//            config.set("orb-of-origin.random.$layer", false)
//            NMSInvoker.setComments(
//                "orb-of-origin.random",
//                listOf("Randomise origin instead of opening the selector upon using the orb")
//            )
//            instance.saveConfig()
//        }

        sortLayers()

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            OriginsRebornPlaceholderExpansion(layer).register()
        }
    }


    fun loadOrigin(file: File, addon: OriginsAddon) {
        var targetFile = file
        if (targetFile.name.lowercase(Locale.getDefault()) != targetFile.name) {
            val lowercaseFile = File(targetFile.parentFile, targetFile.name.lowercase(Locale.getDefault()))
            if (!targetFile.renameTo(lowercaseFile)) {
                instance.logger.warning("Origin ${targetFile.name} failed to load - make sure file name is lowercase")
                return
            }
            targetFile = lowercaseFile
        }

        val json = ShortcutUtils.openJSONFile(targetFile)
        val unchoosable = if (json.has("unchoosable")) json.getBoolean("unchoosable") else false

        val (itemName, cmd) = when (val iconObj = json.get("icon")) {
            is JSONObject -> {
                val name = iconObj.getString("item")
                val customModelData = if (iconObj.has("custom_model_data")) iconObj.getInt("custom_model_data") else 0
                name to customModelData
            }
            else -> json.getString("icon") to 0
        }

        val material = Material.matchMaterial(itemName) ?: Material.AIR
        val icon = ItemStack(material)
        var meta = icon.itemMeta
        meta = NMSInvoker.setCustomModelData(meta, cmd)
        icon.itemMeta = meta

        // Получаем имя файла без расширения
        val nameWithoutExt = targetFile.name.substringBefore(".")
        // Форматируем имя: каждое слово с заглавной буквы, разделенные пробелами
        val formattedName = nameWithoutExt
            .split("_")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase(Locale.getDefault()) } }

        var permission: String? = null
        var cost: Int? = null
        val max = if (json.has("max")) json.getInt("max") else -1
        val layer = if (json.has("layer")) json.getString("layer") else "origin"
        if (json.has("permission")) permission = json.getString("permission")
        if (json.has("cost")) cost = json.getInt("cost")

        var extraLayerPriority = 0
        instance.config.getConfigurationSection("origin-selection.layers")?.let { cs ->
            for (s in cs.getValues(false).keys) {
                // Используем minOf для выбора минимального значения
                extraLayerPriority = minOf(extraLayerPriority, cs.getInt(s) - 1)
            }
        }
        registerLayer(layer, extraLayerPriority, addon)

        val displayName = if (json.has("name")) json.getString("name") else formattedName

        // Собираем список powers
        val powers = mutableListOf<Key>()
        if (json.has("powers")) {
            val array = json.getJSONArray("powers")
            for (i in 0 until array.length()) {
                val power = array.getString(i)
                powers.add(Key.key(power))
            }
        }

        val origin = Origin(
            formattedName,
            icon,
            json.getInt("order"),
            json.getInt("impact"),
            displayName,
            powers,
            json.getString("description"),
            addon,
            unchoosable,
            if (json.has("priority")) json.getInt("priority") else 1,
            permission,
            cost,
            max,
            layer
        )

        val actualName = origin.getActualName().lowercase(Locale.getDefault())
        val keyName = nameWithoutExt.replace("_", " ")
        val previouslyRegisteredOrigin = originNameMap[keyName]
        if (previouslyRegisteredOrigin != null) {
            if (previouslyRegisteredOrigin.priority > origin.priority) {
                return
            } else {
                origins.remove(previouslyRegisteredOrigin)
                originNameMap.remove(keyName)
                originFileNameMap.remove(actualName)
            }
        }
        origins.add(origin)
        originNameMap[keyName] = origin
        originFileNameMap[actualName] = origin
    }


    @get:Deprecated(
        """Origins-Reborn now has a 'layer' system, allowing for multiple origins to be set at once
      """
    )
    val defaultOrigin: Origin?
        /**
         * @return The default origin for the 'origin' layer
         */
        get() = getDefaultOrigin("origin")

    /**
     * @return The default origin for the specified layer
     */
    @JvmStatic
    fun getDefaultOrigin(layer: String?): Origin? {
        val originName = instance.config.getString("origin-selection.default-origin.$layer", "NONE")!!
        return originFileNameMap[originName]
    }

}
