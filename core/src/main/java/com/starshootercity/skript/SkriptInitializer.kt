package com.starshootercity.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class SkriptInitializer {
    public static void initialize(JavaPlugin plugin) {
        try {
            SkriptAddon addon = Skript.registerAddon(plugin);
            addon.loadClasses("com.starshootercity.skript", "elements");
        } catch (NoClassDefFoundError ignored) {

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
