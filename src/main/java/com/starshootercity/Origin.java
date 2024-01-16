package com.starshootercity;

import com.starshootercity.abilities.Ability;
import com.starshootercity.abilities.VisibleAbility;
import com.starshootercity.abilities.AbilityRegister;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

public class Origin {
    private final ItemStack icon;
    private final int position;
    private final char impact;
    private final String name;
    private final boolean unchoosable;
    private final JavaPlugin plugin;
    private final List<Key> abilities;
    private final List<OriginSwapper.LineData.LineComponent> lineComponent;

    public List<OriginSwapper.LineData.LineComponent> getLineData() {
        return lineComponent;
    }

    public boolean isUnchoosable() {
        return unchoosable;
    }

    public Origin(String name, ItemStack icon, int position, @Range(from = 0, to = 3) int impact, List<Key> abilities, String description, JavaPlugin plugin, boolean unchoosable) {
        this.lineComponent = OriginSwapper.LineData.makeLineFor(description, OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
        this.name = name;
        this.abilities = abilities;
        this.icon = icon;
        this.position = position;
        this.unchoosable = unchoosable;
        this.impact = switch (impact) {
            case 0 -> '\uE002';
            case 1 -> '\uE003';
            case 2 -> '\uE004';
            default -> '\uE005';
        };
        this.plugin = plugin;
    }

    public List<VisibleAbility> getVisibleAbilities() {
        List<VisibleAbility> result = new ArrayList<>();
        for (Key key : abilities) {
            if (AbilityRegister.abilityMap.get(key) instanceof VisibleAbility visibleAbility) {
                result.add(visibleAbility);
            }
        }
        return result;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public List<Ability> getAbilities() {
        return new ArrayList<>() {{
            for (Key key : abilities) {
                add(AbilityRegister.abilityMap.get(key));
            }
        }};
    }

    public boolean hasAbility(Key key) {
        return abilities.contains(key);
    }

    public char getImpact() {
        return impact;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
    }
}
