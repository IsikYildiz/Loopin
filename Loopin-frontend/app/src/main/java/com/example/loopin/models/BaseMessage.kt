// In com.example.loopin.models package (e.g., BaseMessage.kt)

package com.example.loopin.models

// Mark the interface as sealed and explicitly declare equals and hashCode
sealed interface BaseMessage {
    val messageId: Int
    val senderId: Int
    val content: String
    val sentAt: String
    val senderName: String
    val senderImage: String?

    // Explicitly declare equals and hashCode.
    // Data classes implementing this interface will automatically override these.
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}