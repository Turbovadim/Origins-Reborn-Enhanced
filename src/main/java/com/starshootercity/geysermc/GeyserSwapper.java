package com.starshootercity.geysermc;

import com.starshootercity.Origin;
import com.starshootercity.OriginLoader;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import com.starshootercity.events.PlayerSwapOriginEvent;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.cumulus.component.ButtonComponent;
import org.geysermc.cumulus.component.Component;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.response.FormResponse;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GeyserSwapper {
    public static void openOriginSwapper(Player player, PlayerSwapOriginEvent.SwapReason reason, boolean showUnchoosable) {
        GeyserConnection geyserPlayer = GeyserApi.api().connectionByUuid(player.getUniqueId());
        assert geyserPlayer != null;

        List<Origin> origins = new ArrayList<>(OriginLoader.origins);
        if (!showUnchoosable) origins.removeIf(Origin::isUnchoosable);

        SimpleForm.Builder form = SimpleForm.builder().title("Origins");
        for (Origin origin : origins) {
            if (origin.getName().equals("Human")) {
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
                        openOriginSwapper(player, reason, showUnchoosable);
                    }
                })
                .validResultHandler(response -> {
                    setOrigin(player, response.getClickedButton().text().toLowerCase(), reason, false);
                }).build());
    }

    public static void setOrigin(Player player, String origin, PlayerSwapOriginEvent.SwapReason reason, boolean reset) {
        Bukkit.getScheduler().runTask(OriginsReborn.getInstance(), () -> {
            OriginSwapper.setOrigin(player, OriginLoader.originNameMap.get(origin), reason, reset);
        });
    }

    public static void openOriginInfo(Player player, Origin origin) {

    }
}
