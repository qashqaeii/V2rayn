"""
ادمین: پروفایل کاربران، سفارش اشتراک با اکشن تایید/رد.
"""
from django.contrib import admin
from django.utils import timezone
from django.utils.html import format_html

from accounts.models import SubscriptionOrder, SubscriptionPlan, UserProfile


@admin.register(UserProfile)
class UserProfileAdmin(admin.ModelAdmin):
    list_display = [
        "user",
        "account_type",
        "subscription_status",
        "can_access_pro_servers",
        "subscription_end_date",
        "created_at",
    ]
    list_filter = ["account_type", "subscription_status"]
    search_fields = ["user__username", "user__email"]
    raw_id_fields = ["user"]
    readonly_fields = ["created_at"]

    def get_readonly_fields(self, request, obj=None):
        """فقط سوپرکاربر بتواند account_type و can_access_pro_servers را مستقیم عوض کند."""
        if request.user.is_superuser:
            return ["created_at"]
        return ["account_type", "can_access_pro_servers", "created_at"]


def approve_orders(modeladmin, request, queryset):
    """تایید سفارش: کاربر را Pro می‌کند و تاریخ اشتراک را تنظیم می‌کند."""
    from accounts.models import UserProfile
    from datetime import timedelta

    now = timezone.now()
    for order in queryset.filter(status=SubscriptionOrder.STATUS_PENDING):
        order.status = SubscriptionOrder.STATUS_APPROVED
        order.approved_at = now
        order.approved_by = request.user
        order.save(update_fields=["status", "approved_at", "approved_by"])

        profile = order.user.userprofile
        profile.account_type = UserProfile.ACCOUNT_PRO
        profile.subscription_status = UserProfile.SUBSCRIPTION_ACTIVE
        profile.subscription_start_date = now
        
        # استفاده از مدت اعتبار از پلن یا مقدار پیش‌فرض
        duration_days = order.plan.duration_days if order.plan else 30
        profile.subscription_end_date = now + timedelta(days=duration_days)
        profile.can_access_pro_servers = True
        profile.save(
            update_fields=[
                "account_type",
                "subscription_status",
                "subscription_start_date",
                "subscription_end_date",
                "can_access_pro_servers",
            ]
        )


def reject_orders(modeladmin, request, queryset):
    """رد سفارش."""
    queryset.filter(status=SubscriptionOrder.STATUS_PENDING).update(
        status=SubscriptionOrder.STATUS_REJECTED
    )


approve_orders.short_description = "تایید سفارش‌های انتخاب‌شده"
reject_orders.short_description = "رد سفارش‌های انتخاب‌شده"


@admin.register(SubscriptionPlan)
class SubscriptionPlanAdmin(admin.ModelAdmin):
    list_display = ["plan_type", "price", "duration_days", "is_active", "updated_at"]
    list_editable = ["price", "duration_days", "is_active"]
    search_fields = ["plan_type"]
    readonly_fields = ["created_at", "updated_at"]


@admin.register(SubscriptionOrder)
class SubscriptionOrderAdmin(admin.ModelAdmin):
    list_display = [
        "id",
        "user",
        "plan_type",
        "plan",
        "status_display",
        "payment_tracking_code",
        "created_at",
        "approved_at",
        "approved_by",
    ]
    list_filter = ["status", "plan_type"]
    search_fields = ["user__username", "payment_tracking_code"]
    raw_id_fields = ["user", "approved_by", "plan"]
    readonly_fields = ["created_at", "approved_at", "approved_by"]
    actions = [approve_orders, reject_orders]

    def status_display(self, obj):
        colors = {
            SubscriptionOrder.STATUS_PENDING: "orange",
            SubscriptionOrder.STATUS_APPROVED: "green",
            SubscriptionOrder.STATUS_REJECTED: "red",
        }
        color = colors.get(obj.status, "gray")
        return format_html(
            '<span style="color: {};">{}</span>',
            color,
            obj.get_status_display(),
        )

    status_display.short_description = "وضعیت"
