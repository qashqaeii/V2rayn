# Generated migration for UserProfile and SubscriptionOrder

import django.db.models.deletion
from django.conf import settings
from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.CreateModel(
            name="UserProfile",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                (
                    "account_type",
                    models.CharField(
                        choices=[("free", "رایگان"), ("pro", "حرفه‌ای")],
                        default="free",
                        max_length=16,
                    ),
                ),
                (
                    "subscription_status",
                    models.CharField(
                        choices=[
                            ("none", "بدون اشتراک"),
                            ("pending_payment", "در انتظار پرداخت"),
                            ("active", "فعال"),
                            ("expired", "منقضی"),
                        ],
                        default="none",
                        max_length=24,
                    ),
                ),
                ("subscription_start_date", models.DateTimeField(blank=True, null=True)),
                ("subscription_end_date", models.DateTimeField(blank=True, null=True)),
                ("can_access_pro_servers", models.BooleanField(default=False)),
                ("created_at", models.DateTimeField(auto_now_add=True)),
                (
                    "user",
                    models.OneToOneField(
                        on_delete=django.db.models.deletion.CASCADE,
                        related_name="userprofile",
                        to=settings.AUTH_USER_MODEL,
                    ),
                ),
            ],
            options={
                "verbose_name": "پروفایل کاربر",
                "verbose_name_plural": "پروفایل‌های کاربران",
            },
        ),
        migrations.CreateModel(
            name="SubscriptionOrder",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                (
                    "receipt_image",
                    models.ImageField(blank=True, null=True, upload_to="subscription_receipts/%Y/%m/"),
                ),
                ("payment_tracking_code", models.CharField(blank=True, max_length=128)),
                (
                    "plan_type",
                    models.CharField(choices=[("pro", "حرفه‌ای")], default="pro", max_length=16),
                ),
                (
                    "status",
                    models.CharField(
                        choices=[
                            ("pending", "در انتظار تایید"),
                            ("approved", "تایید شده"),
                            ("rejected", "رد شده"),
                        ],
                        default="pending",
                        max_length=16,
                    ),
                ),
                ("created_at", models.DateTimeField(auto_now_add=True)),
                ("approved_at", models.DateTimeField(blank=True, null=True)),
                (
                    "approved_by",
                    models.ForeignKey(
                        blank=True,
                        null=True,
                        on_delete=django.db.models.deletion.SET_NULL,
                        related_name="approved_orders",
                        to=settings.AUTH_USER_MODEL,
                    ),
                ),
                (
                    "user",
                    models.ForeignKey(
                        on_delete=django.db.models.deletion.CASCADE,
                        related_name="subscription_orders",
                        to=settings.AUTH_USER_MODEL,
                    ),
                ),
            ],
            options={
                "ordering": ["-created_at"],
                "verbose_name": "سفارش اشتراک",
                "verbose_name_plural": "سفارش‌های اشتراک",
            },
        ),
    ]
