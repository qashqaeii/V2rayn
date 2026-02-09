package com.v2ray.ang.api

import com.google.gson.annotations.SerializedName

/**
 * پاسخ لاگین از API.
 */
data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto
)

/**
 * اطلاعات کاربر.
 */
data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("profile") val profile: UserProfileDto
)

/**
 * پروفایل کاربر.
 */
data class UserProfileDto(
    @SerializedName("account_type") val accountType: String,
    @SerializedName("account_type_display") val accountTypeDisplay: String,
    @SerializedName("subscription_status") val subscriptionStatus: String,
    @SerializedName("subscription_status_display") val subscriptionStatusDisplay: String,
    @SerializedName("subscription_start_date") val subscriptionStartDate: String?,
    @SerializedName("subscription_end_date") val subscriptionEndDate: String?,
    @SerializedName("can_access_pro_servers") val canAccessProServers: Boolean,
    @SerializedName("created_at") val createdAt: String
)
