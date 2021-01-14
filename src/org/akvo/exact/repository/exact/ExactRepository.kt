package org.akvo.exact.repository.exact

import io.ktor.auth.*
import org.akvo.exact.repository.exact.api.ReceivableInvoice
import org.akvo.exact.repository.exact.api.ReceivableInvoicesResult
import org.akvo.exact.repository.exact.api.RefreshTokenResponse
import org.akvo.exact.repository.exact.api.SalesInvoice
import org.akvo.exact.repository.exact.api.SalesInvoicesResult

interface ExactRepository {

    suspend fun getInvoicesFromExact(accessToken: String?): Pair<List<SalesInvoice>, List<ReceivableInvoice>>
    suspend fun refreshToken(refreshToken: String, oauthSettings: OAuthServerSettings.OAuth2ServerSettings): RefreshTokenResponse
}
