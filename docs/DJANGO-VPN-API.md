# API لیست سرورها با Django (هماهنگ با بک‌اند و اپ)

این سند دو بخش دارد: **(۱)** بک‌اند واقعی این ریپو (پوشهٔ `backend`) و **(۲)** راهنمای کلی برای تطبیق با اپ اندروید.

---

## ۱. بک‌اند این ریپو (`backend/`)

در این ریپو یک بک‌اند Django آماده وجود دارد که با اپ اندروید هماهنگ است.

### مسیر API
- **آدرس:** `GET /api/servers/`  
  مثال: `http://77.110.116.139:8000/api/servers/`
- در اپ، `VPN_API_BASE_URL` باید با `/` تمام شود و مسیر `servers/` به آن اضافه می‌شود؛ یعنی مقدار پیشنهادی:  
  `VPN_API_BASE_URL=http://77.110.116.139:8000/api/`

### مدل سرور (`servers/models.py`)
| فیلد | نوع | توضیح |
|------|-----|--------|
| `name` | CharField | نام نمایشی |
| `country` | CharField | کد کشور (مثل US، EN) |
| `flag_emoji` | CharField | پرچم (ایموجی یا کد دوحرفی) |
| `config_vless` | TextField | **لینک VLESS به‌صورت متن ساده** (فقط در دیتابیس؛ در API رمزنگاری می‌شود) |
| `is_active` | BooleanField | فقط سرورهای فعال در API برگردانده می‌شوند |
| `priority` | IntegerField | اولویت نمایش (عدد بالاتر = بالاتر در لیست) |
| `created_at` | DateTimeField | زمان ایجاد |

### فرمت پاسخ API
خروجی آرایهٔ JSON است؛ هر آیتم شامل:
- `id` (رشته، مثلاً `"1"`)
- `name`
- `country`
- `flag` (همان `flag_emoji`)
- `config` (**متن رمزنگاری‌شده با AES-256-CBC**؛ اپ با همان کلید رمزگشایی می‌کند)
- `status` (`"online"` / `"offline"`)

کانفیگ در API هرگز به‌صورت plain VLESS ارسال نمی‌شود.

### کلید رمزنگاری
- در **بک‌اند:** در `.env` متغیر `CONFIG_ENCRYPTION_KEY` (دقیقاً ۳۲ کاراکتر).
- در **اپ:** در `V2rayNG/.env` همان مقدار را در `CONFIG_ENCRYPTION_KEY` قرار دهید؛ در بیلد داخل `BuildConfig` رفته و اپ قبل از پارس کردن VLESS، با این کلید فیلد `config` را رمزگشایی می‌کند.

مثال (همان مقدار در هر دو طرف):  
`CONFIG_ENCRYPTION_KEY=vpnclient-aes256-key-32bytes!!!!`

### مدیریت سرورها
سرورها را در **پنل ادمین جنگو** ایجاد و ویرایش کنید:  
`http://77.110.116.139:8000/admin/servers/server/`

### تنظیمات اپ (App settings)
- **مدل:** `AppSetting` (key-value). مقادیر پیش‌فرض همان تنظیمات فعلی اپ (مثلاً از pref_settings.xml) هستند.
- **پنل ادمین:**  
  `http://77.110.116.139:8000/admin/servers/appsetting/`  
  در ادمین می‌توانید هر کلید (مثل `pref_speed_enabled`, `pref_mode`) را با مقدار دلخواه تنظیم کنید. کاربر در اپ **نمی‌تواند** تنظیمات را ببیند یا تغییر دهد؛ فقط مقادیر ارسالی از سرور اعمال می‌شوند.
- **API:** `GET /api/config/`  
  خروجی یک آبجکت JSON از کلید–مقدار است، مثلاً:  
  `{"pref_speed_enabled": "false", "pref_mode": "VPN", ...}`  
  اپ هنگام شروع (همراه با دریافت لیست سرورها) این endpoint را فراخوانی کرده و مقادیر را به‌صورت محلی اعمال می‌کند.

راهنمای اجرا و دیپلوی بک‌اند در `backend/README.md` و در صورت وجود `backend/deploy/` است.

---

## ۲. راهنمای کلی (سایر پروژه‌های Django)

اگر پروژهٔ Django جداگانه دارید و می‌خواهید با همین اپ صحبت کند، باید این موارد را رعایت کنید.

### انتظار اپ از API
- **متد و مسیر:** `GET {VPN_API_BASE_URL}servers` یا `GET {VPN_API_BASE_URL}servers/`  
  مثلاً اگر `VPN_API_BASE_URL = http://example.com/api/` باشد، درخواست به `http://example.com/api/servers/` زده می‌شود.
- **پاسخ:** آرایهٔ JSON. هر عنصر حداقل این فیلدها را داشته باشد:
  - `id` (رشته)
  - `name`
  - `flag` (اختیاری؛ کد کشور یا ایموجی پرچم)
  - `config` (رشتهٔ VLESS/VMess یا **متن رمزنگاری‌شده**؛ در صورت رمزنگاری، اپ با `CONFIG_ENCRYPTION_KEY` رمزگشایی می‌کند)

اگر `config` را رمزنگاری می‌کنید، فرمت باید با بک‌اند این ریپو یکسان باشد: **AES-256-CBC**، خروجی **Base64(IV_16_bytes + ciphertext)**، کلید ۳۲ بایت.

### مدل سرور (نمونه)
```python
class Server(models.Model):
    name = models.CharField(max_length=255)
    country = models.CharField(max_length=10)
    flag_emoji = models.CharField(max_length=10)  # یا flag
    config_vless = models.TextField()  # در API می‌توانید رمزنگاری کنید
    is_active = models.BooleanField(default=True)
    priority = models.IntegerField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ['-priority', 'id']
```

### امنیت
- در production از **HTTPS** استفاده کنید و در اپ آدرس پایه را با `https://` در `.env` قرار دهید.
- مقدار `CONFIG_ENCRYPTION_KEY` را در لاگ یا در ریپو commit نکنید.

---

## ۳. خلاصه

| مورد | در بک‌اند این ریپو | انتظار اپ |
|------|-------------------|------------|
| مسیر | `GET /api/servers/` | `VPN_API_BASE_URL` = `http://HOST:8000/api/` |
| فیلدها | `id`, `name`, `country`, `flag`, `config`, `status` | استفاده از `id`, `name`, `flag`, `config` |
| کانفیگ | رمزنگاری AES-256-CBC در API | رمزگشایی با `CONFIG_ENCRYPTION_KEY` قبل از پارس VLESS |

با این تنظیمات، اپ و بک‌اند این ریپو با یکدیگر هماهنگ هستند و می‌توانید سرورها را فقط از پنل ادمین مدیریت کنید.
