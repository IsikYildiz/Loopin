package com.example.loopin.network.Api

import com.example.loopin.models.FriendRequest
import com.example.loopin.models.FriendResponse
import com.example.loopin.models.FriendSuggestionsResponse
import com.example.loopin.models.PendingRequestsResponse
import com.example.loopin.models.UserFriendsResponse
import retrofit2.Response
import retrofit2.http.*

interface FriendApi {
    @POST("friends/requests")
    suspend fun sendFriendRequest(@Body request: FriendRequest): Response<FriendResponse>

    // Backend router'da PUT olarak tanımlanmış
    @PUT("friends/requests/accept")
    suspend fun acceptFriendRequest(@Body request: FriendRequest): Response<FriendResponse>

    // Backend router'da PUT olarak tanımlanmış
    @PUT("friends/requests/reject")
    suspend fun rejectFriendRequest(@Body request: FriendRequest): Response<FriendResponse>

    // Backend router'da DELETE olarak tanımlanmış ve body bekliyor
    @HTTP(method = "DELETE", path = "friends/", hasBody = true)
    suspend fun removeFriend(@Body request: FriendRequest): Response<FriendResponse>

    // Backend router'da 'friends/user/:userId' olarak tanımlanmış
    @GET("friends/user/{userId}")
    suspend fun getUserFriends(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<UserFriendsResponse>

    @GET("friends/requests/{userId}")
    suspend fun getPendingFriendRequests(
        @Path("userId") userId: Int,
        @Query("type") type: String = "incoming",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PendingRequestsResponse>

    @GET("friends/suggestions/{userId}")
    suspend fun getFriendSuggestions(
        @Path("userId") userId: Int,
        @Query("limit") limit: Int = 10
    ): Response<FriendSuggestionsResponse>

    @GET("friends/search/{userId}")
    suspend fun searchFriends(
        @Path("userId") userId: Int,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<UserFriendsResponse>
}