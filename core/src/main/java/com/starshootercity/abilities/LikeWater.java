package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LikeWater implements VisibleAbility, FlightAllowingAbility, Listener  {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:like_water");
    }

    @Override
    public boolean canFly(Player player) {
        return player.isInWater() && !player.isInBubbleColumn();
    }

    @Override
    public float getFlightSpeed(Player player) {
        return 0.06f;
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            if (event.getPlayer().isInWater()) event.getPlayer().setFlying(false);
        });
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().isInWater() && !event.getPlayer().isSwimming()) {
            AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> event.getPlayer().setFlying((event.getPlayer().isFlying() || event.getTo().getY() > event.getFrom().getY()) && !event.getPlayer().isInBubbleColumn()));
        }
    }

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        if (!event.getPlayer().isFlying()) return;
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            if (event.getPlayer().isInWater()) {
                event.getPlayer().setFlying(false);
            }
        });
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        AbilityRegister.runForAbility(event.getPlayer(), getKey(), () -> {
            if (event.getPlayer().isInWater()) event.setCancelled(true);
        });
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("When underwater, you do not sink to the ground unless you want to.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Like Water", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
