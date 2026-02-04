package com.v2ray.ang.helper

import androidx.appcompat.app.AppCompatActivity

/**
 * QR code scanner helper.
 * Locked build: scanning disabled; launch() always invokes callback with null.
 */
class QRCodeScannerHelper(private val activity: AppCompatActivity) {

    fun launch(onResult: (String?) -> Unit) {
        onResult(null)
    }
}
