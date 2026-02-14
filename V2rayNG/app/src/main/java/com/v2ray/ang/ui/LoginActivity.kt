package com.v2ray.ang.ui

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.v2ray.ang.R
import com.v2ray.ang.api.LoginResponse
import com.v2ray.ang.api.VpnServersRepository
import com.v2ray.ang.databinding.ActivityLoginBinding
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.util.JsonUtil
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val repository = VpnServersRepository()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentViewWithToolbar(binding.root, showHomeAsUp = false, title = getString(R.string.login))

        // اگر قبلاً لاگین شده، به MainActivity برو
        if (MmkvManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // اگر کاربر جدید است (هنوز ثبت‌نام نکرده)، به صفحه ثبت‌نام بفرست
        if (!MmkvManager.hasRegistered()) {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
            return
        }

        // بارگذاری اطلاعات ذخیره شده (Remember Me)
        loadSavedCredentials()

        // تنظیم آیکون چشم برای نمایش/مخفی کردن رمز عبور
        setupPasswordToggle()

        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.etPassword.setOnEditorActionListener { _, _, _ ->
            attemptLogin()
            true
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loadSavedCredentials() {
        if (MmkvManager.isRememberMeEnabled()) {
            val (username, password) = MmkvManager.getSavedCredentials()
            username?.let { binding.etUsername.setText(it) }
            password?.let { binding.etPassword.setText(it) }
            binding.cbRemember.isChecked = true
        }
    }

    private fun setupPasswordToggle() {
        binding.tilPassword.setEndIconOnClickListener {
            isPasswordVisible = !isPasswordVisible
            val editText = binding.etPassword
            if (isPasswordVisible) {
                editText.transformationMethod = null
                binding.tilPassword.endIconDrawable = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_view)
            } else {
                editText.transformationMethod = PasswordTransformationMethod()
                binding.tilPassword.endIconDrawable = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_view)
            }
            // انتقال cursor به انتهای متن
            editText.setSelection(editText.text?.length ?: 0)
        }
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (username.isEmpty()) {
            binding.tilUsername.error = getString(R.string.username_required)
            return
        }
        binding.tilUsername.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.password_required)
            return
        }
        binding.tilPassword.error = null

        binding.btnLogin.isEnabled = false
        showLoading()

        lifecycleScope.launch {
            try {
                val response = repository.login(username, password)
                saveLoginData(response, username, password)
                Toast.makeText(this@LoginActivity, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, getString(R.string.login_failed) + ": " + e.message, Toast.LENGTH_LONG).show()
            } finally {
                binding.btnLogin.isEnabled = true
                hideLoading()
            }
        }
    }

    private fun saveLoginData(response: LoginResponse, username: String, password: String) {
        MmkvManager.saveAuthToken(response.token)
        MmkvManager.saveUserProfile(JsonUtil.toJson(response.user.profile))
        MmkvManager.saveCurrentUsername(response.user.username)
        
        // علامت‌گذاری که کاربر لاگین کرده است (یعنی قبلاً ثبت‌نام کرده)
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
