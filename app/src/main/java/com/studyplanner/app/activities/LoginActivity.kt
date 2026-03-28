package com.studyplanner.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.studyplanner.app.api.RetrofitClient
import com.studyplanner.app.databinding.ActivityLoginBinding
import com.studyplanner.app.models.LoginRequest
import com.studyplanner.app.models.LoginResponse
import com.studyplanner.app.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // If already logged in, go straight to Dashboard
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard()
            return
        }

        // Login button click
        binding.btnLogin.setOnClickListener { handleLogin() }

        // Navigate to Register
        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            showError(getString(com.studyplanner.app.R.string.error_fill_fields))
            return
        }

        setLoading(true)
        hideError()

        // Build request and call API
        val request = LoginRequest(email, password)

        RetrofitClient.getInstance(this).login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                setLoading(false)

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        // Save token and user data
                        sessionManager.saveToken(body.token)
                        sessionManager.saveUser(body.user)
                        navigateToDashboard()
                    }
                } else {
                    // Handle 400/401 — invalid credentials
                    showError(getString(com.studyplanner.app.R.string.error_invalid_login))
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                setLoading(false)
                // Handle network error
                showError(getString(com.studyplanner.app.R.string.error_network))
            }
        })
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.btnLogin.text = if (loading)
            getString(com.studyplanner.app.R.string.btn_verifying)
        else
            getString(com.studyplanner.app.R.string.btn_login)

        if (loading) {
            binding.btnLogin.setBackgroundResource(com.studyplanner.app.R.drawable.rounded_button_disabled)
        } else {
            binding.btnLogin.setBackgroundResource(com.studyplanner.app.R.drawable.rounded_button_black)
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
