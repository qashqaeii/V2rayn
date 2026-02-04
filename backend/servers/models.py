"""
Server model: VLESS config, country, flag, active, priority.
Plain config is never exposed via API; only encrypted.

AppSetting: key-value store for app defaults. Managed only in Django admin;
app fetches via GET /api/config/ and applies (user cannot change in app).
"""
from django.db import models


class AppSetting(models.Model):
    """Singleton-style key-value for app preferences. Admin-only; app reads via API."""

    key = models.CharField(max_length=128, unique=True, db_index=True)
    value = models.TextField(blank=True)

    class Meta:
        ordering = ["key"]
        verbose_name = "App setting"
        verbose_name_plural = "App settings"

    def __str__(self):
        return f"{self.key}={self.value[:50]}..."


class Server(models.Model):
    """VPN server entry. config_vless is stored plain in DB, encrypted in API response."""

    name = models.CharField(max_length=100)
    country = models.CharField(max_length=5)
    flag_emoji = models.CharField(max_length=10)
    config_vless = models.TextField(help_text="Plain VLESS URL; encrypted when sent to app.")
    is_active = models.BooleanField(default=True)
    priority = models.IntegerField(default=0, help_text="Higher = shown first.")
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering = ["-priority", "id"]

    def __str__(self):
        return f"{self.name} ({self.country})"
