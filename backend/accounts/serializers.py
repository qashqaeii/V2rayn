"""
سریالایزرهای احراز هویت، پروفایل و سفارش اشتراک.
"""
from rest_framework import serializers
from django.contrib.auth import get_user_model
from django.contrib.auth import authenticate

from accounts.models import SubscriptionOrder, UserProfile

User = get_user_model()


class LoginSerializer(serializers.Serializer):
    username = serializers.CharField()
    password = serializers.CharField(style={"input_type": "password"}, write_only=True)

    def validate(self, attrs):
        user = authenticate(
            request=self.context.get("request"),
            username=attrs.get("username"),
            password=attrs.get("password"),
        )
        if not user:
            raise serializers.ValidationError("نام کاربری یا رمز عبور اشتباه است.")
        attrs["user"] = user
        return attrs


class UserProfileSerializer(serializers.ModelSerializer):
    account_type_display = serializers.CharField(source="get_account_type_display", read_only=True)
    subscription_status_display = serializers.CharField(source="get_subscription_status_display", read_only=True)

    class Meta:
        model = UserProfile
        fields = [
            "account_type",
            "account_type_display",
            "subscription_status",
            "subscription_status_display",
            "subscription_start_date",
            "subscription_end_date",
            "can_access_pro_servers",
            "created_at",
        ]
        read_only_fields = fields


class UserWithProfileSerializer(serializers.ModelSerializer):
    profile = UserProfileSerializer(read_only=True)

    class Meta:
        model = User
        fields = ["id", "username", "profile"]


class SubscriptionOrderSerializer(serializers.ModelSerializer):
    status_display = serializers.CharField(source="get_status_display", read_only=True)
    plan_type_display = serializers.CharField(source="get_plan_type_display", read_only=True)
    receipt_image_url = serializers.SerializerMethodField()

    class Meta:
        model = SubscriptionOrder
        fields = [
            "id",
            "plan_type",
            "plan_type_display",
            "payment_tracking_code",
            "receipt_image",
            "receipt_image_url",
            "status",
            "status_display",
            "created_at",
            "approved_at",
        ]
        read_only_fields = ["status", "approved_at"]

    def get_receipt_image_url(self, obj):
        if obj.receipt_image:
            request = self.context.get("request")
            if request:
                return request.build_absolute_uri(obj.receipt_image.url)
            return obj.receipt_image.url
        return None

    def create(self, validated_data):
        validated_data["user"] = self.context["request"].user
        return super().create(validated_data)
