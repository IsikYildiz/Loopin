package com.example.loopin.network.Api
import com.example.loopin.models.AddGroupMemberRequest
import com.example.loopin.models.AddGroupMemberResponse
import com.example.loopin.models.CreateGroupRequest
import com.example.loopin.models.CreateGroupResponse
import com.example.loopin.models.DeleteGroupRequest
import com.example.loopin.models.DeleteGroupResponse
import com.example.loopin.models.GroupMembersResponse
import com.example.loopin.models.GroupMessagesResponse
import com.example.loopin.models.GroupResponse
import com.example.loopin.models.RemoveGroupMemberRequest
import com.example.loopin.models.RemoveGroupMemberResponse
import com.example.loopin.models.SendGroupMessageRequest
import com.example.loopin.models.SendGroupMessageResponse
import com.example.loopin.models.UpdateGroupRequest
import com.example.loopin.models.UpdateGroupResponse
import com.example.loopin.models.UpdateMemberRoleRequest
import com.example.loopin.models.UpdateMemberRoleResponse
import com.example.loopin.models.UserGroupsResponse
import retrofit2.Response
import retrofit2.http.*

interface GroupApi {
    @POST("groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<CreateGroupResponse>

    @PATCH("groups/{groupId}")
    suspend fun updateGroup(
        @Path("groupId") groupId: Int,
        @Body request: UpdateGroupRequest
    ): Response<UpdateGroupResponse>

    @DELETE("groups/{groupId}")
    suspend fun deleteGroup(
        @Path("groupId") groupId: Int,
        @Body request: DeleteGroupRequest
    ): Response<DeleteGroupResponse>

    @GET("groups/{groupId}")
    suspend fun getGroupById(@Path("groupId") groupId: Int): Response<GroupResponse>

    @GET("groups/user/{userId}")
    suspend fun getUserGroups(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<UserGroupsResponse>

    @POST("groups/{groupId}/members")
    suspend fun addGroupMember(
        @Path("groupId") groupId: Int,
        @Body request: AddGroupMemberRequest
    ): Response<AddGroupMemberResponse>

    @DELETE("groups/{groupId}/members")
    suspend fun removeGroupMember(
        @Path("groupId") groupId: Int,
        @Body request: RemoveGroupMemberRequest
    ): Response<RemoveGroupMemberResponse>

    @GET("groups/{groupId}/members")
    suspend fun getGroupMembers(
        @Path("groupId") groupId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<GroupMembersResponse>

    @GET("groups/{groupId}/messages")
    suspend fun getGroupMessages(
        @Path("groupId") groupId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<GroupMessagesResponse>

    @POST("groups/{groupId}/messages")
    suspend fun sendGroupMessage(
        @Path("groupId") groupId: Int,
        @Body request: SendGroupMessageRequest
    ): Response<SendGroupMessageResponse>

    @PUT("groups/{groupId}/members/role")
    suspend fun updateMemberRole(
        @Path("groupId") groupId: Int,
        @Body request: UpdateMemberRoleRequest
    ): Response<UpdateMemberRoleResponse>
}