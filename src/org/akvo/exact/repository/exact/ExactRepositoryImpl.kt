package org.akvo.exact.repository.exact

import io.ktor.auth.*
import org.akvo.exact.repository.exact.api.ReceivableInvoice
import org.akvo.exact.repository.exact.api.RefreshTokenResponse
import org.akvo.exact.repository.exact.api.SalesInvoice

class ExactRepositoryImpl : ExactRepository {
    private val exactApiDataSource = ExactApiDataSource()

    override suspend fun getInvoicesFromExact(accessToken: String?): Pair<List<SalesInvoice>, List<ReceivableInvoice>> {
        val division = exactApiDataSource.getUserDivision(accessToken)
        val salesInvoices = exactApiDataSource.getAllSalesInvoices(division, accessToken)
        val receivableInvoices = exactApiDataSource.getAllReceivableInvoices(division, accessToken)
        return Pair(salesInvoices, receivableInvoices)
    }

    override suspend fun refreshToken(
        refreshToken: String,
        oauthSettings: OAuthServerSettings.OAuth2ServerSettings
    ): RefreshTokenResponse {
        return exactApiDataSource.refreshToken(refreshToken, oauthSettings.clientId, oauthSettings.clientSecret)
    }
}
