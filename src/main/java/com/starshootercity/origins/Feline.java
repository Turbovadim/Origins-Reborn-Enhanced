package com.starshootercity.origins;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.starshootercity.OriginSwapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Feline implements Listener {
    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent ignored) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            OriginSwapper.runForOrigin(player, "Feline",
                    () -> {
                        if (player.isSprinting()) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 5, 1, false, false));
                        }
                    });
        }
    }
}
