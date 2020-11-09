package org.akvo.exact.repository.auth

import org.akvo.exact.repository.DataBaseFactory
import org.akvo.exact.repository.Users
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.*

class AuthRepositoryImpl : AuthRepository {
    init {
        DataBaseFactory.init()
    }

    private val authMapper = AuthMapper()

    override suspend fun saveUser(token: String, refreshToken: String): String {
        var statement: InsertStatement<Number>? = null
        DataBaseFactory.dbQuery {
            Users.deleteAll()
        }
        DataBaseFactory.dbQuery {
            statement = Users.insert { user ->
                user[Users.token] = token
                user[Users.refreshToken] = refreshToken
            }
        }
        return authMapper.rowToToken(statement?.resultedValues?.get(0))
    }

    override suspend fun loadSavedRefreshToken(): String {
        val tokens: List<String> = DataBaseFactory.dbQuery {
            Users.selectAll().limit(1).map { authMapper.rowToRefreshToken(it) }
        }
        return when {
            tokens.isEmpty() -> {
                ""
            }
            else -> {
                tokens[0]
            }
        }
    }
}
