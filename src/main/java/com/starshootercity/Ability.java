package com.starshootercity;

import org.bukkit.NamespacedKey;

public class Ability {
    private NamespacedKey key;
    private String title;
    private String description;
    public Ability(NamespacedKey key, String title, String description) {
        this.key = key;
        this.title = title;
        this.description = description;
    }
}
