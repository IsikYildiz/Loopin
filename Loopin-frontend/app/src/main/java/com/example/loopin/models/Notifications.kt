package com.example.loopin.models

data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<Notification> = emptyList(),
    val pagination: PaginationInfo,
    val error: String? = null
)

data class Notification(
    val notificationId: Int,
    val userId: Int,
    val type: String,
    val content: Map<String, Any>,
    val isRead: Boolean,
    val sentAt: String
)

data class MarkAsReadRequest(
    val userId: Int
)

data class MarkAsReadResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class UnreadCountResponse(
    val success: Boolean,
    val unreadCount: Int,
    val error: String? = null
)

data class DeleteNotificationRequest(
    val userId: Int
)

data class DeleteNotificationResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)