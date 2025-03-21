package com.starshootercity.database

import com.starshootercity.database.schema.SelectedOrigins
import com.starshootercity.database.schema.UsedOrigins
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

lateinit var db: Database

fun initDb(dataFolder: File) {

    println("Initializing database with storage type: H2")
    initH2(dataFolder)

    transaction(db) {
        println("Creating missing tables and columns if any...")
        SchemaUtils.createMissingTablesAndColumns(SelectedOrigins, UsedOrigins)
    }
}

private fun initH2(dataFolder: File) {
    println("Connecting to H2 database...")
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:h2:${dataFolder.absolutePath}/h2.db;DB_CLOSE_DELAY=-1"
        driverClassName = "org.h2.Driver"
        maximumPoolSize = 10
    }
    val dataSource = HikariDataSource(hikariConfig)
    db = Database.connect(dataSource)
    println("Initialized H2")
}