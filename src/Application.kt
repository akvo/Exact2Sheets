package org.akvo.exact

import com.google.gson.internal.GsonBuildConfig
import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.put
import io.ktor.config.HoconApplicationConfig
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import kotlinx.html.*
import java.text.SimpleDateFormat
import java.util.*


private const val SERVER_NAME = "IdentityServer4"
private const val EXACT_REDIRECT_URL = "http://localhost:8080/oauth"
private const val EXACT_HOST = "start.exactonline.nl"
private const val GOOGLE_SHEET_ID = "17d68qe4vxUlVHS-v8BVQFXKuhlvfR7PZ0D-DY7x5PLA"

@KtorExperimentalAPI
val config = HoconApplicationConfig(ConfigFactory.load("secret.conf"))

@KtorExperimentalAPI
val clientSettings = OAuthServerSettings.OAuth2ServerSettings(
    name = SERVER_NAME,
    authorizeUrl = "https://start.exactonline.nl/api/oauth2/auth", // OAuth authorization endpoint
    accessTokenUrl = "https://start.exactonline.nl/api/oauth2/token", // OAuth token endpoint
    clientId = config.property("ktor.secret.clientId").getString(),
    clientSecret = config.property("ktor.secret.clientSecret").getString(),
    accessTokenRequiresBasicAuth = false, // basic auth implementation is not "OAuth style" so falling back to post body
    requestMethod = HttpMethod.Post // must POST to token endpoint
)

@KtorExperimentalAPI
val googleToken = config.property("ktor.secret.googleToken").getString()

@KtorExperimentalAPI
fun main(args: Array<String>) {

    embeddedServer(Netty, 8080) {

        install(Authentication) {
            oauth("IdentityServer4") {
                client = HttpClient(Apache)
                providerLookup = { clientSettings }
                urlProvider = { EXACT_REDIRECT_URL } //redirect_url
            }
        }

        routing {
            get("/") {
                call.respondText(
                    """Click <a href="/oauth">here</a> to import Exact invoices to google sheets""",
                    ContentType.Text.Html
                )
            }
            authenticate(SERVER_NAME) {
                get("/oauth") {
                    val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()

                    val client = HttpClient(Apache) {
                        install(JsonFeature)
                    }

                    val invoicesResult = getInvoicesFromExact(client, principal)
                    val insertionResult = writeDataToSheet(invoicesResult, client)
                    print(insertionResult)

                    call.respondText(
                        """Data successfully inserted, click <a href="https://docs.google.com/spreadsheets/d/$GOOGLE_SHEET_ID/edit?usp=sharing">here</a> to open""",
                        ContentType.Text.Html
                    )
                }
            }
        }
    }.start(wait = true)
}

private suspend fun getInvoicesFromExact(
    client: HttpClient,
    principal: OAuthAccessTokenResponse.OAuth2?
): InvoicesResult {
    val divisionResult = client.get<DivisionResult> {
        url {
            protocol = URLProtocol.HTTPS
            host = EXACT_HOST
            encodedPath = "/api/v1/current/Me?\$select=CurrentDivision"
        }
        contentType(ContentType.Application.Json)
        headers {
            header("Authorization", "Bearer ${principal?.accessToken}")
        }
    }
    //get first division????
    //make them select the division?
    val division = divisionResult.d.results[0].currentDivision
    print("Your division is $division\n")
    val result = client.get<InvoicesResult> {

        url {
            protocol = URLProtocol.HTTPS
            host = EXACT_HOST
            encodedPath =
                "/api/v1/$division/salesinvoice/SalesInvoices?\$filter=Status+lt+50&\$select=OrderNumber,InvoiceToName,AmountDC,OrderDate,Currency"
        }
        contentType(ContentType.Application.Json)
        headers {
            header("Authorization", "Bearer ${principal?.accessToken}")
        }
    }
    return result
}

@KtorExperimentalAPI
private suspend fun writeDataToSheet(result: InvoicesResult, client: HttpClient): String {
    val invoices = result.d.results
    val pattern = "dd-MM-yyyy"
    val simpleDateFormat = SimpleDateFormat(pattern)
    val values = mutableListOf<List<String>>()
    values.add(listOf("OrderNumber", "InvoiceToName", "Amount", "OrderDate"))
    for (invoice in invoices) {
        val amount = invoice.amountDC.toString() + invoice.currency
        val replace = invoice.orderDate.replace("/Date(", "").replace(")/", "")
        val date = Date(replace.toLong())
        val formattedDate = simpleDateFormat.format(date)
        values.add(listOf(invoice.orderNumber.toString(), invoice.invoiceToName, amount, formattedDate))
    }
    val structure = SheetStructure("Sheet1!A1:D5", "ROWS", values)
    return client.put {
        url {
            protocol = URLProtocol.HTTPS
            host = "sheets.googleapis.com"
            body = structure
            encodedPath =
                "/v4/spreadsheets/$GOOGLE_SHEET_ID/values/Sheet1!A1:D5?valueInputOption=USER_ENTERED"
        }
        contentType(ContentType.Application.Json)
        header(
            "Authorization",
            "Bearer $googleToken"
        )
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.displayHtmlTable(
    result: InvoicesResult
) {
    val invoices = result.d.results
    val pattern = "dd-MM-yyyy"
    val simpleDateFormat = SimpleDateFormat(pattern)
    fun TABLE.row(invoice: Invoice) {

        val amount = invoice.amountDC.toString() + invoice.currency
        val replace = invoice.orderDate.replace("/Date(", "").replace(")/", "")
        val date = Date(replace.toLong())
        val formattedDate = simpleDateFormat.format(date)

        tr {
            td { +invoice.orderNumber.toString() }
            td { +invoice.invoiceToName }
            td { +amount }
            td { +formattedDate }
        }
    }

    call.respondHtml {
        head {
            title { +"Exact Invoices" }
        }
        body {
            table {
                for (invoice in invoices) {
                    row(invoice)
                }
            }
        }
    }
}
