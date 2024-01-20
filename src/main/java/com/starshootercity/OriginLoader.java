package com.starshootercity;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Subst;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OriginLoader {
    public static List<Origin> origins = new ArrayList<>();
    public static Map<String, Origin> originNameMap = new HashMap<>();
    private static final List<OriginsAddon> registeredAddons = new ArrayList<>();

    public static void register(OriginsAddon addon) {
        if (registeredAddons.contains(addon)) {
            registeredAddons.remove(addon);
            origins.removeIf(origin -> origin.getPlugin().getName().equals(addon.getName()));
        }
        registeredAddons.add(addon);
        loadOriginsFor(addon);
        sortOrigins();
    }

    public static void reloadOrigins() {
        origins = new ArrayList<>();
        originNameMap = new HashMap<>();
        for (OriginsAddon addon : registeredAddons) {
            loadOriginsFor(addon);
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

    private static void loadOriginsFor(OriginsAddon addon) {
        File originFolder = new File(addon.getDataFolder(), "origins");
        if (!originFolder.exists()) {
            addon.saveResource("origins/", false);
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
            try (Scanner scanner = new Scanner(file)) {
                StringBuilder data = new StringBuilder();
                while (scanner.hasNext()) {
                    data.append(scanner.next());
                }
                JSONObject object = new JSONObject(data.toString());
                boolean unchoosable = false;
                if (object.has("unchoosable")) {
                    unchoosable = object.getBoolean("unchoosable");
                }
                String itemName;
                if (object.get("icon") instanceof JSONObject jsonObject) {
                    itemName = jsonObject.getString("item");
                } else itemName = object.getString("icon");
                Material material = Material.matchMaterial(itemName);
                if (material == null) {
                    material = Material.AIR;
                }
                ItemStack icon = new ItemStack(material);
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
                Origin previouslyRegisteredOrigin = originNameMap.get(name.replace("_", " "));
                if (previouslyRegisteredOrigin != null) {
                    if (previouslyRegisteredOrigin.getPriority() > origin.getPriority()) {
                        Bukkit.broadcast(Component.text(previouslyRegisteredOrigin.getPriority() + " " + origin.getPriority() + " " + previouslyRegisteredOrigin.getName() + " " + origin.getName()));
                        return;
                    } else {
                        origins.remove(previouslyRegisteredOrigin);
                        originNameMap.remove(name.replace("_", " "));
                    }
                }
                origins.add(origin);
                originNameMap.put(name.replace("_", " "), origin);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
