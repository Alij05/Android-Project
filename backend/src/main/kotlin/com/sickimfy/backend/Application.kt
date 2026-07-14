package com.sickimfy.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.io.File
import java.util.Date
import kotlin.time.Duration.Companion.seconds

data class ServerSettings(
    val port: Int,
    val databasePath: String,
    val mediaDirectory: String,
    val jwtSecret: String,
    val jwtIssuer: String,
    val jwtAudience: String,
    val adminKey: String
) {
    companion object {
        fun fromEnvironment() = ServerSettings(
            port = env("PORT", "8080").toInt(),
            databasePath = env("DATABASE_PATH", "data/sickimfy.db"),
            mediaDirectory = env("MEDIA_DIRECTORY", "media"),
            jwtSecret = env("JWT_SECRET", "development-secret-change-before-deployment"),
            jwtIssuer = env("JWT_ISSUER", "sickimfy-backend"),
            jwtAudience = env("JWT_AUDIENCE", "sickimfy-mobile"),
            adminKey = env("ADMIN_KEY", "development-admin-key-change-before-deployment")
        )

        private fun env(name: String, fallback: String) =
            System.getenv(name) ?: System.getProperty(name) ?: fallback
    }
}

class TokenService(private val settings: ServerSettings) {
    private val algorithm = Algorithm.HMAC256(settings.jwtSecret)
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(settings.jwtIssuer)
        .withAudience(settings.jwtAudience)
        .build()

    fun create(userId: Int): String = JWT.create()
        .withIssuer(settings.jwtIssuer)
        .withAudience(settings.jwtAudience)
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + TOKEN_VALIDITY_MILLIS))
        .sign(algorithm)

    fun userIdFrom(token: String): Int? = runCatching {
        verifier.verify(token).getClaim("userId").asInt()
    }.getOrNull()

    private companion object {
        const val TOKEN_VALIDITY_MILLIS = 7 * 24 * 60 * 60 * 1000L
    }
}

fun main() {
    val settings = ServerSettings.fromEnvironment()
    embeddedServer(Netty, port = settings.port, host = "0.0.0.0") {
        sickimfyModule(settings)
    }.start(wait = true)
}

fun Application.sickimfyModule(settings: ServerSettings = ServerSettings.fromEnvironment()) {
    val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    val database = AppDatabase(settings.databasePath).also { it.initialize() }
    val tokenService = TokenService(settings)
    val chatHub = ChatHub(json)
    File(settings.mediaDirectory).mkdirs()

    install(CallLogging) { level = Level.INFO }
    install(ContentNegotiation) { json(json) }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }
    install(StatusPages) {
        exception<ApiException> { call, cause -> call.respond(cause.status, ErrorResponse(cause.message ?: "Request failed")) }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled request failure", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected server error"))
        }
    }
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Sickimfy API"
            verifier(tokenService.verifier)
            validate { credential ->
                credential.payload.getClaim("userId").asInt()?.let { JWTPrincipal(credential.payload) }
            }
        }
    }
    install(WebSockets) {
        pingPeriod = 20.seconds
        timeout = 30.seconds
        maxFrameSize = 64 * 1024
        masking = false
    }

    routing {
        apiRoutes(database, tokenService, settings, chatHub)
        chatSocket(database, tokenService, chatHub)
        staticFiles("/media", File(settings.mediaDirectory))
    }
}
