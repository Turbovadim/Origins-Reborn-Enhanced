package com.starshootercity.database

import com.starshootercity.database.schema.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and

/**
 * Singleton object responsible for managing database-related operations. Provides methods
 * to query and update data in relation to origins and layers associated with UUIDs.
 */
object DatabaseManager {

    /**
     * Retrieves the selected origins associated with the specified UUID.
     *
     * @param uuid The unique identifier for which the selected origins are fetched.
     * @return The `UUIDOrigin` object containing the selected origins, or `null` if no matching data is found.
     */
    suspend fun getSelectedOrigins(uuid: String) = dbQuery {
        // Находим запись в таблице UUIDOrigins по uuid
        val uuidEntity = UUIDOriginEntity.find { UUIDOrigins.uuid eq uuid }.firstOrNull()
        uuidEntity?.toUUIDOrigin()
    }

    /**
     * Retrieves the origin associated with the specified UUID and layer.
     *
     * @param uuid The unique identifier for which the origin is fetched.
     * @param layer The specific layer within the UUID to fetch the associated origin.
     * @return The origin associated with the given UUID and layer, or null if no matching data is found.
     */
    suspend fun getSelectedOrigin(uuid: String, layer: String) = dbQuery {
        // Находим запись в таблице UUIDOrigins по uuid
        val uuidEntity = UUIDOriginEntity.find { UUIDOrigins.uuid eq uuid }.firstOrNull()
        uuidEntity?.toUUIDOrigin()?.layerOriginPairs[layer]
    }

    /**
     * Retrieves a list of previously used origins associated with the specified UUID.
     *
     * @param uuid The unique identifier for which the used origins are to be fetched.
     * @return A list of used origins as strings, sorted in ascending order by their IDs. Returns an empty list if the UUID is not found or has no used origins.
     */
    suspend fun getUsedOrigins(uuid: String): List<String> = dbQuery {
        // Находим запись в таблице UUIDOrigins по uuid
        val uuidEntity = UUIDOriginEntity.find { UUIDOrigins.uuid eq uuid }.firstOrNull()
        uuidEntity?.let {
            UsedOriginEntity.find { UsedOrigins.parent eq it.id }
                .orderBy(UsedOrigins.id to SortOrder.ASC)
                .map { it.usedOrigin }
        } ?: emptyList()
    }

    /**
     * Retrieves the origin associated with a specific layer for a given UUID from the database.
     *
     * @param uuid The unique identifier for which the origin is being retrieved.
     * @param layer The layer for which the associated origin is being retrieved.
     * @return The origin associated with the given UUID and layer, or null if no such origin exists.
     */
    suspend fun getOriginForLayer(uuid: String, layer: String): String? = dbQuery {
        val uuidEntity = UUIDOriginEntity.find { UUIDOrigins.uuid eq uuid }.firstOrNull()
        uuidEntity?.let {
            OriginKeyValuePairEntity.find {
                (OriginKeyValuePairs.parent eq it.id) and (OriginKeyValuePairs.layer eq layer)
            }.firstOrNull()?.origin
        }
    }

    /**
     *
     */
    suspend fun updateOrigin(uuid: String, layer: String, newOrigin: String) = dbQuery {
        // Получаем или создаём запись в таблице UUIDOrigins для данного uuid
        val uuidEntity = UUIDOriginEntity.find { UUIDOrigins.uuid eq uuid }.firstOrNull() ?: UUIDOriginEntity.new {
            this.uuid = uuid
        }

        // Получаем или создаём пару ключ-значение для указанного слоя
        val originPair = OriginKeyValuePairEntity.find {
            (OriginKeyValuePairs.parent eq uuidEntity.id) and (OriginKeyValuePairs.layer eq layer)
        }.firstOrNull()

        if (originPair != null) {
            originPair.origin = newOrigin
        } else {
            OriginKeyValuePairEntity.new {
                this.parent = uuidEntity
                this.layer = layer
                this.origin = newOrigin
            }
        }

        // Добавляем новую запись в историю использованных происхождений
        UsedOriginEntity.new {
            this.parent = uuidEntity
            this.usedOrigin = newOrigin
        }.toUsedOrigin()
    }
}