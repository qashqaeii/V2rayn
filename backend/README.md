# VPN Backend — Server Management API

Backend برای اپ Android VPN: لیست سرورها و کانفیگ‌های رمزگذاری‌شده VLESS.

- **مدیریت سرور:** Django Admin
- **API:** `GET /api/servers/` — فقط سرورهای فعال، کانفیگ AES-256 رمز شده، مرتب‌سازی با `priority`
- **بدون کاربر/پرداخت/اشتراک**

## نیازمندی‌ها

- Python 3.11+
- PostgreSQL (production)
- Redis (production cache)

## راه‌اندازی محلی

```bash
cd backend
python -m venv .venv
.venv\Scripts\activate   # Windows
pip install -r requirements.txt
cp .env.example .env
# در .env مقدار CONFIG_ENCRYPTION_KEY را ۳۲ کاراکتر قرار دهید (همان کلید اپ اندروید)
python manage.py migrate
python manage.py createsuperuser
python manage.py runserver
```

- Admin: http://127.0.0.1:8000/admin/
- API: http://127.0.0.1:8000/api/servers/

## اپ اندروید (هماهنگ با این بک‌اند)

۱. **Base URL:** در `V2rayNG/.env` مقدار `VPN_API_BASE_URL` را طوری قرار دهید که درخواست به `GET /api/servers/` برسد؛ مثلاً `http://77.110.116.139:8000/api/` (با اسلش پایانی).
۲. **کلید رمزگشایی:** در همان فایل `V2rayNG/.env` متغیر `CONFIG_ENCRYPTION_KEY` را دقیقاً برابر با `CONFIG_ENCRYPTION_KEY` در `backend/.env` قرار دهید (۳۲ کاراکتر). اپ با این کلید فیلد `config` پاسخ API را رمزگشایی می‌کند.

## تست

```bash
python manage.py test tests
```

## Docker (Production)

```bash
docker-compose up -d
# یا فقط build و run:
docker build -t vpn-backend .
docker run -p 8000:8000 --env-file .env vpn-backend
```

## استقرار روی سرور اوبونتو

برای نصب روی سرور لینوکس (مثلاً Ubuntu 24.04) راهنمای گام‌به‌گام و اسکریپت‌ها در پوشه **`deploy/`** قرار دارد. فایل **`deploy/DEPLOY-FA.md`** را بخوانید.

## متغیرهای محیط (.env)

| متغیر | توضیح |
|--------|--------|
| `SECRET_KEY` | کلید مخفی Django |
| `CONFIG_ENCRYPTION_KEY` | کلید AES-256 (۳۲ کاراکتر)، باید با اپ یکی باشد |
| `DATABASE_URL` | مثلاً `postgres://user:pass@host:5432/db` |
| `REDIS_URL` | مثلاً `redis://localhost:6379/0` |
| `CORS_ALLOWED_ORIGINS` | مبداهای مجاز CORS |

## ساختار

- `core/` — تنظیمات، URLها
- `servers/` — مدل Server، Admin، API
- `api/` — روت‌های API
- `utils/encryption.py` — رمزگذاری AES برای کانفیگ
