package ru.turbovadim.skript.elements

import ch.njol.skript.Skript
import ch.njol.skript.classes.Changer
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import ch.njol.util.coll.CollectionUtils
import ru.turbovadim.commands.FlightToggleCommand
import org.bukkit.entity.Player
import org.bukkit.event.Event

class ExprFlight : SimpleExpression<Boolean>() {

    private var player: Expression<Player>? = null

    override fun isSingle() = true

    override fun getReturnType(): Class<out Boolean> = Boolean::class.java

    override fun get(event: Event): Array<Boolean> {
        val p = player?.getSingle(event)
        return arrayOf(if (p != null) FlightToggleCommand.canFly(p) else false)
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return "Example expression with expression player: ${player?.toString(event, debug)}"
    }

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<Expression<*>>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        player = expressions[0] as Expression<Player>
        return true
    }

    override fun change(event: Event?, delta: Array<out Any?>?, mode: Changer.ChangeMode?) {
        val p = player?.getSingle(event) ?: return
        if (mode == Changer.ChangeMode.SET && delta?.isNotEmpty() == true && (delta[0] as? Boolean) == true) {
            FlightToggleCommand.setCanFly(p, true)
            return
        }
        FlightToggleCommand.setCanFly(p, false)
    }

    override fun acceptChange(mode: Changer.ChangeMode): Array<Class<*>> {
        return if (setOf(Changer.ChangeMode.DELETE, Changer.ChangeMode.SET, Changer.ChangeMode.RESET).contains(mode)) {
            CollectionUtils.array(Boolean::class.java)
        } else {
            CollectionUtils.array()
        }
    }

    companion object {
        init {
            Skript.registerExpression(
                ExprFlight::class.java,
                Boolean::class.java,
                ExpressionType.COMBINED,
                "[the] flight ability of %player%"
            )
        }
    }
}
