package com.starshootercity

import com.starshootercity.OriginsReborn.Companion.NMSInvoker
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.packetsenders.OriginsRebornResourcePackInfo
import com.viaversion.viaversion.api.Via
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PackApplier : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (OriginsReborn.mainConfig.resourcePack.enabled) {
            sendPacks(event.getPlayer())
            if (ShortcutUtils.isBedrockPlayer(event.getPlayer().uniqueId)) return
            Bukkit.getScheduler().scheduleSyncDelayedTask(instance, Runnable { sendPacks(event.getPlayer()) }, 60)
        }
    }

    companion object {
        private val addonPacks: MutableMap<Class<out OriginsAddon>, OriginsRebornResourcePackInfo> =
            HashMap<Class<out OriginsAddon>, OriginsRebornResourcePackInfo>()

        @JvmStatic
        fun sendPacks(player: Player) {
            NMSInvoker.sendResourcePacks(player, getPackURL(player), addonPacks)
        }

        fun getPackURL(player: Player): String {
            val ver: Array<String?> =
                getVersion(player)!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return when (ver[ver.size - 1]) {
                "1.19.1", "1.19.2" -> "https://github.com/Turbovadim/Origins-Reborn-Enhanced/raw/main/packs/1.19.1-1.19.2.zip"
                "1.19.3" -> "https://github.com/Turbovadim/Origins-Reborn-Enhanced/raw/main/packs/1.19.3.zip"
                "1.19.4" -> "https://github.com/Turbovadim/Origins-Reborn-Enhanced/raw/main/packs/1.19.4.zip"
                "1.20", "1.20.1" -> "https://github.com/Turbovadim/Origins-Reborn-Enhanced/raw/main/packs/1.20-1.20.1.zip"
                "1.20.2" -> "https://github.com/Turbovadim/Origins-Reborn-Enhanced/raw/main/packs/1.20.2.zip"
                "1.20.3", "1.20.4" -> "https://github.com/Turbovadim/Origins-Reborn-Enhanced/raw/main/packs/1.20.3-1.20.4.zip"
                "1.20.5", "1.20.6" -> "https://github.com/Turbovadim/Origins-Reborn-Enhanced/raw/main/packs/1.20.5-1.20.6.zip"
                "1.21", "1.21.1" -> "https://github.com/Turbovadim/Origins-Reborn-Enhanced/raw/main/packs/1.21.zip"
                "1.21.2", "1.21.3" -> "https://github.com/Turbovadim/Origins-Reborn-Enhanced/raw/main/packs/1.21.3.zip"
                "1.21.4" -> "https://github.com/Turbovadim/Origins-Reborn-Enhanced/raw/main/packs/1.21.4.zip"
                else -> "https://github.com/Turbovadim/Origins-Reborn-Enhanced/raw/main/src/main/Origins%20Pack.zip"
            }
        }

        fun getVersion(player: Player): String? {
            return try {
                Via.getAPI().getPlayerProtocolVersion(player.uniqueId).name
            } catch (_: NoClassDefFoundError) {
                Bukkit.getBukkitVersion().split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            }
        }

        /*
    @Subscribe
    public void onGeyserLoadResourcePacks(GeyserLoadResourcePacksEvent event) {
        event.resourcePacks().add(new File(OriginsReborn.getInstance().getDataFolder(), "bedrock-packs/bedrock.mcpack").toPath());
    }

    public PackApplier() {
        OriginsReborn.getInstance().saveResource("bedrock.mcpack", false);
    }

 */
        fun addResourcePack(addon: OriginsAddon, info: OriginsRebornResourcePackInfo) {
            addonPacks.put(addon.javaClass, info)
        }
    }
}
