"""
API: لاگین، پروفایل، سفارش اشتراک.
"""
from rest_framework import status
from rest_framework.authtoken.models import Token
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from accounts.models import SubscriptionOrder, SubscriptionPlan, UserProfile
from accounts.serializers import (
    LoginSerializer,
    RegisterSerializer,
    SubscriptionPlanSerializer,
    UserWithProfileSerializer,
    UserProfileSerializer,
    SubscriptionOrderSerializer,
)


class RegisterView(APIView):
    """POST /api/auth/register/ — ثبت‌نام کاربر جدید."""

    permission_classes = [AllowAny]

    def post(self, request):
        serializer = RegisterSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        user = serializer.save()
        # ایجاد پروفایل رایگان برای کاربر جدید
        profile, _ = UserProfile.objects.get_or_create(
            user=user,
            defaults={
                "account_type": UserProfile.ACCOUNT_FREE,
                "subscription_status": UserProfile.SUBSCRIPTION_NONE,
                "can_access_pro_servers": False,
            },
        )
        token, _ = Token.objects.get_or_create(user=user)
        user_serializer = UserWithProfileSerializer(user)
        return Response(
            {
                "token": token.key,
                "user": user_serializer.data,
            },
            status=status.HTTP_201_CREATED,
        )


class LoginView(APIView):
    """POST /api/auth/login/ — نام کاربری و رمز عبور؛ برمی‌گرداند: token، user و profile."""

    permission_classes = [AllowAny]

    def post(self, request):
        serializer = LoginSerializer(data=request.data, context={"request": request})
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        user = serializer.validated_data["user"]
        profile, _ = UserProfile.objects.get_or_create(
            user=user,
            defaults={
                "account_type": UserProfile.ACCOUNT_FREE,
                "subscription_status": UserProfile.SUBSCRIPTION_NONE,
                "can_access_pro_servers": False,
            },
        )
        token, _ = Token.objects.get_or_create(user=user)
        user_serializer = UserWithProfileSerializer(user)
        return Response(
            {
                "token": token.key,
                "user": user_serializer.data,
            },
            status=status.HTTP_200_OK,
        )


class ProfileView(APIView):
    """GET /api/profile/ — پروفایل کاربر جاری."""

    permission_classes = [IsAuthenticated]

    def get(self, request):
        profile, _ = UserProfile.objects.get_or_create(
            user=request.user,
            defaults={
                "account_type": UserProfile.ACCOUNT_FREE,
                "subscription_status": UserProfile.SUBSCRIPTION_NONE,
                "can_access_pro_servers": False,
            },
        )
        return Response(UserProfileSerializer(profile).data, status=status.HTTP_200_OK)


class SubscriptionOrderListCreateView(APIView):
    """GET /api/orders/ — لیست سفارش‌های من. POST /api/orders/ — ثبت سفارش جدید (فیش + کد پیگیری)."""

    permission_classes = [IsAuthenticated]

    def get(self, request):
        orders = SubscriptionOrder.objects.filter(user=request.user).order_by("-created_at")
        return Response(
            SubscriptionOrderSerializer(orders, many=True, context={"request": request}).data,
            status=status.HTTP_200_OK,
        )

    def post(self, request):
        serializer = SubscriptionOrderSerializer(
            data=request.data,
            context={"request": request},
            partial=True,
        )
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        serializer.save()
        return Response(serializer.data, status=status.HTTP_201_CREATED)


class SubscriptionPlanView(APIView):
    """GET /api/plans/ — لیست پلن‌های فعال اشتراک."""

    permission_classes = [IsAuthenticated]

    def get(self, request):
        plans = SubscriptionPlan.objects.filter(is_active=True)
        return Response(
            SubscriptionPlanSerializer(plans, many=True).data,
            status=status.HTTP_200_OK,
        )
