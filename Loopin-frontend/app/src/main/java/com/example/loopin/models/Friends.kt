package com.example.loopin.models

data class FriendRequest(
    val senderId: Int,
    val receiverId: Int
)

data class FriendResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class UserFriendsResponse(
    val success: Boolean,
    val friends: List<Friend> = emptyList(),
    val pagination: PaginationInfo,
    val error: String? = null
)

data class Friend(
    val userId: Int,
    val fullName: String,
    val userName: String,
    val profileImage: String? = null,
    val friendshipDirection: String
)

data class PendingRequestsResponse(
    val success: Boolean,
    val requests: List<FriendRequestInfo> = emptyList(),
    val pagination: PaginationInfo,
    val error: String? = null
)

data class FriendRequestInfo(
    val userId: Int,
    val fullName: String,
    val userName: String,
    val profileImage: String? = null,
    val senderId: Int,
    val receiverId: Int
)

data class FriendSuggestionsResponse(
    val success: Boolean,
    val suggestions: List<FriendSuggestion> = emptyList(),
    val error: String? = null
)

data class FriendSuggestion(
    val userId: Int,
    val fullName: String,
    val userName: String,
    val profileImage: String? = null,
    val mutualFriends: Int
)

data class SearchFriendsRequest(
    val query: String,
    val page: Int = 1,
    val limit: Int = 20
)