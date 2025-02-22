package com.starshootercity

import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException

object WidthGetter {
    private var charWidthMap: Map<Char, Int> = emptyMap()
    private var file: File? = null
    private var fileConfiguration: FileConfiguration? = null

    fun initialize(plugin: JavaPlugin) {

        file = File(plugin.dataFolder, "characters.yml")
        if (!file!!.exists()) {
            file!!.parentFile.mkdirs()
            plugin.saveResource("characters.yml", false)
        }
        fileConfiguration = YamlConfiguration()
        try {
            fileConfiguration!!.load(file!!)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InvalidConfigurationException) {
            throw RuntimeException(e)
        }
        // Инициализируем карту при первом запуске
        charWidthMap = computeCharWidthMap()
    }

    @JvmStatic
    fun reload() {
        try {
            fileConfiguration!!.load(file!!)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InvalidConfigurationException) {
            throw RuntimeException(e)
        }
        // Пересчитываем карту после перезагрузки конфига
        charWidthMap = computeCharWidthMap()
    }

    private fun computeCharWidthMap(): Map<Char, Int> {
        val map = mutableMapOf<Char, Int>()
        // Предположим, что возможные ширины варьируются от 2 до 9.
        for (width in 2..9) {
            val key = "character-widths.$width"
            val chars = fileConfiguration?.getString(key, "") ?: ""
            chars.forEach { c ->
                // Если символ уже встречался, можно оставить первое найденное значение.
                map.computeIfAbsent(c) { width }
            }
        }
        return map
    }

    fun getWidth(c: Char): Int {
        // Если символ не найден в конфигурации, возвращаем значение по умолчанию (например, 0).
        return charWidthMap[c] ?: 0
    }
}
