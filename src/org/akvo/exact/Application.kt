package org.akvo.exact

import com.typesafe.config.ConfigFactory
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.config.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.sentry.Sentry
import io.sentry.Sentry.init
import io.sentry.SentryOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.p
import kotlinx.html.title
import org.akvo.exact.repository.auth.AuthRepository
import org.akvo.exact.repository.auth.AuthRepositoryImpl
import org.akvo.exact.repository.exact.ExactRepository
import org.akvo.exact.repository.exact.ExactRepositoryImpl
import org.akvo.exact.repository.sheets.GOOGLE_SHEET_ID
import org.akvo.exact.repository.sheets.GoogleSheetRepository
import org.akvo.exact.repository.sheets.SheetRepository
import java.util.concurrent.TimeUnit

private const val SERVER_NAME = "IdentityServer4"

@KtorExperimentalAPI
val secrets = HoconApplicationConfig(ConfigFactory.load("secret.conf"))

@KtorExperimentalAPI
val redirectUrl = secrets.property("ktor.secret.redirectUrl").getString()

@KtorExperimentalAPI
val clientSettings = OAuthServerSettings.OAuth2ServerSettings(
    name = SERVER_NAME,
    authorizeUrl = "https://start.exactonline.nl/api/oauth2/auth", // OAuth authorization endpoint
    accessTokenUrl = "https://start.exactonline.nl/api/oauth2/token", // OAuth token endpoint
    clientId = secrets.property("ktor.secret.clientId").getString(),
    clientSecret = secrets.property("ktor.secret.clientSecret").getString(),
    accessTokenRequiresBasicAuth = false, // basic auth implementation is not "OAuth style" so falling back to post body
    requestMethod = HttpMethod.Post // must POST to token endpoint
)

private val sheetRepository: SheetRepository = GoogleSheetRepository()
private val exactRepository: ExactRepository = ExactRepositoryImpl()
private val authRepository: AuthRepository = AuthRepositoryImpl()

@Suppress("unused")
@KtorExperimentalAPI
fun main(args: Array<String>) {
    init { options: SentryOptions ->
        options.dsn = secrets.property("ktor.secret.sentryDsn").getString()
    }
    EngineMain.main(args)
}

@KtorExperimentalAPI
@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    launch {
        while(true) {
            delay(TimeUnit.HOURS.toMillis(1)) //agreed to repeat every hour
            runRefreshTask()
        }
    }
    embeddedServer(Netty, 8080) {

        install(Authentication) {
            oauth(SERVER_NAME) {
                client = HttpClient(Apache)
                providerLookup = { clientSettings }
                urlProvider = { redirectUrl }
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
                                +"All previous data on that sheet will replaced!"
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
                    saveTokens(principal)
                    val (insertedSales, insertedReceivables) = refreshExactData(principal?.accessToken)
                    if (insertedSales.isBlank() || insertedReceivables.isBlank()) {
                        call.respondText(
                            """<b>Error inserting pending and/or outstanding Invoices</b>""",
                            ContentType.Text.Html
                        )
                    } else {
                        call.respondText(
                            """Data successfully inserted, click <a href="https://docs.google.com/spreadsheets/d/$insertedSales/edit?usp=sharing">here</a> to open""",
                            ContentType.Text.Html
                        )
                    }
                }
            }
            get("/refresh") {
                runRefreshTask()
            }
        }
    }.start(wait = true)
}

private suspend fun runRefreshTask() {
    val refreshToken = authRepository.loadSavedRefreshToken()
    if (refreshToken.isNotEmpty()) {
        try {
            val refreshTokenResponse = exactRepository.refreshToken(refreshToken, clientSettings)
            val accessToken = refreshTokenResponse.accessToken
            authRepository.saveUser(accessToken, refreshTokenResponse.refreshToken)
            refreshExactData(accessToken)
        } catch (e: Throwable) {
            /**
             * after reading exact exceptions it seems we should be aware of them all
             * see https://support.exactonline.com/community/s/knowledge-base#All-All-DNO-Content-respcodeserrorhandling
             * Since polling will be frequent, no need to retry
             */
            Sentry.captureException(e)
        }
    } else {
        Sentry.captureException(Exception("Refresh token not found"))
    }
}

private suspend fun refreshExactData(accessToken: String?): Pair<String, String> {
    val invoicesResult = exactRepository.getInvoicesFromExact(accessToken)
    val insertedSales = sheetRepository.insertSalesInvoices(invoicesResult.first)
    val insertedReceivables = sheetRepository.insertReceivablesInvoices(invoicesResult.second)
    return Pair(insertedSales, insertedReceivables)
}

private suspend fun saveTokens(principal: OAuthAccessTokenResponse.OAuth2?) {
    principal?.let {
        principal.refreshToken?.let { refreshToken ->
            authRepository.saveUser(token=principal.accessToken, refreshToken=refreshToken)
        }
    }
}
