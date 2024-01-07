package com.starshootercity.abilities;

import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NetherSpawn implements DefaultSpawnAbility, VisibleAbility {
    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:nether_spawn");
    }

    @Override
    public @Nullable World getWorld() {
        String nether = OriginsReborn.getInstance().getConfig().getString("worlds.world_nether");
        if (nether == null) {
            nether = "world_nether";
            OriginsReborn.getInstance().getConfig().set("worlds.world_nether", "world_nether");
            OriginsReborn.getInstance().saveConfig();
        }
        return Bukkit.getWorld(nether);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor("Your natural spawn will be in the Nether.", OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor("Nether Inhabitant", OriginSwapper.LineData.LineComponent.LineType.TITLE);
    }
}
