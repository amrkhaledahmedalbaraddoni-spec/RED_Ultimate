package com.red.feature.chat

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

/**
 * Message Pin API — mirrors the backend PinController at /api/pins.
 */
interface PinApi {
    @POST("api/pins")
    suspend fun pinMessage(@Body request: PinMessageRequest): Response<PinnedMessageDto>

    @DELETE("api/pins/{messageId}")
    suspend fun unpinMessage(@Path("messageId") messageId: String): Response<Unit>

    @GET("api/pins/{conversationId}")
    suspend fun getPinnedMessages(@Path("conversationId") conversationId: String): Response<List<PinnedMessageDto>>
}

@JsonClass(generateAdapter = true)
data class PinMessageRequest(
    val messageId: String,
    val conversationId: String
)

@JsonClass(generateAdapter = true)
data class PinnedMessageDto(
    val messageId: String,
    val conversationId: String,
    val pinnedBy: String,
    val pinnedAt: Long
)
