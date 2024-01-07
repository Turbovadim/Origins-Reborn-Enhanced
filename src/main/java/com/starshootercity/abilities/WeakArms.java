package com.starshootercity.abilities;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import com.starshootercity.SavedPotionEffect;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeakArms implements VisibleAbility, Listener {
    private static final List<Material> naturalStones = new ArrayList<>() {{
        add(Material.STONE);
        add(Material.TUFF);
        add(Material.ANDESITE);
        add(Material.SANDSTONE);
        add(Material.SMOOTH_SANDSTONE);
        add(Material.RED_SANDSTONE);
        add(Material.SMOOTH_RED_SANDSTONE);
        add(Material.DEEPSLATE);
        add(Material.BLACKSTONE);
        add(Material.NETHERRACK);
    }};
    Map<Player, SavedPotionEffect> storedEffects = new HashMap<>();

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AbilityRegister.runForAbility(player, getKey(), () -> {
                Block target = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
                PotionEffect strength = player.getPotionEffect(PotionEffectType.INCREASE_DAMAGE);
                int sides = 3;
                if (strength != null && target != null && naturalStones.contains(target.getType())) {
                    sides = 0;
                    if (naturalStones.contains(target.getRelative(BlockFace.DOWN).getType())) sides++;
                    if (naturalStones.contains(target.getRelative(BlockFace.UP).getType())) sides++;
                    if (naturalStones.contains(target.getRelative(BlockFace.WEST).getType())) sides++;
                    if (naturalStones.contains(target.getRelative(BlockFace.EAST).getType())) sides++;
                    if (naturalStones.contains(target.getRelative(BlockFace.NORTH).getType())) sides++;
                    if (naturalStones.contains(target.getRelative(BlockFace.SOUTH).getType())) sides++;
                }
                if (sides > 2 && strength == null && target != null && naturalStones.contains(target.getType())) {
                    PotionEffect effect = player.getPotionEffect(PotionEffectType.SLOW_DIGGING);
                    boolean ambient = false;
                    boolean showParticles = false;
                    if (effect != null) {
                        ambient = effect.isAmbient();
                        showParticles = effect.hasParticles();
                        if (effect.getAmplifier() != -1) {
                            storedEffects.put(player, new SavedPotionEffect(effect, Bukkit.getCurrentTick()));
                            player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                        }
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, PotionEffect.INFINITE_DURATION, -1, ambient, showParticles));
                } else {
                    if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                        PotionEffect effect = player.getPotionEffect(PotionEffectType.SLOW_DIGGING);
                        if (effect != null) {
                            if (effect.getAmplifier() == -1) player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                        }
                    }
                    if (storedEffects.containsKey(player)) {
                        SavedPotionEffect effect = storedEffects.get(player);
                        storedEffects.remove(player);
                        PotionEffect potionEffect = effect.effect();
                        int time = potionEffect.getDuration() - (Bukkit.getCurrentTick() - effect.currentTime());
                        if (time > 0) {
                            player.addPotionEffect(new PotionEffect(
                                    potionEffect.getType(),
                                    time,
                                    potionEffect.getAmplifier(),
                                    potionEffect.isAmbient(),
                                    potionEffect.hasParticles()
                            ));
                        }
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            storedEffects.remove(event.getPlayer());
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:weak_arms");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("When not under the effect of a strength potion, you can only mine natural stone if there are at most 2 other natural stone blocks adjacent to it.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Weak Arms", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

}
