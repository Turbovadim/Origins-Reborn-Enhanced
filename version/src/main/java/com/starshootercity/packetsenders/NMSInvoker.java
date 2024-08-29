package com.starshootercity.packetsenders;

import com.destroystokyo.paper.entity.ai.Goal;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class NMSInvoker implements Listener {
    public abstract void sendEntityData(Player player, Entity entity, byte bytes);

    public abstract Goal<Creeper> getCreeperAfraidGoal(LivingEntity creeper, Predicate<Player> hasAbility, Predicate<LivingEntity> hasKey);

    public abstract boolean wasTouchingWater(Player player);

    public abstract float getDestroySpeed(ItemStack item, Material block);

    public abstract float getDestroySpeed(Material block);

    public abstract void setNoPhysics(Player player, boolean noPhysics);

    public abstract void sendPhasingGamemodeUpdate(Player player, GameMode gameMode);

    public abstract void sendResourcePacks(Player player, String pack, Map<?, OriginsRebornResourcePackInfo> extraPacks);

    public abstract @NotNull PotionEffectType getNauseaEffect();

    public abstract @NotNull PotionEffectType getMiningFatigueEffect();

    public abstract @NotNull PotionEffectType getHasteEffect();

    public abstract @NotNull PotionEffectType getJumpBoostEffect();

    public abstract @NotNull PotionEffectType getSlownessEffect();

    public abstract @NotNull PotionEffectType getStrengthEffect();

    public abstract @NotNull Enchantment getUnbreakingEnchantment();

    public abstract @NotNull Enchantment getEfficiencyEnchantment();

    public abstract @NotNull Enchantment getRespirationEnchantment();

    public abstract @NotNull Enchantment getAquaAffinityEnchantment();

    public abstract @NotNull Enchantment getBaneOfArthropodsEnchantment();

    public abstract @Nullable Location getRespawnLocation(Player player);

    public abstract void resetRespawnLocation(Player player);

    public abstract @Nullable AttributeModifier getAttributeModifier(AttributeInstance instance, NamespacedKey key);

    public abstract void dealDryOutDamage(LivingEntity entity, int amount);

    public abstract void dealDrowningDamage(LivingEntity entity, int amount);

    public abstract void dealFreezeDamage(LivingEntity entity, int amount);

    public boolean supportsInfiniteDuration() {
        return true;
    }

    public abstract boolean isUnderWater(LivingEntity entity);

    public abstract void knockback(LivingEntity entity, double strength, double x, double z);

    public abstract void setFlyingFallDamage(Player player, TriState state);

    public abstract void broadcastSlotBreak(Player player, EquipmentSlot slot, Collection<Player> players);

    public abstract void sendBlockDamage(Player player, Location location, float damage, Entity entity);

    public abstract Attribute getBlockInteractionRangeAttribute();

    public abstract Attribute getEntityInteractionRangeAttribute();

    public abstract Attribute getBlockBreakSpeedAttribute();

    public abstract void addAttributeModifier(AttributeInstance instance, NamespacedKey key, String name, double amount, AttributeModifier.Operation operation);

    public abstract void setWorldBorderOverlay(Player player, boolean show);

    public abstract void setComments(String path, List<String> comments);

    public NMSInvoker(FileConfiguration config) {
        this.config = config;
    }

    protected final FileConfiguration config;

    public abstract Component applyFont(Component component, Key font);

    public @Nullable Material getOminousBottle() {
        return null;
    }
}