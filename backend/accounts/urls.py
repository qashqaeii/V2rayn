from django.urls import path

from accounts.views import (
    LoginView,
    RegisterView,
    ProfileView,
    SubscriptionOrderListCreateView,
    SubscriptionPlanView,
)

urlpatterns = [
    path("auth/register/", RegisterView.as_view(), name="auth-register"),
    path("auth/login/", LoginView.as_view(), name="auth-login"),
    path("profile/", ProfileView.as_view(), name="profile"),
    path("orders/", SubscriptionOrderListCreateView.as_view(), name="orders"),
    path("plans/", SubscriptionPlanView.as_view(), name="plans"),
]
