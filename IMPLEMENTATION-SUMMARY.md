# خلاصه پیاده‌سازی سیستم چندکاربره و اشتراک‌محور

## ✅ کارهای انجام شده

### Backend (Django)

1. **اپ `accounts`** با مدل‌های:
   - `UserProfile`: پروفایل کاربر با account_type (free/pro) و subscription_status
   - `SubscriptionOrder`: سفارش خرید اشتراک با receipt_image و payment_tracking_code

2. **فیلد `server_type`** به مدل `Server` اضافه شد (free/pro)

3. **سیگنال** برای ایجاد پروفایل رایگان برای کاربران جدید

4. **API endpoints**:
   - `POST /api/auth/login/` - لاگین کاربر
   - `GET /api/profile/` - پروفایل کاربر
   - `GET /api/servers/` - لیست سرورها با `can_use` بر اساس نوع کاربر
   - `GET /api/orders/` - لیست سفارش‌های کاربر
   - `POST /api/orders/` - ثبت سفارش جدید

5. **Middleware** برای بررسی انقضای اشتراک

6. **Admin** برای تایید/رد سفارش‌ها و تبدیل کاربر به Pro

### Android

1. **DTOها**:
   - `LoginResponse`, `UserDto`, `UserProfileDto`
   - `SubscriptionOrderDto`
   - `ApiServerDto` با فیلدهای `server_type` و `can_use`

2. **API Repository**:
   - اضافه کردن توکن به header درخواست‌ها
   - متدهای لاگین، پروفایل و سفارش

3. **MmkvManager**:
   - ذخیره/بازیابی توکن
   - ذخیره/بازیابی پروفایل کاربر
   - متدهای logout

4. **ServerAffiliationInfo**:
   - فیلدهای `serverType` و `canUse` اضافه شد

5. **UI**:
   - `LoginActivity` - صفحه لاگین
   - `ProfileActivity` - صفحه پروفایل و خرید اشتراک
   - `MainActivity` - بررسی لاگین قبل از نمایش سرورها
   - `MainRecyclerAdapter` - نمایش سرورهای قفل شده با alpha و غیرفعال کردن کلیک

---

## 📝 فایل‌های باقی‌مانده برای ایجاد

### Layout Files

1. **`activity_login.xml`**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:gravity="center">
    
    <EditText
        android:id="@+id/et_username"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="نام کاربری"
        android:inputType="text"/>
    
    <EditText
        android:id="@+id/et_password"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="رمز عبور"
        android:inputType="textPassword"/>
    
    <Button
        android:id="@+id/btn_login"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="ورود"/>
</LinearLayout>
```

2. **`activity_profile.xml`**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <!-- اطلاعات پروفایل -->
        <TextView android:text="نوع حساب:" />
        <TextView android:id="@+id/tv_account_type" />
        
        <TextView android:text="وضعیت اشتراک:" />
        <TextView android:id="@+id/tv_subscription_status" />
        
        <TextView android:text="دسترسی به سرورهای Pro:" />
        <TextView android:id="@+id/tv_can_access_pro" />
        
        <TextView android:text="تاریخ انقضا:" />
        <TextView android:id="@+id/tv_subscription_end" />
        
        <Button
            android:id="@+id/btn_upgrade"
            android:text="ارتقا به Pro"/>
        
        <!-- فرم خرید اشتراک -->
        <LinearLayout
            android:id="@+id/layout_upgrade"
            android:orientation="vertical"
            android:visibility="gone">
            
            <EditText
                android:id="@+id/et_tracking_code"
                android:hint="کد پیگیری پرداخت"/>
            
            <Button
                android:id="@+id/btn_select_receipt"
                android:text="انتخاب فیش"/>
            
            <ImageView
                android:id="@+id/iv_receipt"
                android:visibility="gone"/>
            
            <Button
                android:id="@+id/btn_submit_order"
                android:text="ثبت سفارش"/>
        </LinearLayout>
        
        <!-- لیست سفارش‌ها -->
        <TextView android:text="سفارش‌های من"/>
        <TextView
            android:id="@+id/tv_orders_empty"
            android:text="هیچ سفارشی ثبت نشده است"/>
        
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recycler_orders"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"/>
        
        <Button
            android:id="@+id/btn_logout"
            android:text="خروج"/>
    </LinearLayout>
</ScrollView>
```

### String Resources

افزودن به `res/values/strings.xml`:

```xml
<string name="login">ورود</string>
<string name="username_required">نام کاربری الزامی است</string>
<string name="password_required">رمز عبور الزامی است</string>
<string name="login_success">ورود موفق</string>
<string name="login_failed">ورود ناموفق</string>
<string name="profile">پروفایل</string>
<string name="yes">بله</string>
<string name="no">خیر</string>
<string name="not_applicable">-</string>
<string name="failed_to_load_profile">خطا در بارگذاری پروفایل</string>
<string name="tracking_code_required">کد پیگیری الزامی است</string>
<string name="receipt_image_required">تصویر فیش الزامی است</string>
<string name="order_submitted">سفارش ثبت شد</string>
<string name="order_submit_failed">خطا در ثبت سفارش</string>
<string name="pro_server_locked">این سرور فقط برای کاربران Pro در دسترس است</string>
```

### Menu Resources

افزودن به `res/menu/menu_main_locked.xml`:

```xml
<item
    android:id="@+id/profile"
    android:title="پروفایل"
    android:icon="@android:drawable/ic_menu_manage"/>
```

### AndroidManifest.xml

افزودن Activityها:

```xml
<activity
    android:name=".ui.LoginActivity"
    android:exported="true"
    android:launchMode="singleTop">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<activity
    android:name=".ui.ProfileActivity"
    android:exported="false"/>
```

### تصحیح ProfileActivity

در `ProfileActivity.kt`، متد `submitOrder` نیاز به تصحیح دارد:

```kotlin
private fun submitOrder() {
    val trackingCode = binding.etTrackingCode.text.toString().trim()
    if (trackingCode.isEmpty()) {
        binding.etTrackingCode.error = getString(R.string.tracking_code_required)
        return
    }

    if (selectedImageUri == null) {
        Toast.makeText(this, getString(R.string.receipt_image_required), Toast.LENGTH_SHORT).show()
        return
    }

    binding.btnSubmitOrder.isEnabled = false
    showLoading()

    lifecycleScope.launch {
        try {
            // خواندن فایل از URI
            val inputStream = contentResolver.openInputStream(selectedImageUri!!)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            
            val tempFile = File(cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
            tempFile.writeBytes(bytes ?: ByteArray(0))
            
            repository.createOrder(trackingCode, tempFile)
            Toast.makeText(this@ProfileActivity, getString(R.string.order_submitted), Toast.LENGTH_SHORT).show()
            binding.layoutUpgrade.visibility = View.GONE
            binding.etTrackingCode.text.clear()
            selectedImageUri = null
            binding.ivReceipt.visibility = View.GONE
            loadOrders()
        } catch (e: Exception) {
            Toast.makeText(this@ProfileActivity, getString(R.string.order_submit_failed) + ": " + e.message, Toast.LENGTH_LONG).show()
        } finally {
            binding.btnSubmitOrder.isEnabled = true
            hideLoading()
        }
    }
}
```

---

## 🔧 تنظیمات لازم

1. **Backend**: اجرای مایگریشن‌ها:
```bash
python manage.py migrate
```

2. **Backend**: ساخت کاربر ادمین:
```bash
python manage.py createsuperuser
```

3. **Backend**: تنظیم `server_type` برای سرورهای موجود در Django Admin

4. **Android**: اطمینان از اینکه `BuildConfig.VPN_API_BASE_URL` به درستی تنظیم شده است

---

## 📋 تست

1. لاگین با کاربر جدید (باید به صورت رایگان ایجاد شود)
2. مشاهده سرورهای رایگان و Pro
3. ثبت سفارش خرید اشتراک
4. تایید سفارش در Django Admin
5. بررسی تبدیل کاربر به Pro و دسترسی به سرورهای Pro

---

## ⚠️ نکات مهم

1. همه درخواست‌های API نیاز به توکن دارند (به جز لاگین)
2. سرورهای Pro برای کاربران رایگان نمایش داده می‌شوند اما غیرفعال هستند
3. بعد از تایید سفارش توسط ادمین، کاربر باید اپ را restart کند یا پروفایل را refresh کند
4. انقضای اشتراک به صورت خودکار در middleware بررسی می‌شود
