package com.v2ray.ang.api

import android.util.Log
import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.fmt.Hysteria2Fmt
import com.v2ray.ang.fmt.ShadowsocksFmt
import com.v2ray.ang.fmt.SocksFmt
import com.v2ray.ang.fmt.TrojanFmt
import com.v2ray.ang.fmt.VlessFmt
import com.v2ray.ang.fmt.VmessFmt
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.fmt.WireguardFmt
import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.MmkvManager

/**
 * Imports API-provided VLESS/VMess links into the existing internal storage/model.
 *
 * IMPORTANT:
 * - Uses existing v2rayNG parsers only (no new parsing system).
 * - Never logs raw configs.
 */
object ApiServersImporter {

    /**
     * Replaces the whole server list with API servers.
     *
     * If [apiServers] is empty, it will wipe local server list.
     */
    fun replaceAll(apiServers: List<ApiServerDto>) {
        // Remove any user-provided servers/subscriptions
        MmkvManager.removeAllServersKeepSettings()
        MmkvManager.clearAllSubscriptions()

        if (apiServers.isEmpty()) {
            return
        }

        for (srv in apiServers) {
            val profile = parseSupportedConfig(srv.config) ?: continue
            profile.subscriptionId = "" // locked build: no subscriptions/groups
            profile.remarks = srv.name
            profile.description = AngConfigManager.generateDescription(profile)
            val guid = MmkvManager.encodeServerConfig("", profile)
            MmkvManager.encodeServerApiMeta(guid, srv.id, srv.flag)
        }
    }

    private fun parseSupportedConfig(raw: String): ProfileItem? {
        var str = raw.trim()
        if (str.isEmpty()) return null
        // اگر متن با هیچ پروتکلی شروع نشد، احتمالاً کانفیگ رمزنگاری‌شده از بک‌اند است (AES-256-CBC)
        if (!str.startsWith(EConfigType.VMESS.protocolScheme) &&
            !str.startsWith(EConfigType.VLESS.protocolScheme) &&
            !str.startsWith(EConfigType.SHADOWSOCKS.protocolScheme) &&
            !str.startsWith(EConfigType.SOCKS.protocolScheme) &&
            !str.startsWith(EConfigType.TROJAN.protocolScheme) &&
            !str.startsWith(EConfigType.WIREGUARD.protocolScheme) &&
            !str.startsWith(EConfigType.HYSTERIA2.protocolScheme) &&
            !str.startsWith(AppConfig.HY2)
        ) {
            val key = BuildConfig.CONFIG_ENCRYPTION_KEY?.trim()
            if (!key.isNullOrEmpty()) {
                str = ConfigDecryptor.decrypt(str, key) ?: return null
                if (str.isEmpty()) return null
            } else {
                return null
            }
        }
        return try {
            when {
                str.startsWith(EConfigType.VMESS.protocolScheme) -> VmessFmt.parse(str)
                str.startsWith(EConfigType.VLESS.protocolScheme) -> VlessFmt.parse(str)
                str.startsWith(EConfigType.SHADOWSOCKS.protocolScheme) -> ShadowsocksFmt.parse(str)
                str.startsWith(EConfigType.SOCKS.protocolScheme) -> SocksFmt.parse(str)
                str.startsWith(EConfigType.TROJAN.protocolScheme) -> TrojanFmt.parse(str)
                str.startsWith(EConfigType.WIREGUARD.protocolScheme) -> WireguardFmt.parse(str)
                str.startsWith(EConfigType.HYSTERIA2.protocolScheme) || str.startsWith(AppConfig.HY2) -> Hysteria2Fmt.parse(str)
                else -> null
            }
        } catch (e: Exception) {
            Log.w(AppConfig.TAG, "API server config parse failed", e)
            null
        }
    }
}

