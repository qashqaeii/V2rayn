package com.v2ray.ang.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface VpnServersApi {
    @GET("servers/")
    suspend fun getServers(): List<ApiServerDto>

    /** تنظیمات اپ از پنل ادمین؛ کاربر در اپ نمی‌تواند ببیند یا تغییر دهد. */
    @GET("config/")
    suspend fun getConfig(): Map<String, String>

    /** لاگین کاربر */
    @POST("auth/login/")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    /** دریافت پروفایل کاربر */
    @GET("profile/")
    suspend fun getProfile(): UserProfileDto

    /** لیست سفارش‌های کاربر */
    @GET("orders/")
    suspend fun getOrders(): List<SubscriptionOrderDto>

    /** ثبت سفارش جدید */
    @Multipart
    @POST("orders/")
    suspend fun createOrder(
        @Part("payment_tracking_code") trackingCode: RequestBody,
        @Part receiptImage: MultipartBody.Part?
    ): SubscriptionOrderDto

    /** ثبت‌نام کاربر جدید */
    @POST("auth/register/")
    @FormUrlEncoded
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("password_confirm") passwordConfirm: String
    ): LoginResponse

    /** دریافت پلن‌های اشتراک */
    @GET("plans/")
    suspend fun getPlans(): List<SubscriptionPlanDto>
}

