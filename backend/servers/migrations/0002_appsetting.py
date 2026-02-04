# Generated migration for AppSetting model

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ("servers", "0001_initial"),
    ]

    operations = [
        migrations.CreateModel(
            name="AppSetting",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                ("key", models.CharField(db_index=True, max_length=128, unique=True)),
                ("value", models.TextField(blank=True)),
            ],
            options={
                "ordering": ["key"],
                "verbose_name": "App setting",
                "verbose_name_plural": "App settings",
            },
        ),
    ]
