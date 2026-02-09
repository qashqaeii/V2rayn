package com.v2ray.ang.api

import com.v2ray.ang.BuildConfig
import com.v2ray.ang.handler.MmkvManager
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * API repository to fetch server list and handle authentication.
 *
 * SECURITY:
 * - No logging interceptor here.
 * - Certificate pinning is applied when pins are provided.
 * - Token is automatically added to authenticated requests.
 */
class VpnServersRepository {

    private val api: VpnServersApi by lazy {
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .applyCertificatePinning()
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.VPN_API_BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VpnServersApi::class.java)
    }

    suspend fun fetchServers(): List<ApiServerDto> = api.getServers()

    suspend fun fetchConfig(): Map<String, String> = api.getConfig()

    suspend fun login(username: String, password: String): LoginResponse {
        return api.login(username, password)
    }

    suspend fun getProfile(): UserProfileDto {
        return api.getProfile()
    }

    suspend fun getOrders(): List<SubscriptionOrderDto> {
        return api.getOrders()
    }

    suspend fun createOrder(trackingCode: String, receiptImageFile: File?): SubscriptionOrderDto {
        val trackingCodeBody = trackingCode.toRequestBody(MultipartBody.FORM)
        val imagePart = receiptImageFile?.let {
            val mediaType = "image/*".toMediaTypeOrNull()
            MultipartBody.Part.createFormData(
                "receipt_image",
                it.name,
                it.asRequestBody(mediaType)
            )
        }
        return api.createOrder(trackingCodeBody, imagePart)
    }

    private fun OkHttpClient.Builder.applyCertificatePinning(): OkHttpClient.Builder {
        val host = BuildConfig.VPN_API_HOST.trim()
        val pins = BuildConfig.VPN_API_CERT_PINS
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (host.isEmpty() || pins.isEmpty()) {
            return this
        }

        val pinner = CertificatePinner.Builder().apply {
            for (pin in pins) {
                add(host, pin)
            }
        }.build()
        return certificatePinner(pinner)
    }

    /**
     * Interceptor برای اضافه کردن توکن به header درخواست‌های احراز شده.
     */
    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val originalRequest = chain.request()
            val token = MmkvManager.getAuthToken()

            val newRequest = if (!token.isNullOrEmpty() && !originalRequest.url.encodedPath.contains("/auth/login")) {
                originalRequest.newBuilder()
                    .header("Authorization", "Token $token")
                    .build()
            } else {
                originalRequest
            }

            return chain.proceed(newRequest)
        }
    }
}

