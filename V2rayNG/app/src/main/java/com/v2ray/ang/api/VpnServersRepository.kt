package com.v2ray.ang.api

import com.v2ray.ang.BuildConfig
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * API repository to fetch server list.
 *
 * SECURITY:
 * - No logging interceptor here.
 * - Certificate pinning is applied when pins are provided.
 */
class VpnServersRepository {

    private val api: VpnServersApi by lazy {
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
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
}

