package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AerialCombatant implements VisibleAbility, Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            AbilityRegister.runForAbility(event.getDamager(), getKey(), () -> {
                if (player.isGliding()) event.setDamage(event.getDamage() * 2);
            });
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:aerial_combatant");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You deal substantially more damage while in Elytra flight.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Aerial Combatant", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
