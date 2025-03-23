package com.starshootercity.skript.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.starshootercity.abilities.Ability;
import com.starshootercity.abilities.AbilityRegister;
import com.starshootercity.skript.NamedSkriptAbility;
import com.starshootercity.skript.SkriptAbility;
import net.kyori.adventure.key.Key;
import org.bukkit.event.Event;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class EffAbility extends Effect {

    static {
        Skript.registerEffect(EffAbility.class, "(register|create) [(a new|an|a)] (ability|power) with (key|id) %string% [[and] (title|name) %-string% [and] description %-string%]");
    }

    @Override
    protected void execute(@NotNull Event event) {
        if (ability == null) return;
        @Subst("skript:ability") String key = ability.getSingle(event);
        if (key == null) return;
        if ((title == null) != (description == null)) return;
        Ability a;
        if (title != null) {
            String titl = title.getSingle(event);
            String desc = description.getSingle(event);
            a = new NamedSkriptAbility(Key.key(key), titl, desc);
        } else a = new SkriptAbility(Key.key(key));
        AbilityRegister.registerAbility(
                a,
                Skript.getInstance()
        );
    }

    private Expression<String> ability;
    private Expression<String> title;
    private Expression<String> description;

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        if (event == null) return "Ability register effect";
        if ((title != null) && (description != null)) {
            return "Ability register effect with expression ability\">: " + ability.toString(event, debug) + " and title\">: " + title.toString(event, debug) + " and description\">: " + description.toString(event, debug);
        }
        return "Ability register effect with expression ability\">: " + ability.toString(event, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        this.ability = (Expression<String>) expressions[0];
        this.title = (Expression<String>) expressions[1];
        this.description = (Expression<String>) expressions[2];
        return true;
    }
}
