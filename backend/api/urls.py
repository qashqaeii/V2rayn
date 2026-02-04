from django.urls import path, include

from servers.views import AppConfigView

urlpatterns = [
    path("servers/", include("servers.urls")),
    path("config/", AppConfigView.as_view(), name="app-config"),
]
