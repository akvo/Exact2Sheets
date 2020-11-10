package org.akvo.exact.repository.exact

import io.ktor.auth.*
import org.akvo.exact.repository.exact.api.ReceivableInvoicesResult
import org.akvo.exact.repository.exact.api.RefreshTokenResponse
import org.akvo.exact.repository.exact.api.SalesInvoicesResult

class ExactRepositoryImpl : ExactRepository {
    private val exactApiDataSource = ExactApiDataSource()

    override suspend fun getInvoicesFromExact(accessToken: String?): Pair<SalesInvoicesResult, ReceivableInvoicesResult> {
        val division = exactApiDataSource.getUserDivision(accessToken)
        val salesInvoices = exactApiDataSource.getSalesInvoices(division, accessToken)
        val receivableInvoices = exactApiDataSource.getReceivableInvoices(division, accessToken)
        return Pair(salesInvoices, receivableInvoices)
    }

    override suspend fun refreshToken(
        refreshToken: String,
        oauthSettings: OAuthServerSettings.OAuth2ServerSettings
    ): RefreshTokenResponse {
        return exactApiDataSource.refreshToken(refreshToken, oauthSettings.clientId, oauthSettings.clientSecret)
    }
}
