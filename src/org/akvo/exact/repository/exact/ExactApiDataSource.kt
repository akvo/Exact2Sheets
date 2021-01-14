package org.akvo.exact.repository.exact

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.akvo.exact.repository.exact.api.DivisionResult
import org.akvo.exact.repository.exact.api.ReceivableInvoice
import org.akvo.exact.repository.exact.api.ReceivableInvoicesResult
import org.akvo.exact.repository.exact.api.RefreshTokenResponse
import org.akvo.exact.repository.exact.api.SalesInvoice
import org.akvo.exact.repository.exact.api.SalesInvoicesResult

private const val EXACT_HOST = "start.exactonline.nl"

class ExactApiDataSource {

    private val client = HttpClient(Apache) {
        install(JsonFeature)
    }

    suspend fun refreshToken(refreshToken: String, clientId: String, clientSecret: String): RefreshTokenResponse {
        return client.post {
            url {
                protocol = URLProtocol.HTTPS
                host = EXACT_HOST
                encodedPath = "/api/oauth2/token"
            }
            body = FormDataContent(Parameters.build {
                append("refresh_token", refreshToken)
                append("grant_type", "refresh_token")
                append("client_id", clientId)
                append("client_secret", clientSecret)
            })
        }
    }

    suspend fun getAllSalesInvoices(division: Int, accessToken: String?): MutableList<SalesInvoice> {
        val salesInvoices = mutableListOf<SalesInvoice>()
        var salesInvoicesResult: SalesInvoicesResult = getSalesInvoices(division, accessToken)
        salesInvoices.addAll(salesInvoicesResult.d.results)
        var nextUrl = salesInvoicesResult.d.nextUrl
        while (nextUrl != null && "" != nextUrl) {
            val skip = nextUrl.substring(nextUrl.lastIndexOf("&"))
            salesInvoicesResult = getSalesInvoices(division, accessToken, skip)
            salesInvoices.addAll(salesInvoicesResult.d.results)
            nextUrl = salesInvoicesResult.d.nextUrl
        }
        return salesInvoices
    }

    suspend fun getAllReceivableInvoices(division: Int, accessToken: String?): MutableList<ReceivableInvoice> {
        val receivableInvoices = mutableListOf<ReceivableInvoice>()
        var receivableInvoicesResult: ReceivableInvoicesResult = getReceivableInvoices(division, accessToken)
        receivableInvoices.addAll(receivableInvoicesResult.d.results)
        var nextUrl = receivableInvoicesResult.d.nextUrl
        while (nextUrl != null && "" != nextUrl) {
            val skip = nextUrl.substring(nextUrl.lastIndexOf("&"))
            receivableInvoicesResult = getReceivableInvoices(division, accessToken, skip)
            receivableInvoices.addAll(receivableInvoicesResult.d.results)
            nextUrl = receivableInvoicesResult.d.nextUrl
        }
        return receivableInvoices
    }

    private suspend fun getReceivableInvoices(
        division: Int,
        accessToken: String?,
        skip: String = ""
    ): ReceivableInvoicesResult {
        return client.get {
            val basePath = "/api/v1/$division/read/financial/ReceivablesList"
            val select = "AccountName,Amount,CurrencyCode,Description,DueDate,InvoiceDate,InvoiceNumber"
            buildRequest(
                "$basePath?\$select=$select$skip",
                accessToken
            )
        }
    }

    private suspend fun getSalesInvoices(division: Int, accessToken: String?, skip: String = ""): SalesInvoicesResult {
        return client.get {
            val basePath = "/api/v1/$division/salesinvoice/SalesInvoices"
            val filter = "Status+lt+50"
            val select = "AmountDC,Currency,Description,InvoiceToContactPersonFullName,InvoiceToName,OrderDate"
            buildRequest(
                "$basePath?\$filter=$filter&\$select=$select$skip",
                accessToken
            )
        }
    }

    suspend fun getUserDivision(accessToken: String?): Int {
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
