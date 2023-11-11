package com.starshootercity;

import com.starshootercity.origins.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class OriginsReborn extends JavaPlugin {
    private static OriginsReborn instance;

    public static OriginsReborn getInstance() {
        return instance;
    }


    public static Economy economy;
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

    @Override
    public void onEnable() {
        instance = this;
        if (!setupEconomy()) {
            getLogger().warning("Vault is not enabled");
        }
        saveDefaultConfig();
        PluginCommand command = getCommand("origin-swap");
        OriginSwapper swapper = new OriginSwapper();
        if (command != null) command.setExecutor(swapper);
        Bukkit.getPluginManager().registerEvents(swapper, this);
        Bukkit.getPluginManager().registerEvents(new Arachnid(), this);
        Bukkit.getPluginManager().registerEvents(new Avian(), this);
        Bukkit.getPluginManager().registerEvents(new Blazeborn(), this);
        Bukkit.getPluginManager().registerEvents(new Elytrian(), this);
        Bukkit.getPluginManager().registerEvents(new Enderian(), this);
        Bukkit.getPluginManager().registerEvents(new Feline(), this);
        Bukkit.getPluginManager().registerEvents(new Merling(), this);
        Bukkit.getPluginManager().registerEvents(new Phantom(), this);
        Bukkit.getPluginManager().registerEvents(new Shulk(), this);
    }
}