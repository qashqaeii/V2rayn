# Add server_type to Server

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ("servers", "0003_populate_appsetting_defaults"),
    ]

    operations = [
        migrations.AddField(
            model_name="server",
            name="server_type",
            field=models.CharField(
                choices=[("free", "رایگان"), ("pro", "حرفه‌ای")],
                default="free",
                help_text="سرور رایگان برای همه؛ سرور حرفه‌ای فقط برای کاربران Pro.",
                max_length=16,
            ),
        ),
    ]
