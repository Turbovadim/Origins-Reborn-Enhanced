package com.starshootercity.packetsenders;

import com.destroystokyo.paper.entity.ai.Goal;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import net.minecraft.Optionull;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.level.GameType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class NMSInvokerV1_21_4 extends NMSInvoker {
    public NMSInvokerV1_21_4(FileConfiguration config) {
        super(config);
    }

    @Override
    public Component applyFont(Component component, Key font) {
        return component.font(font);
    }

    @Override
    public void sendEntityData(Player player, Entity entity, byte bytes) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.world.entity.Entity target = ((CraftEntity) entity).getHandle();

        List<SynchedEntityData.DataValue<?>> eData = new ArrayList<>();
        eData.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), bytes));
        ClientboundSetEntityDataPacket metadata = new ClientboundSetEntityDataPacket(target.getId(), eData);
        serverPlayer.connection.send(metadata);
    }

    @Override
    public Goal<@NotNull Creeper> getCreeperAfraidGoal(LivingEntity creeper, Predicate<Player> hasAbility, Predicate<LivingEntity> hasKey) {
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
    @SuppressWarnings("UnstableApiUsage")
    public float getDestroySpeed(Material block) {
        return ((CraftBlockState) block.createBlockData().createBlockState()).getHandle().destroySpeed;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public float getDestroySpeed(ItemStack item, Material block) {
        net.minecraft.world.level.block.state.BlockState b = ((CraftBlockState) block.createBlockData().createBlockState()).getHandle();
        net.minecraft.world.item.ItemStack handle = CraftItemStack.asNMSCopy(item);
        return handle.getDestroySpeed(b);
    }

    @Override
    public @NotNull Attribute getArmorAttribute() {
        return Attribute.ARMOR;
    }

    @Override
    public @NotNull Attribute getMaxHealthAttribute() {
        return Attribute.MAX_HEALTH;
    }

    @Override
    public @NotNull Attribute getMovementSpeedAttribute() {
        return Attribute.MOVEMENT_SPEED;
    }

    @Override
    public @NotNull Attribute getAttackDamageAttribute() {
        return Attribute.ATTACK_DAMAGE;
    }

    @Override
    public @NotNull Attribute getFlyingSpeedAttribute() {
        return Attribute.FLYING_SPEED;
    }

    @Override
    public @NotNull Attribute getAttackKnockbackAttribute() {
        return Attribute.ATTACK_KNOCKBACK;
    }

    @Override
    public @NotNull Attribute getAttackSpeedAttribute() {
        return Attribute.ATTACK_SPEED;
    }

    @Override
    public @NotNull Attribute getArmorToughnessAttribute() {
        return Attribute.ARMOR_TOUGHNESS;
    }

    @Override
    public @NotNull Attribute getLuckAttribute() {
        return Attribute.LUCK;
    }

    @Override
    public @NotNull Attribute getHorseJumpStrengthAttribute() {
        return Attribute.JUMP_STRENGTH;
    }

    @Override
    public @NotNull Attribute getSpawnReinforcementsAttribute() {
        return Attribute.SPAWN_REINFORCEMENTS;
    }

    @Override
    public @NotNull Attribute getFollowRangeAttribute() {
        return Attribute.FOLLOW_RANGE;
    }

    @Override
    public @NotNull Attribute getKnockbackResistanceAttribute() {
        return Attribute.KNOCKBACK_RESISTANCE;
    }

    @Override
    public @Nullable Attribute getFallDamageMultiplierAttribute() {
        return Attribute.FALL_DAMAGE_MULTIPLIER;
    }

    @Override
    public @Nullable Attribute getMaxAbsorptionAttribute() {
        return Attribute.MAX_ABSORPTION;
    }

    @Override
    public @Nullable Attribute getSafeFallDistanceAttribute() {
        return Attribute.SAFE_FALL_DISTANCE;
    }

    @Override
    public @Nullable Attribute getScaleAttribute() {
        return Attribute.SCALE;
    }

    @Override
    public @Nullable Attribute getStepHeightAttribute() {
        return Attribute.STEP_HEIGHT;
    }

    @Override
    public @Nullable Attribute getGravityAttribute() {
        return Attribute.GRAVITY;
    }

    @Override
    public @Nullable Attribute getJumpStrengthAttribute() {
        return Attribute.JUMP_STRENGTH;
    }

    @Override
    public @Nullable Attribute getBurningTimeAttribute() {
        return Attribute.BURNING_TIME;
    }

    @Override
    public @Nullable Attribute getExplosionKnockbackResistanceAttribute() {
        return Attribute.EXPLOSION_KNOCKBACK_RESISTANCE;
    }

    @Override
    public @Nullable Attribute getMovementEfficiencyAttribute() {
        return Attribute.MOVEMENT_EFFICIENCY;
    }

    @Override
    public @Nullable Attribute getOxygenBonusAttribute() {
        return Attribute.OXYGEN_BONUS;
    }

    @Override
    public @Nullable Attribute getWaterMovementEfficiencyAttribute() {
        return Attribute.WATER_MOVEMENT_EFFICIENCY;
    }

    @Override
    public @Nullable Attribute getTemptRangeAttribute() {
        return Attribute.TEMPT_RANGE;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull ItemMeta setCustomModelData(ItemMeta meta, int cmd) {
        CustomModelDataComponent component = meta.getCustomModelDataComponent();
        component.setStrings(List.of(String.valueOf(cmd)));
        meta.setCustomModelDataComponent(component);
        return meta;
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
        ClientboundPlayerInfoUpdatePacket.Entry entry = new ClientboundPlayerInfoUpdatePacket.Entry(serverPlayer.getUUID(), serverPlayer.getGameProfile(), true, 1, gameType, serverPlayer.getTabListDisplayName(), true, 0, Optionull.map(serverPlayer.getChatSession(), RemoteChatSession::asData));
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE), entry);
        serverPlayer.connection.send(packet);
    }

    @Override
    public void sendResourcePacks(Player player, String pack, Map<?, OriginsRebornResourcePackInfo> extraPacks) {
        try {
            ResourcePackInfo packInfo = ResourcePackInfo.resourcePackInfo()
                    .uri(URI.create(pack))
                    .computeHashAndBuild().get();
            List<ResourcePackInfo> packs = new ArrayList<>();
            packs.add(packInfo);
            for (OriginsRebornResourcePackInfo originsRebornResourcePackInfo : extraPacks.values()) {
                if (originsRebornResourcePackInfo.packInfo() instanceof ResourcePackInfo info) {
                    packs.add(info);
                }
            }
            player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                    .packs(packs)
                    .required(true)
                    .build()
            );
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull PotionEffectType getNauseaEffect() {
        return PotionEffectType.NAUSEA;
    }

    @Override
    public @NotNull PotionEffectType getMiningFatigueEffect() {
        return PotionEffectType.MINING_FATIGUE;
    }

    @Override
    public @NotNull PotionEffectType getHasteEffect() {
        return PotionEffectType.HASTE;
    }

    @Override
    public @NotNull Enchantment getUnbreakingEnchantment() {
        return Enchantment.UNBREAKING;
    }

    @Override
    public @NotNull Enchantment getEfficiencyEnchantment() {
        return Enchantment.EFFICIENCY;
    }

    @Override
    public @NotNull Enchantment getRespirationEnchantment() {
        return Enchantment.RESPIRATION;
    }

    @Override
    public @NotNull PotionEffectType getJumpBoostEffect() {
        return PotionEffectType.JUMP_BOOST;
    }

    @Override
    public @NotNull Enchantment getAquaAffinityEnchantment() {
        return Enchantment.AQUA_AFFINITY;
    }

    @Override
    public @NotNull PotionEffectType getSlownessEffect() {
        return PotionEffectType.SLOWNESS;
    }

    @Override
    public @NotNull Enchantment getBaneOfArthropodsEnchantment() {
        return Enchantment.BANE_OF_ARTHROPODS;
    }

    @Override
    public @Nullable Location getRespawnLocation(Player player) {
        return player.getRespawnLocation();
    }

    @Override
    public void resetRespawnLocation(Player player) {
        player.setRespawnLocation(null);
    }

    @Override
    public @Nullable AttributeModifier getAttributeModifier(AttributeInstance instance, NamespacedKey key) {
        return instance.getModifier(key);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void addAttributeModifier(AttributeInstance instance, NamespacedKey key, String name, double amount, AttributeModifier.Operation operation) {
        instance.addModifier(new AttributeModifier(key, amount, operation, EquipmentSlotGroup.ANY));
    }

    @Override
    public @NotNull PotionEffectType getStrengthEffect() {
        return PotionEffectType.STRENGTH;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void dealDryOutDamage(LivingEntity entity, int amount) {
        entity.damage(amount, DamageSource.builder(DamageType.DRY_OUT).build());
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void dealFreezeDamage(LivingEntity entity, int amount) {
        entity.damage(amount, DamageSource.builder(DamageType.FREEZE).build());
    }

    @Override
    public boolean isUnderWater(LivingEntity entity) {
        return entity.isUnderWater();
    }

    @Override
    public void knockback(LivingEntity entity, double strength, double x, double z) {
        entity.knockback(strength, x, z);
    }

    @Override
    public void setFlyingFallDamage(Player player, TriState state) {
        player.setFlyingFallDamage(state);
    }

    @Override
    public void broadcastSlotBreak(Player player, EquipmentSlot slot, Collection<Player> players) {
        player.broadcastSlotBreak(slot, players);
    }

    @Override
    public void sendBlockDamage(Player player, Location location, float damage, Entity entity) {
        player.sendBlockDamage(location, damage, entity);
    }

    @Override
    public Attribute getBlockInteractionRangeAttribute() {
        return Attribute.BLOCK_INTERACTION_RANGE;
    }

    @Override
    public Attribute getEntityInteractionRangeAttribute() {
        return Attribute.ENTITY_INTERACTION_RANGE;
    }

    @Override
    public Attribute getBlockBreakSpeedAttribute() {
        return Attribute.BLOCK_BREAK_SPEED;
    }

    @Override
    public @Nullable Attribute getMiningEfficiencyAttribute() {
        return Attribute.MINING_EFFICIENCY;
    }

    @Override
    public @Nullable Attribute getSneakingSpeedAttribute() {
        return Attribute.SNEAKING_SPEED;
    }

    @Override
    public @Nullable Attribute getSubmergedMiningSpeedAttribute() {
        return Attribute.SUBMERGED_MINING_SPEED;
    }

    @Override
    public @Nullable Attribute getSweepingDamageRatioAttribute() {
        return Attribute.SWEEPING_DAMAGE_RATIO;
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
    @SuppressWarnings("UnstableApiUsage")
    public void dealDrowningDamage(LivingEntity entity, int amount) {
        entity.damage(amount, DamageSource.builder(DamageType.DROWN).build());
    }

    @EventHandler
    public void onBlockDamageAbort(BlockDamageAbortEvent event) {
        new OriginsRebornBlockDamageAbortEvent(event.getPlayer(), event.getBlock(), event.getItemInHand()).callEvent();
    }

    @Override
    public @Nullable Material getOminousBottle() {
        return Material.OMINOUS_BOTTLE;
    }
}