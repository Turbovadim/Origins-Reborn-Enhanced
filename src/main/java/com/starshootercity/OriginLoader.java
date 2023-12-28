package com.starshootercity;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.json.JSONObject;

import java.io.*;
import java.util.Scanner;

public class OriginLoader {
    public static void loadOrigins() {
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
            try (Scanner scanner = new Scanner(file)) {
                StringBuilder data = new StringBuilder();
                while (scanner.hasNext()) {
                    data.append(scanner.next());
                }
                JSONObject object = new JSONObject(data.toString());
                Bukkit.broadcast(Component.text(object.toString()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
