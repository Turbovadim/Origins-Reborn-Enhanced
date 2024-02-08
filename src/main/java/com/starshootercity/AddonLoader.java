package com.starshootercity;

import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AddonLoader {
    public static List<Origin> origins = new ArrayList<>();
    public static Map<String, Origin> originNameMap = new HashMap<>();
    public static Map<String, Origin> originFileNameMap = new HashMap<>();
    private static final List<OriginsAddon> registeredAddons = new ArrayList<>();

    public static void register(OriginsAddon addon) {
        if (registeredAddons.contains(addon)) {
            registeredAddons.remove(addon);
            origins.removeIf(origin -> origin.getAddon().getName().equals(addon.getName()));
        }
        registeredAddons.add(addon);
        loadOriginsFor(addon);
        prepareLanguagesFor(addon);
        sortOrigins();
    }

    private static Map<String, String> languageData = new HashMap<>();

    public static @NotNull String getTextFor(String key, String def) {
        String result = languageData.get(key);
        return result == null ? def : result;
    }

    public static @Nullable String getTextFor(String key) {
        return languageData.get(key);
    }

    public static void reloadAddons() {
        origins = new ArrayList<>();
        originNameMap = new HashMap<>();
        languageData = new HashMap<>();
        for (OriginsAddon addon : registeredAddons) {
            loadOriginsFor(addon);
            prepareLanguagesFor(addon);
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
            Origin origin = new Origin(formattedName.toString(), icon, object.getInt("order"), object.getInt("impact"), new ArrayList<>() {{
                if (object.has("powers")) {
                    JSONArray array = object.getJSONArray("powers");
                    for (@Subst("origins:ability") Object o : array) {
                        add(Key.key(((String) o)));
                    }
                }
            }}, object.getString("description"), addon, unchoosable, object.has("priority") ? object.getInt("priority") : 1);
            String actualName = origin.getActualName().toLowerCase();
            Origin previouslyRegisteredOrigin = originNameMap.get(name.replace("_", " "));
            if (previouslyRegisteredOrigin != null) {
                if (previouslyRegisteredOrigin.getPriority() > origin.getPriority()) {
                    continue;
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
    }
}
