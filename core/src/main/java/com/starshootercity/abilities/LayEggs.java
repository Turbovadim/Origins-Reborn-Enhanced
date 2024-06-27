package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LayEggs implements VisibleAbility, Listener {
    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        if (event.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                AbilityRegister.runForAbility(player, getKey(), () -> {
                    if (player.isDeeplySleeping()) {
                        player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.EGG));
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, SoundCategory.PLAYERS, 1, 1);
                    }
                });
            }
        }
    }
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:lay_eggs");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("Whenever you wake up in the morning, you will lay an egg.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Oviparous", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
