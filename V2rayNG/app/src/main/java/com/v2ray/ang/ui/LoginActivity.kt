package com.v2ray.ang.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
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

        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.etPassword.setOnEditorActionListener { _, _, _ ->
            attemptLogin()
            true
        }
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (username.isEmpty()) {
            binding.etUsername.error = getString(R.string.username_required)
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.password_required)
            return
        }

        binding.btnLogin.isEnabled = false
        showLoading()

        lifecycleScope.launch {
            try {
                val response = repository.login(username, password)
                saveLoginData(response)
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

    private fun saveLoginData(response: LoginResponse) {
        MmkvManager.saveAuthToken(response.token)
        MmkvManager.saveUserProfile(JsonUtil.toJson(response.user.profile))
    }
}
