package com.red.feature.stories

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.http.*

interface StoryApi {
    @GET("api/stories")
    suspend fun list(): Response<List<StoryDto>>

    @POST("api/stories")
    suspend fun create(@Body req: StoryCreateRequest): Response<StoryDto>

    @Multipart
    @POST("api/media/upload")
    suspend fun upload(@Part file: MultipartBody.Part): Response<MediaUploadResponse>
}

data class StoryDto(
    val id: String, val ownerId: String, val mediaUrl: String,
    val createdAt: Long, val expiresAt: Long
)

data class StoryCreateRequest(
    val mediaUrl: String,
    val ttlMinutes: Long = 1440
)

data class MediaUploadResponse(
    val objectName: String,
    val downloadUrl: String,
    val contentType: String,
    val size: Long
)

object MediaParts {
    fun part(bytes: ByteArray, filename: String = "story.jpg", mime: String = "image/jpeg"): MultipartBody.Part {
        val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", filename, body)
    }
}
