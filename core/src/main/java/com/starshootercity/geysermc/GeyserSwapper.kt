package com.starshootercity.geysermc

import com.starshootercity.AddonLoader.getOrigin
import com.starshootercity.AddonLoader.getOrigins
import com.starshootercity.OrbOfOrigin
import com.starshootercity.Origin
import com.starshootercity.OriginSwapper
import com.starshootercity.OriginSwapper.Companion.getOrigin
import com.starshootercity.OriginSwapper.Companion.shouldResetPlayer
import com.starshootercity.OriginSwapper.LineData
import com.starshootercity.OriginsReborn.Companion.getCooldowns
import com.starshootercity.OriginsReborn.Companion.instance
import com.starshootercity.ShortcutUtils.isBedrockPlayer
import com.starshootercity.commands.OriginCommand
import com.starshootercity.events.PlayerSwapOriginEvent.SwapReason
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.geysermc.cumulus.form.Form
import org.geysermc.cumulus.form.ModalForm
import org.geysermc.cumulus.form.SimpleForm
import org.geysermc.cumulus.util.FormImage
import org.geysermc.floodgate.api.FloodgateApi
import org.geysermc.geyser.api.GeyserApi
import java.util.*
import java.util.function.Consumer

object GeyserSwapper {
    fun checkBedrockSwap(
        player: Player,
        reason: SwapReason,
        cost: Boolean,
        displayOnly: Boolean,
        layer: String
    ): Boolean {
        try {
            if (!isBedrockPlayer(player.uniqueId)) {
                return true
            } else {
                openOriginSwapper(player, reason, displayOnly, cost, layer)
                return false
            }
        } catch (_: NoClassDefFoundError) {
            return true
        }
    }

    fun openOriginSwapper(
        player: Player,
        reason: SwapReason,
        displayOnly: Boolean,
        cost: Boolean,
        layer: String
    ) {
        if (displayOnly) {
            getOrigin(player, layer)?.let { origin ->
                openOriginInfo(player, origin, SwapReason.COMMAND, true, false, layer)
            }
            return
        }

        val origins = getOrigins(layer)
            .filterNotNull()
            .filterNot { origin ->
                origin.isUnchoosable(player) ||
                        (origin.hasPermission() && (origin.permission == null || !player.hasPermission(origin.permission)))
            }
            .toMutableList()

        origins.sortWith(compareBy({ it.impact }, { it.position }))

        val form = SimpleForm.builder().title("Choose your Origin")
        origins.forEach { origin ->
            val imageUrl = if (origin.icon.type == Material.PLAYER_HEAD)
                "https://mc-heads.net/avatar/MHF_Steve"
            else
                origin.getResourceURL()
            form.button(origin.getName(), FormImage.Type.URL, imageUrl)
        }

        if (instance.getConfig().getBoolean("origin-selection.random-option.enabled")) {
            form.button(
                "Random",
                FormImage.Type.URL,
                "https://static.wikia.nocookie.net/origins-smp/images/1/13/Origin_Orb.png/revision/latest?cb=20210411202749"
            )
        }

        sendForm(
            player.uniqueId,
            form
                .closedOrInvalidResultHandler(Runnable {
                    if (getOrigin(player, layer) == null) {
                        openOriginSwapper(player, reason, false, cost, layer)
                    }
                })
                .validResultHandler(Consumer { response ->
                    val clickedOrigin = getOrigin(response!!.clickedButton().text().lowercase(Locale.getDefault()))
                    openOriginInfo(player, clickedOrigin, reason, false, cost, layer)
                })
                .build()
        )
    }

    private fun sendForm(uuid: UUID, form: Form) {
        try {
            FloodgateApi.getInstance().sendForm(uuid, form)
        } catch (_: NoClassDefFoundError) {
            GeyserApi.api().sendForm(uuid, form)
        }
    }

    private val random = Random()

    fun setOrigin(
        player: Player,
        origin: Origin?,
        reason: SwapReason,
        cost: Boolean,
        layer: String
    ) {
        var selectedOrigin = origin

        if (instance.isVaultEnabled && cost) {
            val defaultCost = instance.getConfig().getInt("swap-command.vault.cost", 1000)
            // Use the provided origin's cost if available; otherwise, fall back to defaultCost.
            val amount = selectedOrigin?.cost ?: defaultCost
            val amountDouble = amount.toDouble()
            val economy = checkNotNull(instance.economy)
            if (economy.has(player, amountDouble)) {
                economy.withdrawPlayer(player, amountDouble)
            } else {
                val symbol = instance.getConfig().getString("swap-command.vault.currency-symbol", "$")!!
                player.sendMessage(Component.text("You need $symbol$amount to swap your origin!"))
                return
            }
        }

        if (reason == SwapReason.ORB_OF_ORIGIN) {
            // Check main hand then offhand for the orb item.
            val hand = when {
                player.inventory.itemInMainHand.itemMeta?.persistentDataContainer
                    ?.has<Byte, Boolean>(OrbOfOrigin.orbKey, OriginSwapper.BooleanPDT.BOOLEAN) == true -> EquipmentSlot.HAND
                player.inventory.itemInOffHand.itemMeta?.persistentDataContainer
                    ?.has<Byte, Boolean>(OrbOfOrigin.orbKey, OriginSwapper.BooleanPDT.BOOLEAN) == true -> EquipmentSlot.OFF_HAND
                else -> null
            } ?: return

            OriginSwapper.orbCooldown[player] = System.currentTimeMillis()
            when (hand) {
                EquipmentSlot.HAND -> player.swingMainHand()
                EquipmentSlot.OFF_HAND -> player.swingOffHand()
                else -> {}
            }
            if (instance.getConfig().getBoolean("orb-of-origin.consume")) {
                player.inventory.getItem(hand)?.let { item ->
                    item.amount = item.amount - 1
                }
            }
        }

        val resetPlayer = shouldResetPlayer(reason)
        if (selectedOrigin == null) {
            val excludedOrigins = instance.getConfig().getStringList("origin-selection.random-option.exclude")
            val origins = getOrigins(layer)
                .filterNotNull()
                .filter { !it.isUnchoosable(player) && excludedOrigins.contains(it.getName())}
            selectedOrigin = origins[random.nextInt(origins.size)]
        }

        getCooldowns().setCooldown(player, OriginCommand.key)
        OriginSwapper.setOrigin(player, selectedOrigin, reason, resetPlayer, layer)
    }

    fun openOriginInfo(
        player: Player,
        origin: Origin?,
        reason: SwapReason,
        displayOnly: Boolean,
        cost: Boolean,
        layer: String
    ) {
        val form = ModalForm.builder()
        val info = StringBuilder()

        if (origin != null) {
            form.title(origin.getName())
            LineData(origin).rawLines.forEach { line ->
                checkNotNull(line)
                info.append(if (line.isEmpty) "\n\n" else line.rawText)
                if (line.type == LineData.LineComponent.LineType.TITLE) info.append("\n")
            }
        } else {
            form.title("Random Origin")
            val excludedOrigins = instance.getConfig().getStringList("origin-selection.random-option.exclude")
            val origins = getOrigins(layer).filterNotNull().filter { !it.isUnchoosable(player) }
            info.append("You'll be assigned one of the following:\n\n")
            origins.filter { it.getName() !in excludedOrigins }
                .forEach { possibleOrigin -> info.append(possibleOrigin.getName()).append("\n") }
        }

        if (cost) {
            val symbol = instance.getConfig().getString("swap-command.vault.currency-symbol", "$")!!
            var amount = instance.getConfig().getInt("swap-command.vault.cost", 1000)
            origin?.cost?.let { amount = it }
            info.append("\n\n\n§eThis will cost you $symbol$amount!")
        }

        form.content(info.toString())
            .button1("Cancel")
            .button2("Confirm")

        sendForm(
            player.uniqueId, form
                .closedOrInvalidResultHandler(Runnable {
                    if (!displayOnly) {
                        openOriginInfo(player, origin, reason, false, cost, layer)
                    }
                })
                .validResultHandler(Consumer { response ->
                    if (displayOnly) return@Consumer
                    if (response?.clickedButtonId() == 0) {
                        openOriginSwapper(player, reason, false, cost, layer)
                    } else {
                        setOrigin(player, origin, reason, cost, layer)
                    }
                })
                .build()
        )
    }

}
