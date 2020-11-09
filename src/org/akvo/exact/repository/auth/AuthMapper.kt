package org.akvo.exact.repository.auth

import org.akvo.exact.repository.Users
import org.jetbrains.exposed.sql.ResultRow

class AuthMapper {

    fun rowToRefreshToken(row: ResultRow): String {
        if (row == null) {
            return ""
        }
        return row[Users.refreshToken]
    }

    fun rowToToken(row: ResultRow?): String {
        if (row == null) {
            return ""
        }
        return row[Users.token]
    }
}
