package com.v2ray.ang.ui

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.v2ray.ang.R
import com.v2ray.ang.api.LoginResponse
import com.v2ray.ang.api.VpnServersRepository
import com.v2ray.ang.databinding.ActivityRegisterBinding
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.util.JsonUtil
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val repository = VpnServersRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentViewWithToolbar(binding.root, showHomeAsUp = true, title = getString(R.string.register))

        // اگر قبلاً لاگین شده، به MainActivity برو
        if (MmkvManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.btnRegister.setOnClickListener {
            attemptRegister()
        }

        binding.tvLoginLink.setOnClickListener {
            finish()
        }

        // بارگذاری اطلاعات Remember Me اگر فعال باشد
        if (MmkvManager.isRememberMeEnabled()) {
            val (username, _) = MmkvManager.getSavedCredentials()
            username?.let { binding.etUsername.setText(it) }
            binding.cbRemember.isChecked = true
        }
    }

    private fun attemptRegister() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val passwordConfirm = binding.etPasswordConfirm.text.toString()

        var hasError = false

        if (username.isEmpty() || username.length < 3) {
            binding.tilUsername.error = getString(R.string.register_username_error)
            hasError = true
        } else {
            binding.tilUsername.error = null
        }

        if (password.isEmpty() || password.length < 8) {
            binding.tilPassword.error = getString(R.string.register_password_error)
            hasError = true
        } else {
            binding.tilPassword.error = null
        }

        if (passwordConfirm.isEmpty() || passwordConfirm != password) {
            binding.tilPasswordConfirm.error = getString(R.string.register_password_confirm_error)
            hasError = true
        } else {
            binding.tilPasswordConfirm.error = null
        }

        if (hasError) {
            return
        }

        binding.btnRegister.isEnabled = false
        showLoading()

        lifecycleScope.launch {
            try {
                val response = repository.register(username, password, passwordConfirm)
                saveLoginData(response, username, password)
                Toast.makeText(this@RegisterActivity, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                val errorMsg = e.message ?: getString(R.string.register_failed)
                Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()
            } finally {
                binding.btnRegister.isEnabled = true
                hideLoading()
            }
        }
    }

    private fun saveLoginData(response: LoginResponse, username: String, password: String) {
        MmkvManager.saveAuthToken(response.token)
        MmkvManager.saveUserProfile(JsonUtil.toJson(response.user.profile))
        
        // علامت‌گذاری که کاربر ثبت‌نام کرده است
        MmkvManager.setHasRegistered(true)
        
        // ذخیره اطلاعات برای Remember Me
        val rememberMe = binding.cbRemember.isChecked
        MmkvManager.setRememberMe(rememberMe)
        if (rememberMe) {
            MmkvManager.saveCredentials(username, password)
        } else {
            MmkvManager.clearSavedCredentials()
        }
    }
}
