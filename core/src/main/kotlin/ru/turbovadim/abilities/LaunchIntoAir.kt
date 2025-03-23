package ru.turbovadim.abilities

import ru.turbovadim.OriginSwapper.LineData.Companion.makeLineFor
import ru.turbovadim.OriginSwapper.LineData.LineComponent
import ru.turbovadim.cooldowns.CooldownAbility
import ru.turbovadim.cooldowns.Cooldowns.CooldownInfo
import net.kyori.adventure.key.Key
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.util.Vector

class LaunchIntoAir : VisibleAbility, Listener, CooldownAbility {

    override fun getKey(): Key {
        return Key.key("origins:launch_into_air")
    }

    override val description: MutableList<LineComponent> = makeLineFor(
        "Every 30 seconds, you are able to launch about 20 blocks up into the air.",
        LineComponent.LineType.DESCRIPTION
    )

    override val title: MutableList<LineComponent> = makeLineFor(
        "Gift of the Winds",
        LineComponent.LineType.TITLE
    )

    @EventHandler
    fun onSneakToggle(event: PlayerToggleSneakEvent) {
        if (!event.isSneaking) return
        val player = event.player
        runForAbility(player) { p ->
            if (p.isGliding && !hasCooldown(p)) {
                setCooldown(p)
                p.velocity = p.velocity.add(Vector(0, 2, 0))
            }
        }
    }

    override val cooldownInfo: CooldownInfo
        get() = CooldownInfo(600, "launch")
}
