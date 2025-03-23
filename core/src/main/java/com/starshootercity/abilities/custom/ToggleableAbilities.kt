package com.starshootercity.abilities.custom;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ToggleableAbilities {
    public static List<ToggleableAbility> getAbilities() {
        return List.of(

        );
    }

    private static File file;
    private static FileConfiguration fileConfiguration;

    public static void initialize(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "toggleable-abilities.yml");
        if (!file.exists()) {
            boolean ignored = file.getParentFile().mkdirs();
            plugin.saveResource("toggleable-abilities.yml", false);
        }


        fileConfiguration = new YamlConfiguration();

        try {
            fileConfiguration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerAbility(ToggleableAbility ability) {
        if (!fileConfiguration.contains(ability.getKey().toString())) {
            fileConfiguration.set(ability.getKey().toString(), false);
            try {
                fileConfiguration.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean isEnabled(ToggleableAbility ability) {
        return fileConfiguration.getBoolean(ability.toString(), false);
    }
}
