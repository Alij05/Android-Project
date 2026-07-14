# Sickimfy Backend

این سرویس با **Kotlin + Ktor** نوشته شده و از **SQLite** استفاده می‌کند. برای نسخهٔ درسی، SQLite انتخاب مناسبی است: راه‌اندازی ندارد، فایل دیتابیس قابل حمل است و تمام APIها و WebSocket روی یک سرویس Ktor واقعی در دسترس‌اند. در صورت نیاز به استقرار چندکاربره، لایهٔ API ثابت می‌ماند و فقط درایور/تنظیمات دیتابیس به PostgreSQL تغییر می‌کند.

## اجرای محلی

پیش‌نیاز: JDK 21 و اتصال اینترنت برای اولین اجرای Gradle.

```powershell
cd backend
$env:JWT_SECRET = "a-long-random-development-secret"
$env:ADMIN_KEY = "local-admin-key"
..\gradlew.bat run
```

سرویس روی `http://localhost:8080` اجرا می‌شود و در اجرای نخست فایل `data/sickimfy.db` را می‌سازد. تنظیمات دیگر در `.env.example` فهرست شده‌اند؛ برای اجرای محلی می‌توان همان مقادیر پیش‌فرض را نگه داشت.

برای تست:

```powershell
cd backend
..\gradlew.bat test
```

فایل‌های صوتی و کاور را بعداً در `backend/media/` قرار دهید. سپس فایل‌ها با این شکل قابل دسترسی‌اند:

```text
http://<server-address>:8080/media/song-01.mp3
http://<server-address>:8080/media/song-01.jpg
```

خود فایل‌های رسانه و دیتابیس در Git ثبت نمی‌شوند.

## داده‌های اولیه

در این مرحله آهنگی وارد نشده است، اما جدول `tracks` و API مدیریت آن آماده‌اند. پس از مشخص شدن آهنگ‌ها، ابتدا با یک کاربر ثبت‌نام کنید، سپس با کلید مدیر یک آهنگ اضافه کنید:

```powershell
$token = "JWT_FROM_LOGIN"
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/tracks" `
  -Headers @{ Authorization = "Bearer $token"; "X-Admin-Key" = "local-admin-key" } `
  -ContentType "application/json" `
  -Body '{"title":"Sample Track","artistName":"Sample Artist","genre":"Pop","audioUrl":"http://localhost:8080/media/sample.mp3","coverImageUrl":"http://localhost:8080/media/sample.jpg","durationSeconds":180,"isFeatured":true}'
```

برای دمو، حداقل ۵۰ رکورد از همین مسیر اضافه کنید. URL صوتی و کاور می‌تواند بعداً با `PATCH /api/admin/tracks/{id}` به‌روز شود.

## امنیت توسعه

- `JWT_SECRET` و `ADMIN_KEY` را قبل از استقرار حتماً تغییر دهید.
- `ADMIN_KEY` فقط برای ثبت/ویرایش/حذف آهنگ روی سرور استفاده می‌شود و نباید داخل APK قرار بگیرد.
- رمز عبور با BCrypt هش می‌شود؛ خود رمز در دیتابیس ذخیره نمی‌شود.
- SQLite برای ارائه و توسعهٔ محلی مناسب است. برای انتشار عمومی چندکاربره، PostgreSQL توصیه می‌شود.

