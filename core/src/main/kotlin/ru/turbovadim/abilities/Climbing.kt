package ru.turbovadim.abilities

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import com.destroystokyo.paper.event.server.ServerTickEndEvent
import ru.turbovadim.OriginSwapper
import ru.turbovadim.OriginSwapper.LineData.Companion.makeLineFor
import ru.turbovadim.OriginSwapper.LineData.LineComponent
import ru.turbovadim.OriginsRebornEnhanced.Companion.NMSInvoker
import ru.turbovadim.OriginsRebornEnhanced.Companion.bukkitDispatcher
import ru.turbovadim.OriginsRebornEnhanced.Companion.instance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import org.endera.enderalib.utils.async.ioDispatcher
import java.lang.Boolean
import java.time.Instant
import kotlin.Float

class Climbing : FlightAllowingAbility, Listener, VisibleAbility {
    var stoppedClimbingKey: NamespacedKey = NamespacedKey(instance, "stoppedclimbing")
    var startedClimbingKey: NamespacedKey = NamespacedKey(instance, "startedclimbing")

    private val cardinals = listOf(BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH)

    @EventHandler
    fun onServerTickEnd(event: ServerTickEndEvent?) {
        CoroutineScope(ioDispatcher).launch {
            for (p in Bukkit.getOnlinePlayers().toList()) {
                runForAbilityAsync(p) { player ->
                    val baseBlock = player.location.block
                    var hasSolid = false
                    var hasSolidAbove = false

                    withContext(bukkitDispatcher) {
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
                    }
                }
            }
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

    override val description: MutableList<LineComponent> = makeLineFor(
        "You are able to climb up any kind of wall, not just ladders.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent> = makeLineFor(
        "Climbing",
        LineComponent.LineType.TITLE
    )

    override fun canFly(player: Player): kotlin.Boolean {
        return canFly.getOrDefault(player, false)!!
    }

    override fun getFlightSpeed(player: Player): Float {
        return 0.05f
    }

    override fun getFlyingFallDamage(player: Player): TriState {
        return TriState.TRUE
    }
}
