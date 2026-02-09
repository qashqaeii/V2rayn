from django.urls import path

from accounts.views import LoginView, ProfileView, SubscriptionOrderListCreateView

urlpatterns = [
    path("auth/login/", LoginView.as_view(), name="auth-login"),
    path("profile/", ProfileView.as_view(), name="profile"),
    path("orders/", SubscriptionOrderListCreateView.as_view(), name="orders"),
]
