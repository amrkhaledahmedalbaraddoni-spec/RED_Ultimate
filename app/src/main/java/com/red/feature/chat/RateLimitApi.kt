package com.red.feature.chat

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

/**
 * Rate Limit API — mirrors the backend RateLimitController at /api/rate-limits.
 */
interface RateLimitApi {
    @GET("api/rate-limits")
    suspend fun getMyRateLimits(): Response<List<RateLimitStatusDto>>

    @GET("api/rate-limits/{endpoint}")
    suspend fun getEndpointRateLimit(@Path("endpoint") endpoint: String): Response<RateLimitStatusDto>
}

@JsonClass(generateAdapter = true)
data class RateLimitStatusDto(
    val endpoint: String,
    val requestCount: Int,
    val limit: Int,
    val remaining: Int,
    val resetAt: Long
)
