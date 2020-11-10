package org.akvo.exact.repository.auth

interface AuthRepository {

    suspend fun saveUser(token: String, refreshToken: String): String
    suspend fun loadSavedRefreshToken(): String
}
