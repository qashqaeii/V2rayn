# Migration: SubscriptionPlan و فیلد plan در SubscriptionOrder

import django.db.models.deletion
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ("accounts", "0002_add_help_texts"),
    ]

    operations = [
        migrations.CreateModel(
            name="SubscriptionPlan",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                (
                    "plan_type",
                    models.CharField(
                        choices=[("pro", "حرفه‌ای")],
                        default="pro",
                        max_length=16,
                        unique=True,
                    ),
                ),
                ("price", models.DecimalField(decimal_places=2, default=0.0, max_digits=10)),
                ("duration_days", models.IntegerField(default=30)),
                ("is_active", models.BooleanField(default=True)),
                ("description", models.TextField(blank=True)),
                ("created_at", models.DateTimeField(auto_now_add=True)),
                ("updated_at", models.DateTimeField(auto_now=True)),
            ],
            options={
                "verbose_name": "پلن اشتراک",
                "verbose_name_plural": "پلن‌های اشتراک",
            },
        ),
        migrations.AddField(
            model_name="subscriptionorder",
            name="plan",
            field=models.ForeignKey(
                blank=True,
                null=True,
                on_delete=django.db.models.deletion.SET_NULL,
                related_name="orders",
                to="accounts.subscriptionplan",
            ),
        ),
    ]
