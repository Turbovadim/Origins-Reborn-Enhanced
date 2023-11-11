package com.starshootercity.origins;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginsReborn;
import com.starshootercity.OriginSwapper;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("unused")
public class Merling implements Listener {
    @EventHandler
    public void onEntityAirChange(EntityAirChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            OriginSwapper.runForOrigin(player, "Merling", () -> {
                if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(airKey, PersistentDataType.BOOLEAN)))
                    return;
                if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(dehydrationKey, PersistentDataType.BOOLEAN)))
                    return;
                if (player.getRemainingAir() - event.getAmount() > 0) {
                    if (!player.isUnderWater()) return;
                } else if (player.isUnderWater()) return;
                event.setCancelled(true);
            });
        }
    }

    NamespacedKey airKey = new NamespacedKey(OriginsReborn.getInstance(), "fullair");
    NamespacedKey dehydrationKey = new NamespacedKey(OriginsReborn.getInstance(), "dehydrating");

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String origin = OriginSwapper.getOrigin(player);
            if (origin == null) continue;
            OriginSwapper.runForOrigin(player, "Merling", () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, -1, 1, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, -1, 1, false, false));
                if (player.isUnderWater()) {
                    if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(airKey, PersistentDataType.BOOLEAN))) {
                        player.setRemainingAir(-5);
                        return;
                    }
                    player.setRemainingAir(Math.min(Math.max(player.getRemainingAir() + 4, 4), player.getMaximumAir()));
                    if (player.getRemainingAir() == player.getMaximumAir()) {
                        player.setRemainingAir(-5);
                        player.getPersistentDataContainer().set(airKey, PersistentDataType.BOOLEAN, true);
                    }
                } else {
                    if (Boolean.TRUE.equals(player.getPersistentDataContainer().get(airKey, PersistentDataType.BOOLEAN))) {
                        player.setRemainingAir(player.getMaximumAir());
                        player.getPersistentDataContainer().set(airKey, PersistentDataType.BOOLEAN, false);
                    }
                    player.setRemainingAir(player.getRemainingAir() - 1);
                    if (player.getRemainingAir() < -25) {
                        player.getPersistentDataContainer().set(dehydrationKey, PersistentDataType.BOOLEAN, true);
                        player.setRemainingAir(-5);
                        player.getPersistentDataContainer().set(dehydrationKey, PersistentDataType.BOOLEAN, false);
                        DamageSource source = ((CraftPlayer) player).getHandle().damageSources().dryOut();
                        ((CraftPlayer) player).getHandle().hurt(source, 2);
                    }
                }
            }, () -> {
                if (player.getPersistentDataContainer().has(airKey)) {
                    player.setRemainingAir(player.getMaximumAir());
                    player.getPersistentDataContainer().remove(airKey);
                }
            });
        }
    }
}
