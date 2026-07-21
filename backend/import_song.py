"""
Import a single audio file into sickimfy.db and copy it to backend/media/
Usage: python import_song.py <path_to_audio_file>
"""
import sqlite3
import shutil
import os
import sys
import urllib.parse

try:
    from mutagen import File as MutagenFile
    from mutagen.mp3 import MP3
    from mutagen.id3 import ID3, TIT2, TPE1, TALB
    HAS_MUTAGEN = True
except ImportError:
    HAS_MUTAGEN = False
    print("[WARNING] mutagen not available, metadata will be guessed from filename")

# ── Config ──────────────────────────────────────────────────────────────────
SCRIPT_DIR  = os.path.dirname(os.path.abspath(__file__))
DB_PATH     = os.path.join(SCRIPT_DIR, "data", "sickimfy.db")
MEDIA_DIR   = os.path.join(SCRIPT_DIR, "media")
CLIENT_HOST = "http://10.0.2.2:8080"   # Android emulator → host loopback
# ─────────────────────────────────────────────────────────────────────────────

def get_duration_seconds(path: str) -> int:
    """Return audio duration in seconds (best-effort)."""
    if HAS_MUTAGEN:
        try:
            audio = MutagenFile(path)
            if audio is not None and hasattr(audio, "info") and hasattr(audio.info, "length"):
                return int(audio.info.length)
        except Exception as e:
            print(f"  [warn] Could not read duration with mutagen: {e}")
    # Fallback: file-size heuristic for 128 kbps MP3
    size_bytes = os.path.getsize(path)
    return int(size_bytes / (128 * 1024 / 8))


def get_id3_tags(path: str):
    """Return (title, artist, album) from ID3 or guess from filename."""
    title = artist = album = None
    if HAS_MUTAGEN:
        try:
            audio = MutagenFile(path, easy=True)
            if audio:
                title  = (audio.get("title")  or [None])[0]
                artist = (audio.get("artist") or [None])[0]
                album  = (audio.get("album")  or [None])[0]
        except Exception as e:
            print(f"  [warn] Could not read ID3 tags: {e}")

    # Guess from filename  "Hayedeh - Ravi.mp3" → artist=Hayedeh, title=Ravi
    basename = os.path.splitext(os.path.basename(path))[0]
    if not title and not artist:
        parts = [p.strip() for p in basename.split("-", 1)]
        if len(parts) == 2:
            artist = parts[0]
            title  = parts[1]
        else:
            title = basename

    title  = title  or basename
    artist = artist or "Unknown Artist"
    album  = album  or "Single"
    return title, artist, album


def import_track(audio_path: str):
    audio_path = os.path.abspath(audio_path)
    if not os.path.isfile(audio_path):
        print(f"[ERROR] File not found: {audio_path}")
        sys.exit(1)

    filename = os.path.basename(audio_path)

    # 1. Copy to backend/media/
    os.makedirs(MEDIA_DIR, exist_ok=True)
    dest = os.path.join(MEDIA_DIR, filename)
    if os.path.exists(dest):
        print(f"[INFO] File already in media/, skipping copy: {filename}")
    else:
        shutil.copy2(audio_path, dest)
        print(f"[OK]   Copied -> {dest}")

    # 2. Extract metadata
    title, artist, album = get_id3_tags(audio_path)
    duration = get_duration_seconds(audio_path)

    print(f"  Title   : {title}")
    print(f"  Artist  : {artist}")
    print(f"  Album   : {album}")
    print(f"  Duration: {duration}s  ({duration//60}:{duration%60:02d})")

    # 3. Build URLs
    escaped   = urllib.parse.quote(filename)
    audio_url = f"{CLIENT_HOST}/media/{escaped}"
    cover_url = f"{CLIENT_HOST}/media/default_cover.png"

    print(f"  Audio URL : {audio_url}")
    print(f"  Cover URL : {cover_url}")

    # 4. Check if track already exists in DB
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()

    c.execute("SELECT id, title FROM tracks WHERE audio_url = ?", (audio_url,))
    existing = c.fetchone()
    if existing:
        print(f"[INFO] Track already in DB (id={existing[0]}): '{existing[1]}' – skipping insert.")
        conn.close()
        return existing[0]

    # 5. Insert into tracks table
    c.execute("""
        INSERT INTO tracks (title, artist_name, album_name, genre,
                            cover_image_url, audio_url, duration_seconds, is_featured)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """, (title, artist, album, "Pop", cover_url, audio_url, duration, 1))

    conn.commit()
    new_id = c.lastrowid
    conn.close()

    print(f"[OK]   Track inserted into DB with id={new_id}")
    return new_id


if __name__ == "__main__":
    if len(sys.argv) < 2:
        # Default: import the file from ../music/ relative to backend/
        music_dir = os.path.join(SCRIPT_DIR, "..", "music")
        mp3_files = [f for f in os.listdir(music_dir) if f.lower().endswith((".mp3",".m4a",".wav",".ogg"))]
        if not mp3_files:
            print("[ERROR] No audio files found in ../music/ and no argument given.")
            sys.exit(1)
        for f in mp3_files:
            print(f"\n{'='*50}")
            print(f"Importing: {f}")
            print('='*50)
            import_track(os.path.join(music_dir, f))
    else:
        import_track(sys.argv[1])

    print("\nDone! You can now start the backend and play the track.")
