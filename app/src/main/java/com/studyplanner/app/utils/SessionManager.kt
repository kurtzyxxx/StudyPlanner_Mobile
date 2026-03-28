package com.studyplanner.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.studyplanner.app.models.UserData

/**
 * Manages user session data using SharedPreferences.
 * Stores: JWT Token + User info (id, username, email, fullName)
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("StudyPlannerPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "TOKEN"
        private const val KEY_USER = "USER"
    }

    // ===== TOKEN =====

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    // ===== USER =====

    fun saveUser(user: UserData) {
        val json = Gson().toJson(user)
        prefs.edit().putString(KEY_USER, json).apply()
    }

    fun getUser(): UserData? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return Gson().fromJson(json, UserData::class.java)
    }

    // ===== SESSION STATE =====

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
