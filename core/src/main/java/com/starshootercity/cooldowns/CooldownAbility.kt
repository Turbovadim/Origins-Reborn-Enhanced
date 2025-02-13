package com.starshootercity.cooldowns;

import com.starshootercity.OriginsReborn;
import com.starshootercity.abilities.Ability;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@SuppressWarnings("unused") // Some functions here are unused but are useful in addons
public interface CooldownAbility extends Ability {
    default NamespacedKey getCooldownKey() {
        return new NamespacedKey(OriginsReborn.getInstance(), getKey().asString().replace(":", "-"));
    }

    default void setCooldown(Player player) {
        if (OriginsReborn.getInstance().getConfig().getBoolean("cooldowns.disable-all-cooldowns")) return;
        OriginsReborn.getCooldowns().setCooldown(player, getCooldownKey());
    }

    default void setCooldown(Player player, int amount) {
        if (OriginsReborn.getInstance().getConfig().getBoolean("cooldowns.disable-all-cooldowns")) return;
        OriginsReborn.getCooldowns().setCooldown(player, getCooldownKey(), amount, getCooldownInfo().isStatic());
    }

    default boolean hasCooldown(Player player) {
        if (OriginsReborn.getInstance().getConfig().getBoolean("cooldowns.disable-all-cooldowns")) return false;
        return OriginsReborn.getCooldowns().hasCooldown(player, getCooldownKey());
    }

    default long getCooldown(Player player) {
        return OriginsReborn.getCooldowns().getCooldown(player, getCooldownKey());
    }

    Cooldowns.CooldownInfo getCooldownInfo();
}
