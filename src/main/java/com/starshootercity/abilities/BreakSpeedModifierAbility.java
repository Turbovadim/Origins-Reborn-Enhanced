package com.starshootercity.abilities;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.Origin;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import com.starshootercity.SavedPotionEffect;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public interface BreakSpeedModifierAbility extends Ability {
    BlockMiningContext provideContextFor(Player player);
    boolean shouldActivate(Player player);

    class BlockMiningContext {
        private final PotionEffect slowDigging;
        private final PotionEffect fastDigging;
        private final PotionEffect conduitPower;
        private final ItemStack heldItem;
        private final boolean underwater;
        private final boolean aquaAffinity;
        private final boolean onGround;
        public BlockMiningContext(ItemStack heldItem, @Nullable PotionEffect slowDigging, @Nullable PotionEffect fastDigging, @Nullable PotionEffect conduitPower, boolean underwater, boolean aquaAffinity, boolean onGround) {
            this.heldItem = heldItem;
            this.slowDigging = slowDigging;
            this.fastDigging = fastDigging;
            this.conduitPower = conduitPower;
            this.underwater = underwater;
            this.aquaAffinity = aquaAffinity;
            this.onGround = onGround;
        }

        public boolean isOnGround() {
            return onGround;
        }

        public boolean hasAquaAffinity() {
            return aquaAffinity;
        }

        public boolean isUnderwater() {
            return underwater;
        }

        public ItemStack getHeldItem() {
            return heldItem;
        }

        public boolean hasDigSpeed() {
            return fastDigging != null || conduitPower != null;
        }

        public boolean hasDigSlowdown() {
            return slowDigging != null;
        }

        public int getDigSlowdown() {
            if (slowDigging == null) return 0;
            return slowDigging.getAmplifier();
        }

        public int getDigSpeedAmplification() {
            int i = 0;
            int j = 0;
            if (fastDigging != null) {
                i = fastDigging.getAmplifier();
            }
            if (conduitPower != null) {
                j = conduitPower.getAmplifier();
            }
            return Math.max(i, j);
        }
    }


    class BreakSpeedModifierAbilityListener implements Listener {
        Random random = new Random();
        @EventHandler
        public void onBlockDamage(BlockDamageEvent event) {
            if (blockbreakingTasks.containsKey(event.getPlayer())) {
                cancelTask(blockbreakingTasks.get(event.getPlayer()));
                blockbreakingTasks.remove(event.getPlayer());
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> {

                Origin origin = OriginSwapper.getOrigin(event.getPlayer());
                if (origin == null) return;
                BreakSpeedModifierAbility speedModifierAbility = null;
                for (Ability ability : origin.getAbilities()) {
                    if (ability instanceof BreakSpeedModifierAbility modifierAbility) {
                        if (modifierAbility.shouldActivate(event.getPlayer())) {
                            speedModifierAbility = modifierAbility;
                            break;
                        }
                    }
                }
                if (speedModifierAbility == null) return;
                AtomicInteger time = new AtomicInteger();
                Entity marker = event.getPlayer().getWorld().spawnEntity(event.getPlayer().getLocation(), EntityType.MARKER);
                BreakSpeedModifierAbility finalSpeedModifierAbility = speedModifierAbility;
                int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(OriginsReborn.getInstance(), () -> {

                    BreakSpeedModifierAbility.BlockMiningContext context = finalSpeedModifierAbility.provideContextFor(event.getPlayer());

                    float damage = getBlockDamage(event.getBlock(), context, time.getAndIncrement());
                    if (damage >= 1) {
                        int taskNum = blockbreakingTasks.get(event.getPlayer());
                        cancelTask(taskNum);
                        BlockBreakEvent blockBreakEvent = new StrongArmsBreakSpeed.StrongArmsFastBlockBreakEvent(event.getBlock(), event.getPlayer());
                        blockBreakEvent.callEvent();
                        ItemStack handItem = event.getPlayer().getInventory().getItemInMainHand();
                        if (Tag.ITEMS_TOOLS.isTagged(handItem.getType())) {
                            int unbreakingLevel = handItem.getEnchantmentLevel(Enchantment.DURABILITY) + 1;
                            int itemDamage = 0;
                            if (random.nextDouble() <= 1d / unbreakingLevel) {
                                itemDamage += 1;
                            }
                            if (event.getBlock().getDrops(context.getHeldItem()).size() == 0) {
                                if (random.nextDouble() <= 1d / unbreakingLevel) {
                                    itemDamage += 1;
                                }
                            }
                            if (handItem.getItemMeta() instanceof Damageable damageable) {
                                damageable.setDamage(damageable.getDamage() + itemDamage);
                                if (handItem.getType().getMaxDurability() <= damageable.getDamage()) {
                                    event.getPlayer().broadcastSlotBreak(EquipmentSlot.HAND, new ArrayList<>() {{
                                        for (Player player : Bukkit.getOnlinePlayers()) {
                                            if (player.getWorld() != event.getPlayer().getWorld()) continue;
                                            if (player.getLocation().distance(event.getPlayer().getLocation()) < 32) {
                                                add(player);
                                            }
                                        }
                                    }});
                                    event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                                } else handItem.setItemMeta(damageable);
                            }
                        }
                        if (!blockBreakEvent.isCancelled()) {
                            event.getBlock().breakNaturally(event.getPlayer().getInventory().getItemInMainHand(), true, true);
                        }
                        return;
                    }
                    event.getPlayer().sendBlockDamage(event.getBlock().getLocation(), damage, marker);

                    Block target = event.getPlayer().getTargetBlockExact(8, FluidCollisionMode.NEVER);
                    if (target == null || !target.getLocation().equals(event.getBlock().getLocation())) {
                        int taskNum = blockbreakingTasks.get(event.getPlayer());
                        cancelTask(taskNum);
                    }
                }, 1, 0);
                blockbreakingTasks.put(event.getPlayer(), task);
                taskEntityMap.put(task, marker);
                taskBlockMap.put(task, event.getBlock());
                taskPlayerMap.put(task, event.getPlayer());
            });
        }

        private final Map<Integer, Entity> taskEntityMap = new HashMap<>();
        private final Map<Integer, Player> taskPlayerMap = new HashMap<>();
        private final Map<Integer, Block> taskBlockMap = new HashMap<>();
        private final Map<Player, Integer> blockbreakingTasks = new HashMap<>();

        private void cancelTask(int task) {
            Bukkit.getScheduler().cancelTask(task);
            if (taskEntityMap.containsKey(task)) {
                Entity marker = taskEntityMap.get(task);
                taskPlayerMap.get(task).sendBlockDamage(taskBlockMap.get(task).getLocation(), 0, marker);
                marker.remove();
                taskEntityMap.remove(task);
                taskBlockMap.remove(task);
                taskPlayerMap.remove(task);
            }
        }

        private static float getBlockDamage(Block block, BreakSpeedModifierAbility.BlockMiningContext context, int time) {
            return (float) (Math.round(getDestroySpeed(context, block.getType()) * time * 1000) / 1000) / (block.getDrops(context.getHeldItem()).isEmpty() ? 100 : 30);
        }


        @SuppressWarnings("UnstableApiUsage")
        public static float getDestroySpeed(BreakSpeedModifierAbility.BlockMiningContext context, Material blockType) {
            net.minecraft.world.level.block.state.BlockState block = ((CraftBlockState) blockType.createBlockData().createBlockState()).getHandle();
            net.minecraft.world.item.ItemStack handle = CraftItemStack.asNMSCopy(context.getHeldItem());
            float f;
            if (handle != null) {
                f = handle.getDestroySpeed(block);
            } else f = 1;

            if (f > 1.0F) {
                ItemStack itemstack = context.getHeldItem();
                int i = itemstack.getEnchantmentLevel(Enchantment.DIG_SPEED);

                if (i > 0 && !itemstack.isEmpty()) {
                    f += (float) (i * i + 1);
                }
            }

            if (context.hasDigSpeed()) {
                f *= 1.0F + (float) (context.getDigSpeedAmplification() + 1) * 0.2F;
            }

            if (context.hasDigSlowdown()) {
                float f1;
                int digSlowdown = context.getDigSlowdown();
                f1 = switch (digSlowdown) {
                    case 0 -> 0.3F;
                    case 1 -> 0.09F;
                    case 2 -> 0.0027F;
                    default -> 1;
                };
                f *= f1;
            }

            if (context.isUnderwater() && !context.hasAquaAffinity()) {
                f /= 5.0F;
            }

            if (!context.isOnGround()) {
                f /= 5.0F;
            }

            float d = block.destroySpeed;

            return f / d;
        }

        @EventHandler
        public void onBlockDamage(BlockDamageAbortEvent event) {
            if (blockbreakingTasks.containsKey(event.getPlayer())) {
                int taskNum = blockbreakingTasks.get(event.getPlayer());
                cancelTask(taskNum);
            }
        }

        Map<Player, SavedPotionEffect> storedEffects = new HashMap<>();

        @EventHandler
        public void onServerTickEnd(ServerTickEndEvent event) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Origin origin = OriginSwapper.getOrigin(player);
                if (origin == null) continue;
                BreakSpeedModifierAbility speedModifierAbility = null;
                for (Ability ability : origin.getAbilities()) {
                    if (ability instanceof BreakSpeedModifierAbility modifierAbility) {
                        if (modifierAbility.shouldActivate(player)) {
                            speedModifierAbility = modifierAbility;
                            break;
                        }
                    }
                }
                if (speedModifierAbility != null) {
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
                            if (effect.getAmplifier() == -1)
                                player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
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
            }
        }
    }
}
