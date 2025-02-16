package com.starshootercity

import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException

object WidthGetter {
    private val charWidthMap: Map<Char, Int> by lazy {
        val map = mutableMapOf<Char, Int>()
        // Предположим, что возможные ширины варьируются от 2 до 7.
        for (width in 2..7) {
            val key = "character-widths.$width"
            val chars = fileConfiguration?.getString(key, "") ?: ""
            chars.forEach { c ->
                // Если символ уже встречался, можно решить, что брать — первый найденный или последний.
                map[c] = width
            }
        }
        map
    }

    fun getWidth(c: Char): Int {
        // Если символ не найден в конфигурации, можно вернуть значение по умолчанию (например, 1).
        println("$c -> ${charWidthMap['О']}")
        print("ебу")
        return charWidthMap[c] ?: 1
    }


    fun initialize(plugin: JavaPlugin) {
        file = File(plugin.dataFolder, "characters.yml")

        if (!file!!.exists()) {
            val ignored = file!!.getParentFile().mkdirs()
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
    }

    private var file: File? = null
    private var fileConfiguration: FileConfiguration? = null
}
