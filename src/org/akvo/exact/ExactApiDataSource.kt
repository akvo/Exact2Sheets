package org.akvo.exact

import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging

private const val EXACT_HOST = "start.exactonline.nl"

class ExactApiDataSource {

    private val client = HttpClient(Apache) {
        install(JsonFeature)
    }

    suspend fun getInvoicesFromExact(
        accessToken: String?
    ): Pair<SalesInvoicesResult, ReceivableInvoicesResult> {

        val division = getUserDivision(client, accessToken)
        val salesInvoices = getSalesInvoices(client, division, accessToken)
        val receivableInvoices = getReceivableInvoices(client, division, accessToken)
        return Pair(salesInvoices, receivableInvoices)
    }

    suspend fun refreshToken(
        refreshToken: String,
        oauthSettings: OAuthServerSettings.OAuth2ServerSettings
    ): RefreshTokenResponse {
        try {
            return client.post {
                url {
                    protocol = URLProtocol.HTTPS
                    host = EXACT_HOST
                    encodedPath = "/api/oauth2/token"
                }
                body = FormDataContent(Parameters.build {
                    append("refresh_token", refreshToken)
                    append("grant_type", "refresh_token")
                    append("client_id", oauthSettings.clientId)
                    append("client_secret", oauthSettings.clientSecret)
                })
            }
        } catch (e: Exception) {
            println(e)
            throw e
        }
    }

    private suspend fun getReceivableInvoices(
        client: HttpClient,
        division: Int,
        accessToken: String?
    ): ReceivableInvoicesResult {
        return client.get {
            val basePath = "/api/v1/$division/read/financial/ReceivablesList"
            val select = "AccountName,Amount,CurrencyCode,Description,DueDate,InvoiceDate,InvoiceNumber"
            buildRequest(
                "$basePath?\$select=$select",
                accessToken
            )
        }
    }

    private suspend fun getSalesInvoices(client: HttpClient, division: Int, accessToken: String?): SalesInvoicesResult {
        return client.get {
            val basePath = "/api/v1/$division/salesinvoice/SalesInvoices"
            val filter = "Status+lt+50"
            val select = "AmountDC,Currency,Description,InvoiceToContactPersonFullName,InvoiceToName,OrderDate"
            buildRequest(
                "$basePath?\$filter=$filter&\$select=$select",
                accessToken
            )
        }
    }

    private suspend fun getUserDivision(client: HttpClient, accessToken: String?): Int {
        val divisionResult = client.get<DivisionResult> {
            url {
                protocol = URLProtocol.HTTPS
                host = EXACT_HOST
                encodedPath = "/api/v1/current/Me?\$select=CurrentDivision"
            }
            contentType(ContentType.Application.Json)
            headers {
                header("Authorization", "Bearer $accessToken")
            }
        }
        val division = divisionResult.d.results[0].currentDivision
        print("Your division is $division\n")
        return division
    }

    private fun HttpRequestBuilder.buildRequest(path: String, accessToken: String?) {
        url {
            protocol = URLProtocol.HTTPS
            host = EXACT_HOST
            encodedPath = path
        }
        contentType(ContentType.Application.Json)
        headers {
            header("Authorization", "Bearer $accessToken")
        }
    }
}
