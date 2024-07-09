package com.starshootercity.abilities;

import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class Aquatic implements Ability, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:aquatic");
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        AbilityRegister.runForAbility(event.getEntity(), getKey(), () -> {
            if (event.getDamager() instanceof Trident trident) {
                event.setDamage(event.getDamage() + trident.getItem().getEnchantmentLevel(Enchantment.IMPALING) * 2.5);
            } else if (event.getDamager() instanceof LivingEntity entity) {
                if (entity.getEquipment() == null) return;
                event.setDamage(event.getDamage() + entity.getEquipment().getItemInMainHand().getEnchantmentLevel(Enchantment.IMPALING) * 2.5);
            }
        });
    }
}
