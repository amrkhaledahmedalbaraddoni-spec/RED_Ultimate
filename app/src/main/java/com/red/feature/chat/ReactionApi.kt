package com.red.feature.chat

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

/**
 * Message Reactions API — mirrors the backend ReactionController at /api/reactions.
 */
interface ReactionApi {
    @POST("api/reactions")
    suspend fun addReaction(@Body request: AddReactionRequest): ReactionDto

    @DELETE("api/reactions/{messageId}")
    suspend fun removeReaction(@Path("messageId") messageId: String): Response<Unit>

    @GET("api/reactions/{messageId}")
    suspend fun getReactions(@Path("messageId") messageId: String): Response<MessageReactionsDto>

    @POST("api/reactions/batch")
    suspend fun getBatchReactions(@Body messageIds: List<String>): Response<Map<String, MessageReactionsDto>>
}

@JsonClass(generateAdapter = true)
data class AddReactionRequest(
    val messageId: String,
    val emoji: String
)

@JsonClass(generateAdapter = true)
data class ReactionDto(
    val messageId: String,
    val userId: String,
    val emoji: String,
    val createdAt: Long
)

@JsonClass(generateAdapter = true)
data class MessageReactionsDto(
    val messageId: String,
    val reactions: List<ReactionSummaryDto>
)

@JsonClass(generateAdapter = true)
data class ReactionSummaryDto(
    val emoji: String,
    val count: Int,
    val hasMyReaction: Boolean
)
