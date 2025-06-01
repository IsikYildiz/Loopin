package com.example.loopin.network.Api
import com.example.loopin.models.DeleteNotificationRequest
import com.example.loopin.models.DeleteNotificationResponse
import com.example.loopin.models.MarkAsReadRequest
import com.example.loopin.models.MarkAsReadResponse
import com.example.loopin.models.NotificationsResponse
import com.example.loopin.models.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.*

interface NotificationApi {
    @GET("notifications/{userId}")
    suspend fun getUserNotifications(
        @Path("userId") userId: Int,
        @Query("isRead") isRead: Boolean? = null,
        @Query("type") type: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<NotificationsResponse>

    @PUT("notifications/{notificationId}/read")
    suspend fun markAsRead(
        @Path("notificationId") notificationId: Int,
        @Body request: MarkAsReadRequest
    ): Response<MarkAsReadResponse>

    @PUT("notifications/mark-all-read")
    suspend fun markAllAsRead(@Body request: MarkAsReadRequest): Response<MarkAsReadResponse>

    @GET("notifications/{userId}/unread-count")
    suspend fun getUnreadCount(@Path("userId") userId: Int): Response<UnreadCountResponse>

    @DELETE("notifications/{notificationId}")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: Int,
        @Body request: DeleteNotificationRequest
    ): Response<DeleteNotificationResponse>
}