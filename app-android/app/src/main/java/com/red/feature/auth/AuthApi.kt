package com.red.feature.auth

import com.red.core.models.AuthResponse
import com.red.core.models.StatusResponse
import com.red.core.models.User
import com.red.core.models.UserView
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: Map<String, String>): Response<UserView>

    @POST("api/auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<AuthResponse>

    @GET("api/auth/status")
    suspend fun getStatus(): Response<StatusResponse>

    @POST("api/admin/users/approve")
    suspend fun approveUser(@Query("email") email: String, @Query("status") status: String = "APPROVED"): Response<Unit>

    @GET("api/users/me")
    suspend fun getMyProfile(): Response<UserView>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: Map<String, String>): Response<UserView>

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: Map<String, String>): Response<Map<String, String>>
}
