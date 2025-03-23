package com.starshootercity.skript.elements

import ch.njol.skript.Skript
import ch.njol.skript.lang.Condition
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import com.starshootercity.abilities.AbilityRegister
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.Event

class CondAbility : Condition() {
    private var player: Expression<Player?>? = null
    private var ability: Expression<String?>? = null

    override fun check(event: Event): Boolean {
        val p = player?.getSingle(event) ?: return isNegated
        val abilityStr = ability?.getSingle(event) ?: return isNegated

        val key = try {
            Key.key(abilityStr)
        } catch (_: InvalidKeyException) {
            return isNegated
        }

        val registeredAbility = AbilityRegister.abilityMap[key] ?: return isNegated
        return registeredAbility.hasAbility(p) != isNegated
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return if (event == null) {
            "Player has ability"
        } else {
            "Player ${player?.toString(event, debug)} has ability with id ${ability?.toString(event, debug)}"
        }
    }

    companion object {
        private const val NEGATION_MARK = 2

        init {
            Skript.registerCondition(
                CondAbility::class.java,
                "%player% (1¦has|2¦does( not|n't) have) [the] [(ability|power)] [with (id|key)] %string%"
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        player = expressions.getOrNull(0) as? Expression<Player?>
        ability = expressions.getOrNull(1) as? Expression<String?>
        if (player == null || ability == null) return false

        isNegated = parseResult.mark == NEGATION_MARK
        return true
    }
}
