package com.starshootercity.skript.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.starshootercity.commands.FlightToggleCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ExprFlight extends SimpleExpression<Boolean> {
    static {
        Skript.registerExpression(ExprFlight.class, Boolean.class, ExpressionType.COMBINED, "[the] flight ability of %player%");
    }

    @Override
    protected Boolean @NotNull [] get(@NotNull Event event) {
        Player p = player.getSingle(event);
        if (p != null) {
            return new Boolean[]{FlightToggleCommand.canFly(p)};
        }
        return new Boolean[]{false};
    }

    private Expression<Player> player;

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "Example expression with expression player>: " + player.toString(event, b);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        player = (Expression<Player>) expressions[0];
        return true;
    }

    @Override
    public void change(@NotNull Event event, Object @NotNull [] delta, Changer.@NotNull ChangeMode mode) {
        Player p = player.getSingle(event);
        if (mode.equals(Changer.ChangeMode.SET)) {
            if ((boolean) delta[0]) {
                FlightToggleCommand.setCanFly(p, true);
                return;
            }
        }
        FlightToggleCommand.setCanFly(p, false);
    }

    @Override
    public Class<?> @NotNull [] acceptChange(Changer.@NotNull ChangeMode mode) {
        if (Set.of(Changer.ChangeMode.DELETE, Changer.ChangeMode.SET, Changer.ChangeMode.RESET).contains(mode)) {
            return CollectionUtils.array(Boolean.class);
        }
        return CollectionUtils.array();
    }
}
