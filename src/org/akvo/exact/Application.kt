package org.akvo.exact

import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
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
import kotlinx.html.*

private const val SERVER_NAME = "IdentityServer4"
private const val EXACT_HOST = "start.exactonline.nl"

@KtorExperimentalAPI
val config = HoconApplicationConfig(ConfigFactory.load("secret.conf"))

@KtorExperimentalAPI
val redirectUrl = config.property("ktor.secret.redirectUrl").getString()

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

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    embeddedServer(Netty, 8080) {

        install(Authentication) {
            oauth(SERVER_NAME) {
                client = HttpClient(Apache)
                providerLookup = { clientSettings }
                urlProvider = { redirectUrl } //redirect_url
            }
        }

        routing {
            get("/") {
                call.respondHtml {
                    head {
                        title { +"Exact Invoices" }
                    }
                    body {
                        p {
                            +"Once you login, this tool will import pending Exact invoices to "
                            a {
                                href =
                                    "https://docs.google.com/spreadsheets/d/$GOOGLE_SHEET_ID/edit?usp=sharing"
                                +"this google sheet."
                            }
                        }
                        p {
                            b {
                                +"All previous data on that sheet will be kept!"
                            }
                        }
                        p {
                            +"Click "
                            a {
                                href = "/oauth"
                                +"here"
                            }
                            +" to begin."
                        }
                    }
                }
            }
            authenticate(SERVER_NAME) {
                get("/oauth") {
                    val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()

                    val client = HttpClient(Apache) {
                        install(JsonFeature)
                    }

                    val invoicesResult = getInvoicesFromExact(client, principal)
                    val insertedSales = SpreadSheetDataSource().insertToSheet(
                        SpreadSheetDataMapper().salesInvoicesToStrings(invoicesResult.first),
                        RANGE_SHEET1
                    )
                    val insertedReceivables = SpreadSheetDataSource().insertToSheet(
                        SpreadSheetDataMapper().receivableInvoicesToStrings(invoicesResult.second),
                        RANGE_SHEET2
                    )

                    if (insertedSales.isBlank() || insertedReceivables.isBlank()) {
                        call.respondText("""<b>Error inserting pending and/or outstanding Invoices</b>""", ContentType.Text.Html)
                    } else {
                        call.respondText(
                            """Data successfully inserted, click <a href="https://docs.google.com/spreadsheets/d/$insertedSales/edit?usp=sharing">here</a> to open""",
                            ContentType.Text.Html
                        )
                    }
                }
            }
        }
    }.start(wait = true)
}

private suspend fun getInvoicesFromExact(
    client: HttpClient,
    principal: OAuthAccessTokenResponse.OAuth2?
): Pair<SalesInvoicesResult, ReceivableInvoicesResult> {
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
    val division = divisionResult.d.results[0].currentDivision
    print("Your division is $division\n")
    val salesInvoices = client.get<SalesInvoicesResult> {
        buildRequest(principal, "/api/v1/$division/salesinvoice/SalesInvoices?\$filter=Status+lt+50&\$select=AmountDC,Currency,Description,InvoiceToContactPersonFullName,InvoiceToName,OrderDate")
    }

    val receivableInvoices = client.get<ReceivableInvoicesResult> {
        buildRequest(principal, "/api/v1/$division/read/financial/ReceivablesList?\$select=AccountName,Amount,CurrencyCode,Description,DueDate,InvoiceDate,InvoiceNumber")
    }
    return Pair(salesInvoices, receivableInvoices)
}

private fun HttpRequestBuilder.buildRequest(
    principal: OAuthAccessTokenResponse.OAuth2?,
    path: String
) {
    url {
        protocol = URLProtocol.HTTPS
        host = EXACT_HOST
        encodedPath = path
    }
    contentType(ContentType.Application.Json)
    headers {
        header("Authorization", "Bearer ${principal?.accessToken}")
    }
}

