package com.starshootercity.database.schema

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

data class SelectedOrigin(
    val id: Int,
    val uuid: String,
    val selectedOrigin: String,
)

class SelectedOriginEntity(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<SelectedOriginEntity>(SelectedOrigins)

    var uuid by SelectedOrigins.uuid
    var selectedOrigin by SelectedOrigins.selectedOrigin

    fun toSelectedOrigin() = SelectedOrigin(
        id = id.value,
        uuid = uuid,
        selectedOrigin = selectedOrigin,
    )
}

object SelectedOrigins : IntIdTable() {
    val uuid = varchar("uuid", 255)
    val selectedOrigin = varchar("selectedOrigin", 512)
}