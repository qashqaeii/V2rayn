"""
بررسی انقضای اشتراک: در هر درخواست کاربر احراز هویت‌شده، در صورت انقضا پروفایل را به رایگان برمی‌گرداند.
برای کاربران قدیمی بدون پروفایل، پروفایل رایگان ایجاد می‌شود.
"""
from django.utils import timezone

from accounts.models import UserProfile


class SubscriptionExpiryMiddleware:
    """اگر subscription_end_date گذشته باشد، کاربر را به رایگان و وضعیت منقضی برمی‌گرداند."""

    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        if request.user.is_authenticated:
            profile, _ = UserProfile.objects.get_or_create(
                user=request.user,
                defaults={
                    "account_type": UserProfile.ACCOUNT_FREE,
                    "subscription_status": UserProfile.SUBSCRIPTION_NONE,
                    "can_access_pro_servers": False,
                },
            )
            if (
                profile.account_type == UserProfile.ACCOUNT_PRO
                and profile.subscription_end_date
                and profile.subscription_end_date < timezone.now()
            ):
                profile.account_type = UserProfile.ACCOUNT_FREE
                profile.subscription_status = UserProfile.SUBSCRIPTION_EXPIRED
                profile.can_access_pro_servers = False
                profile.save(update_fields=["account_type", "subscription_status", "can_access_pro_servers"])
        return self.get_response(request)
