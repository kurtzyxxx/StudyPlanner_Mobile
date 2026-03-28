package com.studyplanner.app.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.studyplanner.app.R
import com.studyplanner.app.api.RetrofitClient
import com.studyplanner.app.databinding.ActivityProfileBinding
import com.studyplanner.app.models.ChangePasswordRequest
import com.studyplanner.app.models.MessageResponse
import com.studyplanner.app.models.ProfileResponse
import com.studyplanner.app.models.UpdateNameRequest
import com.studyplanner.app.models.UpdateNameResponse
import com.studyplanner.app.models.UpdateProfileRequest
import com.studyplanner.app.utils.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sessionManager: SessionManager
    private var userId: Int = 0

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    // Modern image picker using GetContent contract (works on all Android versions)
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Show instant preview
            binding.tvAvatarInitial.visibility = View.GONE
            binding.imgAvatar.visibility = View.VISIBLE
            binding.imgAvatar.setImageURI(it)
            // Upload to server
            uploadAvatar(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val user = sessionManager.getUser()
        if (user == null) {
            finish()
            return
        }

        userId = user.id

        // Populate identity card
        binding.etFullName.setText(user.fullName)
        binding.tvDisplayName.text = user.fullName
        binding.tvUsernameBadge.text = "@${user.username}"
        binding.tvEmailDisplay.text = user.email
        binding.tvUsername.text = user.username
        binding.tvEmail.text = user.email
        binding.tvAvatarInitial.text = user.fullName.firstOrNull()?.uppercase() ?: "U"

        // Setup all click listeners
        binding.btnBack.setOnClickListener { finish() }
        binding.btnLogout.setOnClickListener { showLogoutDialog() }
        binding.btnChangePhoto.setOnClickListener { handleChangePhoto() }
        binding.btnSaveProfile.setOnClickListener { handleSaveProfile() }
        binding.btnChangePassword.setOnClickListener { handleChangePassword() }

        // Load existing profile data from API
        loadProfile()
    }

    // ============================================
    // LOAD PROFILE DATA
    // ============================================
    private fun loadProfile() {
        binding.progressBar.visibility = View.VISIBLE

        RetrofitClient.getInstance(this).getProfile(userId).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    response.body()?.let { profile ->
                        // Fill all editable fields
                        binding.etBio.setText(profile.bio ?: "")
                        binding.etMajor.setText(profile.major ?: "")
                        binding.etSchool.setText(profile.school ?: "")

                        // Load avatar photo if exists
                        if (!profile.avatar_url.isNullOrEmpty()) {
                            val fullUrl = "http://10.0.2.2:5000${profile.avatar_url}"
                            showAvatarImage(fullUrl)
                        }
                    }
                } else {
                    Toast.makeText(this@ProfileActivity,
                        "Could not load profile (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@ProfileActivity,
                    "Network error loading profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ============================================
    // CHANGE PHOTO
    // ============================================
    private fun handleChangePhoto() {
        // Use GetContent which doesn't need permissions - it uses system file picker
        pickImageLauncher.launch("image/*")
    }

    private fun uploadAvatar(uri: Uri) {
        binding.btnChangePhoto.isEnabled = false
        binding.btnChangePhoto.text = "Uploading..."

        try {
            // Read the selected image into a temp file
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Toast.makeText(this, "Cannot read selected image", Toast.LENGTH_SHORT).show()
                resetPhotoButton()
                return
            }

            val tempFile = File(cacheDir, "avatar_upload_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            // Build multipart body
            val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val avatarPart = MultipartBody.Part.createFormData("avatar", tempFile.name, requestBody)

            // Send to API
            RetrofitClient.getInstance(this).uploadAvatar(userId, avatarPart)
                .enqueue(object : Callback<MessageResponse> {
                    override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                        resetPhotoButton()

                        if (response.isSuccessful) {
                            Toast.makeText(this@ProfileActivity,
                                "Photo updated successfully!", Toast.LENGTH_SHORT).show()
                            // Reload profile to get new avatar URL from server
                            loadProfile()
                        } else {
                            Toast.makeText(this@ProfileActivity,
                                "Upload failed (${response.code()}). Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                        resetPhotoButton()
                        Toast.makeText(this@ProfileActivity,
                            "Network error uploading photo", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            resetPhotoButton()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetPhotoButton() {
        binding.btnChangePhoto.isEnabled = true
        binding.btnChangePhoto.text = "Change Photo"
    }

    private fun showAvatarImage(url: String) {
        binding.tvAvatarInitial.visibility = View.GONE
        binding.imgAvatar.visibility = View.VISIBLE
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.circle_avatar_bg)
            .error(R.drawable.circle_avatar_bg)
            .into(binding.imgAvatar)
    }

    // ============================================
    // SAVE PROFILE (Full Name + Academic Info)
    // ============================================
    private fun handleSaveProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()
        val major = binding.etMajor.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()

        if (fullName.isEmpty()) {
            showProfileMessage("Full name cannot be empty.", false)
            return
        }

        setSaveLoading(true)
        hideProfileMessage()

        // Step 1: Save academic profile (bio, major, school)
        val profileRequest = UpdateProfileRequest(bio, major, school)
        RetrofitClient.getInstance(this).updateProfile(userId, profileRequest)
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    if (response.isSuccessful) {
                        // Step 2: Check if name changed, update if so
                        val currentUser = sessionManager.getUser()
                        if (currentUser != null && fullName != currentUser.fullName) {
                            saveNameToServer(fullName)
                        } else {
                            setSaveLoading(false)
                            showProfileMessage("Profile saved successfully!", true)
                            autoHideMessage(true)
                        }
                    } else {
                        setSaveLoading(false)
                        showProfileMessage("Failed to save profile (${response.code()}).", false)
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    setSaveLoading(false)
                    showProfileMessage("Network error. Check your connection.", false)
                }
            })
    }

    private fun saveNameToServer(newName: String) {
        val request = UpdateNameRequest(userId, newName)
        RetrofitClient.getInstance(this).updateName(request)
            .enqueue(object : Callback<UpdateNameResponse> {
                override fun onResponse(call: Call<UpdateNameResponse>, response: Response<UpdateNameResponse>) {
                    setSaveLoading(false)

                    if (response.isSuccessful) {
                        response.body()?.user?.let { updatedUser ->
                            // Save updated user data locally
                            sessionManager.saveUser(updatedUser)

                            // Update all UI elements with new name
                            binding.tvDisplayName.text = updatedUser.fullName
                            binding.tvAvatarInitial.text =
                                updatedUser.fullName.firstOrNull()?.uppercase() ?: "U"
                        }
                        showProfileMessage("Profile and name saved successfully!", true)
                        autoHideMessage(true)
                    } else {
                        showProfileMessage("Profile saved, but name update failed.", false)
                    }
                }

                override fun onFailure(call: Call<UpdateNameResponse>, t: Throwable) {
                    setSaveLoading(false)
                    showProfileMessage("Profile saved, but name update failed (network).", false)
                }
            })
    }

    // ============================================
    // CHANGE PASSWORD
    // ============================================
    private fun handleChangePassword() {
        val currentPw = binding.etCurrentPassword.text.toString().trim()
        val newPw = binding.etNewPassword.text.toString().trim()

        // Validation
        if (currentPw.isEmpty() || newPw.isEmpty()) {
            showPasswordMessage("Please fill in both password fields.", false)
            return
        }

        if (newPw.length < 4) {
            showPasswordMessage("New password must be at least 4 characters.", false)
            return
        }

        if (currentPw == newPw) {
            showPasswordMessage("New password must be different from current.", false)
            return
        }

        setPasswordLoading(true)
        hidePasswordMessage()

        val request = ChangePasswordRequest(userId, currentPw, newPw)
        RetrofitClient.getInstance(this).changePassword(request)
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    setPasswordLoading(false)

                    if (response.isSuccessful) {
                        showPasswordMessage("Password changed successfully!", true)
                        // Clear password fields
                        binding.etCurrentPassword.setText("")
                        binding.etNewPassword.setText("")
                        autoHideMessage(false)
                    } else {
                        val errorMsg = when (response.code()) {
                            400 -> "Incorrect current password."
                            401 -> "Unauthorized. Please login again."
                            else -> "Failed to change password (${response.code()})."
                        }
                        showPasswordMessage(errorMsg, false)
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    setPasswordLoading(false)
                    showPasswordMessage("Network error. Check your connection.", false)
                }
            })
    }

    // ============================================
    // LOGOUT
    // ============================================
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                sessionManager.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ============================================
    // UI HELPERS
    // ============================================
    private fun setSaveLoading(loading: Boolean) {
        binding.btnSaveProfile.isEnabled = !loading
        binding.btnSaveProfile.text = if (loading) "Saving..." else "Save Profile"
        binding.btnSaveProfile.setBackgroundResource(
            if (loading) R.drawable.rounded_button_disabled else R.drawable.rounded_button_blue
        )
    }

    private fun setPasswordLoading(loading: Boolean) {
        binding.btnChangePassword.isEnabled = !loading
        binding.btnChangePassword.text = if (loading) "Updating..." else "Change Password"
        binding.btnChangePassword.setBackgroundResource(
            if (loading) R.drawable.rounded_button_disabled else R.drawable.rounded_button_black
        )
    }

    private fun showProfileMessage(msg: String, success: Boolean) {
        binding.tvProfileMessage.visibility = View.VISIBLE
        binding.tvProfileMessage.text = if (success) "\u2705  $msg" else "\u274C  $msg"
        binding.tvProfileMessage.setBackgroundColor(
            Color.parseColor(if (success) "#064E3B" else "#7F1D1D")
        )
        binding.tvProfileMessage.setTextColor(
            Color.parseColor(if (success) "#34D399" else "#FCA5A5")
        )
    }

    private fun hideProfileMessage() { binding.tvProfileMessage.visibility = View.GONE }

    private fun showPasswordMessage(msg: String, success: Boolean) {
        binding.tvPasswordMessage.visibility = View.VISIBLE
        binding.tvPasswordMessage.text = if (success) "\u2705  $msg" else "\u274C  $msg"
        binding.tvPasswordMessage.setBackgroundColor(
            Color.parseColor(if (success) "#064E3B" else "#7F1D1D")
        )
        binding.tvPasswordMessage.setTextColor(
            Color.parseColor(if (success) "#34D399" else "#FCA5A5")
        )
    }

    private fun hidePasswordMessage() { binding.tvPasswordMessage.visibility = View.GONE }

    private fun autoHideMessage(isProfile: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isProfile) hideProfileMessage() else hidePasswordMessage()
        }, 4000)
    }
}
