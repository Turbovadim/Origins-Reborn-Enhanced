package ru.turbovadim.skript

import ch.njol.skript.Skript
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException

object SkriptInitializer {
    fun initialize(plugin: JavaPlugin) {
        try {
            val addon = Skript.registerAddon(plugin)
            addon.loadClasses("ru.turbovadim.skript", "elements")
        } catch (_: NoClassDefFoundError) {
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
