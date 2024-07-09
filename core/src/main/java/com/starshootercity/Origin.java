package com.starshootercity;

import com.starshootercity.abilities.Ability;
import com.starshootercity.abilities.AbilityRegister;
import com.starshootercity.abilities.MultiAbility;
import com.starshootercity.abilities.VisibleAbility;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

public class Origin {
    private final ItemStack icon;
    private final int position;
    private final char impact;
    private final String name;
    private final int priority;
    private final boolean unchoosable;
    private final OriginsAddon addon;
    private final List<Key> abilities;
    private final String description;
    private final String permission;
    private final Integer cost;
    private final int max;

    public Integer getCost() {
        return cost;
    }

    public boolean isUnchoosable(Player player) {
        if (unchoosable) return true;
        String mode = OriginsReborn.getInstance().getConfig().getString("restrictions.reusing-origins", "NONE");
        boolean same = OriginsReborn.getInstance().getConfig().getBoolean("restrictions.prevent-same-origins");
        if (max != -1) {
            int num = 0;
            for (String p : OriginSwapper.getOriginFileConfiguration().getKeys(false)) {
                if (OriginSwapper.getOriginFileConfiguration().getString(p, "").equals(getName().toLowerCase())) {
                    num++;
                }
            }
            if (num >= max) return true;
        }
        if (same) {
            for (String p : OriginSwapper.getOriginFileConfiguration().getKeys(false)) {
                if (OriginSwapper.getOriginFileConfiguration().getString(p, "").equals(getName().toLowerCase())) {
                    return true;
                }
            }
        }
        if (mode.equals("PERPLAYER")) {
            return OriginSwapper.getUsedOriginFileConfiguration().getStringList(player.getUniqueId().toString()).contains(getName().toLowerCase());
        } else if (mode.equals("ALL")) {
            for (String p : OriginSwapper.getUsedOriginFileConfiguration().getKeys(false)) {
                if (OriginSwapper.getUsedOriginFileConfiguration().getStringList(p).contains(getName().toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getPriority() {
        return priority;
    }

    private final Team team;

    public Team getTeam() {
        return team;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission() {
        return permission != null;
    }

    public Origin(String name, ItemStack icon, int position, @Range(from = 0, to = 3) int impact, List<Key> abilities, String description, OriginsAddon addon, boolean unchoosable, int priority, String permission, Integer cost, int max) {
        this.description = description;
        this.name = name;
        this.permission = permission;
        this.cost = cost;
        this.max = max;
        if (OriginsReborn.getInstance().getConfig().getBoolean("display.enable-prefixes")) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team oldTeam = scoreboard.getTeam(name);
            if (oldTeam != null) oldTeam.unregister();
            team = scoreboard.registerNewTeam(name);
            team.displayName(Component.text("[")
                            .color(NamedTextColor.DARK_GRAY)
                            .append(Component.text(name)
                                    .color(NamedTextColor.WHITE))
                    .append(Component.text("] ")
                            .color(NamedTextColor.DARK_GRAY)
                    ));
        } else team = null;
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
        this.addon = addon;
        this.priority = priority;
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

    public OriginsAddon getAddon() {
        return addon;
    }

    public List<Ability> getAbilities() {
        List<Ability> originAbilities = new ArrayList<>();
        for (Key key : abilities) {
            Ability a = AbilityRegister.abilityMap.get(key);
            originAbilities.add(a);
            if (a instanceof MultiAbility multiAbility) originAbilities.addAll(multiAbility.getAbilities());
        }
        return originAbilities;
    }

    public boolean hasAbility(Key key) {
        for (MultiAbility ability : AbilityRegister.multiAbilityMap.getOrDefault(key, List.of())) {
            if (abilities.contains(ability.getKey())) return true;
        }
        return abilities.contains(key);
    }

    public char getImpact() {
        return impact;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return AddonLoader.getTextFor("origin." + addon.getNamespace() + "." + name.replace(" ", "_").toLowerCase() + ".name", name);
    }

    public String getActualName() {
        return name;
    }

    public String getDescription() {
        return AddonLoader.getTextFor("origin." + addon.getNamespace() + "." + name.replace(" ", "_").toLowerCase() + ".description", description);
    }

    public ItemStack getIcon() {
        return icon;
    }

    public String getResourceURL() {
        String key = icon.getType().getKey().value();
        return "https://assets.mcasset.cloud/1.20.4/assets/minecraft/textures/%s/%s.png".formatted(icon.getType().isBlock() ? "block" : "item", key);
    }
}
