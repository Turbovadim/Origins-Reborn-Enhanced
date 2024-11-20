package com.starshootercity.packetsenders;

import com.destroystokyo.paper.entity.ai.Goal;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class NMSInvokerV1_19_2 extends NMSInvoker {

    @Override
    public @Nullable Attribute getMiningEfficiencyAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getSneakingSpeedAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getSubmergedMiningSpeedAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getSweepingDamageRatioAttribute() {
        return null;
    }

    @Override
    public @NotNull Attribute getFlyingSpeedAttribute() {
        return Attribute.GENERIC_FLYING_SPEED;
    }

    @Override
    public @NotNull Attribute getAttackKnockbackAttribute() {
        return Attribute.GENERIC_ATTACK_KNOCKBACK;
    }

    @Override
    public @NotNull Attribute getAttackSpeedAttribute() {
        return Attribute.GENERIC_ATTACK_SPEED;
    }

    @Override
    public @NotNull Attribute getArmorToughnessAttribute() {
        return Attribute.GENERIC_ARMOR_TOUGHNESS;
    }

    @Override
    public @NotNull Attribute getLuckAttribute() {
        return Attribute.GENERIC_LUCK;
    }

    @Override
    public @NotNull Attribute getHorseJumpStrengthAttribute() {
        return Attribute.HORSE_JUMP_STRENGTH;
    }

    @Override
    public @NotNull Attribute getSpawnReinforcementsAttribute() {
        return Attribute.ZOMBIE_SPAWN_REINFORCEMENTS;
    }

    @Override
    public @NotNull Attribute getFollowRangeAttribute() {
        return Attribute.GENERIC_FOLLOW_RANGE;
    }

    @Override
    public @NotNull Attribute getKnockbackResistanceAttribute() {
        return Attribute.GENERIC_KNOCKBACK_RESISTANCE;
    }

    @Override
    public @Nullable Attribute getFallDamageMultiplierAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getMaxAbsorptionAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getSafeFallDistanceAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getScaleAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getStepHeightAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getGravityAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getJumpStrengthAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getBurningTimeAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getExplosionKnockbackResistanceAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getMovementEfficiencyAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getOxygenBonusAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getWaterMovementEfficiencyAttribute() {
        return null;
    }

    @Override
    public @Nullable Attribute getTemptRangeAttribute() {
        return null;
    }

    public NMSInvokerV1_19_2(FileConfiguration config) {
        super(config);
    }

    @Override
    public void dealDrowningDamage(LivingEntity entity, int amount) {
        net.minecraft.world.entity.LivingEntity livingEntity = ((CraftLivingEntity) entity).getHandle();
        livingEntity.hurt(DamageSource.DROWN, amount);
    }

    @Override
    public @NotNull Enchantment getRespirationEnchantment() {
        return Enchantment.OXYGEN;
    }

    @Override
    public Component applyFont(Component component, Key font) {
        return component.font(font);
    }

    @EventHandler
    public void onBlockDamageAbort(BlockDamageAbortEvent event) {
        new OriginsRebornBlockDamageAbortEvent(event.getPlayer(), event.getBlock(), event.getItemInHand()).callEvent();
    }

    @Override
    public void sendEntityData(Player player, Entity entity, byte bytes) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        SynchedEntityData data = ((CraftEntity) entity).getHandle().getEntityData();
        data.set(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), bytes);
        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(entity.getEntityId(), data, false);
        serverPlayer.connection.send(packet);
    }

    @Override
    public Goal<Creeper> getCreeperAfraidGoal(LivingEntity creeper, Predicate<Player> hasAbility, Predicate<LivingEntity> hasKey) {
        return new AvoidEntityGoal<>(
                (PathfinderMob) ((CraftEntity) creeper).getHandle(),
                net.minecraft.world.entity.player.Player.class,
                6,
                1,
                1.2,
                livingEntity -> {
                    if (livingEntity.getBukkitEntity() instanceof Player player) {
                        if (hasAbility.test(player)) {
                            return (!hasKey.test(creeper));
                        }
                    }
                    return false;
                }

        ).asPaperVanillaGoal();
    }

    @Override
    public boolean wasTouchingWater(Player player) {
        return ((CraftPlayer) player).getHandle().wasTouchingWater;
    }

    @Override
    public float getDestroySpeed(ItemStack item, Material block) {
        BlockState b = ((CraftBlockData) block.createBlockData()).getState();
        net.minecraft.world.item.ItemStack handle = CraftItemStack.asNMSCopy(item);
        return handle.getDestroySpeed(b);
    }

    @Override
    public float getDestroySpeed(Material block) {
        return ((CraftBlockData) block.createBlockData()).getState().destroySpeed;
    }

    @Override
    public void setNoPhysics(Player player, boolean noPhysics) {
        ((CraftPlayer) player).getHandle().noPhysics = noPhysics;
    }

    @Override
    public void sendPhasingGamemodeUpdate(Player player, GameMode gameMode) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

        GameType gameType = switch (gameMode) {
            case CREATIVE -> GameType.CREATIVE;
            case SURVIVAL -> GameType.SURVIVAL;
            case ADVENTURE -> GameType.ADVENTURE;
            case SPECTATOR -> GameType.SPECTATOR;
        };

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeEnum(ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE);
        buf.writeCollection(List.of("null"), (buf2, e) -> {
            buf2.writeUUID(serverPlayer.getUUID());
            buf2.writeVarInt(gameType.getId());
        });

        ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(buf);

        serverPlayer.connection.send(packet);
    }

    @Override
    public @NotNull Attribute getArmorAttribute() {
        return Attribute.GENERIC_ARMOR;
    }

    @Override
    public @NotNull Attribute getMaxHealthAttribute() {
        return Attribute.GENERIC_MAX_HEALTH;
    }

    @Override
    public @NotNull Attribute getMovementSpeedAttribute() {
        return Attribute.GENERIC_MOVEMENT_SPEED;
    }

    @Override
    public @NotNull Attribute getAttackDamageAttribute() {
        return Attribute.GENERIC_ATTACK_DAMAGE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendResourcePacks(Player player, String pack, Map<?, OriginsRebornResourcePackInfo> extraPacks) {
        player.setResourcePack(pack);
    }

    @Override
    public @NotNull PotionEffectType getNauseaEffect() {
        return PotionEffectType.CONFUSION;
    }

    @Override
    public @NotNull PotionEffectType getMiningFatigueEffect() {
        return PotionEffectType.SLOW_DIGGING;
    }

    @Override
    public @NotNull PotionEffectType getHasteEffect() {
        return PotionEffectType.FAST_DIGGING;
    }

    @Override
    public @NotNull PotionEffectType getJumpBoostEffect() {
        return PotionEffectType.JUMP;
    }

    @Override
    public @NotNull PotionEffectType getSlownessEffect() {
        return PotionEffectType.SLOW;
    }

    @Override
    public @NotNull PotionEffectType getStrengthEffect() {
        return PotionEffectType.INCREASE_DAMAGE;
    }

    @Override
    public @NotNull Enchantment getUnbreakingEnchantment() {
        return Enchantment.DURABILITY;
    }

    @Override
    public @NotNull Enchantment getAquaAffinityEnchantment() {
        return Enchantment.WATER_WORKER;
    }

    @Override
    public @NotNull Enchantment getBaneOfArthropodsEnchantment() {
        return Enchantment.DAMAGE_ARTHROPODS;
    }

    @Override
    public @NotNull Enchantment getEfficiencyEnchantment() {
        return Enchantment.DIG_SPEED;
    }

    @Override
    public @Nullable Location getRespawnLocation(Player player) {
        return player.getBedSpawnLocation();
    }

    @Override
    public void resetRespawnLocation(Player player) {
        player.setBedSpawnLocation(null);
    }

    @Override
    public @Nullable AttributeModifier getAttributeModifier(AttributeInstance instance, NamespacedKey key) {
        UUID u = UUID.nameUUIDFromBytes(key.toString().getBytes());
        for (AttributeModifier am : instance.getModifiers()) {
            if (am.getUniqueId().equals(u)) return am;
        }
        return null;
    }

    @Override
    public void addAttributeModifier(AttributeInstance instance, NamespacedKey key, String name, double amount, AttributeModifier.Operation operation) {
        instance.addModifier(new AttributeModifier(UUID.nameUUIDFromBytes(key.toString().getBytes()), name, amount, operation));
    }

    @Override
    public boolean supportsInfiniteDuration() {
        return false;
    }

    @Override
    public boolean isUnderWater(LivingEntity entity) {
        return ((CraftLivingEntity) entity).getHandle().isUnderWater();
    }

    @Override
    public void knockback(LivingEntity entity, double strength, double x, double z) {
        ((CraftLivingEntity) entity).getHandle().knockback(strength, x, z);
    }

    @Override
    public Attribute getBlockInteractionRangeAttribute() {
        return null;
    }

    @Override
    public Attribute getEntityInteractionRangeAttribute() {
        return null;
    }

    @Override
    public void dealDryOutDamage(LivingEntity entity, int amount) {
        net.minecraft.world.entity.LivingEntity livingEntity = ((CraftLivingEntity) entity).getHandle();
        livingEntity.hurt(DamageSource.DRY_OUT, amount);
    }

    @Override
    public void dealFreezeDamage(LivingEntity entity , int amount) {
        net.minecraft.world.entity.LivingEntity livingEntity = ((CraftLivingEntity) entity).getHandle();
        livingEntity.hurt(DamageSource.FREEZE, amount);
    }

    @Override
    public void broadcastSlotBreak(Player player, EquipmentSlot slot, Collection<Player> players) {
        player.broadcastSlotBreak(slot, players);
    }

    @Override
    public void sendBlockDamage(Player player, Location location, float damage, Entity entity) {
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(entity.getEntityId(), new BlockPos(location.getX(), location.getY(), location.getZ()), (int) (damage*10));
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    public void setFlyingFallDamage(Player player, TriState state) {
        flyingFallDamage.put(player, state);
    }

    public Map<Player, TriState> flyingFallDamage = new HashMap<>();

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getAllowFlight()) {
                if (flyingFallDamage.get(player) == TriState.FALSE) event.setCancelled(true);
            }
        }
    }

    @Override
    public Attribute getBlockBreakSpeedAttribute() {
        return null;
    }

    @Override
    public void setWorldBorderOverlay(Player player, boolean show) {
        if (show) {
            WorldBorder border = Bukkit.createWorldBorder();
            border.setCenter(player.getWorld().getWorldBorder().getCenter());
            border.setSize(player.getWorld().getWorldBorder().getSize());
            border.setWarningDistance((int) (player.getWorld().getWorldBorder().getSize()*2));
            player.setWorldBorder(border);
        } else player.setWorldBorder(null);
    }

    @Override
    public void setComments(String path, List<String> comments) {
        config.setComments(path, comments);
    }
}