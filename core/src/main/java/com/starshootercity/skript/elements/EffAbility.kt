package com.starshootercity.skript.elements

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import com.starshootercity.abilities.Ability
import com.starshootercity.abilities.AbilityRegister.registerAbility
import com.starshootercity.skript.NamedSkriptAbility
import com.starshootercity.skript.SkriptAbility
import net.kyori.adventure.key.Key
import org.bukkit.event.Event

class EffAbility : Effect() {
    private var ability: Expression<String?>? = null
    private var title: Expression<String?>? = null
    private var description: Expression<String?>? = null

    override fun execute(event: Event) {
        val keyString = ability?.getSingle(event) ?: return

        // Check that either both title and description are provided or neither is.
        if ((title == null) xor (description == null)) return

        val key = Key.key(keyString)
        val abilityInstance: Ability = if (title != null) {
            NamedSkriptAbility(key, title!!.getSingle(event)!!, description!!.getSingle(event)!!)
        } else {
            SkriptAbility(key)
        }
        registerAbility(abilityInstance, Skript.getInstance())
    }

    override fun toString(event: Event?, debug: Boolean): String {
        return if (event == null) {
            "Ability register effect"
        } else if (title != null && description != null) {
            "Ability register effect with expression ability: ${ability?.toString(event, debug)} " +
                    "and title: ${title?.toString(event, debug)} and description: ${description?.toString(event, debug)}"
        } else {
            "Ability register effect with expression ability: ${ability?.toString(event, debug)}"
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        ability = expressions[0] as Expression<String?>?
        title = expressions[1] as Expression<String?>?
        description = expressions[2] as Expression<String?>?
        return true
    }

    companion object {
        init {
            Skript.registerEffect(
                EffAbility::class.java,
                "(register|create) [(a new|an|a)] (ability|power) with (key|id) %string% [[and] (title|name) %-string% [and] description %-string%]"
            )
        }
    }
}
