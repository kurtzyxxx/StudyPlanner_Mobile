package com.studyplanner.app.models

// ========================
// RESPONSE DATA MODELS
// ========================

data class UserData(
    val id: Int,
    val username: String,
    val email: String,
    val fullName: String
)

data class LoginResponse(
    val message: String,
    val token: String,
    val user: UserData
)

data class RegisterResponse(
    val message: String,
    val token: String,
    val user: UserData
)

data class ProfileResponse(
    val id: Int,
    val user_id: Int,
    val bio: String?,
    val major: String?,
    val school: String?,
    val avatar_url: String?,
    val updated_at: String?
)

data class MessageResponse(
    val message: String
)

data class UpdateNameResponse(
    val message: String,
    val user: UserData
)
