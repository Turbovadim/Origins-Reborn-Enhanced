package com.starshootercity.geysermc;

import com.starshootercity.*;
import com.starshootercity.commands.OriginCommand;
import com.starshootercity.events.PlayerSwapOriginEvent;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.geyser.api.GeyserApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.starshootercity.OriginSwapper.orbCooldown;

public class GeyserSwapper {
    public static boolean checkBedrockSwap(Player player, PlayerSwapOriginEvent.SwapReason reason, boolean cost, boolean displayOnly) {
        try {
            if (!ShortcutUtils.isBedrockPlayer(player.getUniqueId())) {
                return true;
            } else {
                openOriginSwapper(player, reason, displayOnly, cost);
                return false;
            }
        } catch (NoClassDefFoundError e) {
            return true;
        }
    }

    public static void openOriginSwapper(Player player, PlayerSwapOriginEvent.SwapReason reason, boolean displayOnly, boolean cost) {
        List<Origin> origins = new ArrayList<>(AddonLoader.origins);
        if (!displayOnly) origins.removeIf(origin -> origin.isUnchoosable(player) || origin.hasPermission() && !player.hasPermission(origin.getPermission()));
        else {
            openOriginInfo(player, OriginSwapper.getOrigin(player), PlayerSwapOriginEvent.SwapReason.COMMAND, true, false);
            return;
        }
        origins.sort((o1, o2) -> {
            if (o1.getImpact() == o2.getImpact()) {
                if (o1.getPosition() == o2.getPosition()) return 0;
                return o1.getPosition() > o2.getPosition() ? 1 : -1;
            }
            return o1.getImpact() > o2.getImpact() ? 1 : -1;
        });

        SimpleForm.Builder form = SimpleForm.builder().title("Choose your Origin");

        for (Origin origin : origins) {
            if (origin.getIcon().getType() == Material.PLAYER_HEAD) {
                form.button(origin.getName(), FormImage.Type.URL, "https://mc-heads.net/avatar/MHF_Steve");
            } else {
                form.button(origin.getName(), FormImage.Type.URL, origin.getResourceURL());
            }

        }

        boolean enableRandom = OriginsReborn.getInstance().getConfig().getBoolean("origin-selection.random-option.enabled");
        if (enableRandom) form.button("Random", FormImage.Type.URL, "https://static.wikia.nocookie.net/origins-smp/images/1/13/Origin_Orb.png/revision/latest?cb=20210411202749");

        sendForm(player.getUniqueId(), form
                .closedOrInvalidResultHandler(() -> {
                    if (OriginSwapper.getOrigin(player) == null) {
                        openOriginSwapper(player, reason, false, cost);
                    }
                })
                .validResultHandler(response -> openOriginInfo(player, AddonLoader.originNameMap.get(response.clickedButton().text().toLowerCase()), reason, false, cost)).build());

    }

    private static void sendForm(UUID uuid, Form form) {
        try {
            FloodgateApi.getInstance().sendForm(uuid, form);
        } catch (NoClassDefFoundError e) {
            GeyserApi.api().sendForm(uuid, form);
        }
    }

    private static final Random random = new Random();

    public static void setOrigin(Player player, Origin origin, PlayerSwapOriginEvent.SwapReason reason, boolean cost) {
        if (OriginsReborn.getInstance().isVaultEnabled() && cost) {
            int amount = OriginsReborn.getInstance().getConfig().getInt("swap-command.vault.cost", 1000);
            if (origin.getCost() != null) amount = origin.getCost();
            Economy economy = OriginsReborn.getInstance().getEconomy();
            if (economy.has(player, amount)) {
                economy.withdrawPlayer(player, amount);
            } else {
                String symbol = OriginsReborn.getInstance().getConfig().getString("swap-command.vault.currency-symbol", "$");
                player.sendMessage(Component.text("You need %s%s to swap your origin!".formatted(symbol, amount)));
                return;
            }
        }


        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

        if (reason == PlayerSwapOriginEvent.SwapReason.ORB_OF_ORIGIN) {
            EquipmentSlot hand = null;
            if (meta != null) {
                if (meta.getPersistentDataContainer().has(OrbOfOrigin.orbKey, OriginSwapper.BooleanPDT.BOOLEAN)) {
                    hand = EquipmentSlot.HAND;
                }
            }
            if (hand == null) {
                ItemMeta offhandMeta = player.getInventory().getItemInOffHand().getItemMeta();
                if (offhandMeta != null) {
                    if (offhandMeta.getPersistentDataContainer().has(OrbOfOrigin.orbKey, OriginSwapper.BooleanPDT.BOOLEAN)) {
                        hand = EquipmentSlot.OFF_HAND;
                    }
                }
            }
            if (hand == null) return;
            orbCooldown.put(player, System.currentTimeMillis());
            if (hand == EquipmentSlot.HAND) player.swingMainHand();
            else player.swingOffHand();
            if (OriginsReborn.getInstance().getConfig().getBoolean("orb-of-origin.consume")) {
                ItemStack i = player.getInventory().getItem(hand);
                if (i != null) i.setAmount(i.getAmount() - 1);
            }
        }
        boolean resetPlayer = OriginSwapper.shouldResetPlayer(reason);
        if (origin == null) {
            List<Origin> origins = new ArrayList<>(AddonLoader.origins);
            origins.removeIf(origin1 -> origin1.isUnchoosable(player));
            List<String> excludedOrigins = OriginsReborn.getInstance().getConfig().getStringList("origin-selection.random-option.exclude");
            origins.removeIf(possibleOrigin -> excludedOrigins.contains(possibleOrigin.getName()));
            origin = origins.get(random.nextInt(origins.size()));
        }
        OriginsReborn.getCooldowns().setCooldown(player, OriginCommand.key);
        OriginSwapper.setOrigin(player, origin, reason, resetPlayer);
    }

    public static void openOriginInfo(Player player, Origin origin, PlayerSwapOriginEvent.SwapReason reason, boolean displayOnly, boolean cost) {
        ModalForm.Builder form = ModalForm.builder();
        StringBuilder info = new StringBuilder();
        if (origin != null) {
            form.title(origin.getName());
            for (OriginSwapper.LineData.LineComponent line : new OriginSwapper.LineData(origin).getRawLines()) {
                if (line.isEmpty()) {
                    info.append("\n\n");
                } else {
                    info.append(line.getRawText());
                }
                if (line.getType() == OriginSwapper.LineData.LineComponent.LineType.TITLE) info.append("\n");
            }
        } else {
            form.title("Random Origin");
            List<Origin> origins = new ArrayList<>(AddonLoader.origins);
            origins.removeIf(origin1 -> origin1.isUnchoosable(player));
            List<String> excludedOrigins = OriginsReborn.getInstance().getConfig().getStringList("origin-selection.random-option.exclude");
            info.append("You'll be assigned one of the following:\n\n");
            for (Origin possibleOrigin : origins) {
                if (!excludedOrigins.contains(possibleOrigin.getName())) {
                    info.append(possibleOrigin.getName()).append("\n");
                }
            }
        }
        if (cost) {
            String symbol = OriginsReborn.getInstance().getConfig().getString("swap-command.vault.currency-symbol", "$");
            int amount = OriginsReborn.getInstance().getConfig().getInt("swap-command.vault.cost", 1000);
            if (origin != null) {
                if (origin.getCost() != null) amount = origin.getCost();
            }
            info.append("\n\n\n§eThis will cost you %s%s!".formatted(symbol, amount));
        }
        form.content(info.toString());
        form.button1("Cancel");
        form.button2("Confirm");
        sendForm(player.getUniqueId(), form
                .closedOrInvalidResultHandler(() -> {
                    if (!displayOnly) {
                        openOriginInfo(player, origin, reason, false, cost);
                    }
                })
                .validResultHandler(response -> {
                    if (displayOnly) return;
                    if (response.clickedButtonId() == 0) {
                        openOriginSwapper(player, reason, false, cost);
                    } else {
                        setOrigin(player, origin, reason, cost);
                    }
                }).build());
    }
}
