package com.v2ray.ang.api

import retrofit2.http.GET

interface VpnServersApi {
    @GET("servers/")
    suspend fun getServers(): List<ApiServerDto>

    /** تنظیمات اپ از پنل ادمین؛ کاربر در اپ نمی‌تواند ببیند یا تغییر دهد. */
    @GET("config/")
    suspend fun getConfig(): Map<String, String>
}

