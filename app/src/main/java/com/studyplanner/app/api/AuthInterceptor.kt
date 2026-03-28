package com.studyplanner.app.api

import android.content.Context
import com.studyplanner.app.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor that automatically adds the Bearer token
 * to every outgoing request if the user is logged in.
 *
 * This satisfies the assignment requirement:
 *   "For protected routes, always include: Authorization: Bearer <token>"
 */
class AuthInterceptor(context: Context) : Interceptor {

    private val sessionManager = SessionManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // If we have a saved token, attach it as Bearer
        sessionManager.getToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
