"""
API: list active servers; app config (settings) for admin-managed defaults.
"""
from django.utils.decorators import method_decorator
from django.views.decorators.cache import cache_page
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from servers.models import AppSetting, Server
from servers.serializers import ServerListSerializer

CACHE_TTL = 60  # seconds


class ServerListView(APIView):
    """GET /api/servers/ — active servers only, encrypted config, sorted by priority."""

    throttle_scope = "anon"

    @method_decorator(cache_page(CACHE_TTL))
    def get(self, request):
        qs = Server.objects.filter(is_active=True).order_by("-priority", "id")
        serializer = ServerListSerializer(qs, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class AppConfigView(APIView):
    """GET /api/config/ — key-value app settings. Admin-managed; app applies and does not show UI."""

    throttle_scope = "anon"

    @method_decorator(cache_page(30))
    def get(self, request):
        qs = AppSetting.objects.all().order_by("key")
        data = {obj.key: obj.value for obj in qs}
        return Response(data, status=status.HTTP_200_OK)
