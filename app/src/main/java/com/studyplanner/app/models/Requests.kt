package com.studyplanner.app.models

// ========================
// REQUEST DATA MODELS
// ========================

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val username: String,
    val email: String,
    val password: String
)

data class UpdateProfileRequest(
    val bio: String,
    val major: String,
    val school: String
)

data class ChangePasswordRequest(
    val userId: Int,
    val currentPassword: String,
    val newPassword: String
)

data class UpdateNameRequest(
    val userId: Int,
    val newName: String
)
