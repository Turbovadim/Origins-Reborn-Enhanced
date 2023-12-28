package com.starshootercity;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Range;

public class Origin {
    private final ItemStack icon;
    private final int position;
    private final int impact;
    public Origin(ItemStack icon, int position, @Range(from = 0, to = 3) int impact) {
        this.icon = icon;
        this.position = position;
        this.impact = impact;
    }

    public int getImpact() {
        return impact;
    }

    public int getPosition() {
        return position;
    }

    public ItemStack getIcon() {
        return icon;
    }
}
