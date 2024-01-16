package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StrongArms implements VisibleAbility, Listener {
    private static final List<Material> naturalStones = new ArrayList<>() {{
        add(Material.STONE);
        add(Material.TUFF);
        add(Material.GRANITE);
        add(Material.DIORITE);
        add(Material.ANDESITE);
        add(Material.SANDSTONE);
        add(Material.SMOOTH_SANDSTONE);
        add(Material.RED_SANDSTONE);
        add(Material.SMOOTH_RED_SANDSTONE);
        add(Material.DEEPSLATE);
        add(Material.BLACKSTONE);
        add(Material.NETHERRACK);
    }};

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            if (naturalStones.contains(event.getBlock().getType())) {
                if (!Tag.ITEMS_PICKAXES.isTagged(event.getPlayer().getInventory().getItemInMainHand().getType())) {
                    event.setCancelled(true);
                    ItemStack item = new ItemStack(Material.IRON_PICKAXE);
                    item.addEnchantments(event.getPlayer().getInventory().getItemInMainHand().getEnchantments());
                    event.getBlock().breakNaturally(item, event instanceof StrongArmsBreakSpeed.StrongArmsFastBlockBreakEvent);
                }
            }
        });
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:strong_arms");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You are strong enough to break natural stones without using a pickaxe.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Strong Arms", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
