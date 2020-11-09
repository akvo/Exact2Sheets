package org.akvo.exact.repository

import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.*

class AuthRepositoryImpl: AuthRepository {
    init {
        DataBaseFactory.init()
    }

    override suspend fun saveUser(token: String, refreshToken: String): String {
        var statement : InsertStatement<Number>? = null
        DataBaseFactory.dbQuery {
            Users.deleteAll()
        }
        DataBaseFactory.dbQuery {
            statement = Users.insert { user ->
                user[Users.token] = token
                user[Users.refreshToken] = refreshToken
            }
        }
        return rowToToken(statement?.resultedValues?.get(0))
    }

    override suspend fun loadSavedRefreshToken(): String {
        val tokens: List<String> = DataBaseFactory.dbQuery {
            Users.selectAll().limit(1).map { rowToRefreshToken(it) }
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

    //TODO: move to a mapper class
    private fun rowToRefreshToken(row: ResultRow): String {
        if (row == null) {
            return ""
        }
        return row[Users.refreshToken]
    }

    //TODO: move to a mapper class
    private fun rowToToken(row: ResultRow?): String {
        if (row == null) {
            return ""
        }
        return row[Users.token]
    }
}
