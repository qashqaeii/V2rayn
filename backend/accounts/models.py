"""
مدل‌های پروفایل کاربر، نوع اشتراک و سفارش خرید اشتراک (کارت به کارت).
"""
from django.conf import settings
from django.db import models


class UserProfile(models.Model):
    """پروفایل هر کاربر؛ نوع حساب و وضعیت اشتراک."""

    ACCOUNT_FREE = "free"
    ACCOUNT_PRO = "pro"
    ACCOUNT_CHOICES = [
        (ACCOUNT_FREE, "رایگان"),
        (ACCOUNT_PRO, "حرفه‌ای"),
    ]

    SUBSCRIPTION_NONE = "none"
    SUBSCRIPTION_PENDING = "pending_payment"
    SUBSCRIPTION_ACTIVE = "active"
    SUBSCRIPTION_EXPIRED = "expired"
    SUBSCRIPTION_CHOICES = [
        (SUBSCRIPTION_NONE, "بدون اشتراک"),
        (SUBSCRIPTION_PENDING, "در انتظار پرداخت"),
        (SUBSCRIPTION_ACTIVE, "فعال"),
        (SUBSCRIPTION_EXPIRED, "منقضی"),
    ]

    user = models.OneToOneField(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="userprofile",
    )
    account_type = models.CharField(
        max_length=16,
        choices=ACCOUNT_CHOICES,
        default=ACCOUNT_FREE,
    )
    subscription_status = models.CharField(
        max_length=24,
        choices=SUBSCRIPTION_CHOICES,
        default=SUBSCRIPTION_NONE,
    )
    subscription_start_date = models.DateTimeField(null=True, blank=True)
    subscription_end_date = models.DateTimeField(null=True, blank=True)
    can_access_pro_servers = models.BooleanField(
        default=False,
        help_text="بر اساس account_type و انقضای اشتراک به‌صورت خودکار تنظیم می‌شود.",
    )
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = "پروفایل کاربر"
        verbose_name_plural = "پروفایل‌های کاربران"

    def __str__(self):
        return f"{self.user.username} ({self.get_account_type_display()})"

    def can_use_pro_servers(self):
        """دسترسی به سرورهای حرفه‌ای فقط در صورت Pro و قبل از انقضا."""
        if self.account_type != self.ACCOUNT_PRO:
            return False
        from django.utils import timezone

        if self.subscription_end_date and self.subscription_end_date < timezone.now():
            return False
        return True


class SubscriptionPlan(models.Model):
    """پلن اشتراک: قیمت و مدت اعتبار."""

    PLAN_PRO = "pro"
    PLAN_CHOICES = [(PLAN_PRO, "حرفه‌ای")]

    plan_type = models.CharField(
        max_length=16,
        choices=PLAN_CHOICES,
        default=PLAN_PRO,
        unique=True,
    )
    price = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        default=0.00,
        help_text="قیمت به تومان",
    )
    duration_days = models.IntegerField(
        default=30,
        help_text="مدت اعتبار به روز",
    )
    is_active = models.BooleanField(
        default=True,
        help_text="آیا این پلن فعال است؟",
    )
    description = models.TextField(
        blank=True,
        help_text="توضیحات پلن",
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = "پلن اشتراک"
        verbose_name_plural = "پلن‌های اشتراک"

    def __str__(self):
        return f"{self.get_plan_type_display()} - {self.price} تومان - {self.duration_days} روز"


class SubscriptionOrder(models.Model):
    """سفارش خرید اشتراک (کارت به کارت)؛ تایید توسط ادمین."""

    PLAN_PRO = "pro"
    PLAN_CHOICES = [(PLAN_PRO, "حرفه‌ای")]

    STATUS_PENDING = "pending"
    STATUS_APPROVED = "approved"
    STATUS_REJECTED = "rejected"
    STATUS_CHOICES = [
        (STATUS_PENDING, "در انتظار تایید"),
        (STATUS_APPROVED, "تایید شده"),
        (STATUS_REJECTED, "رد شده"),
    ]

    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="subscription_orders",
    )
    receipt_image = models.ImageField(
        upload_to="subscription_receipts/%Y/%m/",
        blank=True,
        null=True,
        help_text="تصویر فیش واریز",
    )
    payment_tracking_code = models.CharField(max_length=128, blank=True)
    plan_type = models.CharField(
        max_length=16,
        choices=PLAN_CHOICES,
        default=PLAN_PRO,
    )
    status = models.CharField(
        max_length=16,
        choices=STATUS_CHOICES,
        default=STATUS_PENDING,
    )
    created_at = models.DateTimeField(auto_now_add=True)
    approved_at = models.DateTimeField(null=True, blank=True)
    approved_by = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="approved_orders",
    )
    plan = models.ForeignKey(
        "SubscriptionPlan",
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="orders",
        help_text="پلن انتخابی کاربر",
    )

    class Meta:
        ordering = ["-created_at"]
        verbose_name = "سفارش اشتراک"
        verbose_name_plural = "سفارش‌های اشتراک"

    def __str__(self):
        return f"{self.user.username} - {self.get_plan_type_display()} ({self.get_status_display()})"
