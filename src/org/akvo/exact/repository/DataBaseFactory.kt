package org.akvo.exact.repository

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

@KtorExperimentalAPI
object DataBaseFactory {

    private val secrets = HoconApplicationConfig(ConfigFactory.load("secret.conf"))
    private val databaseName = secrets.property("ktor.database.name").getString()
    private val databaseUser = secrets.property("ktor.database.user").getString()
    private val databaseUsername = secrets.property("ktor.database.user").getString()
    private val databaseUserPassword = secrets.property("ktor.database.password").getString()

    fun init() {
        Database.connect(hikari())

        transaction {
            SchemaUtils.create(Users)
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = "jdbc:postgresql:$databaseName?user=$databaseUser"
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        if (databaseUsername != null && databaseUsername.isNotEmpty()) {
            config.username = databaseUsername
        }
        if (databaseUserPassword != null) {
            config.password = databaseUserPassword
        }
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }
}
