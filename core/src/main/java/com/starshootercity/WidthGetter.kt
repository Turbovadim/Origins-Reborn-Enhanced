package com.starshootercity

import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException

object WidthGetter {
    fun getWidth(c: Char): Int {
        for (i in 2..7) {
            val key = "character-widths.$i"
            // Получаем значение по ключу; если fileConfiguration равен null, используем пустую строку
            val widths = fileConfiguration?.getString(key, "") ?: ""
            if (widths.contains(c)) {
                return i
            }
        }
        return 0
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
