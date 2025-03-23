package ru.turbovadim.abilities

import ru.turbovadim.OriginSwapper.LineData.Companion.makeLineFor
import ru.turbovadim.OriginSwapper.LineData.LineComponent
import ru.turbovadim.OriginsRebornEnhanced.Companion.NMSInvoker
import ru.turbovadim.abilities.BreakSpeedModifierAbility.BlockMiningContext
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType

class AquaAffinity : VisibleAbility, BreakSpeedModifierAbility {
    override fun getKey(): Key {
        return Key.key("origins:aqua_affinity")
    }

    override val description: MutableList<LineComponent> = makeLineFor("You may break blocks underwater as others do on land.", LineComponent.LineType.DESCRIPTION)

    override val title: MutableList<LineComponent> = makeLineFor("Aqua Affinity", LineComponent.LineType.TITLE)

    override fun provideContextFor(player: Player) = with(player) {
        BlockMiningContext(
            inventory.itemInMainHand,
            getPotionEffect(NMSInvoker.hasteEffect),
            getPotionEffect(NMSInvoker.miningFatigueEffect),
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
