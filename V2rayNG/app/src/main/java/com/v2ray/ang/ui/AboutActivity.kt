package com.v2ray.ang.ui

import android.os.Bundle
import android.view.View
import com.v2ray.ang.BuildConfig
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ActivityAboutBinding
import com.v2ray.ang.handler.V2RayNativeManager

class AboutActivity : BaseActivity() {
    private val binding by lazy { ActivityAboutBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithToolbar(binding.root, showHomeAsUp = true, title = getString(R.string.title_about))

        binding.layoutOssLicenses.setOnClickListener {
            val webView = android.webkit.WebView(this)
            webView.loadUrl("file:///android_asset/open_source_licenses.html")
            android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_oss_license))
                .setView(webView)
                .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()
        }

        binding.tvVersion.text = "v${BuildConfig.VERSION_NAME} · ${V2RayNativeManager.getLibVersion()}"
        binding.tvAppId.text = BuildConfig.APPLICATION_ID
        binding.tvAppId.visibility = View.GONE
    }
}