# دستورات بروزرسانی و استقرار سیستم

این فایل شامل دستورات لازم برای بروزرسانی سیستم بعد از کپی کردن فایل‌های جدید است.

---

## 📋 دستورات بروزرسانی (بعد از کپی فایل‌ها)

### 1️⃣ ساخت مجدد Image (بدون کش)

```bash
cd /root/vpn-backend
docker compose -f deploy/docker-compose.server.yml build --no-cache web
```

### 2️⃣ توقف سرویس‌ها

```bash
docker compose -f deploy/docker-compose.server.yml down
```

### 3️⃣ اجرای مایگریشن‌های جدید

بعد از کپی کردن فایل‌های به‌روز (از جمله پوشه `accounts/migrations/`):

```bash
docker compose -f deploy/docker-compose.server.yml up -d db
sleep 5
docker compose -f deploy/docker-compose.server.yml exec -T web python manage.py migrate --noinput
```

اگر پیام «Your models in app(s): 'accounts' have changes that are not yet reflected in a migration» دیدید، یعنی فایل مایگریشن جدید (مثلاً `0002_add_help_texts.py`) روی سرور کپی نشده است. بعد از کپی کردن آن، دوباره همان دستور `migrate --noinput` را اجرا کنید.

یا اگر سرویس web هنوز بالا نیست:

```bash
docker compose -f deploy/docker-compose.server.yml up -d
sleep 10
docker compose -f deploy/docker-compose.server.yml exec -T web python manage.py migrate --noinput
```

### 4️⃣ جمع‌آوری فایل‌های استاتیک (در صورت نیاز)

```bash
docker compose -f deploy/docker-compose.server.yml exec -T web python manage.py collectstatic --noinput
```

### 5️⃣ راه‌اندازی مجدد سرویس‌ها

```bash
docker compose -f deploy/docker-compose.server.yml up -d
```

---

## 🚀 دستورات کامل بروزرسانی (یکجا)

```bash
cd /root/vpn-backend
docker compose -f deploy/docker-compose.server.yml build --no-cache web
docker compose -f deploy/docker-compose.server.yml down
docker compose -f deploy/docker-compose.server.yml up -d
sleep 10
docker compose -f deploy/docker-compose.server.yml exec -T web python manage.py migrate --noinput
docker compose -f deploy/docker-compose.server.yml exec -T web python manage.py collectstatic --noinput
docker compose -f deploy/docker-compose.server.yml restart web
```

---

## 📦 دستورات استقرار اولیه

### ساخت Image و اجرای سرویس‌ها برای اولین بار

```bash
cd /root/vpn-backend
docker compose -f deploy/docker-compose.server.yml build --no-cache
docker compose -f deploy/docker-compose.server.yml up -d
```

### اجرای مایگریشن و ساخت کاربر ادمین

```bash
bash deploy/run-first-time.sh
```

یا دستی:

```bash
docker compose -f deploy/docker-compose.server.yml exec -T web python manage.py migrate --noinput
docker compose -f deploy/docker-compose.server.yml exec -T web python manage.py createsuperuser
```

---

## 🔧 دستورات مفید

### مشاهده لاگ‌ها

```bash
docker compose -f deploy/docker-compose.server.yml logs -f web
```

### مشاهده لاگ‌های همه سرویس‌ها

```bash
docker compose -f deploy/docker-compose.server.yml logs -f
```

### ورود به شل کانتینر web

```bash
docker compose -f deploy/docker-compose.server.yml exec web bash
```

### راه‌اندازی مجدد سرویس web

```bash
docker compose -f deploy/docker-compose.server.yml restart web
```

### توقف کامل سرویس‌ها

```bash
docker compose -f deploy/docker-compose.server.yml down
```

### مشاهده وضعیت سرویس‌ها

```bash
docker compose -f deploy/docker-compose.server.yml ps
```

### مشاهده استفاده از منابع

```bash
docker compose -f deploy/docker-compose.server.yml stats
```

---

## 🗄️ دستورات پایگاه داده

### پشتیبان‌گیری از پایگاه داده

```bash
docker compose -f deploy/docker-compose.server.yml exec -T db pg_dump -U vpn vpndb > backup_$(date +%Y%m%d_%H%M%S).sql
```

### بازگردانی پایگاه داده

```bash
docker compose -f deploy/docker-compose.server.yml exec -T db psql -U vpn vpndb < backup_file.sql
```

### مشاهده جداول پایگاه داده

```bash
docker compose -f deploy/docker-compose.server.yml exec -T db psql -U vpn vpndb -c "\dt"
```

---

## 🔐 دستورات کاربر و پروفایل

### ساخت کاربر جدید از خط فرمان

```bash
docker compose -f deploy/docker-compose.server.yml exec -T web python manage.py createsuperuser
```

### ساخت کاربر عادی (از Django shell)

```bash
docker compose -f deploy/docker-compose.server.yml exec -T web python manage.py shell
```

سپس در shell:

```python
from django.contrib.auth import get_user_model
from accounts.models import UserProfile

User = get_user_model()
user = User.objects.create_user(username='testuser', password='testpass123')
profile, created = UserProfile.objects.get_or_create(
    user=user,
    defaults={'account_type': 'free', 'subscription_status': 'none', 'can_access_pro_servers': False}
)
print(f"User created: {user.username}, Profile: {profile.account_type}")
```

---

## 📝 نکات مهم

1. **قبل از بروزرسانی**: همیشه از پایگاه داده پشتیبان بگیرید.
2. **بعد از بروزرسانی**: لاگ‌ها را بررسی کنید تا مطمئن شوید همه چیز درست کار می‌کند.
3. **تغییرات در .env**: بعد از تغییر `.env` باید سرویس web را restart کنید:
   ```bash
   docker compose -f deploy/docker-compose.server.yml restart web
   ```
4. **مایگریشن‌های جدید**: همیشه بعد از pull کردن کد جدید، مایگریشن‌ها را اجرا کنید.
5. **فایل‌های رسانه**: اگر فایل‌های رسانه (مثل receipt_image) را تغییر داده‌اید، مطمئن شوید volume درست mount شده است.

---

## ⚠️ عیب‌یابی

### اگر سرویس web بالا نمی‌آید

```bash
docker compose -f deploy/docker-compose.server.yml logs web
```

### اگر مایگریشن خطا می‌دهد

```bash
docker compose -f deploy/docker-compose.server.yml exec web python manage.py showmigrations
```

### پاک کردن همه چیز و شروع مجدد (⚠️ خطرناک!)

```bash
docker compose -f deploy/docker-compose.server.yml down -v
docker compose -f deploy/docker-compose.server.yml build --no-cache
docker compose -f deploy/docker-compose.server.yml up -d
```

---

## 📞 تست API

### تست لاگین

```bash
curl -X POST http://localhost:8000/api/auth/login/ \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}'
```

### تست لیست سرورها (نیاز به توکن)

```bash
TOKEN="your-token-here"
curl -X GET http://localhost:8000/api/servers/ \
  -H "Authorization: Token $TOKEN"
```

### تست پروفایل

```bash
TOKEN="your-token-here"
curl -X GET http://localhost:8000/api/profile/ \
  -H "Authorization: Token $TOKEN"
```
