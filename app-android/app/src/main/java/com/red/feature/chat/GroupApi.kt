package com.red.feature.chat

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

/**
 * Group chat API — mirrors the backend GroupController at /api/groups.
 */
interface GroupApi {
    @POST("api/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<GroupDto>

    @GET("api/groups")
    suspend fun listGroups(): Response<List<GroupDto>>

    @GET("api/groups/{groupId}")
    suspend fun getGroup(@Path("groupId") groupId: String): Response<GroupDto>

    @PUT("api/groups/{groupId}")
    suspend fun updateGroup(@Path("groupId") groupId: String, @Body request: UpdateGroupRequest): Response<GroupDto>

    @POST("api/groups/{groupId}/members")
    suspend fun addMember(@Path("groupId") groupId: String, @Body request: AddMemberRequest): Response<Unit>

    @DELETE("api/groups/{groupId}/members/{userId}")
    suspend fun removeMember(@Path("groupId") groupId: String, @Path("userId") userId: String): Response<Unit>

    @POST("api/groups/{groupId}/admins")
    suspend fun promoteAdmin(@Path("groupId") groupId: String, @Body request: PromoteAdminRequest): Response<Unit>

    @DELETE("api/groups/{groupId}")
    suspend fun deleteGroup(@Path("groupId") groupId: String): Response<Unit>
}

@JsonClass(generateAdapter = true)
data class CreateGroupRequest(
    val name: String,
    val description: String = "",
    val memberIds: List<String>
)

@JsonClass(generateAdapter = true)
data class GroupDto(
    val id: String,
    val name: String,
    val description: String,
    val avatarUrl: String? = null,
    val ownerId: String,
    val memberIds: List<String>,
    val adminIds: List<String>,
    val createdAt: Long,
    val memberCount: Int
)

@JsonClass(generateAdapter = true)
data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null,
    val avatarUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class AddMemberRequest(
    val userId: String
)

@JsonClass(generateAdapter = true)
data class PromoteAdminRequest(
    val userId: String
)
