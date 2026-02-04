package com.v2ray.ang.dto

/**
 * Misc server info stored locally.
 *
 * SECURITY:
 * - Must never include raw config.
 */
data class ServerAffiliationInfo(
    var testDelayMillis: Long = 0L,
    // The API server id for this profile (used to identify API-owned entries).
    var apiServerId: String? = null,
    // Country flag code from API (e.g. "de"). UI can convert to emoji locally.
    var flag: String? = null
) {
    fun getTestDelayString(): String {
        if (testDelayMillis == 0L) {
            return ""
        }
        return testDelayMillis.toString() + "ms"
    }
}
