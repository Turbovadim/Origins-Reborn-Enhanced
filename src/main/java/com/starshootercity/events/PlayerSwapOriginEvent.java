package com.starshootercity.events;

import com.starshootercity.Origin;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PlayerSwapOriginEvent extends PlayerEvent implements Cancellable {
    private boolean cancelled = false;
    private boolean resetPlayer;
    private final SwapReason reason;
    private final Origin oldOrigin;
    private Origin newOrigin;
    public PlayerSwapOriginEvent(@NotNull Player who, SwapReason reason, boolean resetPlayer, Origin oldOrigin, Origin newOrigin) {
        super(who);
        this.reason = reason;
        this.resetPlayer = resetPlayer;
        this.oldOrigin = oldOrigin;
        this.newOrigin = newOrigin;
    }

    public Origin getNewOrigin() {
        return newOrigin;
    }

    public Origin getOldOrigin() {
        return oldOrigin;
    }

    public void setNewOrigin(Origin newOrigin) {
        this.newOrigin = newOrigin;
    }

    public SwapReason getReason() {
        return reason;
    }

    public boolean isResetPlayer() {
        return resetPlayer;
    }

    public void setResetPlayer(boolean resetPlayer) {
        this.resetPlayer = resetPlayer;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public enum SwapReason {
        /**
         * Swapped origin with the /oswap command
         */
        COMMAND("command"),
        /**
         * Swapped origin using an Orb of Origin
         */
        ORB_OF_ORIGIN("orb"),
        /**
         * Swapped origin due to having died and respawned
         */
        DIED("died");

        private final String reason;

        public String getReason() {
            return reason;
        }

        public @Nullable SwapReason get(String reason) {
            return swapReasonMap.getOrDefault(reason, null);
        }

        private static final Map<String, SwapReason> swapReasonMap = new HashMap<>();

        SwapReason(String reason) {
            this.reason = reason;
            registerSwapReason(this, reason);
        }

        private void registerSwapReason(SwapReason reason, String reasonName) {
            swapReasonMap.put(reasonName, reason);
        }
    }
}
