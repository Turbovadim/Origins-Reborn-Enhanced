package com.starshootercity.abilities;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Claustrophobia implements VisibleAbility, Listener {
    private final Map<Player, Integer> stacks = new HashMap<>();

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AbilityRegister.runForAbility(player, getKey(), () -> {
                if (player.getLocation().getBlock().getRelative(BlockFace.UP, 2).isSolid()) {
                    stacks.put(player, Math.min(stacks.getOrDefault(player, -200) + 1, 3600));
                } else stacks.put(player, Math.max(stacks.getOrDefault(player, -200) - 1, -200));
                int time = stacks.getOrDefault(player, -200);
                if (time > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, time, 0, true, true, true));
                    player.addPotionEffect(new PotionEffect(OriginsReborn.getNMSInvoker().getSlownessEffect(), time, 0, true, true, true));
                }
            });
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            stacks.put(event.getPlayer(), Math.min(stacks.getOrDefault(event.getPlayer(), -200), 0));
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:claustrophobia");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("Being somewhere with a low ceiling for too long will weaken you and make you slower.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Claustrophobia", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
