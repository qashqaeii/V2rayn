"""
API: list active servers (auth required); app config for admin-managed defaults.
"""
from rest_framework import status
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView
from django.utils.decorators import method_decorator
from django.views.decorators.cache import cache_page

from servers.models import AppSetting, Server
from servers.serializers import ServerListSerializer

CACHE_TTL = 60


class ServerListView(APIView):
    """GET /api/servers/ — سرورهای فعال با can_use بر اساس نوع کاربر؛ نیاز به احراز هویت."""

    permission_classes = [IsAuthenticated]
    throttle_scope = "user"

    def get(self, request):
        qs = Server.objects.filter(is_active=True).order_by("-priority", "id")
        serializer = ServerListSerializer(qs, many=True, context={"request": request})
        return Response(serializer.data, status=status.HTTP_200_OK)


class AppConfigView(APIView):
    """GET /api/config/ — key-value app settings. Admin-managed; app applies and does not show UI."""

    permission_classes = [IsAuthenticated]
    throttle_scope = "user"

    @method_decorator(cache_page(30))
    def get(self, request):
        qs = AppSetting.objects.all().order_by("key")
        data = {obj.key: obj.value for obj in qs}
        return Response(data, status=status.HTTP_200_OK)
