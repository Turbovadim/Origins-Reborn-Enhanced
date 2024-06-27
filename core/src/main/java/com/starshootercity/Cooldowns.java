package com.starshootercity;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Cooldowns {
    private final Map<Player, Map<NamespacedKey, Integer>> cooldowns = new HashMap<>();
    private final Map<NamespacedKey, Integer> registeredCooldowns = new HashMap<>();

    private int getCooldown(Player player, NamespacedKey key) {
        if (!cooldowns.containsKey(player)) cooldowns.put(player, new HashMap<>());
        return Math.max(0, cooldowns.get(player).getOrDefault(key, 0) - Bukkit.getCurrentTick());
    }

    public boolean hasCooldown(Player player, NamespacedKey key) {
        return getCooldown(player, key) > 0;
    }

    private void setCooldown(Player player, NamespacedKey key, int cooldown) {
        if (!cooldowns.containsKey(player)) cooldowns.put(player, new HashMap<>());
        cooldowns.get(player).put(key, Bukkit.getCurrentTick() + cooldown);
    }

    public void setCooldown(Player player, NamespacedKey key) {
        setCooldown(player, key, registeredCooldowns.get(key));
    }

    public NamespacedKey registerCooldown(NamespacedKey key, int regularCooldown) {
        if (!fileConfiguration.contains(key.toString())) {
            fileConfiguration.set(key.toString(), -1);
            try {
                fileConfiguration.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        int i = fileConfiguration.getInt(key.toString());
        if (i == -1) i = regularCooldown;
        registeredCooldowns.put(key, i);
        return key;
    }

    private final File file;

    private final FileConfiguration fileConfiguration;

    public Cooldowns() {
        file = new File(OriginsReborn.getInstance().getDataFolder(), "cooldown-config.yml");
        if (!file.exists()) {
            boolean ignored = file.getParentFile().mkdirs();
            OriginsReborn.getInstance().saveResource("cooldown-config.yml", false);
        }

        fileConfiguration = new YamlConfiguration();

        try {
            fileConfiguration.load(file);
        } catch (InvalidConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
