package com.starshootercity.abilities

import com.destroystokyo.paper.MaterialTags
import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.abilities.Ability.AbilityRunner
import com.starshootercity.abilities.BreakSpeedModifierAbility.BlockMiningContext
import com.starshootercity.abilities.StrongArms.StrongArmsBreakSpeed.StrongArmsFastBlockBreakEvent
import net.kyori.adventure.key.Key
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

class StrongArms : MultiAbility, VisibleAbility, Listener {

    override fun getKey(): Key {
        return Key.key("origins:strong_arms")
    }

    override val description: MutableList<LineComponent> = makeLineFor(
        "You are strong enough to break natural stones without using a pickaxe.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent> = makeLineFor("Strong Arms", LineComponent.LineType.TITLE)

    override val abilities: MutableList<Ability> = mutableListOf(
        StrongArmsDrops.Companion.strongArmsDrops,
        StrongArmsBreakSpeed.Companion.strongArmsBreakSpeed
    )

    class StrongArmsDrops : Ability, Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun onBlockBreak(event: BlockBreakEvent) {
            runForAbility(event.player, AbilityRunner { player ->
                if (event.block.type in naturalStones &&
                    !MaterialTags.PICKAXES.isTagged(player.inventory.itemInMainHand.type)
                ) {
                    event.isCancelled = true
                    val pickaxe = ItemStack(Material.IRON_PICKAXE).apply {
                        addUnsafeEnchantments(player.inventory.itemInMainHand.enchantments)
                    }
                    event.block.breakNaturally(pickaxe, event is StrongArmsFastBlockBreakEvent)
                }
            })
        }


        override fun getKey(): Key {
            return Key.key("origins:strong_arms_drops")
        }

        companion object {
            var strongArmsDrops: StrongArmsDrops = StrongArmsDrops()

            private val naturalStones: MutableList<Material?> = object : ArrayList<Material?>() {
                init {
                    add(Material.STONE)
                    add(Material.TUFF)
                    add(Material.GRANITE)
                    add(Material.DIORITE)
                    add(Material.ANDESITE)
                    add(Material.SANDSTONE)
                    add(Material.SMOOTH_SANDSTONE)
                    add(Material.RED_SANDSTONE)
                    add(Material.SMOOTH_RED_SANDSTONE)
                    add(Material.DEEPSLATE)
                    add(Material.BLACKSTONE)
                    add(Material.NETHERRACK)
                }
            }
        }
    }

    class StrongArmsBreakSpeed : BreakSpeedModifierAbility, Listener {
        override fun getKey(): Key {
            return Key.key("origins:strong_arms_break_speed")
        }

        override fun provideContextFor(player: Player): BlockMiningContext {
            var aquaAffinity = false
            val helmet = player.inventory.helmet
            if (helmet != null) {
                if (helmet.containsEnchantment(NMSInvoker.aquaAffinityEnchantment)) aquaAffinity = true
            }
            return BlockMiningContext(
                ItemStack(Material.IRON_PICKAXE),
                player.getPotionEffect(NMSInvoker.miningFatigueEffect),
                player.getPotionEffect(NMSInvoker.hasteEffect),
                player.getPotionEffect(PotionEffectType.CONDUIT_POWER),
                NMSInvoker.isUnderWater(player),
                aquaAffinity,
                player.isOnGround
            )
        }

        override fun shouldActivate(player: Player): Boolean {
            if (MaterialTags.PICKAXES.isTagged(player.inventory.itemInMainHand.type)) return false
            val target = player.getTargetBlockExact(8, FluidCollisionMode.NEVER) ?: return false
            return target.type in naturalStones
        }

        class StrongArmsFastBlockBreakEvent(theBlock: Block, player: Player) : BlockBreakEvent(theBlock, player)
        companion object {
            var strongArmsBreakSpeed: StrongArmsBreakSpeed = StrongArmsBreakSpeed()

            private val naturalStones: MutableList<Material?> = object : ArrayList<Material?>() {
                init {
                    add(Material.STONE)
                    add(Material.TUFF)
                    add(Material.GRANITE)
                    add(Material.DIORITE)
                    add(Material.ANDESITE)
                    add(Material.SANDSTONE)
                    add(Material.SMOOTH_SANDSTONE)
                    add(Material.RED_SANDSTONE)
                    add(Material.SMOOTH_RED_SANDSTONE)
                    add(Material.DEEPSLATE)
                    add(Material.BLACKSTONE)
                    add(Material.NETHERRACK)
                }
            }
        }
    }
}
