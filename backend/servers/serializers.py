"""
Serializers for Server API. Config is encrypted before response.
"""
from django.conf import settings
from rest_framework import serializers

from servers.models import Server
from utils.encryption import encrypt_plaintext


def can_use_server(user, server):
    """سطح دسترسی: سوپرکاربر همه؛ سرور رایگان همه؛ سرور حرفه‌ای فقط کاربر Pro."""
    if user.is_superuser:
        return True
    if server.server_type == Server.SERVER_FREE:
        return True
    if server.server_type == Server.SERVER_PRO and hasattr(user, "userprofile"):
        return user.userprofile.can_use_pro_servers()
    return False


class ServerListSerializer(serializers.ModelSerializer):
    """لیست سرورها: id، name، country، flag، config رمز، status، server_type، can_use."""

    id = serializers.SerializerMethodField()
    flag = serializers.CharField(source="flag_emoji", read_only=True)
    config = serializers.SerializerMethodField()
    status = serializers.SerializerMethodField()
    server_type = serializers.CharField(read_only=True)
    can_use = serializers.SerializerMethodField()

    class Meta:
        model = Server
        fields = ["id", "name", "country", "flag", "config", "status", "server_type", "can_use"]

    def get_id(self, obj: Server) -> str:
        return str(obj.pk)

    def get_config(self, obj: Server) -> str:
        key = getattr(settings, "CONFIG_ENCRYPTION_KEY", None) or ""
        if isinstance(key, str):
            key = key.encode("utf-8")[:32].ljust(32, b"\0")
        return encrypt_plaintext(obj.config_vless, key)

    def get_status(self, obj: Server) -> str:
        return "online" if obj.is_active else "offline"

    def get_can_use(self, obj: Server) -> bool:
        request = self.context.get("request")
        if not request or not request.user.is_authenticated:
            return False
        return can_use_server(request.user, obj)
