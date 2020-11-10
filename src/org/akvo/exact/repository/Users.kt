package org.akvo.exact.repository

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val userId: Column<Int> = integer("id").autoIncrement().primaryKey()
    val token = text("token")
    val refreshToken = text("refresh_token")
}
