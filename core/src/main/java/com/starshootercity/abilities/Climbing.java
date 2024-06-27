package com.starshootercity.abilities;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Climbing implements FlightAllowingAbility, Listener, VisibleAbility {

    NamespacedKey stoppedClimbingKey = new NamespacedKey(OriginsReborn.getInstance(), "stoppedclimbing");
    NamespacedKey startedClimbingKey = new NamespacedKey(OriginsReborn.getInstance(), "startedclimbing");

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AbilityRegister.runForAbility(player, getKey(), () -> {
                boolean anySolid = false;
                boolean anysolidAbove = false;
                for (BlockFace face : new ArrayList<BlockFace>() {{
                    add(BlockFace.WEST);
                    add(BlockFace.EAST);
                    add(BlockFace.NORTH);
                    add(BlockFace.SOUTH);
                }}) {
                    anySolid = player.getLocation().getBlock().getRelative(face).isSolid();
                    anysolidAbove = player.getLocation().getBlock().getRelative(BlockFace.UP).getRelative(face).isSolid();
                    if (anySolid) break;
                }
                setCanFly(player, anySolid);
                if (anySolid) {
                    OriginsReborn.getNMSInvoker().setFlyingFallDamage(player, TriState.TRUE);
                }
                if (player.getAllowFlight() && anysolidAbove) {
                    if (!Boolean.TRUE.equals(player.getPersistentDataContainer().get(stoppedClimbingKey, OriginSwapper.BooleanPDT.BOOLEAN))) {
                        if (!player.isOnGround()) player.setFlying(true);
                    } else {
                        if (player.isOnGround()) player.getPersistentDataContainer().set(stoppedClimbingKey, OriginSwapper.BooleanPDT.BOOLEAN, false);
                    }
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
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!event.isFlying()) {
            Long time = event.getPlayer().getPersistentDataContainer().get(startedClimbingKey, PersistentDataType.LONG);
            if (time != null) {
                if (Instant.now().getEpochSecond() - time < 2) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        event.getPlayer().getPersistentDataContainer().set(stoppedClimbingKey, OriginSwapper.BooleanPDT.BOOLEAN, !event.isFlying());
    }
    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        event.getPlayer().getPersistentDataContainer().set(startedClimbingKey, PersistentDataType.LONG, Instant.now().getEpochSecond());
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:climbing");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("You are able to climb up any kind of wall, not just ladders.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Climbing", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    @Override
    public boolean canFly(Player player) {
        return canFly.getOrDefault(player, false);
    }

    @Override
    public float getFlightSpeed(Player player) {
        return 0.05f;
    }

    @Override
    public @NotNull TriState getFlyingFallDamage(Player player) {
        return TriState.TRUE;
    }
}
