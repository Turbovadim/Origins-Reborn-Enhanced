package com.starshootercity.abilities;

import com.destroystokyo.paper.MaterialTags;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StrongArms implements MultiAbility, VisibleAbility, Listener {

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

    @Override
    public List<Ability> getAbilities() {
        return List.of(StrongArmsDrops.strongArmsDrops, StrongArmsBreakSpeed.strongArmsBreakSpeed);
    }

    public static class StrongArmsDrops implements Ability, Listener {
        public static StrongArmsDrops strongArmsDrops = new StrongArmsDrops();

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

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onBlockBreak(BlockBreakEvent event) {
            AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
                if (naturalStones.contains(event.getBlock().getType())) {
                    if (!MaterialTags.PICKAXES.isTagged(event.getPlayer().getInventory().getItemInMainHand().getType())) {
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
            return Key.key("origins:strong_arms_drops");
        }
    }

    public static class StrongArmsBreakSpeed implements BreakSpeedModifierAbility, Listener {
        public static StrongArmsBreakSpeed strongArmsBreakSpeed = new StrongArmsBreakSpeed();

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
                if (helmet.containsEnchantment(OriginsReborn.getNMSInvoker().getAquaAffinityEnchantment())) aquaAffinity = true;
            }
            return new BlockMiningContext(
                    new ItemStack(Material.IRON_PICKAXE),
                    player.getPotionEffect(OriginsReborn.getNMSInvoker().getMiningFatigueEffect()),
                    player.getPotionEffect(OriginsReborn.getNMSInvoker().getHasteEffect()),
                    player.getPotionEffect(PotionEffectType.CONDUIT_POWER),
                    OriginsReborn.getNMSInvoker().isUnderWater(player),
                    aquaAffinity,
                    player.isOnGround()
            );
        }

        @Override
        public boolean shouldActivate(Player player) {
            Block target = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
            return !MaterialTags.PICKAXES.isTagged(player.getInventory().getItemInMainHand().getType()) && target != null && naturalStones.contains(target.getType());
        }

        public static class StrongArmsFastBlockBreakEvent extends BlockBreakEvent {
            public StrongArmsFastBlockBreakEvent(@NotNull Block theBlock, @NotNull Player player) {
                super(theBlock, player);
            }
        }
    }
}
