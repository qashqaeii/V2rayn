package com.v2ray.ang.api

import retrofit2.http.GET

interface VpnServersApi {
    @GET("servers/")
    suspend fun getServers(): List<ApiServerDto>
}

