package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.abilities.BreakSpeedModifierAbility.BlockMiningContext
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType

class AquaAffinity : VisibleAbility, BreakSpeedModifierAbility {
    override fun getKey(): Key {
        return Key.key("origins:aqua_affinity")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor("You may break blocks underwater as others do on land.", LineComponent.LineType.DESCRIPTION)
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Aqua Affinity", LineComponent.LineType.TITLE)
    }

    override fun provideContextFor(player: Player) = with(player) {
        BlockMiningContext(
            inventory.itemInMainHand,
            getPotionEffect(NMSInvoker.getHasteEffect()),
            getPotionEffect(NMSInvoker.getMiningFatigueEffect()),
            getPotionEffect(PotionEffectType.CONDUIT_POWER),
            true,
            true,
            true
        )
    }

    override fun shouldActivate(player: Player): Boolean {
        return player.isInWater
    }
}
