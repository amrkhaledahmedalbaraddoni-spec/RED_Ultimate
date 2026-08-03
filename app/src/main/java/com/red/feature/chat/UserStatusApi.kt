package com.red.feature.chat

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

/**
 * User Status API — mirrors the backend UserStatusController at /api/status.
 */
interface UserStatusApi {
    @PUT("api/status")
    suspend fun setStatus(@Body request: SetStatusRequest): Response<UserStatusDto>

    @GET("api/status/{userId}")
    suspend fun getStatus(@Path("userId") userId: String): Response<UserStatusDto>

    @DELETE("api/status")
    suspend fun clearStatus(): Response<UserStatusDto>
}

@JsonClass(generateAdapter = true)
data class SetStatusRequest(
    val emoji: String = "😊",
    val text: String = ""
)

@JsonClass(generateAdapter = true)
data class UserStatusDto(
    val userId: String,
    val emoji: String,
    val text: String,
    val updatedAt: Long
)
