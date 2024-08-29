package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Vegetarian implements VisibleAbility, Listener {
    List<Material> meat = new ArrayList<>() {{
        add(Material.PORKCHOP);
        add(Material.COOKED_PORKCHOP);
        add(Material.BEEF);
        add(Material.COOKED_BEEF);
        add(Material.CHICKEN);
        add(Material.COOKED_CHICKEN);
        add(Material.RABBIT);
        add(Material.COOKED_RABBIT);
        add(Material.MUTTON);
        add(Material.COOKED_MUTTON);
        add(Material.RABBIT_STEW);
        add(Material.COD);
        add(Material.COOKED_COD);
        add(Material.TROPICAL_FISH);
        add(Material.SALMON);
        add(Material.COOKED_SALMON);
        add(Material.PUFFERFISH);
    }};

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.POTION) return;
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            if (meat.contains(event.getItem().getType())) {
                event.setCancelled(true);
                event.getItem().setAmount(event.getItem().getAmount() - 1);
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 300, 1, false, true));
            }
        });
    }
    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor(
                "You can't digest any meat.",
                OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor(
                "Vegetarian",
                OriginSwapper.LineData.LineComponent.LineType.TITLE
        );
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:vegetarian");
    }
}
