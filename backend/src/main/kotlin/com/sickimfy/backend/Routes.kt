package com.sickimfy.backend

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

fun Route.apiRoutes(database: AppDatabase, tokens: TokenService, settings: ServerSettings, chatHub: ChatHub) {
    route("/api") {
        get("/health") { call.respond(HealthResponse()) }

        route("/auth") {
            post("/register") {
                val request = call.receive<AuthRequest>()
                validateCredentials(request)
                val user = io { database.register(request.email, request.password, request.displayName!!.trim()) }
                call.respond(HttpStatusCode.Created, AuthResponse(tokens.create(user.id), user))
            }
            post("/login") {
                val request = call.receive<AuthRequest>()
                if (request.email.isBlank() || request.password.isBlank()) badRequest("Email and password are required")
                val user = io { database.login(request.email, request.password) }
                call.respond(AuthResponse(tokens.create(user.id), user))
            }
        }

        get("/tracks") {
            val limit = call.queryLimit()
            val offset = call.queryOffset()
            call.respond(io { database.tracks(call.request.queryParameters["query"], call.request.queryParameters["genre"], offset, limit) })
        }
        get("/tracks/{id}") { call.respond(io { database.track(call.pathId()) }) }
        get("/home") { call.respond(io { HomeResponse(database.featuredTracks(12), database.latestTracks(12), database.playlists(publicOnly = true).take(12)) }) }
        get("/playlists/public") { call.respond(io { database.playlists(publicOnly = true) }) }

        authenticate("auth-jwt") {
            get("/profile/me") { call.respond(io { database.profile(call.userId()) }) }
            get("/users") { call.respond(io { database.searchUsers(call.request.queryParameters["query"], call.userId()) }) }
            patch("/profile/me") {
                val request = call.receive<UpdateProfileRequest>()
                call.respond(io { database.updateProfile(call.userId(), request) })
            }
            post("/profile/me/upgrade") { call.respond(io { database.upgradePremium(call.userId()) }) }

            get("/playlists") { call.respond(io { database.playlists(ownerId = call.userId()) }) }
            post("/playlists") {
                val request = call.receive<CreatePlaylistRequest>()
                val playlist = io { database.createPlaylist(call.userId(), request) }
                call.respond(HttpStatusCode.Created, playlist)
            }
            get("/playlists/{id}") { call.respond(io { database.playlistDetails(call.pathId(), call.userId()) }) }
            post("/playlists/{id}/tracks") {
                val request = call.receive<AddTrackToPlaylistRequest>()
                io { database.addTrackToPlaylist(call.userId(), call.pathId(), request.trackId) }
                call.respond(HttpStatusCode.NoContent)
            }
            delete("/playlists/{id}/tracks/{trackId}") {
                io { database.removeTrackFromPlaylist(call.userId(), call.pathId(), call.pathInt("trackId")) }
                call.respond(HttpStatusCode.NoContent)
            }

            get("/likes") { call.respond(io { database.likedTracks(call.userId()) }) }
            post("/likes/{trackId}") { io { database.likeTrack(call.userId(), call.pathInt("trackId")) }; call.respond(HttpStatusCode.NoContent) }
            delete("/likes/{trackId}") { io { database.unlikeTrack(call.userId(), call.pathInt("trackId")) }; call.respond(HttpStatusCode.NoContent) }

            get("/search-history") { call.respond(io { database.searchHistory(call.userId()) }) }
            post("/search-history") {
                val request = call.receive<CreateSearchHistoryRequest>()
                val created = io { database.addSearchHistory(call.userId(), request.query) }
                call.respond(HttpStatusCode.Created, created)
            }
            delete("/search-history/{id}") { io { database.deleteSearchHistory(call.userId(), call.pathId()) }; call.respond(HttpStatusCode.NoContent) }

            get("/follows") { call.respond(io { database.followedUsers(call.userId()) }) }
            post("/follows/{userId}") { io { database.follow(call.userId(), call.pathInt("userId")) }; call.respond(HttpStatusCode.NoContent) }
            delete("/follows/{userId}") { io { database.unfollow(call.userId(), call.pathInt("userId")) }; call.respond(HttpStatusCode.NoContent) }

            get("/conversations") { call.respond(io { database.conversations(call.userId()) }) }
            post("/conversations") {
                val request = call.receive<CreateConversationRequest>()
                val conversation = io { database.getOrCreateConversation(call.userId(), request.participantId) }
                call.respond(HttpStatusCode.Created, conversation)
            }
            get("/conversations/{id}/messages") { call.respond(io { database.messages(call.userId(), call.pathId()) }) }
            post("/conversations/{id}/messages") {
                val request = call.receive<SendMessageRequest>()
                val message = io { database.sendMessage(call.userId(), call.pathId(), request) }
                chatHub.broadcast(message.conversationId, SocketEvent(SocketEventType.MESSAGE, message = message))
                call.respond(HttpStatusCode.Created, message)
            }

            route("/admin") {
                post("/tracks") {
                    call.requireAdmin(settings)
                    val request = call.receive<UpsertTrackRequest>()
                    call.respond(HttpStatusCode.Created, io { database.createTrack(request) })
                }
                patch("/tracks/{id}") {
                    call.requireAdmin(settings)
                    val request = call.receive<UpsertTrackRequest>()
                    call.respond(io { database.updateTrack(call.pathId(), request) })
                }
                delete("/tracks/{id}") {
                    call.requireAdmin(settings)
                    io { database.deleteTrack(call.pathId()) }
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

fun Route.chatSocket(database: AppDatabase, tokens: TokenService, hub: ChatHub) {
    webSocket("/ws/conversations/{id}") {
        val conversationId = call.parameters["id"]?.toIntOrNull()
        val token = call.request.queryParameters["token"]
        val userId = token?.let(tokens::userIdFrom)
        if (conversationId == null || userId == null || !io { database.isConversationMember(userId, conversationId) }) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized conversation"))
            return@webSocket
        }
        hub.join(conversationId, this)
        try {
            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                val event = runCatching { hub.json.decodeFromString<SocketEvent>(frame.readText()) }.getOrNull() ?: continue
                if (event.type == SocketEventType.TYPING) hub.broadcast(conversationId, SocketEvent(SocketEventType.TYPING, isTyping = event.isTyping == true))
            }
        } finally {
            hub.leave(conversationId, this)
        }
    }
}

class ChatHub(val json: Json) {
    private val sessions = ConcurrentHashMap<Int, MutableSet<DefaultWebSocketServerSession>>()

    fun join(conversationId: Int, session: DefaultWebSocketServerSession) {
        sessions.computeIfAbsent(conversationId) { ConcurrentHashMap.newKeySet() }.add(session)
    }

    fun leave(conversationId: Int, session: DefaultWebSocketServerSession) {
        sessions[conversationId]?.remove(session)
    }

    suspend fun broadcast(conversationId: Int, event: SocketEvent) {
        val payload = json.encodeToString(SocketEvent.serializer(), event)
        sessions[conversationId]?.toList()?.forEach { session ->
            try { session.send(Frame.Text(payload)) } catch (_: Throwable) { leave(conversationId, session) }
        }
    }
}

private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }
private fun ApplicationCall.userId(): Int = principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asInt() ?: unauthorized()
private fun ApplicationCall.pathId(): Int = pathInt("id")
private fun ApplicationCall.pathInt(name: String): Int = parameters[name]?.toIntOrNull() ?: badRequest("Invalid $name")
private fun ApplicationCall.queryLimit(): Int = request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 20
private fun ApplicationCall.queryOffset(): Int = request.queryParameters["offset"]?.toIntOrNull()?.coerceAtLeast(0) ?: 0
private fun ApplicationCall.requireAdmin(settings: ServerSettings) {
    if (request.headers["X-Admin-Key"] != settings.adminKey) throw ApiException(HttpStatusCode.Forbidden, "Admin key is invalid")
}
private fun validateCredentials(request: AuthRequest) {
    if (!request.email.contains('@')) badRequest("A valid email is required")
    if (request.password.length < 8) badRequest("Password must contain at least 8 characters")
    if (request.displayName.isNullOrBlank()) badRequest("Display name is required")
}
private fun badRequest(message: String): Nothing = throw ApiException(HttpStatusCode.BadRequest, message)
private fun unauthorized(): Nothing = throw ApiException(HttpStatusCode.Unauthorized, "Authentication is required")
