package com.example.loopin.models

data class CreateGroupRequest(
    val groupName: String,
    val groupDescription: String? = null,
    val createdBy: Int,
    val groupImage: String? = null
)

data class CreateGroupResponse(
    val success: Boolean,
    val groupId: Int? = null,
    val message: String? = null,
    val error: String? = null
)

data class UpdateGroupRequest(
    val groupName: String? = null,
    val groupDescription: String? = null,
    val groupImage: String? = null,
    val userId: Int
)

data class UpdateGroupResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class DeleteGroupRequest(
    val userId: Int
)

data class DeleteGroupResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class GroupResponse(
    val success: Boolean,
    val group: Group? = null,
    val message: String? = null,
    val error: String? = null
)

data class Group(
    val groupId: Int,
    val groupName: String,
    val groupDescription: String? = null,
    val groupImage: String? = null,
    val createdBy: Int,
    val creatorName: String,
    val creatorImage: String? = null,
    val memberCount: Int
)

data class UserGroupsResponse(
    val success: Boolean,
    val groups: List<GroupInfo> = emptyList(),
    val pagination: PaginationInfo,
    val error: String? = null
)

data class GroupInfo(
    val groupId: Int,
    val groupName: String,
    val groupDescription: String? = null,
    val groupImage: String? = null,
    val createdBy: Int,
    val role: String,
    val joinedAt: String,
    val lastMessage: String? = null,
    val lastMessageTime: String? = null,
    val memberCount: Int
)

data class AddGroupMemberRequest(
    val userId: Int,
    val requesterId: Int
)

data class AddGroupMemberResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class RemoveGroupMemberRequest(
    val userId: Int,
    val requesterId: Int
)

data class RemoveGroupMemberResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class GroupMembersResponse(
    val success: Boolean,
    val members: List<GroupMember> = emptyList(),
    val pagination: PaginationInfo,
    val error: String? = null
)

data class GroupMember(
    val userId: Int,
    val fullName: String,
    val userName: String,
    val profileImage: String? = null,
    val role: String,
    val joinedAt: String
)

data class GroupMessagesResponse(
    val success: Boolean,
    val messages: List<GroupMessage> = emptyList(),
    val pagination: PaginationInfo,
    val error: String? = null
)

data class GroupMessage(
    val messageId: Int,
    val senderId: Int,
    val content: String,
    val sentAt: String,
    val senderName: String,
    val senderImage: String? = null
)

data class SendGroupMessageRequest(
    val senderId: Int,
    val content: String
)

data class SendGroupMessageResponse(
    val success: Boolean,
    val messageId: Int? = null,
    val message: String? = null,
    val error: String? = null
)

data class UpdateMemberRoleRequest(
    val userId: Int,
    val newRole: String,
    val requesterId: Int
)

data class UpdateMemberRoleResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)