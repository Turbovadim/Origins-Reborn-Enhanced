package com.starshootercity;

import com.starshootercity.abilities.Ability;
import com.starshootercity.abilities.AbilityRegister;
import com.starshootercity.events.PlayerSwapOriginEvent;
import com.starshootercity.packetsenders.OriginsRebornResourcePackInfo;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public abstract class OriginsAddon extends JavaPlugin {
    private static OriginsAddon instance;

    public static OriginsAddon getInstance() {
        return instance;
    }

    public @Nullable SwapStateGetter shouldOpenSwapMenu() {
        return null;
    }

    public @Nullable SwapStateGetter shouldAllowOriginSwapCommand() {
        return null;
    }

    public @Nullable KeyStateGetter hasAbilityOverride() {
        return null;
    }

    public interface SwapStateGetter {
        State get(Player player, PlayerSwapOriginEvent.SwapReason reason);
    }

    public interface KeyStateGetter {
        State get(Player player, Key key);
    }

    @SuppressWarnings("unused")
    public enum State {
        ALLOW,
        DEFAULT,
        DENY
    }

    @Override
    public final void onEnable() {
        instance = this;
        onRegister();
        AddonLoader.register(this);
        for (Ability ability : getAbilities()) {
            AbilityRegister.registerAbility(ability, this);
        }
        if (getResourcePackInfo() != null) PackApplier.addResourcePack(this, getResourcePackInfo());
        afterRegister();
    }

    public @Nullable OriginsRebornResourcePackInfo getResourcePackInfo() {
        return null;
    }

    @Override
    public @NotNull File getFile() {
        return super.getFile();
    }

    public void onRegister() {}

    public void afterRegister() {}

    public abstract @NotNull String getNamespace();

    public @NotNull List<Ability> getAbilities() {
        return List.of();
    }
}
