package com.v2ray.ang.api

import com.google.gson.annotations.SerializedName

/**
 * سفارش اشتراک.
 */
data class SubscriptionOrderDto(
    @SerializedName("id") val id: Int,
    @SerializedName("plan_type") val planType: String,
    @SerializedName("plan_type_display") val planTypeDisplay: String,
    @SerializedName("payment_tracking_code") val paymentTrackingCode: String?,
    @SerializedName("receipt_image") val receiptImage: String?,
    @SerializedName("receipt_image_url") val receiptImageUrl: String?,
    @SerializedName("status") val status: String,
    @SerializedName("status_display") val statusDisplay: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("approved_at") val approvedAt: String?
)
