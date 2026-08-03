package com.red.feature.chat

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

/**
 * Disappearing Messages API — mirrors the backend DisappearingMessageController at /api/disappearing.
 */
interface DisappearingMessageApi {
    @PUT("api/disappearing")
    suspend fun setDisappearing(@Body request: SetDisappearingRequest): Response<DisappearingConfigDto>

    @GET("api/disappearing/{conversationId}")
    suspend fun getConfig(@Path("conversationId") conversationId: String): Response<DisappearingConfigDto>
}

@JsonClass(generateAdapter = true)
data class SetDisappearingRequest(
    val conversationId: String,
    val durationSeconds: Long  // 0 = off, 86400 = 24h, 604800 = 7 days
)

@JsonClass(generateAdapter = true)
data class DisappearingConfigDto(
    val conversationId: String,
    val durationSeconds: Long,
    val enabled: Boolean
)
