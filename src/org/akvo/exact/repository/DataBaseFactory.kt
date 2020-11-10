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
    private val password = secrets.property("ktor.database.password").getString()

    fun init() {
        Database.connect(hikari())

        transaction {
            SchemaUtils.create(Users)
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://none/exact2sheets?user=exact2sheetsuser&password=$password&ssl=false&cloudSqlInstance=akvo-lumen:europe-west1:shared-test-database&socketFactory=com.google.cloud.sql.postgres.SocketFactory"
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }
}
