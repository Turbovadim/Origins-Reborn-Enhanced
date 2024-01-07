package com.starshootercity.abilities;

import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class Arthropod implements Ability, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:arthropod");
    }

    private final Random random = new Random();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player && event.getDamager() instanceof LivingEntity entity) {
            if (entity.getActiveItem().containsEnchantment(Enchantment.DAMAGE_ARTHROPODS)) {
                AbilityRegister.runForAbility(player, getKey(), () -> {
                    int level = entity.getActiveItem().getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS);
                    event.setDamage(event.getDamage() + 1.25 * level);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (20 * random.nextDouble(1, 1 + (0.5 * level))), 3, false, true));
                });
            }
        }
    }
}
