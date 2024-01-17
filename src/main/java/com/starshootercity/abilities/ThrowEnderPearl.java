package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import com.starshootercity.events.PlayerLeftClickEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThrowEnderPearl implements VisibleAbility, Listener {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:throw_ender_pearl");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("Whenever you want, you may throw an ender pearl which deals no damage, allowing you to teleport.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Teleportation", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    private final NamespacedKey falseEnderPearlKey = new NamespacedKey(OriginsReborn.getInstance(), "false-ender-pearl");
    private final Map<Player, Integer> lastPearlThrowTickMap = new HashMap<>();

    @EventHandler
    public void onLeftClick(PlayerLeftClickEvent event) {
        if (event.hasBlock()) return;
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            if (Bukkit.getCurrentTick() - lastPearlThrowTickMap.getOrDefault(event.getPlayer(), Bukkit.getCurrentTick() - 600) >= 600) {
                lastPearlThrowTickMap.put(event.getPlayer(), Bukkit.getCurrentTick());
                if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) return;
                Projectile projectile = event.getPlayer().launchProjectile(EnderPearl.class);
                projectile.getPersistentDataContainer().set(falseEnderPearlKey, PersistentDataType.STRING, event.getPlayer().getName());
            }
        });
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(falseEnderPearlKey)) {
            event.setCancelled(true);
            String name = event.getEntity().getPersistentDataContainer().get(falseEnderPearlKey, PersistentDataType.STRING);
            if (name == null) return;
            Player player = Bukkit.getPlayer(name);
            if (player == null) return;
            Location loc = event.getEntity().getLocation();
            loc.setPitch(player.getPitch());
            loc.setYaw(player.getYaw());
            player.teleport(loc);
            event.getEntity().remove();
        }
    }
}
