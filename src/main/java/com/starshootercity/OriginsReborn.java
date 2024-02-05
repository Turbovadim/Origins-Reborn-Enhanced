package com.starshootercity;

import com.starshootercity.abilities.*;
import com.starshootercity.commands.OriginCommand;
import com.starshootercity.events.PlayerLeftClickEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OriginsReborn extends OriginsAddon {
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

    public void updateConfig() {
        String version = getConfig().getString("config-version", "1.0.0");
        if (version.equals("1.0.0")) saveResource("config.yml", true);
        else {
            if (version.equals("2.0.0")) {
                getConfig().set("config-version", "2.0.3");
                getConfig().set("display.enable-prefixes", false);
                getConfig().setComments("display", List.of("Miscellaneous display options"));
                getConfig().setComments("display.enable-prefixes", List.of("Enable prefixes in tab and on display names with the names of origins"));
                saveConfig();
            }
            if (version.equals("2.0.3")) {
                getConfig().set("config-version", "2.1.7");
                getConfig().set("restrictions.reusing-origins", "NONE");
                getConfig().set("restrictions.prevent-same-origins", false);
                getConfig().setComments("restrictions",
                        List.of(
                                "Restrictions placed on origin selection",
                                "These are designed for use with addon plugins that add many new origins",
                                "If you run out of origins that fit the restrictions you may experience issues"
                                )
                );
                getConfig().setComments("restrictions.reusing-origins",
                        List.of(
                                "Rule for reusing origins",
                                "\"NONE\" allows origins to be reused",
                                "\"PERPLAYER\" means individual players can only use an origin once",
                                "\"ALL\" means no players can use an origin again after one has selected it"
                                )
                );
                getConfig().setComments("restrictions.prevent-same-origins",
                        List.of(
                                "Prevent players from having the same origins as other players",
                                "This is locked on if reusing-origins is set to ALL"
                                )
                );
                saveConfig();
            }
            if (version.equals("2.1.7")) {
                getConfig().set("config-version", "2.2.0");
                getConfig().set("display.language", "en_us");
                getConfig().setComments("display.language", List.of(
                        "Language file to use - default is en_us"
                ));
                saveConfig();
            }
        }
    }


    @Override
    public void onRegister() {
        instance = this;
        if (getConfig().getBoolean("swap-command.vault.enabled")) {
            vaultEnabled = setupEconomy();
            if (!vaultEnabled) {
                getLogger().warning("Vault is missing, origin swaps will not cost currency");
            }
        } else vaultEnabled = false;
        saveDefaultConfig();
        updateConfig();
        PluginCommand command = getCommand("origin");
        if (command != null) command.setExecutor(new OriginCommand());
        Bukkit.getPluginManager().registerEvents(new OriginSwapper(), this);
        Bukkit.getPluginManager().registerEvents(new OrbOfOrigin(), this);
        Bukkit.getPluginManager().registerEvents(new PackApplier(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLeftClickEvent.PlayerLeftClickEventListener(), this);
        Bukkit.getPluginManager().registerEvents(new ParticleAbility.ParticleAbilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new BreakSpeedModifierAbility.BreakSpeedModifierAbilityListener(), this);
    }

    @Override
    public @NotNull List<Ability> getAbilities() {
        return List.of(
                new PumpkinHate(),
                new FallImmunity(),
                new WeakArms(),
                new Fragile(),
                new SlowFalling(),
                new FreshAir(),
                new Vegetarian(),
                new LayEggs(),
                new Unwieldy(),
                new MasterOfWebs(),
                new Tailwind(),
                new Arthropod(),
                new Climbing(),
                new Carnivore(),
                new WaterBreathing(),
                new WaterVision(),
                new CatVision(),
                new NineLives(),
                new BurnInDaylight(),
                new WaterVulnerability(),
                new Phantomize(),
                new Invisibility(),
                new ThrowEnderPearl(),
                new PhantomizeOverlay(),
                new FireImmunity(),
                new AirFromPotions(),
                new SwimSpeed(),
                new LikeWater(),
                new LightArmor(),
                new MoreKineticDamage(),
                new DamageFromPotions(),
                new DamageFromSnowballs(),
                new Hotblooded(),
                new BurningWrath(),
                new SprintJump(),
                new AerialCombatant(),
                new Elytra(),
                new LaunchIntoAir(),
                new HungerOverTime(),
                new MoreExhaustion(),
                new Aquatic(),
                new NetherSpawn(),
                new Claustrophobia(),
                new VelvetPaws(),
                new AquaAffinity(),
                new FlameParticles(),
                new EnderParticles(),
                new Phasing(),
                new ScareCreepers(),
                new StrongArms(),
                new StrongArmsBreakSpeed(),
                new ShulkerInventory(),
                new NaturalArmor()
        );
    }
}