"""
سیگنال: با ایجاد هر کاربر جدید، پروفایل با نوع رایگان ساخته می‌شود.
"""
from django.contrib.auth import get_user_model
from django.db.models.signals import post_save
from django.dispatch import receiver

from accounts.models import UserProfile

User = get_user_model()


@receiver(post_save, sender=User)
def create_user_profile(sender, instance, created, **kwargs):
    """برای هر کاربر تازه‌ساخته‌شده یک پروفایل رایگان ایجاد کن."""
    if created:
        UserProfile.objects.get_or_create(
            user=instance,
            defaults={
                "account_type": UserProfile.ACCOUNT_FREE,
                "subscription_status": UserProfile.SUBSCRIPTION_NONE,
                "can_access_pro_servers": False,
            },
        )
