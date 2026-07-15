# راهنمای تحویل زیرساخت اپ اندروید

## بخش‌های تکمیل‌شده

- `MainActivity` اکنون Compose را اجرا می‌کند و `SickimfyApp` پنج مقصد Home، Search، Downloads، Playlists و Profile را با Bottom Navigation مدیریت می‌کند.
- Hilt با `SickimfyApplication` فعال است.
- Retrofit/OkHttp به بک‌اند Ktor متصل است.
- Home، Search، Playlists و Profile دارای Repository واقعی هستند.
- Search history با Room ذخیره می‌شود.
- Room جدول‌های liked tracks، downloaded tracks و offline messages را نیز برای مراحل بعد آماده کرده است.
- DataStore تم، زبان، اندازه فونت، Premium و JWT را نگه می‌دارد.

## اجرای محلی

ابتدا بک‌اند را مطابق `backend/README.md` روی پورت 8080 اجرا کنید. مقدار پیش‌فرض اپ برای Emulator برابر است با:

```text
http://10.0.2.2:8080/
```

این مقدار در `app/build.gradle.kts` با نام `API_BASE_URL` تعریف شده است. برای گوشی واقعی آن را به IP شبکهٔ لپ‌تاپ، مانند `http://192.168.1.10:8080/` تغییر دهید.

## احراز هویت

کلاس `SessionRepository` عملیات register، login و logout را آماده کرده و توکن را در DataStore ذخیره می‌کند. هنوز صفحهٔ Login/Register در محدودهٔ این مرحله ساخته نشده است؛ بنابراین Profile تا قبل از ورود، پیام نیاز به ورود نمایش می‌دهد. کسی که بخش Auth UI را می‌سازد باید فقط `SessionRepository` را صدا بزند و نباید منطق ذخیرهٔ JWT را دوباره پیاده‌سازی کند.

## مرزبندی کارهای بعدی

- Player، MediaSession، Audio Focus و Mini Player هنوز در این مرحله پیاده‌سازی نشده‌اند.
- WorkManager باید بعداً فایل دانلودشده را در جدول `downloaded_tracks` ثبت کند.
- بخش Chat باید پیام‌های دریافتی را در جدول `offline_messages` ذخیره کند.
- UI ورود/ثبت‌نام باید از `SessionRepository` استفاده کند.
- Paging 3 برای لیست‌های بلند هنوز باید اضافه شود.

## بررسی پروژه

```powershell
$env:JAVA_HOME = "C:\Path\To\JDK-21"
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```
