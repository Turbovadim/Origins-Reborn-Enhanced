package com.starshootercity;

import com.starshootercity.abilities.*;
import com.starshootercity.abilities.incomplete.*;
import com.starshootercity.commands.OriginCommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class OriginsReborn extends JavaPlugin {
    private static OriginsReborn instance;

    public static OriginsReborn getInstance() {
        return instance;
    }

    private Economy economy;

    public Economy getEconomy() {
        return economy;
    }

    private boolean setupEconomy() {
        try {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }
            return (economy != null);
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    private boolean vaultEnabled;

    public boolean isVaultEnabled() {
        return vaultEnabled;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (getConfig().getBoolean("swap-command.vault.enabled")) {
            vaultEnabled = setupEconomy();
            if (!vaultEnabled) {
                getLogger().warning("Vault is not missing, origin swaps will not cost currency");
            }
        } else vaultEnabled = false;
        saveDefaultConfig();
        PluginCommand command = getCommand("origin");
        if (command != null) command.setExecutor(new OriginCommand());
        OriginLoader.loadOrigins();
        Bukkit.getPluginManager().registerEvents(new OriginSwapper(), this);
        Bukkit.getPluginManager().registerEvents(new OrbOfOrigin(), this);
        Bukkit.getPluginManager().registerEvents(new BreakSpeedModifierAbility.BreakSpeedModifierAbilityListener(), this);

        //<editor-fold desc="Register abilities">
        List<Ability> abilities = new ArrayList<>() {{
            add(new PumpkinHate());
            add(new FallImmunity());
            add(new WeakArms());
            add(new Fragile());
            add(new SlowFalling());
            add(new FreshAir());
            add(new Vegetarian());
            add(new LayEggs());
            add(new Unwieldy());
            add(new MasterOfWebs());
            add(new Tailwind());
            add(new Arthropod());
            add(new Climbing());
            add(new Carnivore());
            add(new WaterBreathing());
            add(new WaterVision());
            add(new CatVision());
            add(new NineLives());
            add(new BurnInDaylight());
            add(new WaterVulnerability());
            add(new Phantomize());
            add(new Invisibility());
            add(new ThrowEnderPearl());
            add(new PhantomizeOverlay());
            add(new FireImmunity());
            add(new AirFromPotions());
            add(new SwimSpeed());
            add(new LikeWater());
            add(new LightArmor());
            add(new MoreKineticDamage());
            add(new DamageFromPotions());
            add(new DamageFromSnowballs());
            add(new Hotblooded());
            add(new BurningWrath());
            add(new SprintJump());
            add(new AerialCombatant());
            add(new Elytra());
            add(new LaunchIntoAir());
            add(new HungerOverTime());
            add(new MoreExhaustion());
            add(new Aquatic());
            add(new NetherSpawn());
            add(new Claustrophobia());
            add(new VelvetPaws());
            add(new AquaAffinity());
            add(new FlameParticles());
            add(new EnderParticles());
            add(new Phasing());
            add(new ScareCreepers());
            add(new StrongArms());
            add(new StrongArmsBreakSpeed());
        }};
        for (Ability ability : abilities) {
            AbilityRegister.registerAbility(ability);
        }
        //</editor-fold>
    }
}