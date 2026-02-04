# راه‌اندازی و بیلد اپلیکیشن (Locked VPN Client)

این پروژه یک **فورک قفل‌شده** از v2rayNG است: سرورها فقط از API شما لود می‌شوند و کاربر نمی‌تواند کانفیگ اضافه/ویرایش/حذف کند.

---

## ۱. بیلد APK روی GitHub Actions

### پیش‌نیاز
- ریپو را روی GitHub خودتان push کنید.
- **تنظیمات API** از فایل **`V2rayNG/.env`** خوانده می‌شود؛ نیازی به قرار دادن آن‌ها در Secrets گیت‌هاب نیست. قبل از push این مقادیر را تنظیم کنید:
  - `VPN_API_BASE_URL`: آدرس پایه API (مثلاً `http://77.110.116.139:8000/api/` برای بک‌اند جنگوی همین ریپو در پوشهٔ `backend`)
  - `CONFIG_ENCRYPTION_KEY`: کلید رمزگشایی کانفیگ (دقیقاً همان مقدار `CONFIG_ENCRYPTION_KEY` در `backend/.env`؛ ۳۲ کاراکتر)
  - اختیاری: `VPN_API_HOST`, `VPN_API_CERT_PINS`
- در **Settings → Secrets and variables → Actions** فقط این Secretها را برای **امضای APK** اضافه کنید:

| Secret | توضیح |
|--------|--------|
| `APP_KEYSTORE_BASE64` | فایل keystore به صورت Base64 |
| `APP_KEYSTORE_PASSWORD` | پسورد keystore |
| `APP_KEYSTORE_ALIAS` | alias کلید |
| `APP_KEY_PASSWORD` | پسورد کلید |

### بیلد
- با هر **push به برنچ `master`** یا از طریق **Actions → Build APK → Run workflow** بیلد اجرا می‌شود.
- **اگر چهار Secret بالا را تنظیم نکرده باشید:** بیلد به‌صورت **Debug** انجام می‌شود و APKهای قابل نصب از تب **Artifacts** قابل دانلود هستند (مناسب تست).
- **اگر Secretهای keystore را اضافه کرده باشید:** بیلد **Release امضا‌شده** انجام می‌شود (مناسب انتشار).
- خروجی APKها در هر صورت در تب **Artifacts** همان ران قابل دانلود است.

---

## ۲. سرور و پنل API

اپ فقط از یک endpoint سرورها را می‌گیرد؛ پنل و دیتابیس سرورها روی **سرور خودتان** است.

### انتظار اپ از API
- **متد:** `GET`
- **آدرس:** `{VPN_API_BASE_URL}servers/`  
  مثلاً اگر `VPN_API_BASE_URL = http://77.110.116.139:8000/api/` باشد، درخواست به `http://77.110.116.139:8000/api/servers/` زده می‌شود (هماهنگ با بک‌اند پوشهٔ `backend`).

### فرمت پاسخ (JSON)
```json
[
  {
    "id": "srv1",
    "name": "Germany #1",
    "flag": "de",
    "config": "vless://uuid@host:port?params#Germany"
  }
]
```

- `id`: شناسه یکتا
- `name`: نام نمایشی در اپ
- `flag`: کد دوحرفی کشور (مثل `de`, `us`) برای نمایش پرچم؛ یا یک ایموجی پرچم
- `config`: لینک VLESS/VMess یا **متن رمزنگاری‌شده** (در بک‌اند این ریپو با AES-256-CBC رمزنگاری می‌شود و اپ با `CONFIG_ENCRYPTION_KEY` رمزگشایی می‌کند؛ هیچ‌وقت در UI یا لاگ نمایش داده نمی‌شود)

### نصب API روی سرور شما
- سرور، پنل و دیتابیس را **خودتان** روی سرور موردنظر راه‌اندازی کنید.
- **هرگز** پسورد یا اطلاعات حساس سرور را داخل ریپو یا داخل اپ commit نکنید.
- برای اتصال اپ به API در محیط واقعی از **HTTPS** استفاده کنید و در `.env` آدرس پایه را با `https://` قرار دهید.

**بک‌اند جنگو همین ریپو:**  
پوشهٔ **`backend`** یک بک‌اند Django آماده دارد که با اپ هماهنگ است: API در `GET /api/servers/`، کانفیگ در پاسخ رمزنگاری می‌شود و اپ با `CONFIG_ENCRYPTION_KEY` رمزگشایی می‌کند. سرورها در **ادمین** (`http://77.110.116.139:8000/admin/servers/server/`) مدیریت می‌شوند. جزئیات در **[docs/DJANGO-VPN-API.md](docs/DJANGO-VPN-API.md)**.

در پوشهٔ `api-example` نیز یک نمونهٔ ساده با Node.js و فایل JSON قرار داده شده که می‌توانید در صورت عدم استفاده از جنگو از آن الگو بگیرید.

---

## ۳. بیلد لوکال (اختیاری)

برای تست روی ماشین خودتان:

```bash
cd V2rayNG
# آدرس API از فایل .env خوانده می‌شود؛ در صورت نیاز آن را ویرایش کنید.
./gradlew assemblePlaystoreRelease
```

APK در مسیری شبیه  
`app/build/outputs/apk/playstore/release/`  
ساخته می‌شود.

---

## ۴. خلاصه جریان داده

```
API (GET /vpn/servers) → اپ (VpnServersRepository)
  → ApiServersImporter (تبدیل با VlessFmt/VmessFmt)
  → MmkvManager (ذخیره فقط سرورهای API)
  → UI (لیست سرورها + Connect/Disconnect)
  → V2RayVpnService
```

---

## ۵. استفاده از سرور اختصاصی برای پنل/API

- پنل و API را روی **سرور خودتان** (مثلاً یک VPS) نصب و اجرا کنید.
- دسترسی به سرور (SSH و غیره) را فقط با رمز/کلید امن نگه دارید و **هیچ‌وقت** پسورد یا IP را در ریپو یا در کد اپ قرار ندهید.
- برای اپ: آدرس **عمومی API** (ترجیحاً با HTTPS و دامنه) را در فایل **`V2rayNG/.env`** قرار دهید، مثلاً:
  - `VPN_API_BASE_URL=https://api.yourdomain.com/vpn/`
  - اگر API را روی IP مستقیم سرو می‌کنید، در محیط واقعی جلوی آن یک reverse proxy با HTTPS (مثلاً nginx + Let's Encrypt) بگذارید و همان آدرس HTTPS را در `.env` استفاده کنید.

با این تنظیمات، پروژه آماده push به GitHub و بیلد خودکار APK است؛ فقط Secretها و API روی سرور خودتان را مطابق بالا تنظیم کنید.
