package com.v2ray.ang.api

import com.google.gson.annotations.SerializedName

/**
 * پلن اشتراک حرفه‌ای.
 */
data class SubscriptionPlanDto(
    @SerializedName("id") val id: Int,
    @SerializedName("plan_type") val planType: String,
    @SerializedName("price") val price: Double,
    @SerializedName("duration_days") val durationDays: Int,
    @SerializedName("description") val description: String?,
    @SerializedName("is_active") val isActive: Boolean
)
