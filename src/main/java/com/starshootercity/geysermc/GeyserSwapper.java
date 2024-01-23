package com.starshootercity.geysermc;

import com.starshootercity.Origin;
import com.starshootercity.OriginLoader;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import com.starshootercity.events.PlayerSwapOriginEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeyserSwapper {
    private final static Random random = new Random();
    public static boolean checkBedrockSwap(Player player, PlayerSwapOriginEvent.SwapReason reason, boolean forceRandom, boolean cost, boolean showUnchoosable) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Geyser-Spigot")) return true;
        if (!GeyserApi.api().isBedrockPlayer(player.getUniqueId())) {
            return true;
        } else {
            openOriginSwapper(player, reason, showUnchoosable, forceRandom, cost);
            return false;
        }
    }
    public static void openOriginSwapper(Player player, PlayerSwapOriginEvent.SwapReason reason, boolean showUnchoosable, boolean forceRandom, boolean cost) {
        Bukkit.broadcast(Component.text(0));
        GeyserConnection geyserPlayer = GeyserApi.api().connectionByUuid(player.getUniqueId());
        if (geyserPlayer == null) return;

        List<Origin> origins = new ArrayList<>(OriginLoader.origins);
        if (!showUnchoosable) origins.removeIf(Origin::isUnchoosable);

        boolean reset = OriginSwapper.shouldResetPlayer(reason);
        if (forceRandom) {
            Origin origin = origins.get(random.nextInt(origins.size()));
            OriginSwapper.setOrigin(player, origin, reason, reset);
            openOriginInfo(player, origin, reason, true, false, false);
            return;
        }

        SimpleForm.Builder form = SimpleForm.builder().title("Origins");
        for (Origin origin : origins) {
            if (origin.getIcon().getType() == Material.PLAYER_HEAD) {
                form.button(origin.getName(), FormImage.Type.URL, "https://minotar.net/avatar/" + player.getName() + "/32");
            } else {
//                form.button(origin.getName(), FormImage.Type.URL, origin.getResourceLocation());
                form.button(origin.getName());
            }
        }
        form.button("Random");

        geyserPlayer.sendForm(form
                .closedOrInvalidResultHandler(() -> {
                    if (reason == PlayerSwapOriginEvent.SwapReason.INITIAL) {
                        openOriginSwapper(player, reason, showUnchoosable, false, cost);
                    }
                })
                .validResultHandler(response -> {
                    openOriginInfo(player, OriginLoader.originNameMap.get(response.clickedButton().text().toLowerCase()), reason, false, reset, cost);
                    //setOrigin(player, response.clickedButton().text().toLowerCase(), reason, reset);
                }).build());

    }

    public static void setOrigin(Player player, Origin origin, PlayerSwapOriginEvent.SwapReason reason, boolean reset, boolean cost) {
        Bukkit.getScheduler().runTask(OriginsReborn.getInstance(), () -> {
            OriginSwapper.setOrigin(player, origin, reason, reset);
        });
    }

    public static void openOriginInfo(Player player, Origin origin, PlayerSwapOriginEvent.SwapReason reason, boolean displayOnly, boolean reset, boolean cost) {
        GeyserConnection geyserPlayer = GeyserApi.api().connectionByUuid(player.getUniqueId());
        if (geyserPlayer == null) return;
        SimpleForm.Builder form = SimpleForm.builder().title(origin.getName());
        form.content("Test");
        form.button("Confirm");
        Bukkit.broadcast(Component.text(2));
        geyserPlayer.sendForm(form
                .closedOrInvalidResultHandler(() -> {
                    openOriginInfo(player, origin, reason, displayOnly, reset, cost);
                })
                .validResultHandler(response -> {
                    if (displayOnly) return;
                    setOrigin(player, origin, reason, reset, cost);
                }));
    }
}
