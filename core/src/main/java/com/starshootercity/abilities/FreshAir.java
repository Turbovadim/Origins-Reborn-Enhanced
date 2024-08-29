package com.starshootercity.abilities;

import com.starshootercity.AddonLoader;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FreshAir implements VisibleAbility, Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!event.getAction().isRightClick()) return;
        if (
                event.getPlayer().isSneaking() &&
                        event.getPlayer().getInventory().getItemInOffHand().getType() == Material.AIR &&
                        event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR
        ) return;
        if (Tag.BEDS.isTagged(event.getClickedBlock().getType())) {
            AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
                if (event.getClickedBlock().getY() < 86) {
                    String overworld = OriginsReborn.getInstance().getConfig().getString("worlds.world");
                    if (overworld == null) {
                        overworld = "world";
                        OriginsReborn.getInstance().getConfig().set("worlds.world", "world");
                        OriginsReborn.getInstance().saveConfig();
                    }
                    boolean isInOverworld = event.getPlayer().getWorld() == Bukkit.getWorld(overworld);

                    if (!isInOverworld) return;
                    if (event.getClickedBlock().getWorld().isDayTime() && event.getClickedBlock().getWorld().isClearWeather()) return;
                    event.setCancelled(true);
                    event.getPlayer().swingMainHand();
                    event.getPlayer().sendActionBar(Component.text(AddonLoader.getTextFor("origins.avian_sleep_fail", "You need fresh air to sleep")));
                }
            });
        }
    }
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:fresh_air");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("When sleeping, your bed needs to be at an altitude of at least %s blocks, so you can breathe fresh air.".formatted(OriginsReborn.getInstance().getConfig().getInt("extra-settings.fresh-air-required-sleep-height", 86)), OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Fresh Air", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
