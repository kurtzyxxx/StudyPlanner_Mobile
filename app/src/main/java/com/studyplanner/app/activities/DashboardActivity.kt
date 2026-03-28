package com.studyplanner.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.studyplanner.app.R
import com.studyplanner.app.databinding.ActivityDashboardBinding
import com.studyplanner.app.utils.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Get user data and display welcome message
        val user = sessionManager.getUser()
        if (user != null) {
            binding.tvWelcome.text = getString(R.string.welcome_message, user.fullName)
        } else {
            // No user data, redirect to login
            navigateToLogin()
            return
        }

        // Setup Bottom Navigation
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        // Set Dashboard as selected
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    // Already on Dashboard
                    true
                }
                R.id.nav_subjects -> {
                    Toast.makeText(this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show()
                    false
                }
                R.id.nav_tasks -> {
                    Toast.makeText(this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show()
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    // When returning from Profile, re-select Dashboard tab
    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard

        // Refresh welcome name in case it was updated in Profile
        val user = sessionManager.getUser()
        if (user != null) {
            binding.tvWelcome.text = getString(R.string.welcome_message, user.fullName)
        }
    }

    // Handle back press — show logout confirmation
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                sessionManager.logout()
                navigateToLogin()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun navigateToLogin() {
        sessionManager.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
