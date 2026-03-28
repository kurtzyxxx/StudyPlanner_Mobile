package com.studyplanner.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.studyplanner.app.R
import com.studyplanner.app.api.RetrofitClient
import com.studyplanner.app.databinding.ActivityRegisterBinding
import com.studyplanner.app.models.RegisterRequest
import com.studyplanner.app.models.RegisterResponse
import com.studyplanner.app.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Register button click
        binding.btnRegister.setOnClickListener { handleRegister() }

        // Navigate to Login
        binding.tvLoginLink.setOnClickListener { finish() }
    }

    private fun handleRegister() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validate all fields
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()) {
            showError(getString(R.string.error_fill_fields))
            return
        }

        // Check password match
        if (password != confirmPassword) {
            showError(getString(R.string.error_passwords_mismatch))
            return
        }

        setLoading(true)
        hideError()

        val request = RegisterRequest(fullName, username, email, password)

        RetrofitClient.getInstance(this).register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                setLoading(false)

                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity,
                        getString(R.string.success_registration), Toast.LENGTH_LONG).show()
                    // Navigate back to Login
                    finish()
                } else {
                    // Parse error message from server
                    showError("Registration failed. Email or username may already exist.")
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                setLoading(false)
                showError(getString(R.string.error_network))
            }
        })
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
        binding.btnRegister.text = if (loading) "Registering..." else getString(R.string.btn_register)

        if (loading) {
            binding.btnRegister.setBackgroundResource(R.drawable.rounded_button_disabled)
        } else {
            binding.btnRegister.setBackgroundResource(R.drawable.rounded_button_black)
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }
}
