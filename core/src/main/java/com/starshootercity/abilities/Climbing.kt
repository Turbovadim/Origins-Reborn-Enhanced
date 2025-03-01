package com.starshootercity.abilities

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import com.destroystokyo.paper.event.server.ServerTickEndEvent
import com.starshootercity.OriginSwapper
import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.abilities.Ability.AbilityRunner
import net.kyori.adventure.key.Key
import net.kyori.adventure.util.TriState
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.persistence.PersistentDataType
import java.lang.Boolean
import java.time.Instant
import kotlin.Float

class Climbing : FlightAllowingAbility, Listener, VisibleAbility {
    var stoppedClimbingKey: NamespacedKey = NamespacedKey(instance, "stoppedclimbing")
    var startedClimbingKey: NamespacedKey = NamespacedKey(instance, "startedclimbing")

    private val cardinals = listOf(BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH)

    @EventHandler
    fun onServerTickEnd(ignored: ServerTickEndEvent?) {
        for (p in Bukkit.getOnlinePlayers()) {
            runForAbility(p, AbilityRunner { player ->
                player ?: return@AbilityRunner

                val baseBlock = player.location.block
                var hasSolid = false
                var hasSolidAbove = false

                for (face in cardinals) {
                    hasSolid = baseBlock.getRelative(face).isSolid
                    hasSolidAbove = baseBlock.getRelative(BlockFace.UP).getRelative(face).isSolid

                    if (hasSolid) break
                }

                setCanFly(player, hasSolid)
                if (hasSolid) {
                    NMSInvoker.setFlyingFallDamage(player, TriState.TRUE)
                }

                if (player.allowFlight && hasSolidAbove) {
                    val stoppedClimbing = player.persistentDataContainer.get(
                        stoppedClimbingKey, OriginSwapper.BooleanPDT.BOOLEAN
                    )
                    if (Boolean.TRUE != stoppedClimbing) {
                        if (!player.isOnGround) player.isFlying = true
                    } else {
                        if (player.isOnGround) {
                            player.persistentDataContainer.set(
                                stoppedClimbingKey, OriginSwapper.BooleanPDT.BOOLEAN, false
                            )
                        }
                    }
                }
            })
        }
    }


    private fun setCanFly(player: Player, setFly: kotlin.Boolean) {
        if (setFly) player.allowFlight = true
        canFly.put(player, setFly)
    }

    private val canFly: MutableMap<Player?, kotlin.Boolean?> = HashMap<Player?, kotlin.Boolean?>()

    @EventHandler
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        if (!event.isFlying) {
            val time = event.getPlayer()
                .persistentDataContainer
                .get(startedClimbingKey, PersistentDataType.LONG)
            if (time != null) {
                if (Instant.now().epochSecond - time < 2) {
                    event.isCancelled = true
                    return
                }
            }
        }
        event.getPlayer()
            .persistentDataContainer
            .set(stoppedClimbingKey, OriginSwapper.BooleanPDT.BOOLEAN, !event.isFlying)
    }

    @EventHandler
    fun onPlayerJump(event: PlayerJumpEvent) {
        event.getPlayer()
            .persistentDataContainer
            .set(startedClimbingKey, PersistentDataType.LONG, Instant.now().epochSecond)
    }

    override fun getKey(): Key {
        return Key.key("origins:climbing")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "You are able to climb up any kind of wall, not just ladders.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Climbing", LineComponent.LineType.TITLE)
    }

    override fun canFly(player: Player?): kotlin.Boolean {
        return canFly.getOrDefault(player, false)!!
    }

    override fun getFlightSpeed(player: Player?): Float {
        return 0.05f
    }

    override fun getFlyingFallDamage(player: Player?): TriState {
        return TriState.TRUE
    }
}
