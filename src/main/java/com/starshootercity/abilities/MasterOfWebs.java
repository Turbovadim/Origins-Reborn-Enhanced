package com.starshootercity.abilities;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterOfWebs implements FlightAllowingAbility, Listener, VisibleAbility {
    private final Map<Player, List<Entity>> glowingEntities = new HashMap<>();

    private final Map<Player, Integer> cobwebSpawnCooldowns = new HashMap<>();
    private final List<Location> temporaryCobwebs = new ArrayList<>();

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (temporaryCobwebs.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
            temporaryCobwebs.remove(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            AbilityRegister.runForAbility(player, getKey(), () -> {
                Integer cooldown = cobwebSpawnCooldowns.get(player);
                if ((cooldown == null || Bukkit.getCurrentTick() - cooldown >= 120) && !event.getEntity().getLocation().getBlock().isSolid()) {
                    cobwebSpawnCooldowns.put(player, Bukkit.getCurrentTick());
                    Location location = event.getEntity().getLocation().getBlock().getLocation();
                    temporaryCobwebs.add(location);
                    location.getBlock().setType(Material.COBWEB);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(OriginsReborn.getInstance(), () -> {
                        if (location.getBlock().getType() == Material.COBWEB && temporaryCobwebs.contains(location)) {
                            temporaryCobwebs.remove(location);
                            location.getBlock().setType(Material.AIR);
                        }
                    }, 60);
                }
            });
        }
    }


    private void setCanFly(Player player, boolean setFly) {
        if (setFly) player.setAllowFlight(true);
        canFly.put(player, setFly);
    }

    private final Map<Player, Boolean> canFly = new HashMap<>();


    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        for (Player webMaster : Bukkit.getOnlinePlayers()) {
            AbilityRegister.runForAbility(webMaster, getKey(), () -> {
                if (isInCobweb(webMaster)) {
                    setCanFly(webMaster, true);
                    webMaster.setFlying(true);
                } else {
                    setCanFly(webMaster, false);
                }
                List<Entity> entities = webMaster.getNearbyEntities(16, 16, 16);
                entities.removeIf(entity -> !(entity instanceof LivingEntity));
                if (entities.size() > 16) entities = entities.subList(0, 16);
                entities.addAll(Bukkit.getOnlinePlayers());
                entities.removeIf(entity -> entity.getWorld() != webMaster.getWorld());
                entities.removeIf(entity -> entity.getLocation().distance(webMaster.getLocation()) > 16);
                for (Entity webStuck : entities) {
                    AbilityRegister.runWithoutAbility(webStuck, getKey(), () -> {
                        if (webStuck != webMaster) {
                            if (!glowingEntities.containsKey(webMaster)) {
                                glowingEntities.put(webMaster, new ArrayList<>());
                            }
                            if (isInCobweb(webStuck)) {
                                if (!glowingEntities.get(webMaster).contains(webStuck)) {
                                    glowingEntities.get(webMaster).add(webStuck);
                                }

                                ServerPlayer master = ((CraftPlayer) webMaster).getHandle();
                                net.minecraft.world.entity.Entity stuck = ((CraftEntity) webStuck).getHandle();

                                byte data = 0x40;
                                if (webStuck.getFireTicks() > 0) {
                                    data += 0x01;
                                }
                                if (webStuck.isSneaking()) {
                                    data += 0x02;
                                }
                                if (webStuck instanceof Player stuckPlayer) {
                                    if (stuckPlayer.isSprinting()) {
                                        data += 0x08;
                                    }
                                    if (stuckPlayer.isSwimming()) {
                                        data += 0x10;
                                    }
                                    if (stuckPlayer.isInvisible()) {
                                        data += 0x20;
                                    }
                                    if (stuckPlayer.isGliding()) {
                                        data += 0x80;
                                    }
                                }
                                List<SynchedEntityData.DataValue<?>> eData = new ArrayList<>();
                                eData.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), data));
                                ClientboundSetEntityDataPacket metadata = new ClientboundSetEntityDataPacket(stuck.getId(), eData);
                                master.connection.send(metadata);
                            } else {
                                glowingEntities.get(webMaster).remove(webStuck);
                                AbilityRegister.updateEntity(webMaster, webStuck);
                            }
                        }
                    });
                }
            });
        }
    }

    public MasterOfWebs() {
        NamespacedKey recipeKey = new NamespacedKey(OriginsReborn.getInstance(), "web-recipe");
        ShapelessRecipe webRecipe = new ShapelessRecipe(recipeKey, new ItemStack(Material.COBWEB));
        if (Bukkit.getRecipe(recipeKey) == null) {
            webRecipe.addIngredient(Material.STRING);
            webRecipe.addIngredient(Material.STRING);
            Bukkit.addRecipe(webRecipe);
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() != null) {
            if (event.getRecipe().getResult().getType() == Material.COBWEB) {
                for (HumanEntity entity : event.getInventory().getViewers()) {
                    if (entity instanceof Player player) {
                        AbilityRegister.runWithoutAbility(player, getKey(), () -> event.getInventory().setResult(null));
                    }
                }
            }
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:master_of_webs");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You navigate cobweb perfectly, and are able to climb in them. When you hit an enemy in melee, they get stuck in cobweb for a while. Non-arthropods stuck in cobweb will be sensed by you. You are able to craft cobweb from string.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Master of Webs", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    public boolean isInCobweb(Entity entity) {
        for (Block start : new ArrayList<Block>() {{
            add(entity.getLocation().getBlock());
            add(entity.getLocation().getBlock().getRelative(BlockFace.UP));
        }}) {
            if (start.getType() == Material.COBWEB) return true;
            for (BlockFace face : BlockFace.values()) {
                Block block = start.getRelative(face);
                if (block.getType() != Material.COBWEB) continue;
                if (entity.getBoundingBox().overlaps(block.getBoundingBox())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canFly(Player player) {
        return canFly.getOrDefault(player, false);
    }

    @Override
    public float getFlightSpeed(Player player) {
        return 0.04f;
    }
}
