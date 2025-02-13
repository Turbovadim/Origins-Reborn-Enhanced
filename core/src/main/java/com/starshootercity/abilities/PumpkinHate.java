package com.starshootercity.abilities;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import com.starshootercity.OriginsReborn;
import com.starshootercity.packetsenders.NMSInvoker;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PumpkinHate implements VisibleAbility, Listener {

    public static OriginsReborn origins = OriginsReborn.getInstance();
    // Используем Set для быстрой проверки наличия игрока
    private final Map<Player, Set<Player>> ignoringPlayers = new HashMap<>();

    public static final NMSInvoker nmsInvoker = OriginsReborn.getNMSInvoker();
    // Кэшируем неизменяемый объект AIR
    private static final ItemStack AIR_ITEMSTACK = new ItemStack(Material.AIR);

    private int tickCounter = 0;


    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        tickCounter++;
        if (tickCounter < 10) {
            return;
        }
        tickCounter = 0;
        // Получаем список всех онлайн-игроков один раз
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        // Для каждого игрока с данной способностью ("pumpkin hater")
        for (Player pumpkinHater : onlinePlayers) {
            runForAbility(pumpkinHater, hater -> {
                // Обрабатываем всех остальных игроков
                for (Player pumpkinWearer : onlinePlayers) {
                    if (pumpkinWearer.equals(hater)) continue;

                    ItemStack helmet = pumpkinWearer.getInventory().getHelmet();
                    if (helmet != null && helmet.getType() == Material.CARVED_PUMPKIN) {
                        // Инициализируем набор, если его ещё нет
                        Set<Player> ignoredSet = ignoringPlayers.computeIfAbsent(hater, key -> new HashSet<>());
                        // Добавляем, если ещё не добавлен
                        ignoredSet.add(pumpkinWearer);

                        byte data = getData(pumpkinWearer);
                        nmsInvoker.sendEntityData(hater, pumpkinWearer, data);

                        hater.hidePlayer(origins, pumpkinWearer);
                        hater.sendEquipmentChange(pumpkinWearer, EquipmentSlot.HEAD, AIR_ITEMSTACK);
                    } else {
                        // Если игрок не носит тыкву – удаляем из набора игнорируемых и обновляем видимость
                        Set<Player> ignoredSet = ignoringPlayers.computeIfAbsent(hater, key -> new HashSet<>());
                        ignoredSet.remove(pumpkinWearer);
                        hater.showPlayer(origins, pumpkinWearer);
                        AbilityRegister.updateEntity(hater, pumpkinWearer);
                    }
                }
            });
        }
    }

    /**
     * Метод возвращает битовую маску состояний игрока.
     */
    private static byte getData(Player pumpkinWearer) {
        byte data = 0x20;
        if (pumpkinWearer.getFireTicks() > 0) {
            data |= 0x01;
        }
        if (pumpkinWearer.isSneaking()) {
            data |= 0x02;
        }
        if (pumpkinWearer.isSprinting()) {
            data |= 0x08;
        }
        if (pumpkinWearer.isSwimming()) {
            data |= 0x10;
        }
        if (pumpkinWearer.isGlowing()) {
            data |= 0x40;
        }
        if (pumpkinWearer.isGliding()) {
            data |= (byte) 0x80;
        }
        return data;
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        runForAbility(event.getPlayer(), player -> {
            if (event.getItem().getType() == Material.PUMPKIN_PIE) {
                event.setCancelled(true);
                // Обновляем количество предметов (при достижении 0 сервер сам удалит предмет)
                event.getItem().setAmount(event.getItem().getAmount() - 1);

                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 300, 2, false, true));
                player.addPotionEffect(new PotionEffect(
                        OriginsReborn.getNMSInvoker().getNauseaEffect(), 300, 1, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 1200, 1, false, true));
            }
        });
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getDescription() {
        return OriginSwapper.LineData.makeLineFor(
                "You are afraid of pumpkins. For a good reason.",
                OriginSwapper.LineData.LineComponent.LineType.DESCRIPTION);
    }

    @Override
    public @NotNull List<OriginSwapper.LineData.LineComponent> getTitle() {
        return OriginSwapper.LineData.makeLineFor(
                "Scared of Gourds",
                OriginSwapper.LineData.LineComponent.LineType.TITLE
        );
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("origins:pumpkin_hate");
    }
}
