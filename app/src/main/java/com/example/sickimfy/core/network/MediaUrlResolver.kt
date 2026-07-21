package com.example.sickimfy.core.network

import com.example.sickimfy.BuildConfig
import java.net.URI

/**
 * Replaces development-only media hosts stored in the database with the host
 * used by the app's API. This keeps imported media playable on both emulators
 * and physical devices.
 */
fun resolveMediaUrl(url: String?, apiBaseUrl: String = BuildConfig.API_BASE_URL): String? {
    val rawUrl = url?.trim()?.takeIf { it.isNotEmpty() } ?: return url
    val mediaUri = runCatching { URI(rawUrl) }.getOrNull() ?: return rawUrl
    val mediaPath = mediaUri.rawPath?.let { path ->
        when {
            path.startsWith("/media/") -> path
            path.startsWith("media/") -> "/$path"
            else -> null
        }
    } ?: return rawUrl

    val isRelative = !mediaUri.isAbsolute
    val isDevelopmentHost = mediaUri.host?.lowercase() in DEVELOPMENT_MEDIA_HOSTS
    if (!isRelative && !isDevelopmentHost) return rawUrl

    val apiUri = runCatching { URI(apiBaseUrl) }.getOrNull() ?: return rawUrl
    val origin = apiUri.takeIf { it.scheme != null && it.rawAuthority != null } ?: return rawUrl
    val query = mediaUri.rawQuery?.let { "?$it" }.orEmpty()
    val fragment = mediaUri.rawFragment?.let { "#$it" }.orEmpty()
    return "${origin.scheme}://${origin.rawAuthority}$mediaPath$query$fragment"
}

private val DEVELOPMENT_MEDIA_HOSTS = setOf("10.0.2.2", "127.0.0.1", "localhost")
