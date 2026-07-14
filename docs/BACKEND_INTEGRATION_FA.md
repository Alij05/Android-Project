# راهنمای اتصال اپ اندروید به بک‌اند

## قرارداد تیم

بک‌اند در پوشهٔ `backend/` مستقل از اپ اندروید اجرا می‌شود. اعضای تیم فقط از API استفاده می‌کنند و نباید مستقیم به فایل SQLite دسترسی داشته باشند.

| محیط اجرا | Base URL |
| --- | --- |
| امولاتور Android Studio | `http://10.0.2.2:8080` |
| گوشی واقعی و سرور روی لپ‌تاپ | `http://<LAN-IP-PC>:8080` |
| خود سرور | `http://localhost:8080` |

در توسعهٔ محلی HTTP است. برای اجرای اپ روی Android، مجوز `INTERNET` لازم است و اگر از HTTP استفاده می‌شود باید cleartext traffic فقط برای build توسعه فعال شود. در نسخهٔ نهایی از HTTPS استفاده شود.

همهٔ پاسخ‌ها JSON هستند. مسیرهای دارای علامت 🔒 به این header نیاز دارند:

```text
Authorization: Bearer <token>
```

توکن از ثبت‌نام یا ورود به‌دست می‌آید و تا ۷ روز معتبر است.

## احراز هویت و پروفایل

| متد و مسیر | کاربرد |
| --- | --- |
| `POST /api/auth/register` | ساخت کاربر |
| `POST /api/auth/login` | ورود و دریافت توکن |
| `GET /api/profile/me` 🔒 | پروفایل کاربر فعلی |
| `PATCH /api/profile/me` 🔒 | تغییر `displayName` یا `avatarUrl` |
| `POST /api/profile/me/upgrade` 🔒 | فعال کردن نمایشی Premium |

نمونهٔ ثبت‌نام:

```json
{
  "email": "sara@example.com",
  "password": "password123",
  "displayName": "Sara"
}
```

پاسخ ثبت‌نام و ورود:

```json
{
  "token": "eyJ...",
  "user": {
    "id": 1,
    "email": "sara@example.com",
    "displayName": "Sara",
    "avatarUrl": null,
    "isPremium": false,
    "createdAt": "2026-07-14 12:00:00"
  }
}
```

توکن را با DataStore نگه دارید؛ اطلاعات قابل‌نمایش مانند پروفایل یا آهنگ‌ها را برای نمایش آفلاین با Room ذخیره کنید.

## آهنگ‌ها و خانه

| متد و مسیر | کاربرد |
| --- | --- |
| `GET /api/home` | featured، جدیدترین‌ها و پلی‌لیست‌های عمومی برای Home |
| `GET /api/tracks?query=&genre=&limit=20&offset=0` | لیست صفحه‌بندی‌شده و جست‌وجو |
| `GET /api/tracks/{id}` | جزئیات یک آهنگ |
| `GET /api/playlists/public` | پلی‌لیست‌های قابل نمایش برای همه |

فیلدهای `Track`: `id`، `title`، `artistName`، `albumName`، `genre`، `coverImageUrl`، `audioUrl`، `durationSeconds`، `isFeatured` و `createdAt`.

برای Search، بعد از debounce در ViewModel، `GET /api/tracks?query=<text>` را فراخوانی کنید. اگر کاربر جست‌وجو را تأیید کرد، همان عبارت را با `POST /api/search-history` ذخیره کنید.

## پلی‌لیست، لایک و تاریخچه

| متد و مسیر | کاربرد |
| --- | --- |
| `GET`, `POST /api/playlists` 🔒 | پلی‌لیست‌های خود کاربر / ساخت پلی‌لیست |
| `GET /api/playlists/{id}` 🔒 | جزئیات و آهنگ‌های پلی‌لیست |
| `POST /api/playlists/{id}/tracks` 🔒 | بدنه: `{ "trackId": 3 }` |
| `DELETE /api/playlists/{id}/tracks/{trackId}` 🔒 | حذف آهنگ از پلی‌لیست خود کاربر |
| `GET /api/likes` 🔒 | آهنگ‌های لایک‌شده |
| `POST`, `DELETE /api/likes/{trackId}` 🔒 | لایک/برداشتن لایک |
| `GET`, `POST /api/search-history` 🔒 | تاریخچه / بدنه: `{ "query": "jazz" }` |
| `DELETE /api/search-history/{id}` 🔒 | حذف یک مورد تاریخچه |

## بخش اجتماعی و چت

| متد و مسیر | کاربرد |
| --- | --- |
| `GET /api/follows` 🔒 | کاربران دنبال‌شده |
| `POST`, `DELETE /api/follows/{userId}` 🔒 | Follow / Unfollow |
| `GET /api/conversations` 🔒 | لیست دایرکت‌ها |
| `POST /api/conversations` 🔒 | بدنه: `{ "participantId": 2 }`؛ ساخت یا بازگرداندن چت موجود |
| `GET /api/conversations/{id}/messages` 🔒 | تاریخچه پیام‌ها و علامت‌گذاری خوانده‌شده |
| `POST /api/conversations/{id}/messages` 🔒 | متن یا اشتراک‌گذاری آهنگ |

نمونهٔ پیام متنی:

```json
{ "content": "این آهنگ را گوش کن" }
```

نمونهٔ اشتراک‌گذاری آهنگ:

```json
{ "sharedTrackId": 12 }
```

برای چت بلادرنگ، پس از باز کردن گفت‌وگو به WebSocket وصل شوید:

```text
ws://10.0.2.2:8080/ws/conversations/7?token=<token>
```

رویداد پیام جدید:

```json
{
  "type": "MESSAGE",
  "message": { "id": 10, "conversationId": 7, "senderId": 2, "content": "سلام", "status": "SENT", "createdAt": "..." }
}
```

رویداد typing که کلاینت می‌فرستد و دریافت می‌کند:

```json
{ "type": "TYPING", "isTyping": true }
```

ارسال پیام همیشه با `POST` انجام می‌شود؛ WebSocket فقط اعلان پیام جدید و وضعیت درحال‌تایپ را پخش می‌کند. این کار باعث می‌شود پیام قابل‌اعتماد در دیتابیس ذخیره شود.

## مسئولیت هر بخش اندروید

- **Repository شبکه:** تعریف DTOهای هم‌نام با پاسخ‌ها، قرار دادن Bearer token در interceptor و تبدیل DTO به model دامنه.
- **Home/Search/Playlist:** مصرف endpointهای آهنگ و پلی‌لیست. Paging 3 باید از `limit` و `offset` استفاده کند.
- **Player:** از `audioUrl` برای ExoPlayer استفاده می‌کند؛ دانلود و کش محلی وظیفهٔ کلاینت است.
- **Profile/Settings:** توکن و زبان/تم در DataStore؛ پروفایل روی سرور.
- **Chat:** تاریخچهٔ REST را در Room cache می‌کند و روی WebSocket برای رویدادهای جدید subscribe می‌شود.

## مسیرهای مدیر آهنگ

این سه مسیر فقط برای آماده‌سازی محتوای سرور هستند و header `X-Admin-Key` می‌خواهند:

- `POST /api/admin/tracks`
- `PATCH /api/admin/tracks/{id}`
- `DELETE /api/admin/tracks/{id}`

این کلید نباید در اپ موبایل قرار بگیرد. مسئول بک‌اند هنگام ورود داده‌های ۵۰ آهنگ از آن استفاده می‌کند.
