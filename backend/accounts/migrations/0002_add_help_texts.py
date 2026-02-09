# Migration: add help_text to UserProfile.can_access_pro_servers and SubscriptionOrder.receipt_image

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ("accounts", "0001_initial"),
    ]

    operations = [
        migrations.AlterField(
            model_name="userprofile",
            name="can_access_pro_servers",
            field=models.BooleanField(
                default=False,
                help_text="بر اساس account_type و انقضای اشتراک به‌صورت خودکار تنظیم می‌شود.",
            ),
        ),
        migrations.AlterField(
            model_name="subscriptionorder",
            name="receipt_image",
            field=models.ImageField(
                blank=True,
                null=True,
                upload_to="subscription_receipts/%Y/%m/",
                help_text="تصویر فیش واریز",
            ),
        ),
    ]
