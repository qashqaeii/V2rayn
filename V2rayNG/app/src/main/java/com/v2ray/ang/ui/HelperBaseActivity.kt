package com.v2ray.ang.ui

import android.net.Uri
import android.os.Bundle
import com.v2ray.ang.enums.PermissionType
import com.v2ray.ang.helper.FileChooserHelper
import com.v2ray.ang.helper.PermissionHelper
/**
 * HelperBaseActivity extends BaseActivity and provides file chooser and permission helpers.
 * Locked build: QR scanning is disabled (no-op).
 */
abstract class HelperBaseActivity : BaseActivity() {
    private lateinit var fileChooser : FileChooserHelper
    private lateinit var permissionRequester : PermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileChooser = FileChooserHelper(this)
        permissionRequester = PermissionHelper(this)
    }

    /**
     * Check if permission is granted and request it if not.
     * Convenience method that delegates to permissionRequester.
     *
     * @param permissionType The type of permission to check and request
     * @param onGranted Callback to execute when permission is granted
     */
    protected fun checkAndRequestPermission(
        permissionType: PermissionType,
        onGranted: () -> Unit
    ) {
        permissionRequester.request(permissionType, onGranted)
    }

    /**
     * Launch file chooser with ACTION_GET_CONTENT intent.
     * Convenience method that delegates to fileChooser helper.
     *
     * @param mimeType MIME type filter for files
     * @param onResult Callback invoked with the selected file URI (null if cancelled)
     */
    protected fun launchFileChooser(
        mimeType: String = "*/*",
        onResult: (Uri?) -> Unit
    ) {
        checkAndRequestPermission(PermissionType.READ_STORAGE) {
            fileChooser.launch(mimeType, onResult)
        }
    }

    /**
     * Launch document creator to create a new file at user-selected location.
     * Convenience method that delegates to fileChooser helper.
     * Note: No permission check needed as CreateDocument uses Storage Access Framework.
     *
     * @param fileName Default file name for the new document
     * @param onResult Callback invoked with the created file URI (null if cancelled)
     */
    protected fun launchCreateDocument(
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        fileChooser.createDocument(fileName, onResult)
    }

    /**
     * Launch QR code scanner.
     * Locked build: QR/import disabled; always invokes callback with null.
     */
    protected fun launchQRCodeScanner(onResult: (String?) -> Unit) {
        onResult(null)
    }
}
