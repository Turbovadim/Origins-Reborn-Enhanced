package com.starshootercity;

import org.bukkit.potion.PotionEffect;

public record SavedPotionEffect(PotionEffect effect, int currentTime) {
}
