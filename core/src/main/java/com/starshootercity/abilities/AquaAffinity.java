package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AquaAffinity implements VisibleAbility, BreakSpeedModifierAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:aqua_affinity");
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("Вы можете ломать блоки под водой так же, как и на суше.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Родство с водой", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }

    @Override
    public BlockMiningContext provideContextFor(Player player) {
        return new BlockMiningContext(
                player.getInventory().getItemInMainHand(),
                player.getPotionEffect(OriginsReborn.getNMSInvoker().getHasteEffect()),
                player.getPotionEffect(OriginsReborn.getNMSInvoker().getMiningFatigueEffect()),
                player.getPotionEffect(PotionEffectType.CONDUIT_POWER),
                true,
                true,
                true
        );
    }

    @Override
    public boolean shouldActivate(Player player) {
        return player.isInWater();
    }
}
