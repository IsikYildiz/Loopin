package com.example.loopin.models

data class CreateChatRequest(
    val user1Id: Int,
    val user2Id: Int
)

data class CreateChatResponse(
    val success: Boolean,
    val chatId: Int? = null,
    val message: String? = null,
    val error: String? = null
)

data class UserChatsResponse(
    val success: Boolean,
    val chats: List<ChatInfo> = emptyList(),
    val error: String? = null
)

data class ChatInfo(
    val chatId: Int,
    val user1Id: Int,
    val user2Id: Int,
    val user1Name: String,
    val user2Name: String,
    val user1Image: String? = null,
    val user2Image: String? = null,
    val lastMessage: String? = null,
    val lastMessageTime: String? = null
)

data class ChatByIdResponse(
    val success: Boolean,
    val chat: ChatDetails? = null,
    val message: String? = null,
    val error: String? = null
)

data class ChatDetails(
    val chatId: Int,
    val user1Id: Int,
    val user2Id: Int,
    val user1Name: String,
    val user2Name: String,
    val user1Image: String? = null,
    val user2Image: String? = null
)

data class ChatMessagesResponse(
    val success: Boolean,
    val messages: List<Message> = emptyList(),
    val pagination: PaginationInfo,
    val error: String? = null
)

// Modified Message to implement BaseMessage
data class Message(
    override val messageId: Int, // Added override
    override val senderId: Int, // Added override
    override val content: String, // Added override
    override val sentAt: String, // Added override
    override val senderName: String, // Added override
    override val senderImage: String? = null // Added override
) : BaseMessage // Implement the interface

data class SendMessageRequest(
    val senderId: Int,
    val content: String
)

data class SendMessageResponse(
    val success: Boolean,
    val messageId: Int? = null,
    val message: String? = null,
    val error: String? = null
)

data class DeleteChatRequest(
    val userId: Int
)

data class DeleteChatResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class DeleteMessageRequest(
    val userId: Int
)

data class DeleteMessageResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class PaginationInfo(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)