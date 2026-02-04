"""
Admin: add/edit servers, toggle active, set priority; manage app settings.
"""
from django.contrib import admin
from django.utils.html import format_html

from servers.models import AppSetting, Server


@admin.register(AppSetting)
class AppSettingAdmin(admin.ModelAdmin):
    list_display = ["key", "value_short"]
    search_fields = ["key"]
    ordering = ["key"]

    def value_short(self, obj):
        return (obj.value or "")[:60] + ("..." if len(obj.value or "") > 60 else "")

    value_short.short_description = "Value"


@admin.register(Server)
class ServerAdmin(admin.ModelAdmin):
    list_display = ["name", "country", "flag_display", "is_active", "priority", "created_at"]
    list_editable = ["is_active", "priority"]
    list_filter = ["is_active", "country"]
    search_fields = ["name", "country"]
    ordering = ["-priority", "id"]
    readonly_fields = ["created_at"]

    def flag_display(self, obj):
        return format_html('<span style="font-size:1.2em">{}</span>', obj.flag_emoji or "")

    flag_display.short_description = "Flag"
