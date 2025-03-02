package com.starshootercity.abilities

import com.starshootercity.OriginSwapper.LineData.Companion.makeLineFor
import com.starshootercity.OriginSwapper.LineData.LineComponent
import net.kyori.adventure.key.Key
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExhaustionEvent

class MoreExhaustion : VisibleAbility, Listener {
    override fun getKey(): Key {
        return Key.key("origins:more_exhaustion")
    }

    override fun getDescription(): MutableList<LineComponent?> {
        return makeLineFor(
            "You exhaust much quicker than others, thus requiring you to eat more.",
            LineComponent.LineType.DESCRIPTION
        )
    }

    override fun getTitle(): MutableList<LineComponent?> {
        return makeLineFor("Large Appetite", LineComponent.LineType.TITLE)
    }

    @EventHandler
    fun onEntityExhaustion(event: EntityExhaustionEvent) {
        runForAbility(event.getEntity()) { player ->
            event.exhaustion = event.exhaustion * 1.6f
        }
    }
}
