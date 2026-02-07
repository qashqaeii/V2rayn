package com.v2ray.ang.ui

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ActivityMainBinding
import com.v2ray.ang.databinding.BottomSheetChooseCountryBinding
import com.v2ray.ang.dto.GroupMapItem
import com.v2ray.ang.enums.PermissionType
import com.v2ray.ang.extension.toast
import com.v2ray.ang.extension.toastError
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.handler.SettingsChangeManager
import com.v2ray.ang.handler.SettingsManager
import com.v2ray.ang.handler.V2RayServiceManager
import com.v2ray.ang.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : HelperBaseActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    val mainViewModel: MainViewModel by viewModels()
    private lateinit var groupPagerAdapter: GroupPagerAdapter
    private var countryBottomSheet: BottomSheetDialog? = null

    private val requestVpnPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            startV2Ray()
        }
    }
    private val requestActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (SettingsChangeManager.consumeRestartService() && mainViewModel.isRunning.value == true) {
            restartV2Ray()
        }
        if (SettingsChangeManager.consumeSetupGroupTab()) {
            setupGroupTab()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar(null, false, null)

        groupPagerAdapter = GroupPagerAdapter(this, emptyList())
        binding.viewPager.adapter = groupPagerAdapter
        binding.viewPager.isUserInputEnabled = true

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        })

        binding.btnPower.setOnClickListener { handlePowerAction() }
        binding.tvStatus.setOnClickListener { handleStatusClick() }
        binding.countrySelector.setOnClickListener { openChooseCountrySheet() }
        binding.btnMenu.setOnClickListener { showMenu(it) }

        setupGroupTab()
        setupViewModel()
        mainViewModel.syncServersFromApiOnStart()

        checkAndRequestPermission(PermissionType.POST_NOTIFICATIONS) {}
        refreshSelectedServerUi()
    }

    private fun setupViewModel() {
        mainViewModel.updateTestResultAction.observe(this) { setStatusText(it) }
        mainViewModel.isRunning.observe(this) { isRunning ->
            applyRunningState(false, isRunning)
        }
        mainViewModel.apiSyncState.observe(this) { state ->
            when (state) {
                is MainViewModel.ApiSyncState.Syncing -> {
                    binding.progressBar.isVisible = true
                }
                is MainViewModel.ApiSyncState.Synced,
                is MainViewModel.ApiSyncState.Idle -> {
                    binding.progressBar.isVisible = false
                    refreshSelectedServerUi()
                }
                is MainViewModel.ApiSyncState.Failed -> {
                    binding.progressBar.isVisible = false
                    val msg = state.reason?.takeIf { it.isNotBlank() }
                        ?: getString(R.string.toast_services_failure)
                    toastError(msg)
                }
            }
        }
        mainViewModel.updateListAction.observe(this) { refreshSelectedServerUi() }
        mainViewModel.startListenBroadcast()
        mainViewModel.initAssets(assets)
    }

    private fun setupGroupTab() {
        groupPagerAdapter.update(
            listOf(GroupMapItem(id = "", remarks = getString(R.string.filter_config_all)))
        )
    }

    private fun handlePowerAction() {
        applyRunningState(isLoading = true, isRunning = false)
        if (mainViewModel.isRunning.value == true) {
            V2RayServiceManager.stopVService(this)
        } else if (SettingsManager.isVpnMode()) {
            val intent = VpnService.prepare(this)
            if (intent == null) {
                startV2Ray()
            } else {
                requestVpnPermission.launch(intent)
            }
        } else {
            startV2Ray()
        }
    }

    private fun setStatusText(content: String?) {
        binding.tvStatus.text = content ?: getString(R.string.vpn_disconnected)
    }

    private fun startV2Ray() {
        if (MmkvManager.getSelectServer().isNullOrEmpty()) {
            toast(R.string.vpn_select_location)
            applyRunningState(isLoading = false, isRunning = false)
            return
        }
        V2RayServiceManager.startVService(this)
    }

    fun restartV2Ray() {
        if (mainViewModel.isRunning.value == true) {
            V2RayServiceManager.stopVService(this)
        }
        lifecycleScope.launch {
            delay(500)
            startV2Ray()
        }
    }

    private fun handleStatusClick() {
        if (mainViewModel.isRunning.value == true) {
            binding.tvStatus.text = getString(R.string.connection_test_testing)
            mainViewModel.testCurrentServerRealPing()
        }
    }

    private fun applyRunningState(isLoading: Boolean, isRunning: Boolean) {
        if (isLoading) {
            binding.progressBar.isVisible = true
            binding.btnPower.isEnabled = false
            binding.statusDot.setBackgroundResource(R.drawable.bg_status_dot_disconnected)
            binding.tvStatus.text = getString(R.string.toast_services_start)
            return
        }
        binding.progressBar.isVisible = false
        binding.btnPower.isEnabled = true
        if (isRunning) {
            binding.btnPower.contentDescription = getString(R.string.action_stop_service)
            setStatusText(getString(R.string.vpn_connected))
            binding.statusDot.setBackgroundResource(R.drawable.bg_status_dot_connected)
            binding.tvDownloadSpeed.setTextColor(ContextCompat.getColor(this, R.color.md_theme_onBackground))
            binding.tvUploadSpeed.setTextColor(ContextCompat.getColor(this, R.color.md_theme_onBackground))
        } else {
            binding.btnPower.contentDescription = getString(R.string.tasker_start_service)
            setStatusText(getString(R.string.vpn_disconnected))
            binding.statusDot.setBackgroundResource(R.drawable.bg_status_dot_disconnected)
            binding.tvDownloadSpeed.setTextColor(ContextCompat.getColor(this, R.color.vpn_speed_label))
            binding.tvUploadSpeed.setTextColor(ContextCompat.getColor(this, R.color.vpn_speed_label))
        }
    }

    private fun refreshSelectedServerUi() {
        val guid = MmkvManager.getSelectServer()
        if (guid.isNullOrEmpty()) {
            binding.tvCountryName.text = getString(R.string.vpn_select_location)
            binding.tvCountryFlag.text = "🌍"
            return
        }
        val item = mainViewModel.serversCache.find { it.guid == guid }
        if (item != null) {
            val aff = MmkvManager.decodeServerAffiliationInfo(guid)
            val flagEmoji = aff?.flag?.toFlagEmoji()?.takeIf { it.isNotEmpty() } ?: "🌍"
            binding.tvCountryFlag.text = flagEmoji
            binding.tvCountryName.text = item.profile.remarks.ifEmpty { guid.take(8) }
        } else {
            binding.tvCountryName.text = getString(R.string.vpn_select_location)
            binding.tvCountryFlag.text = "🌍"
        }
    }

    private fun String.toFlagEmoji(): String {
        val code = trim()
        if (code.isEmpty()) return ""
        if (code.codePoints().count() > 1 && code.any { Character.getType(it) == Character.OTHER_SYMBOL.toInt() }) return code
        val cc = code.uppercase(Locale.US)
        if (cc.length != 2 || !cc.all { it in 'A'..'Z' }) return ""
        val base = 0x1F1E6
        return String(Character.toChars(base + (cc[0].code - 'A'.code))) + String(Character.toChars(base + (cc[1].code - 'A'.code)))
    }

    private fun openChooseCountrySheet() {
        countryBottomSheet?.dismiss()
        val list = mainViewModel.serversCache
        if (list.isEmpty()) {
            toast(R.string.vpn_select_location)
            return
        }
        val sheetBinding = BottomSheetChooseCountryBinding.inflate(layoutInflater)
        countryBottomSheet = BottomSheetDialog(this).apply {
            setContentView(sheetBinding.root)
            setCancelable(true)
        }
        val adapter = CountryListAdapter(list.toList()) { guid ->
            if (guid != MmkvManager.getSelectServer()) {
                MmkvManager.setSelectServer(guid)
                if (mainViewModel.isRunning.value == true) restartV2Ray()
                refreshSelectedServerUi()
            }
            countryBottomSheet?.dismiss()
        }
        sheetBinding.recyclerCountries.adapter = adapter
        adapter.updateList(list.toList())
        countryBottomSheet?.show()
    }

    override fun onResume() {
        super.onResume()
        refreshSelectedServerUi()
    }

    private fun showMenu(anchor: View) {
        PopupMenu(this, anchor, Gravity.END).apply {
            menuInflater.inflate(R.menu.menu_main_locked, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.about -> {
                        startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_locked, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.about -> {
            startActivity(Intent(this, AboutActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        countryBottomSheet?.dismiss()
        super.onDestroy()
    }
}
