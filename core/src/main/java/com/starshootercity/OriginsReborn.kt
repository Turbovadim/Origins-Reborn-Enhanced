package com.starshootercity

import com.starshootercity.abilities.*
import com.starshootercity.abilities.BreakSpeedModifierAbility.BreakSpeedModifierAbilityListener
import com.starshootercity.abilities.ParticleAbility.ParticleAbilityListener
import com.starshootercity.abilities.custom.ToggleableAbilities
import com.starshootercity.commands.FlightToggleCommand
import com.starshootercity.commands.OriginCommand
import com.starshootercity.cooldowns.Cooldowns
import com.starshootercity.events.PlayerLeftClickEvent.PlayerLeftClickEventListener
import com.starshootercity.packetsenders.*
import com.starshootercity.skript.SkriptInitializer
import com.starshootercity.util.WorldGuardHook
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.endera.enderalib.bstats.MetricsLite
import java.io.File

class OriginsReborn : OriginsAddon() {

    companion object {
        @JvmStatic
        lateinit var instance: OriginsReborn
            private set

        @JvmStatic
        lateinit var NMSInvoker: NMSInvoker
            private set

        private var cooldowns: Cooldowns? = null

        @JvmStatic
        fun getCooldowns(): Cooldowns {
            return cooldowns!!
        }

        private fun initializenmsInvoker(instance: OriginsReborn) {
            val version: String? =
                Bukkit.getBukkitVersion().split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            NMSInvoker = when (version) {
                "1.18.1" -> NMSInvokerV1_18_1(Companion.instance.getConfig())
                "1.18.2" -> NMSInvokerV1_18_2(Companion.instance.getConfig())
                "1.19" -> NMSInvokerV1_19(Companion.instance.getConfig())
                "1.19.1" -> NMSInvokerV1_19_1(Companion.instance.getConfig())
                "1.19.2" -> NMSInvokerV1_19_2(Companion.instance.getConfig())
                "1.19.3" -> NMSInvokerV1_19_3(Companion.instance.getConfig())
                "1.19.4" -> NMSInvokerV1_19_4(Companion.instance.getConfig())
                "1.20" -> NMSInvokerV1_20(Companion.instance.getConfig())
                "1.20.1" -> NMSInvokerV1_20_1(Companion.instance.getConfig())
                "1.20.2" -> NMSInvokerV1_20_2(Companion.instance.getConfig())
                "1.20.3" -> NMSInvokerV1_20_3(Companion.instance.getConfig())
                "1.20.4" -> NMSInvokerV1_20_4(Companion.instance.getConfig())
                "1.20.5", "1.20.6" -> NMSInvokerV1_20_6(Companion.instance.getConfig())
                "1.21" -> NMSInvokerV1_21(Companion.instance.getConfig())
                "1.21.1" -> NMSInvokerV1_21_1(Companion.instance.getConfig())
                "1.21.2", "1.21.3" -> NMSInvokerV1_21_3(Companion.instance.getConfig())
                "1.21.4" -> NMSInvokerV1_21_4(Companion.instance.getConfig())
                else -> throw IllegalStateException("Unsupported version: " + Bukkit.getMinecraftVersion())
            }
            Bukkit.getPluginManager().registerEvents(NMSInvoker, instance)
        }

        var isWorldGuardHookInitialized: Boolean = false
            private set
    }

    var economy: Economy? = null
        private set

    private fun setupEconomy(): Boolean {
        try {
            val economyProvider = server.servicesManager.getRegistration<Economy?>(Economy::class.java)
            if (economyProvider != null) {
                economy = economyProvider.getProvider()
            }
            return (economy != null)
        } catch (e: NoClassDefFoundError) {
            return false
        }
    }

    var isVaultEnabled: Boolean = false
        private set

    fun updateConfig() {
        val version: String = getConfig().getString("config-version", "1.0.0")!!
        if (version == "1.0.0") saveResource("config.yml", true)
        else {
            if (version == "2.0.0") {
                getConfig().set("config-version", "2.0.3")
                getConfig().set("display.enable-prefixes", false)
                NMSInvoker.setComments("display", mutableListOf<String?>("Miscellaneous display options"))
                NMSInvoker.setComments(
                    "display.enable-prefixes",
                    mutableListOf<String?>("Enable prefixes in tab and on display names with the names of origins")
                )
                saveConfig()
            }
            if (version == "2.0.3") {
                getConfig().set("config-version", "2.1.7")
                getConfig().set("restrictions.reusing-origins", "NONE")
                getConfig().set("restrictions.prevent-same-origins", false)
                NMSInvoker.setComments(
                    "restrictions",
                    mutableListOf<String?>(
                        "Restrictions placed on origin selection",
                        "These are designed for use with addon plugins that add many new origins",
                        "If you run out of origins that fit the restrictions you may experience issues"
                    )
                )
                NMSInvoker.setComments(
                    "restrictions.reusing-origins",
                    mutableListOf<String?>(
                        "Rule for reusing origins",
                        "\"NONE\" allows origins to be reused",
                        "\"PERPLAYER\" means individual players can only use an origin once",
                        "\"ALL\" means no players can use an origin again after one has selected it"
                    )
                )
                NMSInvoker.setComments(
                    "restrictions.prevent-same-origins",
                    mutableListOf<String?>(
                        "Prevent players from having the same origins as other players",
                        "This is locked on if reusing-origins is set to ALL"
                    )
                )
                saveConfig()
            }
            if ((version == "2.1.7" || version == "2.1.10")) {
                getConfig().set("config-version", "2.1.11")
                getConfig().set("worlds.disabled-worlds", mutableListOf<String?>("example_world"))
                NMSInvoker.setComments("worlds.disabled-worlds", mutableListOf<String?>("Worlds to disable origins in"))
                saveConfig()
            }
            if (version == "2.1.14") {
                getConfig().set("config-version", "2.1.11")
                NMSInvoker.setComments("origin-selection.show-initial-gui", null)
                getConfig().set("origin-selection.show-initial-gui", null)
                saveConfig()
            }
            if (version == "2.1.11") {
                getConfig().set("config-version", "2.1.16")
                getConfig().set("origin-selection.auto-spawn-teleport", true)
                NMSInvoker.setComments(
                    "origin-selection.auto-spawn-teleport",
                    mutableListOf<String?>("Automatically teleport players to the world spawn when first selecting an origin")
                )
                saveConfig()
            }
            if (version == "2.1.16") {
                getConfig().set("config-version", "2.1.17")
                getConfig().set("origin-selection.invulnerable-mode", "OFF")
                NMSInvoker.setComments(
                    "origin-selection.invulnerable-mode", mutableListOf<String?>(
                        "OFF - you can take damage with the origin selection GUI open",
                        "ON - you cannot take damage with the origin selection GUI open",
                        "INITIAL - you cannot take damage if you do not have an origin (and therefore cannot close the screen)"
                    )
                )
                saveConfig()
            }
            if (version == "2.1.17") {
                getConfig().set("config-version", "2.1.18")
                getConfig().set("origin-selection.screen-title.prefix", "")
                getConfig().set("origin-selection.screen-title.suffix", "")
                NMSInvoker.setComments(
                    "origin-selection.screen-title.prefix",
                    mutableListOf<String?>("Prefix of GUI title")
                )
                NMSInvoker.setComments(
                    "origin-selection.screen-title.suffix",
                    mutableListOf<String?>("Suffix of GUI title")
                )
                NMSInvoker.setComments(
                    "origin-selection.screen-title",
                    mutableListOf<String?>(
                        "Prefixes and suffixes for the selection screen title",
                        "This is an advanced setting - only use it if you know how"
                    )
                )
                saveConfig()
            }
            if (version == "2.1.18") {
                getConfig().set("config-version", "2.1.19")
                getConfig().set("origin-selection.screen-title.background", "")
                NMSInvoker.setComments(
                    "origin-selection.screen-title.background",
                    mutableListOf<String?>("Background, between Origins-Reborn background and things like text")
                )
                saveConfig()
            }
            if (version == "2.1.19") {
                getConfig().set("config-version", "2.1.20")
                getConfig().set("messages.no-swap-command-permissions", "§cYou don't have permission to do this!")
                NMSInvoker.setComments(
                    "messages.no-swap-command-permissions",
                    mutableListOf<String?>("Player used /origin swap and does not have permission")
                )
                NMSInvoker.setComments(
                    "messages",
                    mutableListOf<String?>(
                        "Configure plugin messages",
                        "Create an issue on the Origins-Reborn Github if you'd like to add a configuration option for a message"
                    )
                )
                saveConfig()
            }
            if (version == "2.1.20") {
                getConfig().set("config-version", "2.2.3")
                getConfig().set("misc-settings.disable-flight-stuff", false)
                NMSInvoker.setComments("misc-settings", mutableListOf<String?>("Miscellaneous settings"))
                NMSInvoker.setComments(
                    "misc-settings.disable-flight-stuff",
                    mutableListOf<String?>("Disable all flight-related features. This does not hide the abilities themselves, they must be removed from the .yml files in the ~/plugins/Origins-Reborn/origins/ folder")
                )
                saveConfig()
            }
            if (version == "2.2.3") {
                getConfig().set("config-version", "2.2.5")
                getConfig().set("geyser.join-form-delay", 20)
                NMSInvoker.setComments("geyser", mutableListOf<String?>("Settings for using GeyserMC"))
                NMSInvoker.setComments(
                    "geyser.join-form-delay",
                    mutableListOf<String?>("The delay in ticks to wait before showing a new Bedrock player the selection GUI")
                )
                saveConfig()
            }
            if (version == "2.2.5") {
                getConfig().set("config-version", "2.2.18")
                getConfig().set("swap-command.vault.default-cost", getConfig().getInt("swap-command.vault.cost", 1000))
                NMSInvoker.setComments("swap-command.vault.cost", null)
                getConfig().set("swap-command.vault.cost", null)
                NMSInvoker.setComments(
                    "swap-command.vault.default-cost",
                    mutableListOf<String?>("Default cost of switching origin, if it hasn't been overriden in the origin file")
                )
                getConfig().set("origin-selection.default-origin", "NONE")
                NMSInvoker.setComments(
                    "origin-selection.default-origin",
                    mutableListOf<String?>(
                        "Default origin, automatically gives players this origin rather than opening the GUI when the player has no origin",
                        "Should be the name of the origin file without the ending, e.g. for 'origin_name.json' the value should be 'origin_name'",
                        "Disabled if set to an invalid name such as \"NONE\""
                    )
                )
                saveConfig()
            }
            if (version == "2.2.18") {
                getConfig().set("config-version", "2.2.20")
                getConfig().set("swap-command.vault.permanent-purchases", false)
                NMSInvoker.setComments(
                    "swap-command.vault.permanent-purchases",
                    mutableListOf<String?>("Allows the player to switch back to origins for free if they already had the origin before")
                )
                saveConfig()
            }
            if (version == "2.2.20") {
                getConfig().set("config-version", "2.3.0")
                getConfig().set("cooldowns.disable-all-cooldowns", false)
                getConfig().set("cooldowns.show-cooldown-icons", true)
                NMSInvoker.setComments("cooldowns", mutableListOf<String?>("Configuration for cooldowns"))
                NMSInvoker.setComments(
                    "cooldowns.disable-all-cooldowns",
                    mutableListOf<String?>(
                        "Disables every cooldown",
                        " To modify specific cooldowns, edit the cooldown-config.yml file"
                    )
                )
                NMSInvoker.setComments(
                    "cooldowns.show-cooldown-icons",
                    mutableListOf<String?>(
                        "Use the actionbar to show cooldown icons",
                        "You may want to disable this if using another plugin that requires the actionbar"
                    )
                )
                NMSInvoker.setComments(
                    "resource-pack.enabled",
                    mutableListOf<String?>(
                        "Whether to enable the resource pack",
                        "If this is set to false you should send the pack to players either in server.properties or in another plugin",
                        "You can find the packs for each version on the GitHub at https://github.com/cometcake575/Origins-Reborn/tree/main/packs/"
                    )
                )
                getConfig().set("resource-pack.link", null)
                getCooldowns().resetFile()
                saveConfig()
            }
            if (version == "2.3.0") {
                getConfig().set("config-version", "2.3.14")
                getConfig().set(
                    "commands-on-origin.example",
                    mutableListOf<String?>("example %player%", "example %uuid%")
                )
                NMSInvoker.setComments(
                    "commands-on-origin.example",
                    mutableListOf<String?>("Example configuration for a command on origin switch")
                )
                NMSInvoker.setComments(
                    "commands-on-origin",
                    mutableListOf<String?>(
                        "Runs commands when the player switches to an origin",
                        "Origins should be formatted as they are in the file names, but without the extension, e.g. \"human\"",
                        "%player% is replaced with the player's username and %uuid% is replaced with their UUID"
                    )
                )
                saveConfig()
            }
            if (version == "2.3.14") {
                getConfig().set("config-version", "2.3.15")
                NMSInvoker.setComments(
                    "commands-on-origin",
                    mutableListOf<String?>(
                        "Runs commands when the player switches to an origin",
                        "Origins should be formatted as they are in the file names, but without the extension, e.g. \"human\"",
                        "%player% is replaced with the player's username and %uuid% is replaced with their UUID",
                        "Use \"default\" for commands that should be run regardless of origin"
                    )
                )
                saveConfig()
            }

            if (version == "2.3.15") {
                getConfig().set("config-version", "2.3.17")
                getConfig().set("extra-settings.fresh-air-required-sleep-height", 86)
                NMSInvoker.setComments(
                    "extra-settings.fresh-air-required-sleep-height",
                    mutableListOf<String?>("Required sleep height for origins with the Fresh Air ability")
                )
                NMSInvoker.setComments("extra-settings", mutableListOf<String?>("Extra settings for abilities"))
                saveConfig()
            }
            if (version == "2.3.17") {
                getConfig().set("config-version", "2.3.18")
                getConfig().set(
                    "prevent-abilities-in.no_water_breathing",
                    mutableListOf<String?>("origins:water_breathing")
                )
                getConfig().set("prevent-abilities-in.no_abilities", mutableListOf<String?>("all"))
                NMSInvoker.setComments(
                    "prevent-abilities-in.no_water_breathing",
                    mutableListOf<String?>("Example region in which the water breathing ability is disabled")
                )
                NMSInvoker.setComments(
                    "prevent-abilities-in.no_abilities",
                    mutableListOf<String?>("Example region where all abilities are disabled")
                )
                NMSInvoker.setComments(
                    "prevent-abilities-in",
                    mutableListOf<String?>("A list of WorldGuard regions in which to prevent the use of certain abilities, use 'all' for all abilities")
                )
                saveConfig()
            }
            if (version == "2.3.18") {
                getConfig().set("config-version", "2.3.20")
                getConfig().set("orb-of-origin.random", false)
                NMSInvoker.setComments(
                    "orb-of-origin.random",
                    mutableListOf<String?>("Randomise origin instead of opening the selector upon using the orb")
                )
                saveConfig()
            }
            if (version == "2.3.20") {
                getConfig().set("config-version", "2.4.0")
                getConfig().set(
                    "origin-selection.default-origin.origin",
                    getConfig().get("origin-selection.default-origin")
                )
                getConfig().set("origin-selection.layer-orders.origin", 1)
                NMSInvoker.setComments(
                    "origin-selection.default-origin",
                    mutableListOf<String?>(
                        "Default origin, automatically gives players this origin rather than opening the GUI when the player has no origin",
                        "Should be the name of the origin file without the ending, e.g. for 'origin_name.json' the value should be 'origin_name'",
                        "Disabled if set to an invalid name such as \"NONE\""
                    )
                )
                NMSInvoker.setComments(
                    "origin-section.layer-orders",
                    mutableListOf<String?>("Priorities for different origin 'layers' to be selected in, higher priority layers are selected first.")
                )
                saveConfig()
            }
            if (version == "2.4.0") {
                getConfig().set("config-version", "2.4.1")
                getConfig().set("orb-of-origin.random.origin", getConfig().get("orb-of-origin.random"))
                getConfig().set("origin-selection.randomise.origin", getConfig().get("origin-selection.randomise"))
                NMSInvoker.setComments(
                    "orb-of-origin.random",
                    mutableListOf<String?>("Randomise origin instead of opening the selector upon using the orb")
                )
                NMSInvoker.setComments(
                    "origin-selection.randomise",
                    mutableListOf<String?>("Randomise origins instead of letting players pick")
                )
                saveConfig()
            }
            if (version == "2.4.1") {
                getConfig().set("config-version", "2.4.2")
                getConfig().set("origin-selection.delay-before-required", 0)
                NMSInvoker.setComments(
                    "origin-selection.delay-before-required",
                    mutableListOf<String?>("The amount of time (in ticks, a tick is a 20th of a second) to wait between a player joining and when the GUI should open")
                )
                saveConfig()
            }
        }
    }

    override fun onLoad() {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                isWorldGuardHookInitialized = WorldGuardHook.tryInitialize()
            }
        } catch (t: Throwable) {
            isWorldGuardHookInitialized = false
        }
    }

    override fun onRegister() {
        instance = this
        if (isWorldGuardHookInitialized) WorldGuardHook.completeInitialize()

        ToggleableAbilities.initialize(this)

        saveDefaultConfig()

        WidthGetter.initialize(this)
        initializenmsInvoker(this)
        AbilityRegister.setupAMAF()

        if (getConfig().getBoolean("swap-command.vault.enabled")) {
            this.isVaultEnabled = setupEconomy()
            if (!this.isVaultEnabled) {
                getLogger().warning("Vault is missing, origin swaps will not cost currency")
            }
        } else this.isVaultEnabled = false
        cooldowns = Cooldowns()
        if (!getConfig().getBoolean("cooldowns.disable-all-cooldowns") && getConfig().getBoolean("cooldowns.show-cooldown-icons")) {
            Bukkit.getPluginManager().registerEvents(cooldowns!!, this)
        }

        val metrics = MetricsLite(this, 24890)

        SkriptInitializer.initialize(this)
        updateConfig()
        val originSwapper = OriginSwapper()
        Bukkit.getPluginManager().registerEvents(originSwapper, this)
        Bukkit.getPluginManager().registerEvents(OrbOfOrigin(), this)
        Bukkit.getPluginManager().registerEvents(PackApplier(), this)
        Bukkit.getPluginManager().registerEvents(PlayerLeftClickEventListener(), this)
        Bukkit.getPluginManager().registerEvents(ParticleAbilityListener(), this)
        Bukkit.getPluginManager().registerEvents(BreakSpeedModifierAbilityListener(), this)
        originSwapper.startScheduledTask()

        val flightCommand = getCommand("fly")
        flightCommand?.setExecutor(FlightToggleCommand())

        val export = File(dataFolder, "export")
        if (!export.exists()) {
            val ignored = export.mkdir()
        }
        val imports = File(dataFolder, "import")
        if (!imports.exists()) {
            val ignored = imports.mkdir()
        }

        val command = getCommand("origin")
        command?.setExecutor(OriginCommand())
    }

    override fun getNamespace(): String {
        return "origins"
    }

    override fun getAbilities(): List<Ability> {
        val abilities: MutableList<Ability> = ArrayList<Ability>(
            mutableListOf<Ability>(
                PumpkinHate(),
                FallImmunity(),
                WeakArms(),
                Fragile(),
                SlowFalling(),
                FreshAir(),
                Vegetarian(),
                LayEggs(),
                Unwieldy(),
                MasterOfWebs(),
                Tailwind(),
                Arthropod(),
                Climbing(),
                Carnivore(),
                WaterBreathing(),
                WaterVision(),
                CatVision(),
                NineLives(),
                BurnInDaylight(),
                WaterVulnerability(),
                Phantomize(),
                Invisibility(),
                ThrowEnderPearl(),
                PhantomizeOverlay(),
                FireImmunity(),
                AirFromPotions(),
                SwimSpeed(),
                LikeWater(),
                LightArmor(),
                MoreKineticDamage(),
                DamageFromPotions(),
                DamageFromSnowballs(),
                Hotblooded(),
                BurningWrath(),
                SprintJump(),
                AerialCombatant(),
                Elytra(),
                LaunchIntoAir(),
                HungerOverTime(),
                MoreExhaustion(),
                Aquatic(),
                NetherSpawn(),
                Claustrophobia(),
                VelvetPaws(),
                AquaAffinity(),
                FlameParticles(),
                EnderParticles(),
                Phasing(),
                ScareCreepers(),
                StrongArms(),
                StrongArms.StrongArmsBreakSpeed.strongArmsBreakSpeed,
                StrongArms.StrongArmsDrops.strongArmsDrops,
                ShulkerInventory(),
                NaturalArmor()
            )
        )
        if (NMSInvoker.getBlockInteractionRangeAttribute() != null && NMSInvoker.getEntityInteractionRangeAttribute() != null) {
            abilities.add(ExtraReach())
            abilities.add(ExtraReach.ExtraReachBlocks.extraReachBlocks)
            abilities.add(ExtraReach.ExtraReachEntities.extraReachEntities)
        }
        abilities.addAll(ToggleableAbilities.getAbilities())
        return abilities.toList()
    }
}