package com.starshootercity.abilities;

import net.kyori.adventure.key.Key;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StrongArmsBreakSpeed implements BreakSpeedModifierAbility, Listener {
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
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:strong_arms_break_speed");
    }
    @Override
    @SuppressWarnings("deprecation")
    public BlockMiningContext provideContextFor(Player player) {
        boolean aquaAffinity = false;
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null) {
            if (helmet.containsEnchantment(Enchantment.WATER_WORKER)) aquaAffinity = true;
        }
        return new BlockMiningContext(
                new ItemStack(Material.IRON_PICKAXE),
                player.getPotionEffect(PotionEffectType.SLOW_DIGGING),
                player.getPotionEffect(PotionEffectType.FAST_DIGGING),
                player.getPotionEffect(PotionEffectType.CONDUIT_POWER),
                player.isUnderWater(),
                aquaAffinity,
                player.isOnGround()
        );
    }

    @Override
    public boolean shouldActivate(Player player) {
        Block target = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
        return !Tag.ITEMS_PICKAXES.isTagged(player.getInventory().getItemInMainHand().getType()) && target != null && naturalStones.contains(target.getType());
    }

    public static class StrongArmsFastBlockBreakEvent extends BlockBreakEvent {
        public StrongArmsFastBlockBreakEvent(@NotNull Block theBlock, @NotNull Player player) {
            super(theBlock, player);
        }
    }
}
