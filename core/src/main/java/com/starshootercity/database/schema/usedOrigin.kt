package com.starshootercity.database.schema

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

data class UsedOrigin(
    val id: Int,
    val uuid: String,
    val usedOrigins: List<String>,
)

class UsedOriginEntity(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<UsedOriginEntity>(UsedOrigins)

    var uuid by UsedOrigins.uuid
    var usedOrigins by UsedOrigins.usedOrigin

    fun toUsedOrigin() = UsedOrigin(
        id = id.value,
        uuid = uuid,
        usedOrigins = usedOrigins.split(","),
    )
}

object UsedOrigins : IntIdTable() {
    val uuid = varchar("uuid", 255)
    val usedOrigin = text("usedOrigins")
}