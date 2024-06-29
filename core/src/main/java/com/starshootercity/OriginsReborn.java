package com.starshootercity;

import com.starshootercity.abilities.*;
import com.starshootercity.commands.FlightToggleCommand;
import com.starshootercity.commands.OriginCommand;
import com.starshootercity.events.PlayerLeftClickEvent;
import com.starshootercity.packetsenders.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

    private static NMSInvoker nmsInvoker;

    private static Cooldowns cooldowns;

    public static Cooldowns getCooldowns() {
        return cooldowns;
    }

    public static NMSInvoker getNMSInvoker() {
        return nmsInvoker;
    }

    private boolean setupEconomy() {
        try {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }
            return (economy != null);
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    private static void initializeNMSInvoker(OriginsReborn instance) {
        nmsInvoker = switch (Bukkit.getMinecraftVersion()) {
            case "1.18.2" -> new NMSInvokerV1_18_2(getInstance().getConfig());
            case "1.19" -> new NMSInvokerV1_19(getInstance().getConfig());
            case "1.19.1" -> new NMSInvokerV1_19_1(getInstance().getConfig());
            case "1.19.2" -> new NMSInvokerV1_19_2(getInstance().getConfig());
            case "1.19.3" -> new NMSInvokerV1_19_3(getInstance().getConfig());
            case "1.19.4" -> new NMSInvokerV1_19_4(getInstance().getConfig());
            case "1.20" -> new NMSInvokerV1_20(getInstance().getConfig());
            case "1.20.1" -> new NMSInvokerV1_20_1(getInstance().getConfig());
            case "1.20.2" -> new NMSInvokerV1_20_2(getInstance().getConfig());
            case "1.20.3" -> new NMSInvokerV1_20_3(getInstance().getConfig());
            case "1.20.4" -> new NMSInvokerV1_20_4(getInstance().getConfig());
            case "1.20.5" -> new NMSInvokerV1_20_5(getInstance().getConfig());
            case "1.20.6" -> new NMSInvokerV1_20_6(getInstance().getConfig());
            case "1.21" -> new NMSInvokerV1_21(getInstance().getConfig());
            default -> throw new IllegalStateException("Unexpected version: " + Bukkit.getMinecraftVersion() + " only versions 1.20 - 1.20.6 are supported");
        };
        Bukkit.getPluginManager().registerEvents(nmsInvoker, instance);
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
                getNMSInvoker().setComments("display", List.of("Miscellaneous display options"));
                getNMSInvoker().setComments("display.enable-prefixes", List.of("Enable prefixes in tab and on display names with the names of origins"));
                saveConfig();
            }
            if (version.equals("2.0.3")) {
                getConfig().set("config-version", "2.1.7");
                getConfig().set("restrictions.reusing-origins", "NONE");
                getConfig().set("restrictions.prevent-same-origins", false);
                getNMSInvoker().setComments("restrictions",
                        List.of(
                                "Restrictions placed on origin selection",
                                "These are designed for use with addon plugins that add many new origins",
                                "If you run out of origins that fit the restrictions you may experience issues"
                                )
                );
                getNMSInvoker().setComments("restrictions.reusing-origins",
                        List.of(
                                "Rule for reusing origins",
                                "\"NONE\" allows origins to be reused",
                                "\"PERPLAYER\" means individual players can only use an origin once",
                                "\"ALL\" means no players can use an origin again after one has selected it"
                                )
                );
                getNMSInvoker().setComments("restrictions.prevent-same-origins",
                        List.of(
                                "Prevent players from having the same origins as other players",
                                "This is locked on if reusing-origins is set to ALL"
                                )
                );
                saveConfig();
            }
            if ((version.equals("2.1.7") || version.equals("2.1.10"))) {
                getConfig().set("config-version", "2.1.11");
                getConfig().set("worlds.disabled-worlds", List.of("example_world"));
                getNMSInvoker().setComments("worlds.disabled-worlds", List.of("Worlds to disable origins in"));
                saveConfig();
            }
            if (version.equals("2.1.14")) {
                getConfig().set("config-version", "2.1.11");
                getNMSInvoker().setComments("origin-selection.show-initial-gui", null);
                getConfig().set("origin-selection.show-initial-gui", null);
                saveConfig();
            }
            if (version.equals("2.1.11")) {
                getConfig().set("config-version", "2.1.16");
                getConfig().set("origin-selection.auto-spawn-teleport", true);
                getNMSInvoker().setComments("origin-selection.auto-spawn-teleport", List.of("Automatically teleport players to the world spawn when first selecting an origin"));
                saveConfig();
            }
            if (version.equals("2.1.16")) {
                getConfig().set("config-version", "2.1.17");
                getConfig().set("origin-selection.invulnerable-mode", "OFF");
                getNMSInvoker().setComments("origin-selection.invulnerable-mode", List.of(
                        "OFF - you can take damage with the origin selection GUI open",
                        "ON - you cannot take damage with the origin selection GUI open",
                        "INITIAL - you cannot take damage if you do not have an origin (and therefore cannot close the screen)"
                ));
                saveConfig();
            }
            if (version.equals("2.1.17")) {
                getConfig().set("config-version", "2.1.18");
                getConfig().set("origin-selection.screen-title.prefix", "");
                getConfig().set("origin-selection.screen-title.suffix", "");
                getNMSInvoker().setComments("origin-selection.screen-title.prefix", List.of("Prefix of GUI title"));
                getNMSInvoker().setComments("origin-selection.screen-title.suffix", List.of("Suffix of GUI title"));
                getNMSInvoker().setComments("origin-selection.screen-title", List.of("Prefixes and suffixes for the selection screen title", "This is an advanced setting - only use it if you know how"));
                saveConfig();
            }
            if (version.equals("2.1.18")) {
                getConfig().set("config-version", "2.1.19");
                getConfig().set("origin-selection.screen-title.background", "");
                getNMSInvoker().setComments("origin-selection.screen-title.background", List.of("Background, between Origins-Reborn background and things like text"));
                saveConfig();
            }
            if (version.equals("2.1.19")) {
                getConfig().set("config-version", "2.1.20");
                getConfig().set("messages.no-swap-command-permissions", "§cYou don't have permission to do this!");
                getNMSInvoker().setComments("messages.no-swap-command-permissions", List.of("Player used /origin swap and does not have permission"));
                getNMSInvoker().setComments("messages", List.of("Configure plugin messages", "Create an issue on the Origins-Reborn Github if you'd like to add a configuration option for a message"));
                saveConfig();
            }
            if (version.equals("2.1.20")) {
                getConfig().set("config-version", "2.2.3");
                getConfig().set("misc-settings.disable-flight-stuff", false);
                getNMSInvoker().setComments("misc-settings", List.of("Miscellaneous settings"));
                getNMSInvoker().setComments("misc-settings.disable-flight-stuff", List.of("Disable all flight-related features. This does not hide the abilities themselves, they must be removed from the .yml files in the ~/plugins/Origins-Reborn/origins/ folder"));
                saveConfig();
            }
            if (version.equals("2.2.3")) {
                getConfig().set("config-version", "2.2.5");
                getConfig().set("geyser.join-form-delay", 20);
                getNMSInvoker().setComments("geyser", List.of("Settings for using GeyserMC"));
                getNMSInvoker().setComments("geyser.join-form-delay", List.of("The delay in ticks to wait before showing a new Bedrock player the selection GUI"));
                saveConfig();
            }
            if (version.equals("2.2.5")) {
                getConfig().set("config-version", "2.2.18");
                getConfig().set("swap-command.vault.default-cost", getConfig().getInt("swap-command.vault.cost", 1000));
                getNMSInvoker().setComments("swap-command.vault.cost", null);
                getConfig().set("swap-command.vault.cost", null);
                getNMSInvoker().setComments("swap-command.vault.default-cost", List.of("Default cost of switching origin, if it hasn't been overriden in the origin file"));
                getConfig().set("origin-selection.default-origin", "NONE");
                getNMSInvoker().setComments("origin-selection.default-origin", List.of("Default origin, automatically gives players this origin rather than opening the GUI when the player has no origin", "Should be the name of the origin file without the ending, e.g. for 'origin_name.json' the value should be 'origin_name'", "Disabled if set to an invalid name such as \"NONE\""));
                saveConfig();
            }
            if (version.equals("2.2.18")) {
                getConfig().set("config-version", "2.2.20");
                getConfig().set("swap-command.vault.permanent-purchases", false);
                getNMSInvoker().setComments("swap-command.vault.permanent-purchases", List.of("Allows the player to switch back to origins for free if they already had the origin before"));
                saveConfig();
            }
            /*
            if (version.equals("2.1.11") || version.equals("2.1.10")) {
                getConfig().set("config-version", "2.2.0");
                getConfig().set("display.language", "en_us");
                getNMSInvoker().setComments("display.language", List.of(
                        "Language file to use - default is en_us"
                ));
                getConfig().set("display.force-unicode-font", "false");
                getNMSInvoker().setComments("display.force-unicode-font", List.of(
                        "Force unicode font for all characters"
                ));
                saveConfig();
            }

             */
        }
    }

    @Override
    public void onRegister() {
        instance = this;

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new OriginsRebornPlaceholderExpansion().register();
        }

        if (getConfig().getBoolean("swap-command.vault.enabled")) {
            vaultEnabled = setupEconomy();
            if (!vaultEnabled) {
                getLogger().warning("Vault is missing, origin swaps will not cost currency");
            }
        } else vaultEnabled = false;
        saveDefaultConfig();
        initializeNMSInvoker(this);
        updateConfig();
        PluginCommand command = getCommand("origin");
        if (command != null) command.setExecutor(new OriginCommand());
        Bukkit.getPluginManager().registerEvents(new OriginSwapper(), this);
        Bukkit.getPluginManager().registerEvents(new OrbOfOrigin(), this);
        Bukkit.getPluginManager().registerEvents(new PackApplier(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLeftClickEvent.PlayerLeftClickEventListener(), this);
        Bukkit.getPluginManager().registerEvents(new ParticleAbility.ParticleAbilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new BreakSpeedModifierAbility.BreakSpeedModifierAbilityListener(), this);

        PluginCommand flightCommand = getCommand("fly");
        if (flightCommand != null) flightCommand.setExecutor(new FlightToggleCommand());

        cooldowns = new Cooldowns();
    }

    @Override
    public @NotNull String getNamespace() {
        return "origins";
    }

    @Override
    public @NotNull List<Ability> getAbilities() {
        List<Ability> abilities = new ArrayList<>(List.of(
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
                StrongArms.StrongArmsBreakSpeed.strongArmsBreakSpeed,
                StrongArms.StrongArmsDrops.strongArmsDrops,
                new ShulkerInventory(),
                new NaturalArmor()
        ));
        if (nmsInvoker.getBlockInteractionRangeAttribute() != null && nmsInvoker.getEntityInteractionRangeAttribute() != null) {
            abilities.add(new ExtraReach());
            abilities.add(ExtraReach.ExtraReachBlocks.extraReachBlocks);
            abilities.add(ExtraReach.ExtraReachEntities.extraReachEntities);
        }
        return abilities;
    }
}