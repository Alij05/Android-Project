package com.sickimfy.backend

import org.mindrot.jbcrypt.BCrypt
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class ApiException(val status: io.ktor.http.HttpStatusCode, override val message: String) : RuntimeException(message)

private data class StoredUser(val profile: UserProfile, val passwordHash: String)

class AppDatabase(databasePath: String) {
    private val jdbcUrl: String

    init {
        Class.forName("org.sqlite.JDBC")
        val databaseFile = File(databasePath).absoluteFile
        databaseFile.parentFile?.mkdirs()
        jdbcUrl = "jdbc:sqlite:${databaseFile.path}"
    }

    fun initialize() = connection { connection ->
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = ON")
            statement.execute("PRAGMA journal_mode = WAL")
            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    display_name TEXT NOT NULL,
                    avatar_url TEXT,
                    is_premium INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent())
            statement.execute("""
                CREATE TABLE IF NOT EXISTS tracks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    artist_name TEXT NOT NULL,
                    album_name TEXT,
                    genre TEXT,
                    cover_image_url TEXT,
                    audio_url TEXT,
                    duration_seconds INTEGER,
                    is_featured INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent())
            statement.execute("""
                CREATE TABLE IF NOT EXISTS playlists (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    title TEXT NOT NULL,
                    description TEXT,
                    cover_image_url TEXT,
                    is_public INTEGER NOT NULL DEFAULT 1,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent())
            statement.execute("""
                CREATE TABLE IF NOT EXISTS playlist_tracks (
                    playlist_id INTEGER NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
                    track_id INTEGER NOT NULL REFERENCES tracks(id) ON DELETE CASCADE,
                    position INTEGER NOT NULL,
                    PRIMARY KEY (playlist_id, track_id)
                )
            """.trimIndent())
            statement.execute("""
                CREATE TABLE IF NOT EXISTS likes (
                    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    track_id INTEGER NOT NULL REFERENCES tracks(id) ON DELETE CASCADE,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (user_id, track_id)
                )
            """.trimIndent())
            statement.execute("""
                CREATE TABLE IF NOT EXISTS follows (
                    follower_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    following_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (follower_id, following_id),
                    CHECK (follower_id <> following_id)
                )
            """.trimIndent())
            statement.execute("""
                CREATE TABLE IF NOT EXISTS search_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    query TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent())
            statement.execute("""
                CREATE TABLE IF NOT EXISTS conversations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent())
            statement.execute("""
                CREATE TABLE IF NOT EXISTS conversation_members (
                    conversation_id INTEGER NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
                    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    PRIMARY KEY (conversation_id, user_id)
                )
            """.trimIndent())
            statement.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    conversation_id INTEGER NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
                    sender_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    content TEXT,
                    shared_track_id INTEGER REFERENCES tracks(id) ON DELETE SET NULL,
                    status TEXT NOT NULL DEFAULT 'SENT',
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CHECK (content IS NOT NULL OR shared_track_id IS NOT NULL)
                )
            """.trimIndent())
            statement.execute("CREATE INDEX IF NOT EXISTS idx_tracks_title ON tracks(title)")
            statement.execute("CREATE INDEX IF NOT EXISTS idx_messages_conversation ON messages(conversation_id, id)")
        }
    }

    fun register(email: String, password: String, displayName: String): UserProfile = connection { connection ->
        if (findStoredUser(connection, email) != null) {
            throw ApiException(io.ktor.http.HttpStatusCode.Conflict, "Email is already registered")
        }
        connection.prepareStatement(
            "INSERT INTO users(email, password_hash, display_name) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS
        ).use { statement ->
            statement.setString(1, email.lowercase())
            statement.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()))
            statement.setString(3, displayName)
            statement.executeUpdate()
            val id = statement.generatedKeys.use { keys -> if (keys.next()) keys.getInt(1) else error("User id was not generated") }
            profile(connection, id)!!
        }
    }

    fun login(email: String, password: String): UserProfile = connection { connection ->
        val user = findStoredUser(connection, email)
            ?: throw ApiException(io.ktor.http.HttpStatusCode.Unauthorized, "Invalid email or password")
        if (!BCrypt.checkpw(password, user.passwordHash)) {
            throw ApiException(io.ktor.http.HttpStatusCode.Unauthorized, "Invalid email or password")
        }
        user.profile
    }

    fun profile(userId: Int): UserProfile = connection { profile(it, userId) ?: notFound("User") }

    fun publicProfile(userId: Int): PublicProfile = connection { publicProfile(it, userId) ?: notFound("User") }

    fun updateProfile(userId: Int, request: UpdateProfileRequest): UserProfile = connection { connection ->
        val old = profile(connection, userId) ?: notFound("User")
        connection.prepareStatement("UPDATE users SET display_name = ?, avatar_url = ? WHERE id = ?").use { statement ->
            statement.setString(1, request.displayName?.trim()?.takeIf { it.isNotEmpty() } ?: old.displayName)
            statement.setString(2, request.avatarUrl ?: old.avatarUrl)
            statement.setInt(3, userId)
            statement.executeUpdate()
        }
        profile(connection, userId)!!
    }

    fun upgradePremium(userId: Int): UserProfile = connection { connection ->
        connection.prepareStatement("UPDATE users SET is_premium = 1 WHERE id = ?").use { it.setInt(1, userId); it.executeUpdate() }
        profile(connection, userId) ?: notFound("User")
    }

    fun createTrack(request: UpsertTrackRequest): Track = connection { connection ->
        validateTrack(request)
        connection.prepareStatement("""
            INSERT INTO tracks(title, artist_name, album_name, genre, cover_image_url, audio_url, duration_seconds, is_featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent(), Statement.RETURN_GENERATED_KEYS).use { statement ->
            setTrackParameters(statement, request)
            statement.executeUpdate()
            val id = statement.generatedKeys.use { keys -> if (keys.next()) keys.getInt(1) else error("Track id was not generated") }
            track(connection, id)!!
        }
    }

    fun updateTrack(trackId: Int, request: UpsertTrackRequest): Track = connection { connection ->
        validateTrack(request)
        connection.prepareStatement("""
            UPDATE tracks SET title=?, artist_name=?, album_name=?, genre=?, cover_image_url=?, audio_url=?, duration_seconds=?, is_featured=?
            WHERE id=?
        """.trimIndent()).use { statement ->
            setTrackParameters(statement, request)
            statement.setInt(9, trackId)
            if (statement.executeUpdate() == 0) notFound("Track")
        }
        track(connection, trackId)!!
    }

    fun deleteTrack(trackId: Int) = connection { connection ->
        connection.prepareStatement("DELETE FROM tracks WHERE id=?").use { statement ->
            statement.setInt(1, trackId)
            if (statement.executeUpdate() == 0) notFound("Track")
        }
    }

    fun track(trackId: Int): Track = connection { track(it, trackId) ?: notFound("Track") }

    fun tracks(query: String?, genre: String?, offset: Int, limit: Int): TrackPage = connection { connection ->
        val clauses = mutableListOf<String>()
        val values = mutableListOf<String>()
        query?.trim()?.takeIf { it.isNotEmpty() }?.let {
            clauses += "(lower(title) LIKE ? OR lower(artist_name) LIKE ? OR lower(album_name) LIKE ?)"
            repeat(3) { _ -> values += "%${it.lowercase()}%" }
        }
        genre?.trim()?.takeIf { it.isNotEmpty() }?.let { clauses += "lower(genre) = ?"; values += it.lowercase() }
        val where = if (clauses.isEmpty()) "" else " WHERE ${clauses.joinToString(" AND ")}"
        val total = connection.prepareStatement("SELECT COUNT(*) FROM tracks$where").use { statement ->
            values.forEachIndexed { index, value -> statement.setString(index + 1, value) }
            statement.executeQuery().use { results -> results.next(); results.getInt(1) }
        }
        val items = connection.prepareStatement("SELECT * FROM tracks$where ORDER BY is_featured DESC, id DESC LIMIT ? OFFSET ?").use { statement ->
            values.forEachIndexed { index, value -> statement.setString(index + 1, value) }
            statement.setInt(values.size + 1, limit)
            statement.setInt(values.size + 2, offset)
            statement.executeQuery().use { results -> results.rows { it.toTrack() } }
        }
        TrackPage(items, offset, limit, total)
    }

    fun featuredTracks(limit: Int) = connection { connection ->
        connection.prepareStatement("SELECT * FROM tracks WHERE is_featured=1 ORDER BY id DESC LIMIT ?").use { statement ->
            statement.setInt(1, limit)
            statement.executeQuery().use { it.rows { row -> row.toTrack() } }
        }
    }

    fun latestTracks(limit: Int) = connection { connection ->
        connection.prepareStatement("SELECT * FROM tracks ORDER BY id DESC LIMIT ?").use { statement ->
            statement.setInt(1, limit)
            statement.executeQuery().use { it.rows { row -> row.toTrack() } }
        }
    }

    fun createPlaylist(ownerId: Int, request: CreatePlaylistRequest): PlaylistSummary = connection { connection ->
        requireText(request.title, "Playlist title")
        connection.prepareStatement("INSERT INTO playlists(owner_id,title,description,cover_image_url,is_public) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS).use { statement ->
            statement.setInt(1, ownerId); statement.setString(2, request.title.trim()); statement.setString(3, request.description)
            statement.setString(4, request.coverImageUrl); statement.setInt(5, request.isPublic.asInt()); statement.executeUpdate()
            val id = statement.generatedKeys.use { keys -> if (keys.next()) keys.getInt(1) else error("Playlist id was not generated") }
            playlistSummary(connection, id)!!
        }
    }

    fun playlists(ownerId: Int? = null, publicOnly: Boolean = false): List<PlaylistSummary> = connection { connection ->
        val where = when {
            ownerId != null -> "WHERE p.owner_id = ?"
            publicOnly -> "WHERE p.is_public = 1"
            else -> ""
        }
        connection.prepareStatement("$PLAYLIST_SUMMARY_SELECT $where GROUP BY p.id ORDER BY p.id DESC").use { statement ->
            if (ownerId != null) statement.setInt(1, ownerId)
            statement.executeQuery().use { it.rows { row -> row.toPlaylistSummary() } }
        }
    }

    fun playlistDetails(playlistId: Int, requesterId: Int?): PlaylistDetails = connection { connection ->
        val summary = playlistSummary(connection, playlistId) ?: notFound("Playlist")
        if (!summary.isPublic && summary.owner.id != requesterId) throw ApiException(io.ktor.http.HttpStatusCode.Forbidden, "This playlist is private")
        val tracks = connection.prepareStatement("""
            SELECT t.* FROM playlist_tracks pt JOIN tracks t ON t.id=pt.track_id
            WHERE pt.playlist_id=? ORDER BY pt.position
        """.trimIndent()).use { statement ->
            statement.setInt(1, playlistId)
            statement.executeQuery().use { it.rows { row -> row.toTrack() } }
        }
        PlaylistDetails(summary, tracks)
    }

    fun addTrackToPlaylist(ownerId: Int, playlistId: Int, trackId: Int) = connection { connection ->
        ensurePlaylistOwner(connection, playlistId, ownerId)
        if (track(connection, trackId) == null) notFound("Track")
        val position = connection.prepareStatement("SELECT COALESCE(MAX(position), 0)+1 FROM playlist_tracks WHERE playlist_id=?").use { statement ->
            statement.setInt(1, playlistId); statement.executeQuery().use { it.next(); it.getInt(1) }
        }
        connection.prepareStatement("INSERT OR IGNORE INTO playlist_tracks(playlist_id,track_id,position) VALUES (?,?,?)").use { statement ->
            statement.setInt(1, playlistId); statement.setInt(2, trackId); statement.setInt(3, position); statement.executeUpdate()
        }
    }

    fun removeTrackFromPlaylist(ownerId: Int, playlistId: Int, trackId: Int) = connection { connection ->
        ensurePlaylistOwner(connection, playlistId, ownerId)
        connection.prepareStatement("DELETE FROM playlist_tracks WHERE playlist_id=? AND track_id=?").use { statement ->
            statement.setInt(1, playlistId); statement.setInt(2, trackId); statement.executeUpdate()
        }
    }

    fun likedTracks(userId: Int): List<Track> = connection { connection ->
        connection.prepareStatement("SELECT t.* FROM likes l JOIN tracks t ON t.id=l.track_id WHERE l.user_id=? ORDER BY l.created_at DESC").use { statement ->
            statement.setInt(1, userId); statement.executeQuery().use { it.rows { row -> row.toTrack() } }
        }
    }

    fun likeTrack(userId: Int, trackId: Int) = connection { connection ->
        if (track(connection, trackId) == null) notFound("Track")
        connection.prepareStatement("INSERT OR IGNORE INTO likes(user_id,track_id) VALUES (?,?)").use { statement ->
            statement.setInt(1, userId); statement.setInt(2, trackId); statement.executeUpdate()
        }
    }

    fun unlikeTrack(userId: Int, trackId: Int) = connection { connection ->
        connection.prepareStatement("DELETE FROM likes WHERE user_id=? AND track_id=?").use { statement ->
            statement.setInt(1, userId); statement.setInt(2, trackId); statement.executeUpdate()
        }
    }

    fun searchHistory(userId: Int): List<SearchHistoryItem> = connection { connection ->
        connection.prepareStatement("SELECT * FROM search_history WHERE user_id=? ORDER BY id DESC LIMIT 20").use { statement ->
            statement.setInt(1, userId)
            statement.executeQuery().use { it.rows { row -> SearchHistoryItem(row.getInt("id"), row.getString("query"), row.getString("created_at")) } }
        }
    }

    fun addSearchHistory(userId: Int, query: String): SearchHistoryItem = connection { connection ->
        requireText(query, "Search query")
        connection.prepareStatement("INSERT INTO search_history(user_id,query) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS).use { statement ->
            statement.setInt(1, userId); statement.setString(2, query.trim()); statement.executeUpdate()
            val id = statement.generatedKeys.use { keys -> if (keys.next()) keys.getInt(1) else error("History id was not generated") }
            connection.prepareStatement("SELECT * FROM search_history WHERE id=?").use { read ->
                read.setInt(1, id); read.executeQuery().use { results -> results.next(); SearchHistoryItem(id, results.getString("query"), results.getString("created_at")) }
            }
        }
    }

    fun deleteSearchHistory(userId: Int, historyId: Int) = connection { connection ->
        connection.prepareStatement("DELETE FROM search_history WHERE id=? AND user_id=?").use { statement ->
            statement.setInt(1, historyId); statement.setInt(2, userId); statement.executeUpdate()
        }
    }

    fun followedUsers(userId: Int): List<FollowedUser> = connection { connection ->
        connection.prepareStatement("""
            SELECT u.id,u.display_name,u.avatar_url,u.is_premium,f.created_at
            FROM follows f JOIN users u ON u.id=f.following_id WHERE f.follower_id=? ORDER BY f.created_at DESC
        """.trimIndent()).use { statement ->
            statement.setInt(1, userId)
            statement.executeQuery().use { it.rows { row -> FollowedUser(row.toPublicProfile(), row.getString("created_at")) } }
        }
    }

    fun follow(userId: Int, targetId: Int) = connection { connection ->
        if (userId == targetId) throw ApiException(io.ktor.http.HttpStatusCode.BadRequest, "You cannot follow yourself")
        if (publicProfile(connection, targetId) == null) notFound("User")
        connection.prepareStatement("INSERT OR IGNORE INTO follows(follower_id,following_id) VALUES (?,?)").use { statement ->
            statement.setInt(1, userId); statement.setInt(2, targetId); statement.executeUpdate()
        }
    }

    fun unfollow(userId: Int, targetId: Int) = connection { connection ->
        connection.prepareStatement("DELETE FROM follows WHERE follower_id=? AND following_id=?").use { statement ->
            statement.setInt(1, userId); statement.setInt(2, targetId); statement.executeUpdate()
        }
    }

    fun getOrCreateConversation(userId: Int, participantId: Int): ConversationSummary = connection { connection ->
        if (userId == participantId) throw ApiException(io.ktor.http.HttpStatusCode.BadRequest, "A conversation needs another user")
        if (publicProfile(connection, participantId) == null) notFound("User")
        val existing = connection.prepareStatement("""
            SELECT c.id FROM conversations c
            JOIN conversation_members m1 ON m1.conversation_id=c.id AND m1.user_id=?
            JOIN conversation_members m2 ON m2.conversation_id=c.id AND m2.user_id=?
            WHERE (SELECT COUNT(*) FROM conversation_members cm WHERE cm.conversation_id=c.id)=2 LIMIT 1
        """.trimIndent()).use { statement ->
            statement.setInt(1, userId); statement.setInt(2, participantId)
            statement.executeQuery().use { results -> if (results.next()) results.getInt(1) else null }
        }
        val id = existing ?: run {
            val generated = connection.prepareStatement("INSERT INTO conversations DEFAULT VALUES", Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.executeUpdate(); statement.generatedKeys.use { keys -> keys.next(); keys.getInt(1) }
            }
            connection.prepareStatement("INSERT INTO conversation_members(conversation_id,user_id) VALUES (?,?), (?,?)").use { statement ->
                statement.setInt(1, generated); statement.setInt(2, userId); statement.setInt(3, generated); statement.setInt(4, participantId); statement.executeUpdate()
            }
            generated
        }
        conversationSummary(connection, id, userId)!!
    }

    fun conversations(userId: Int): List<ConversationSummary> = connection { connection ->
        connection.prepareStatement("""
            SELECT c.id,c.created_at,u.id AS user_id,u.display_name AS user_display_name,
            u.avatar_url AS user_avatar_url,u.is_premium AS user_is_premium
            FROM conversations c
            JOIN conversation_members mine ON mine.conversation_id=c.id AND mine.user_id=?
            JOIN conversation_members other ON other.conversation_id=c.id AND other.user_id<>?
            JOIN users u ON u.id=other.user_id
            ORDER BY COALESCE((SELECT MAX(m.id) FROM messages m WHERE m.conversation_id=c.id), 0) DESC
        """.trimIndent()).use { statement ->
            statement.setInt(1, userId); statement.setInt(2, userId)
            statement.executeQuery().use { it.rows { row ->
                ConversationSummary(row.getInt("id"), row.toPublicProfile("user_"), lastMessage(connection, row.getInt("id")), row.getString("created_at"))
            } }
        }
    }

    fun messages(userId: Int, conversationId: Int): List<Message> = connection { connection ->
        ensureConversationMember(connection, conversationId, userId)
        val messages = connection.prepareStatement("SELECT * FROM messages WHERE conversation_id=? ORDER BY id").use { statement ->
            statement.setInt(1, conversationId); statement.executeQuery().use { it.rows { row -> row.toMessage(connection) } }
        }
        connection.prepareStatement("UPDATE messages SET status='READ' WHERE conversation_id=? AND sender_id<>? AND status='SENT'").use { statement ->
            statement.setInt(1, conversationId); statement.setInt(2, userId); statement.executeUpdate()
        }
        messages
    }

    fun sendMessage(userId: Int, conversationId: Int, request: SendMessageRequest): Message = connection { connection ->
        ensureConversationMember(connection, conversationId, userId)
        val content = request.content?.trim()?.takeIf { it.isNotEmpty() }
        if (content == null && request.sharedTrackId == null) throw ApiException(io.ktor.http.HttpStatusCode.BadRequest, "Message content or sharedTrackId is required")
        if (request.sharedTrackId != null && track(connection, request.sharedTrackId) == null) notFound("Track")
        connection.prepareStatement("INSERT INTO messages(conversation_id,sender_id,content,shared_track_id) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS).use { statement ->
            statement.setInt(1, conversationId); statement.setInt(2, userId); statement.setString(3, content)
            if (request.sharedTrackId == null) statement.setNull(4, java.sql.Types.INTEGER) else statement.setInt(4, request.sharedTrackId)
            statement.executeUpdate()
            val id = statement.generatedKeys.use { keys -> keys.next(); keys.getInt(1) }
            message(connection, id)!!
        }
    }

    fun isConversationMember(userId: Int, conversationId: Int): Boolean = connection { connection ->
        connection.prepareStatement("SELECT 1 FROM conversation_members WHERE conversation_id=? AND user_id=?").use { statement ->
            statement.setInt(1, conversationId); statement.setInt(2, userId); statement.executeQuery().use { it.next() }
        }
    }

    private fun <T> connection(block: (Connection) -> T): T = DriverManager.getConnection(jdbcUrl).use { connection ->
        connection.createStatement().use { it.execute("PRAGMA foreign_keys = ON") }
        block(connection)
    }

    private fun profile(connection: Connection, userId: Int): UserProfile? = connection.prepareStatement("SELECT * FROM users WHERE id=?").use { statement ->
        statement.setInt(1, userId); statement.executeQuery().use { results -> if (results.next()) results.toUserProfile() else null }
    }

    private fun publicProfile(connection: Connection, userId: Int): PublicProfile? = connection.prepareStatement("SELECT * FROM users WHERE id=?").use { statement ->
        statement.setInt(1, userId); statement.executeQuery().use { results -> if (results.next()) results.toPublicProfile() else null }
    }

    private fun findStoredUser(connection: Connection, email: String): StoredUser? = connection.prepareStatement("SELECT * FROM users WHERE lower(email)=?").use { statement ->
        statement.setString(1, email.trim().lowercase()); statement.executeQuery().use { results ->
            if (results.next()) StoredUser(results.toUserProfile(), results.getString("password_hash")) else null
        }
    }

    private fun track(connection: Connection, trackId: Int): Track? = connection.prepareStatement("SELECT * FROM tracks WHERE id=?").use { statement ->
        statement.setInt(1, trackId); statement.executeQuery().use { results -> if (results.next()) results.toTrack() else null }
    }

    private fun message(connection: Connection, messageId: Int): Message? = connection.prepareStatement("SELECT * FROM messages WHERE id=?").use { statement ->
        statement.setInt(1, messageId); statement.executeQuery().use { results -> if (results.next()) results.toMessage(connection) else null }
    }

    private fun lastMessage(connection: Connection, conversationId: Int): Message? = connection.prepareStatement("SELECT * FROM messages WHERE conversation_id=? ORDER BY id DESC LIMIT 1").use { statement ->
        statement.setInt(1, conversationId); statement.executeQuery().use { results -> if (results.next()) results.toMessage(connection) else null }
    }

    private fun playlistSummary(connection: Connection, playlistId: Int): PlaylistSummary? = connection.prepareStatement("$PLAYLIST_SUMMARY_SELECT WHERE p.id=? GROUP BY p.id").use { statement ->
        statement.setInt(1, playlistId); statement.executeQuery().use { results -> if (results.next()) results.toPlaylistSummary() else null }
    }

    private fun conversationSummary(connection: Connection, conversationId: Int, userId: Int): ConversationSummary? = connection.prepareStatement("""
        SELECT c.id,c.created_at,u.id AS user_id,u.display_name AS user_display_name,
        u.avatar_url AS user_avatar_url,u.is_premium AS user_is_premium
        FROM conversations c JOIN conversation_members other ON other.conversation_id=c.id AND other.user_id<>?
        JOIN users u ON u.id=other.user_id WHERE c.id=?
    """.trimIndent()).use { statement ->
        statement.setInt(1, userId); statement.setInt(2, conversationId); statement.executeQuery().use { results ->
            if (results.next()) ConversationSummary(results.getInt("id"), results.toPublicProfile("user_"), lastMessage(connection, conversationId), results.getString("created_at")) else null
        }
    }

    private fun ensurePlaylistOwner(connection: Connection, playlistId: Int, ownerId: Int) {
        val actualOwner = connection.prepareStatement("SELECT owner_id FROM playlists WHERE id=?").use { statement ->
            statement.setInt(1, playlistId); statement.executeQuery().use { results -> if (results.next()) results.getInt(1) else null }
        } ?: notFound("Playlist")
        if (actualOwner != ownerId) throw ApiException(io.ktor.http.HttpStatusCode.Forbidden, "Only the playlist owner can change it")
    }

    private fun ensureConversationMember(connection: Connection, conversationId: Int, userId: Int) {
        if (!connection.prepareStatement("SELECT 1 FROM conversation_members WHERE conversation_id=? AND user_id=?").use { statement ->
                statement.setInt(1, conversationId); statement.setInt(2, userId); statement.executeQuery().use { it.next() }
            }) throw ApiException(io.ktor.http.HttpStatusCode.Forbidden, "You are not part of this conversation")
    }

    private fun validateTrack(request: UpsertTrackRequest) {
        requireText(request.title, "Track title"); requireText(request.artistName, "Artist name")
        if (request.durationSeconds != null && request.durationSeconds < 0) throw ApiException(io.ktor.http.HttpStatusCode.BadRequest, "Duration cannot be negative")
    }

    private fun requireText(value: String, field: String) {
        if (value.trim().isEmpty()) throw ApiException(io.ktor.http.HttpStatusCode.BadRequest, "$field is required")
    }

    private fun setTrackParameters(statement: java.sql.PreparedStatement, request: UpsertTrackRequest) {
        statement.setString(1, request.title.trim()); statement.setString(2, request.artistName.trim()); statement.setString(3, request.albumName)
        statement.setString(4, request.genre); statement.setString(5, request.coverImageUrl); statement.setString(6, request.audioUrl)
        if (request.durationSeconds == null) statement.setNull(7, java.sql.Types.INTEGER) else statement.setInt(7, request.durationSeconds)
        statement.setInt(8, request.isFeatured.asInt())
    }

    private fun ResultSet.toUserProfile() = UserProfile(getInt("id"), getString("email"), getString("display_name"), getString("avatar_url"), getInt("is_premium") == 1, getString("created_at"))
    private fun ResultSet.toPublicProfile(prefix: String = "") = PublicProfile(getInt("${prefix}id"), getString("${prefix}display_name"), getString("${prefix}avatar_url"), getInt("${prefix}is_premium") == 1)
    private fun ResultSet.toTrack() = Track(getInt("id"), getString("title"), getString("artist_name"), getString("album_name"), getString("genre"), getString("cover_image_url"), getString("audio_url"), getIntOrNull("duration_seconds"), getInt("is_featured") == 1, getString("created_at"))
    private fun ResultSet.toPlaylistSummary() = PlaylistSummary(getInt("id"), getString("title"), getString("description"), getString("cover_image_url"), getInt("is_public") == 1, toPublicProfile("owner_"), getInt("track_count"), getString("created_at"))
    private fun ResultSet.toMessage(connection: Connection): Message {
        val sharedTrackId = getIntOrNull("shared_track_id")
        return Message(getInt("id"), getInt("conversation_id"), getInt("sender_id"), getString("content"), sharedTrackId?.let { track(connection, it) }, MessageStatus.valueOf(getString("status")), getString("created_at"))
    }
    private fun ResultSet.getIntOrNull(column: String): Int? = getInt(column).let { if (wasNull()) null else it }
    private fun <T> ResultSet.rows(mapper: (ResultSet) -> T): List<T> = buildList { while (next()) add(mapper(this@rows)) }
    private fun Boolean.asInt() = if (this) 1 else 0
    private fun notFound(name: String): Nothing = throw ApiException(io.ktor.http.HttpStatusCode.NotFound, "$name was not found")

    private companion object {
        val PLAYLIST_SUMMARY_SELECT = """
            SELECT p.id,p.title,p.description,p.cover_image_url,p.is_public,p.created_at,
            u.id AS owner_id,u.display_name AS owner_display_name,u.avatar_url AS owner_avatar_url,u.is_premium AS owner_is_premium,
            COUNT(pt.track_id) AS track_count
            FROM playlists p JOIN users u ON u.id=p.owner_id LEFT JOIN playlist_tracks pt ON pt.playlist_id=p.id
        """.trimIndent()
    }
}
