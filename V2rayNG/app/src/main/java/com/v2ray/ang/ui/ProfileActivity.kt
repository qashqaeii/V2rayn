package com.v2ray.ang.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.v2ray.ang.R
import com.v2ray.ang.api.SubscriptionOrderDto
import com.v2ray.ang.api.SubscriptionPlanDto
import com.v2ray.ang.api.UserProfileDto
import com.v2ray.ang.api.VpnServersRepository
import com.v2ray.ang.databinding.ActivityProfileBinding
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val repository = VpnServersRepository()
    private val ordersAdapter = OrdersAdapter()
    private var selectedImageUri: Uri? = null
    private var currentPlan: SubscriptionPlanDto? = null

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
        loadPlans()
        loadOrders()

        binding.btnUpgrade.setOnClickListener {
            if (currentPlan != null) {
                binding.cardUpgrade.visibility = View.VISIBLE
                displayPlanInfo(currentPlan!!)
            } else {
                Toast.makeText(this, getString(R.string.failed_to_load_plans), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSelectReceipt.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSubmitOrder.setOnClickListener { submitOrder() }
        binding.btnRetryOrder.setOnClickListener { submitOrder() }

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
        val username = MmkvManager.getCurrentUsername() ?: MmkvManager.getSavedCredentials().first ?: getString(R.string.profile)
        binding.tvProfileName.text = username
        binding.tvProfileAvatarLetter.text = username.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

        binding.chipSubscriptionBadge.text = profile.subscriptionStatusDisplay
        binding.chipSubscriptionBadge.setChipBackgroundColorResource(
            if (profile.subscriptionStatus == "active") R.color.vpn_status_connected
            else R.color.vpn_status_connecting
        )
        binding.chipSubscriptionBadge.setTextColor(ContextCompat.getColor(this, android.R.color.white))

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

    private fun loadPlans() {
        lifecycleScope.launch {
            try {
                val plans = repository.getPlans()
                currentPlan = plans.firstOrNull { it.planType == "pro" && it.isActive }
            } catch (e: Exception) {
                // Silent fail for plans
            }
        }
    }

    private fun displayPlanInfo(plan: SubscriptionPlanDto) {
        binding.layoutPlanInfo.visibility = View.VISIBLE
        binding.tvPlanPrice.text = String.format(Locale.getDefault(), getString(R.string.plan_toman), formatPrice(plan.price))
        binding.tvPlanDuration.text = String.format(Locale.getDefault(), getString(R.string.plan_days), plan.durationDays)
    }

    private fun formatPrice(price: Double): String {
        return String.format(Locale.getDefault(), "%,.0f", price)
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
        if (selectedImageUri == null) {
            Toast.makeText(this, getString(R.string.receipt_image_required), Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmitOrder.isEnabled = false
        binding.tvOrderError.visibility = View.GONE
        binding.btnRetryOrder.visibility = View.GONE
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
                repository.createOrder(tempFile)
                runOnUiThread {
                    hideLoading()
                    binding.btnSubmitOrder.isEnabled = true
                    Toast.makeText(this@ProfileActivity, getString(R.string.order_submitted), Toast.LENGTH_SHORT).show()
                    binding.cardUpgrade.visibility = View.GONE
                    selectedImageUri = null
                    binding.ivReceipt.visibility = View.GONE
                    loadOrders()
                    loadProfile()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    hideLoading()
                    binding.btnSubmitOrder.isEnabled = true
                    val isTimeout = e is java.net.SocketTimeoutException ||
                        e.message?.contains("timeout", ignoreCase = true) == true
                    if (isTimeout) {
                        binding.tvOrderError.text = getString(R.string.order_submit_timeout)
                        binding.tvOrderError.visibility = View.VISIBLE
                        binding.btnRetryOrder.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this@ProfileActivity, getString(R.string.order_submit_failed) + ": " + e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun logout() {
        MmkvManager.logout()
        // بعد از logout، کاربر باید دوباره لاگین کند (نه ثبت‌نام)
        // پس hasRegistered را true نگه می‌داریم
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
