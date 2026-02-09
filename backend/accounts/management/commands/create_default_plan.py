"""
دستور Django برای ایجاد پلن پیش‌فرض Pro.
استفاده: python manage.py create_default_plan
"""
from django.core.management.base import BaseCommand
from accounts.models import SubscriptionPlan


class Command(BaseCommand):
    help = "ایجاد پلن پیش‌فرض Pro با قیمت و مدت اعتبار"

    def add_arguments(self, parser):
        parser.add_argument(
            "--price",
            type=float,
            default=100000.0,
            help="قیمت پلن به تومان (پیش‌فرض: 100000)",
        )
        parser.add_argument(
            "--days",
            type=int,
            default=30,
            help="مدت اعتبار به روز (پیش‌فرض: 30)",
        )

    def handle(self, *args, **options):
        price = options["price"]
        days = options["days"]

        plan, created = SubscriptionPlan.objects.get_or_create(
            plan_type=SubscriptionPlan.PLAN_PRO,
            defaults={
                "price": price,
                "duration_days": days,
                "is_active": True,
                "description": f"اشتراک حرفه‌ای {days} روزه",
            },
        )

        if created:
            self.stdout.write(
                self.style.SUCCESS(
                    f"پلن Pro با موفقیت ایجاد شد: قیمت {price:,.0f} تومان، مدت اعتبار {days} روز"
                )
            )
        else:
            plan.price = price
            plan.duration_days = days
            plan.is_active = True
            plan.description = f"اشتراک حرفه‌ای {days} روزه"
            plan.save()
            self.stdout.write(
                self.style.SUCCESS(
                    f"پلن Pro به‌روزرسانی شد: قیمت {price:,.0f} تومان، مدت اعتبار {days} روز"
                )
            )
