package com.starshootercity;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class WidthGetter {
    public static int getWidth(char c) {
        for (int i = 2; i < 8; i++) {
            if (fileConfiguration.getString("character-widths.%s".formatted(i), "").contains(String.valueOf(c))) {
                return i;
            }
        }
//        System.out.println(c);
        return 0;
    }

    public static void initialize(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "characters.yml");

        if (!file.exists()) {
            boolean ignored = file.getParentFile().mkdirs();
            plugin.saveResource("characters.yml", false);
        }

        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reload() {
        try {
            fileConfiguration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static File file;
    private static FileConfiguration fileConfiguration;
}
