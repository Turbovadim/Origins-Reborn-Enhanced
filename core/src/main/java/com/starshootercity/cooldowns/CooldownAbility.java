package com.starshootercity.cooldowns;

import com.starshootercity.OriginsReborn;
import com.starshootercity.abilities.Ability;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public interface CooldownAbility extends Ability {
    default NamespacedKey getCooldownKey() {
        return new NamespacedKey(OriginsReborn.getInstance(), getKey().asString().replace(":", "-"));
    }

    default void setCooldown(Player player) {
        if (OriginsReborn.getInstance().getConfig().getBoolean("cooldowns.disable-all-cooldowns")) return;
        OriginsReborn.getCooldowns().setCooldown(player, getCooldownKey());
    }

    default boolean hasCooldown(Player player) {
        if (OriginsReborn.getInstance().getConfig().getBoolean("cooldowns.disable-all-cooldowns")) return false;
        return OriginsReborn.getCooldowns().hasCooldown(player, getCooldownKey());
    }

    Cooldowns.CooldownInfo getCooldownInfo();
}
