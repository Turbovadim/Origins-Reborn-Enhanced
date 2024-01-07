package com.starshootercity;

import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Subst;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class OriginLoader {
    public static List<Origin> origins;
    public static Map<String, Origin> originNameMap;
    public static void loadOrigins() {
        origins = new ArrayList<>();
        originNameMap = new HashMap<>();
        OriginsReborn instance = OriginsReborn.getInstance();
        File originFolder = new File(instance.getDataFolder(), "origins");
        if (!originFolder.exists()) {
            boolean ignored = originFolder.mkdirs();
            instance.saveResource("origins/arachnid.json", false);
            instance.saveResource("origins/avian.json", false);
            instance.saveResource("origins/blazeborn.json", false);
            instance.saveResource("origins/elytrian.json", false);
            instance.saveResource("origins/enderian.json", false);
            instance.saveResource("origins/feline.json", false);
            instance.saveResource("origins/human.json", false);
            instance.saveResource("origins/merling.json", false);
            instance.saveResource("origins/phantom.json", false);
            instance.saveResource("origins/shulk.json", false);
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
                String itemName = object.getJSONObject("icon").getString("item");
                Material material = Material.matchMaterial(itemName);
                if (material == null) {
                    material = Material.AIR;
                }
                ItemStack icon = new ItemStack(material);
                String name = file.getName().split("\\.")[0];
                Origin origin = new Origin(name.substring(0, 1).toUpperCase() + name.substring(1), icon, object.getInt("order"), object.getInt("impact"), new ArrayList<>() {{
                    if (object.has("powers")) {
                        JSONArray array = object.getJSONArray("powers");
                        for (@Subst("origins:ability") Object o : array) {
                            add(Key.key(((String) o)));
                        }
                    }
                }}, object.getString("description"));
                origins.add(origin);
                originNameMap.put(name, origin);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        origins.sort((o1, o2) -> {
            if (o1.getImpact() == o2.getImpact()) {
                if (o1.getPosition() == o2.getPosition()) return 0;
                return o1.getPosition() > o2.getPosition() ? 1 : -1;
            }
            return o1.getImpact() > o2.getImpact() ? 1 : -1;
        });
    }
}
