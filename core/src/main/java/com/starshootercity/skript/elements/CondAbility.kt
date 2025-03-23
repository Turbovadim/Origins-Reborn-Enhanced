package com.starshootercity.skript.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.starshootercity.abilities.AbilityRegister;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class CondAbility extends Condition {

    static {
        Skript.registerCondition(CondAbility.class, "%player% (1¦has|2¦does( not|n't) have) [the] [(ability|power)] [with (id|key)] %string%");
    }

    Expression<Player> player;
    Expression<String> ability;

    @Override
    public boolean check(@NotNull Event event) {
        Player p = player.getSingle(event);
        @Subst("skript:ability") String a = ability.getSingle(event);
        if (p == null || a == null) return isNegated();
        try {
            Key key = Key.key(a);
            if (!AbilityRegister.abilityMap.containsKey(key)) return isNegated();
            return AbilityRegister.abilityMap.get(key).hasAbility(p) != isNegated();
        } catch (InvalidKeyException e) {
            return isNegated();
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        if (event == null) return "Player has ability";
        return "Player " + player.toString(event, debug) + " has ability with id " + ability.toString(event, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        this.player = (Expression<Player>) expressions[0];
        this.ability = (Expression<String>) expressions[1];
        setNegated(parseResult.mark == 2);
        return true;
    }
}
