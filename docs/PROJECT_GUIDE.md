# Sickimfy — Project Guide

## What the project is

Sickimfy is an Android music-streaming application with a Kotlin/Ktor backend. The Android client uses Jetpack Compose, Hilt, Room, WorkManager, Retrofit/OkHttp, Media3/ExoPlayer and WebSocket chat.

## Running the project

1. Start the backend from `backend` with `./gradlew run` (Windows: `gradlew.bat run`).
2. On an emulator, use `10.0.2.2` as the backend host. For a physical phone connected by USB, run `adb reverse tcp:8080 tcp:8080` and use `http://127.0.0.1:8080/` in the app settings.
3. Build/install the Android app with `./gradlew :app:installDebug`.

## User accounts and social features

- Register with email, password and display name; the display name is what appears in the social UI.
- Search users from Social or `Direct Messages > New chat`; the backend matches display name and email.
- Follow users from their profile in Social. Public playlists from followed users can be opened and played.
- A direct conversation is created automatically when a recipient is selected. Messages are delivered in real time through WebSocket.

## Music and playback

- Home shows featured, popular, recent releases, playlists and Top Artists.
- Tapping a Top Artist opens a search result containing that artist's songs.
- The player supports queue navigation, shuffle, repeat-one, favorite tracks, recently played tracks and personal playlists.
- The `MusicService` hosts a MediaSession, so Android exposes title/artist and Play/Pause/Previous/Next controls in the system notification and lock screen while a track is active.

## Downloads and offline mode

- Tap the download icon in the full player or on a Home track card. WorkManager downloads the audio into the app-private `files/downloads` folder and records it in Room only after the file completes.
- Downloads shows only completed local files. It supports sorting by date, title or artist, swipe-to-dismiss deletion, and local playback.
- If internet connectivity is unavailable, or a network request cannot reach the backend, navigation is restricted to Downloads. Downloaded music remains playable without the backend.

## Sharing tracks in chat

- Open a direct conversation, then use the Share button in the chat app bar to send the currently playing song.
- Shared tracks are stored in Room with message history and displayed as a mini card. Tapping the mini card starts playback.
- Sent, delivered and read states are stored locally and updated via WebSocket; typing indicators are also real time.

## Persistence

- Room holds downloads, liked tracks, recently played tracks, search history, local playlists and offline chat messages.
- DataStore holds the access token, language, theme, font scale, premium flag and API base URL.
- Language selection is persisted in SharedPreferences as well, so it is applied before Activity UI creation. English is LTR and Persian is RTL.

## Main source areas

- `app/src/main/java/com/example/sickimfy/features`: Compose screens, ViewModels and repositories by feature.
- `app/src/main/java/com/example/sickimfy/core/playback`: ExoPlayer, MediaSession and foreground music service.
- `app/src/main/java/com/example/sickimfy/core/data/local`: Room database, DAOs and entities.
- `app/src/main/java/com/example/sickimfy/core/network`: Retrofit API, auth and WebSocket manager.
- `backend/src/main/kotlin/com/sickimfy/backend`: Ktor routes, models and SQLite persistence.

## Troubleshooting checklist

- Cannot load server data on a phone: confirm the backend is running and execute `adb reverse tcp:8080 tcp:8080`.
- Downloads empty: download a track first and wait for the network task to finish; only completed files appear.
- Offline mode: it deliberately hides online tabs. Restore connectivity or restart after backend is available.
- Chat user search: the other user must have registered; search their display name or account email.
