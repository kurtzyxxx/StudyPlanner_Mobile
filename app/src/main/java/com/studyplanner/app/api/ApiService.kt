package com.studyplanner.app.api

import com.studyplanner.app.models.LoginRequest
import com.studyplanner.app.models.LoginResponse
import com.studyplanner.app.models.RegisterRequest
import com.studyplanner.app.models.RegisterResponse
import com.studyplanner.app.models.ChangePasswordRequest
import com.studyplanner.app.models.UpdateNameRequest
import com.studyplanner.app.models.UpdateNameResponse
import com.studyplanner.app.models.ProfileResponse
import com.studyplanner.app.models.UpdateProfileRequest
import com.studyplanner.app.models.MessageResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("auth/change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<MessageResponse>

    @POST("auth/update-name")
    fun updateName(@Body request: UpdateNameRequest): Call<UpdateNameResponse>

    @GET("profile/{userId}")
    fun getProfile(@Path("userId") userId: Int): Call<ProfileResponse>

    @POST("profile/{userId}")
    fun updateProfile(
        @Path("userId") userId: Int,
        @Body request: UpdateProfileRequest
    ): Call<MessageResponse>

    @Multipart
    @POST("profile/{userId}/avatar")
    fun uploadAvatar(
        @Path("userId") userId: Int,
        @Part avatar: MultipartBody.Part
    ): Call<MessageResponse>
}
