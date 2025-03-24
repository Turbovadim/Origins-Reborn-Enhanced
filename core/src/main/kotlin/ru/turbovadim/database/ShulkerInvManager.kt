package ru.turbovadim.database

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import ru.turbovadim.database.schema.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException


object ShulkerInventoryManager {

    /**
     * Сохраняет предмет в инвентаре шалкера
     * @param uuid UUID владельца инвентаря
     * @param slot номер слота (0-8)
     * @param itemStack сериализованные данные предмета
     */
    suspend fun saveItem(uuid: String, slot: Int, itemStack: ItemStack) {
        if (slot !in 0..8) throw IllegalArgumentException("Слот должен быть от 0 до 8")

        return dbQuery {
            // Получаем или создаем запись UUID
            val uuidEntity = UUIDOriginEntity.find { UUIDOrigins.uuid eq uuid }.firstOrNull()
                ?: UUIDOriginEntity.new { this.uuid = uuid }

            // Проверяем, существует ли уже предмет в этом слоте
            val existingItem = ShulkerItemEntity.find {
                (ShulkerInventory.parent eq uuidEntity.id) and (ShulkerInventory.slot eq slot)
            }.firstOrNull()

            if (existingItem != null) {
                // Обновляем существующий предмет
                existingItem.itemStack = itemStackToBase64(itemStack)
            } else {
                // Создаем новый предмет
                ShulkerItemEntity.new {
                    this.parent = uuidEntity
                    this.slot = slot
                    this.itemStack = itemStackToBase64(itemStack)
                }
            }
        }
    }

    /**
     * Получает предмет из инвентаря шалкера
     * @param uuid UUID владельца инвентаря
     * @param slot номер слота (0-8)
     * @return ShulkerItem или null, если слот пустой
     */
    suspend fun getItem(uuid: String, slot: Int): ShulkerItem? {
        if (slot !in 0..8) throw IllegalArgumentException("Слот должен быть от 0 до 8")

        return dbQuery {
            val uuidEntity = UUIDOriginEntity.find {
                UUIDOrigins.uuid eq uuid
            }.firstOrNull() ?: return@dbQuery null

            ShulkerItemEntity.find {
                (ShulkerInventory.parent eq uuidEntity.id) and (ShulkerInventory.slot eq slot)
            }.firstOrNull()?.toShulkerItem()
        }
    }

    /**
     * Получает весь инвентарь шалкера
     * @param uuid UUID владельца инвентаря
     * @return Список предметов в инвентаре
     */
    suspend fun getInventory(uuid: String): List<ShulkerItem> {
        return dbQuery {
            val uuidEntity = UUIDOriginEntity.find {
                UUIDOrigins.uuid eq uuid
            }.firstOrNull() ?: return@dbQuery emptyList()

            ShulkerItemEntity.find {
                ShulkerInventory.parent eq uuidEntity.id
            }.map { it.toShulkerItem() }
        }
    }

    /**
     * Удаляет предмет из слота инвентаря шалкера
     * @param uuid UUID владельца инвентаря
     * @param slot номер слота (0-8)
     * @return true если предмет был удален, false если слот был пуст
     */
    suspend fun removeItem(uuid: String, slot: Int): Boolean {
        if (slot !in 0..8) throw IllegalArgumentException("Слот должен быть от 0 до 8")

        return dbQuery {
            val uuidEntity = UUIDOriginEntity.find {
                UUIDOrigins.uuid eq uuid
            }.firstOrNull() ?: return@dbQuery false

            val item = ShulkerItemEntity.find {
                (ShulkerInventory.parent eq uuidEntity.id) and (ShulkerInventory.slot eq slot)
            }.firstOrNull() ?: return@dbQuery false

            item.delete()
            return@dbQuery true
        }
    }

    /**
     * Очищает весь инвентарь шалкера
     * @param uuid UUID владельца инвентаря
     */
    fun clearInventory(uuid: String) {
        transaction {
            val uuidEntity = UUIDOriginEntity.find {
                UUIDOrigins.uuid eq uuid
            }.firstOrNull() ?: return@transaction

            ShulkerItemEntity.find {
                ShulkerInventory.parent eq uuidEntity.id
            }.forEach { it.delete() }
        }
    }

    fun itemStackToBase64(item: ItemStack): String {
        try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)
            dataOutput.writeObject(item)
            dataOutput.close()
            return Base64Coder.encodeLines(outputStream.toByteArray())
        } catch (e: Exception) {
            throw IllegalStateException("Не удалось сохранить ItemStack.", e)
        }
    }

    fun itemStackFromBase64(data: String): ItemStack {
        try {
            val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
            val dataInput = BukkitObjectInputStream(inputStream)
            val item = dataInput.readObject() as ItemStack
            dataInput.close()
            return item
        } catch (e: Exception) {
            throw IOException("Не удалось загрузить ItemStack.", e)
        }
    }

}