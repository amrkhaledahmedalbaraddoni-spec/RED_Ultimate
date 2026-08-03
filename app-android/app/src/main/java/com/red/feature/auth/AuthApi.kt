package com.red.feature.auth

import com.red.core.models.AuthResponse
import com.red.core.models.StatusResponse
import com.red.core.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: Map<String, String>): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<AuthResponse>

    @GET("api/auth/status")
    suspend fun getStatus(): Response<StatusResponse>

    @POST("api/admin/users/approve")
    suspend fun approveUser(@Query("email") email: String, @Query("status") status: String = "APPROVED"): Response<Unit>
}
