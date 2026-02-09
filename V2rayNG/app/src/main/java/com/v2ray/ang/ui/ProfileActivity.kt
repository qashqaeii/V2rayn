package com.v2ray.ang.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.v2ray.ang.R
import com.v2ray.ang.api.SubscriptionOrderDto
import com.v2ray.ang.api.UserProfileDto
import com.v2ray.ang.api.VpnServersRepository
import com.v2ray.ang.databinding.ActivityProfileBinding
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val repository = VpnServersRepository()
    private val ordersAdapter = OrdersAdapter()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivReceipt.setImageURI(it)
            binding.ivReceipt.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentViewWithToolbar(binding.root, showHomeAsUp = true, title = getString(R.string.profile))

        if (!MmkvManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadProfile()
        loadOrders()

        binding.btnUpgrade.setOnClickListener {
            binding.layoutUpgrade.visibility = View.VISIBLE
        }

        binding.btnSelectReceipt.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSubmitOrder.setOnClickListener {
            submitOrder()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.recyclerOrders.layoutManager = LinearLayoutManager(this)
        binding.recyclerOrders.adapter = ordersAdapter
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                val profile = repository.getProfile()
                displayProfile(profile)
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, getString(R.string.failed_to_load_profile) + ": " + e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayProfile(profile: UserProfileDto) {
        binding.tvAccountType.text = profile.accountTypeDisplay
        binding.tvSubscriptionStatus.text = profile.subscriptionStatusDisplay
        binding.tvCanAccessPro.text = if (profile.canAccessProServers) getString(R.string.yes) else getString(R.string.no)

        if (!profile.subscriptionEndDate.isNullOrBlank()) {
            try {
                val raw = profile.subscriptionEndDate.replace("Z", "").substringBefore(".")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val endDate = dateFormat.parse(raw)
                val displayFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                binding.tvSubscriptionEnd.text = if (endDate != null) displayFormat.format(endDate) else profile.subscriptionEndDate
            } catch (e: Exception) {
                binding.tvSubscriptionEnd.text = profile.subscriptionEndDate
            }
        } else {
            binding.tvSubscriptionEnd.text = getString(R.string.not_applicable)
        }

        binding.btnUpgrade.visibility = if (profile.accountType == "free") View.VISIBLE else View.GONE
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            try {
                val orders = repository.getOrders()
                displayOrders(orders)
            } catch (e: Exception) {
                // Silent fail for orders
            }
        }
    }

    private fun displayOrders(orders: List<SubscriptionOrderDto>) {
        if (orders.isEmpty()) {
            binding.tvOrdersEmpty.visibility = View.VISIBLE
            binding.recyclerOrders.visibility = View.GONE
        } else {
            binding.tvOrdersEmpty.visibility = View.GONE
            binding.recyclerOrders.visibility = View.VISIBLE
            ordersAdapter.submitList(orders)
        }
    }

    private fun submitOrder() {
        val trackingCode = binding.etTrackingCode.text.toString().trim()
        if (trackingCode.isEmpty()) {
            binding.tilTrackingCode.error = getString(R.string.tracking_code_required)
            return
        }
        binding.tilTrackingCode.error = null

        if (selectedImageUri == null) {
            Toast.makeText(this, getString(R.string.receipt_image_required), Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmitOrder.isEnabled = false
        showLoading()

        lifecycleScope.launch {
            try {
                val tempFile = withContext(Dispatchers.IO) {
                    val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    val file = File(cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
                    file.writeBytes(bytes ?: ByteArray(0))
                    file
                }
                repository.createOrder(trackingCode, tempFile)
                Toast.makeText(this@ProfileActivity, getString(R.string.order_submitted), Toast.LENGTH_SHORT).show()
                binding.layoutUpgrade.visibility = View.GONE
                binding.etTrackingCode.text?.clear()
                selectedImageUri = null
                binding.ivReceipt.visibility = View.GONE
                loadOrders()
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, getString(R.string.order_submit_failed) + ": " + e.message, Toast.LENGTH_LONG).show()
            } finally {
                binding.btnSubmitOrder.isEnabled = true
                hideLoading()
            }
        }
    }

    private fun logout() {
        MmkvManager.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
