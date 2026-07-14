package com.sickimfy.backend

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiIntegrationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `registration authentication and track management work together`() = testApplication {
        val directory = createTempDirectory("sickimfy-backend-test")
        application {
            sickimfyModule(
                ServerSettings(
                    port = 0,
                    databasePath = directory.resolve("test.db").toString(),
                    mediaDirectory = directory.resolve("media").toString(),
                    jwtSecret = "test-secret-that-is-long-enough",
                    jwtIssuer = "test-issuer",
                    jwtAudience = "test-audience",
                    adminKey = "test-admin-key"
                )
            )
        }

        assertEquals(HttpStatusCode.OK, client.get("/api/health").status)

        val registration = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"team@example.com","password":"password123","displayName":"Team User"}""")
        }
        assertEquals(HttpStatusCode.Created, registration.status)
        val auth = json.decodeFromString<AuthResponse>(registration.bodyAsText())

        val trackResponse = client.post("/api/admin/tracks") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${auth.token}")
            header("X-Admin-Key", "test-admin-key")
            setBody("""{"title":"Demo Track","artistName":"Demo Artist","isFeatured":true}""")
        }
        assertEquals(HttpStatusCode.Created, trackResponse.status)

        val tracks = client.get("/api/tracks?query=demo")
        assertEquals(HttpStatusCode.OK, tracks.status)
        assertTrue(tracks.bodyAsText().contains("Demo Track"))
    }
}
