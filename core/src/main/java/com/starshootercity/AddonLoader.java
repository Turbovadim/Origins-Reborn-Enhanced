package com.starshootercity;

import com.starshootercity.events.PlayerSwapOriginEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AddonLoader {
    private static final List<Origin> origins = new ArrayList<>();
    private static final Map<String, Origin> originNameMap = new HashMap<>();
    private static final Map<String, Origin> originFileNameMap = new HashMap<>();
    public static final List<OriginsAddon> registeredAddons = new ArrayList<>();
    public static Map<String, List<File>> originFiles = new HashMap<>();
    public static List<String> layers = new ArrayList<>();

    private static final Random random = new Random();

    public static @Nullable String getFirstUnselectedLayer(Player player) {
        for (String layer : layers) {
            if (OriginSwapper.getOrigin(player, layer) == null) return layer;
        }
        return null;
    }

    public static Origin getOrigin(String name) {
        return originNameMap.get(name);
    }

    public static Origin getOriginByFilename(String name) {
        return originFileNameMap.get(name);
    }

    public static List<Origin> getOrigins(String layer) {
        List<Origin> o = new ArrayList<>(origins);
        o.removeIf(or -> !or.getLayer().equals(layer));
        return o;
    }

    public static Origin getFirstOrigin(String layer) {
        return getOrigins(layer).get(0);
    }

    public static Origin getRandomOrigin(String layer) {
        List<Origin> o = getOrigins(layer);
        return o.get(random.nextInt(o.size()));
    }

    public static void register(OriginsAddon addon) {
        if (registeredAddons.contains(addon)) {
            registeredAddons.remove(addon);
            origins.removeIf(origin -> origin.getAddon().getNamespace().equals(addon.getNamespace()));
        }
        registeredAddons.add(addon);
        loadOriginsFor(addon);
        //prepareLanguagesFor(addon);
        if (addon.shouldAllowOriginSwapCommand() != null) allowOriginSwapChecks.add(addon.shouldAllowOriginSwapCommand());
        if (addon.shouldOpenSwapMenu() != null) openSwapMenuChecks.add(addon.shouldOpenSwapMenu());
        if (addon.hasAbilityOverride() != null) hasAbilityOverrideChecks.add(addon.hasAbilityOverride());
        sortOrigins();
    }

    public static boolean shouldOpenSwapMenu(Player player, PlayerSwapOriginEvent.SwapReason reason) {
        for (OriginsAddon.SwapStateGetter getter : openSwapMenuChecks) {
            OriginsAddon.State v = getter.get(player, reason);
            if (v == OriginsAddon.State.DENY) return false;
        }
        return true;
    }

    public static boolean allowOriginSwapCommand(Player player) {
        boolean allowed = false;
        for (OriginsAddon.SwapStateGetter getter : allowOriginSwapChecks) {
            OriginsAddon.State v = getter.get(player, PlayerSwapOriginEvent.SwapReason.COMMAND);
            if (v == OriginsAddon.State.DENY) return false;
            else if (v == OriginsAddon.State.ALLOW) allowed = true;
        }
        return allowed || player.hasPermission(OriginsReborn.getInstance().getConfig().getString("swap-command.permission", "originsreborn.admin"));
    }

    public static final List<OriginsAddon.SwapStateGetter> allowOriginSwapChecks = new ArrayList<>();
    public static final List<OriginsAddon.SwapStateGetter> openSwapMenuChecks = new ArrayList<>();
    public static final List<OriginsAddon.KeyStateGetter> hasAbilityOverrideChecks = new ArrayList<>();

    private static final Map<String, String> languageData = new HashMap<>();

    public static @NotNull String getTextFor(String key, String fallback) {
        String result = languageData.get(key);
        return result == null ? fallback : result;
    }

    public static @Nullable String getTextFor(String key) {
        return languageData.get(key);
    }

    public static void reloadAddons() {
        origins.clear();
        originNameMap.clear();
        languageData.clear();
        originFiles.clear();
        for (OriginsAddon addon : registeredAddons) {
            loadOriginsFor(addon);
            //prepareLanguagesFor(addon);
        }
        sortOrigins();
    }

    public static void sortOrigins() {
        origins.sort((o1, o2) -> {
            if (o1.getImpact() == o2.getImpact()) {
                if (o1.getPosition() == o2.getPosition()) return 0;
                return o1.getPosition() > o2.getPosition() ? 1 : -1;
            }
            return o1.getImpact() > o2.getImpact() ? 1 : -1;
        });
    }


    private static final int BUFFER_SIZE = 4096;

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    @SuppressWarnings("unused")
    private static void prepareLanguagesFor(OriginsAddon addon) {
        File langFolder = new File(addon.getDataFolder(), "lang");
        boolean ignored = langFolder.mkdirs();
        try (ZipInputStream inputStream = new ZipInputStream(new FileInputStream(addon.getFile()))) {
            ZipEntry entry = inputStream.getNextEntry();
            while (entry != null) {
                if (entry.getName().startsWith("lang/") && entry.getName().endsWith(".json")) {
                    extractFile(inputStream, langFolder.getParentFile().getAbsolutePath() + "/" + entry.getName());
                }
                entry = inputStream.getNextEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String lang = OriginsReborn.getInstance().getConfig().getString("display.language", "en_us");
        File[] files = langFolder.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.getName().equals(lang + ".json")) {
                JSONObject object = ShortcutUtils.openJSONFile(file);
                for (String s : object.keySet()) {
                    languageData.put(s, object.getString(s));
                }
            }
        }
    }

    private static void loadOriginsFor(OriginsAddon addon) {
        List<File> addonFiles = new ArrayList<>();
        originFiles.put(addon.getNamespace(), addonFiles);
        File originFolder = new File(addon.getDataFolder(), "origins");
        if (!originFolder.exists()) {
            boolean ignored = originFolder.mkdirs();
            try (ZipInputStream inputStream = new ZipInputStream(new FileInputStream(addon.getFile()))) {
                ZipEntry entry = inputStream.getNextEntry();
                while (entry != null) {
                    if (entry.getName().startsWith("origins/") && entry.getName().endsWith(".json")) {
                        extractFile(inputStream, originFolder.getParentFile().getAbsolutePath() + "/" + entry.getName());
                    }
                    entry = inputStream.getNextEntry();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File[] files = originFolder.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".json")) continue;
            addonFiles.add(file);
            loadOrigin(file, addon);
        }
    }

    private static void sortLayers() {
        layers.sort((o1, o2) -> {
            int a1 = OriginsReborn.getInstance().getConfig().getInt("origin-selection.layers.%s".formatted(o1));
            int a2 = OriginsReborn.getInstance().getConfig().getInt("origin-selection.layers.%s".formatted(o2));
            return a1 - a2;
        });
    }

    public static void registerLayer(String layer, int priority) {
        if (layers.contains(layer)) return;
        layers.add(layer);

        if (!OriginsReborn.getInstance().getConfig().contains("origin-selection.default-origin.%s".formatted(layer))) {
            OriginsReborn.getInstance().getConfig().set("origin-selection.default-origin.%s".formatted(layer), "NONE");
            OriginsReborn.getNMSInvoker().setComments("origin-selection.default-origin", List.of("Default origin, automatically gives players this origin rather than opening the GUI when the player has no origin", "Should be the name of the origin file without the ending, e.g. for 'origin_name.json' the value should be 'origin_name'", "Disabled if set to an invalid name such as \"NONE\""));
            OriginsReborn.getInstance().saveConfig();
        }

        if (!OriginsReborn.getInstance().getConfig().contains("origin-selection.layer-orders.%s".formatted(layer))) {
            OriginsReborn.getInstance().getConfig().set("origin-selection.layer-orders.%s".formatted(layer), priority);
            OriginsReborn.getNMSInvoker().setComments("origin-section.layer-orders", List.of("Priorities for different origin 'layers' to be selected in, higher priority layers are selected first."));
            OriginsReborn.getInstance().saveConfig();
        }

        if (!OriginsReborn.getInstance().getConfig().contains("orb-of-origin.random.%s".formatted(layer))) {
            OriginsReborn.getInstance().getConfig().set("orb-of-origin.random.%s".formatted(layer), false);
            OriginsReborn.getNMSInvoker().setComments("orb-of-origin.random", List.of("Randomise origin instead of opening the selector upon using the orb"));
            OriginsReborn.getInstance().saveConfig();
        }

        sortLayers();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new OriginsRebornPlaceholderExpansion(layer).register();
        }
    }

    public static void loadOrigin(File file, OriginsAddon addon) {
        if (!file.getName().toLowerCase().equals(file.getName())) {
            File lowercaseFile = new File(file.getParentFile(), file.getName().toLowerCase());
            if (!file.renameTo(lowercaseFile)) {
                OriginsReborn.getInstance().getLogger().warning("Origin %s failed to load - make sure file name is lowercase".formatted(file.getName()));
                return;
            }
            file = lowercaseFile;
        }
        JSONObject object = ShortcutUtils.openJSONFile(file);
        boolean unchoosable = false;
        if (object.has("unchoosable")) {
            unchoosable = object.getBoolean("unchoosable");
        }
        String itemName;
        int cmd = 0;
        if (object.get("icon") instanceof JSONObject jsonObject) {
            itemName = jsonObject.getString("item");
            if (jsonObject.has("custom_model_data")) {
                cmd = jsonObject.getInt("custom_model_data");
            }
        } else itemName = object.getString("icon");
        Material material = Material.matchMaterial(itemName);
        if (material == null) {
            material = Material.AIR;
        }
        ItemStack icon = new ItemStack(material);
        ItemMeta meta = icon.getItemMeta();
        meta.setCustomModelData(cmd);
        icon.setItemMeta(meta);
        String name = file.getName().split("\\.")[0];
        StringBuilder formattedName = new StringBuilder();
        String[] parts = name.split("_");
        int num = 0;
        for (String part : parts) {
            formattedName.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
            num++;
            if (num < parts.length) formattedName.append(" ");
        }
        String permission = null;
        Integer cost = null;
        int max;
        if (object.has("max")) {
            max = object.getInt("max");
        } else max = -1;
        String layer;
        if (object.has("layer")) layer = object.getString("layer");
        else layer = "origin";
        if (object.has("permission")) permission = object.getString("permission");
        if (object.has("cost")) cost = object.getInt("cost");
        int extraLayerPriority = 0;
        ConfigurationSection cs = OriginsReborn.getInstance().getConfig().getConfigurationSection("origin-selection.layers");
        if (cs != null) for (String s : cs.getValues(false).keySet()) {
            extraLayerPriority = Math.min(extraLayerPriority, cs.getInt(s)-1);
        }
        registerLayer(layer, extraLayerPriority);
        String displayName;
        if (object.has("name")) displayName = object.getString("name");
        else displayName = formattedName.toString();
        Origin origin = new Origin(formattedName.toString(), icon, object.getInt("order"), object.getInt("impact"), displayName, new ArrayList<>() {{
            if (object.has("powers")) {
                JSONArray array = object.getJSONArray("powers");
                for (@Subst("origins:ability") Object o : array) {
                    add(Key.key(((String) o)));
                }
            }
        }}, object.getString("description"), addon, unchoosable, object.has("priority") ? object.getInt("priority") : 1, permission, cost, max, layer);
        String actualName = origin.getActualName().toLowerCase();
        Origin previouslyRegisteredOrigin = originNameMap.get(name.replace("_", " "));
        if (previouslyRegisteredOrigin != null) {
            if (previouslyRegisteredOrigin.getPriority() > origin.getPriority()) {
                return;
            } else {
                origins.remove(previouslyRegisteredOrigin);
                originNameMap.remove(name.replace("_", " "));
                originFileNameMap.remove(actualName);
            }
        }
        origins.add(origin);
        originNameMap.put(name.replace("_", " "), origin);
        originFileNameMap.put(actualName, origin);
    }

    /**
     * @deprecated Origins-Reborn now has a 'layer' system, allowing for multiple origins to be set at once
     * @return The default origin for the 'origin' layer
     */
    @Deprecated
    public static @Nullable Origin getDefaultOrigin() {
        return getDefaultOrigin("origin");
    }

    /**
     * @return The default origin for the specified layer
     */
    public static @Nullable Origin getDefaultOrigin(String layer) {
        String originName = OriginsReborn.getInstance().getConfig().getString("origin-selection.default-origin.%s".formatted(layer), "NONE");
        return originFileNameMap.getOrDefault(originName, null);
    }
}
