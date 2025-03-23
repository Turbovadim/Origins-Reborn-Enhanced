package ru.turbovadim

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
        charWidthMap = computeCharWidthMap()
    }

    private fun computeCharWidthMap(): Map<Char, Int> {
        val map = mutableMapOf<Char, Int>()
        for (width in 2..9) {
            val key = "character-widths.$width"
            val chars = fileConfiguration?.getString(key, "") ?: ""
            chars.forEach { c ->
                map.computeIfAbsent(c) { width }
            }
        }
        return map
    }

    fun getWidth(c: Char): Int {
        return charWidthMap[c] ?: 0
    }
}