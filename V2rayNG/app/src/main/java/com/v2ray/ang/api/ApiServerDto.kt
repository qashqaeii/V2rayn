package com.v2ray.ang.api

import com.google.gson.annotations.SerializedName

/**
 * Server item fetched from backend API.
 *
 * SECURITY:
 * - Do not log or display [config] anywhere in UI/logs.
 */
data class ApiServerDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("flag") val flag: String?,
    @SerializedName("config") val config: String
)

