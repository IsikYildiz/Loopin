package com.example.loopin.network.Api
import com.example.loopin.models.CreateEventRequest
import com.example.loopin.models.CreateEventResponse
import com.example.loopin.models.DeleteEventRequest
import com.example.loopin.models.DeleteEventResponse
import com.example.loopin.models.EventListResponse
import com.example.loopin.models.EventParticipantsResponse
import com.example.loopin.models.EventResponse
import com.example.loopin.models.JoinEventRequest
import com.example.loopin.models.JoinEventResponse
import com.example.loopin.models.LeaveEventRequest
import com.example.loopin.models.LeaveEventResponse
import com.example.loopin.models.ParticipantCountResponse
import com.example.loopin.models.UpdateEventRequest
import com.example.loopin.models.UpdateEventResponse
import com.example.loopin.models.UpdateParticipantStatusRequest
import com.example.loopin.models.UpdateParticipantStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface EventApi {
    @POST("events")
    suspend fun createEvent(@Body request: CreateEventRequest): Response<CreateEventResponse>

    @PATCH("events/{id}")
    suspend fun updateEvent(
        @Path("id") eventId: Int,
        @Body request: UpdateEventRequest
    ): Response<UpdateEventResponse>

    @HTTP(method = "DELETE", path = "events/{id}", hasBody = true)
    suspend fun deleteEvent(
        @Path("id") eventId: Int,
        @Body request: DeleteEventRequest
    ): Response<DeleteEventResponse>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") eventId: Int): Response<EventResponse>

    @GET("events/public")
    suspend fun getPublicEvents(
        @Query("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<EventListResponse>

    @GET("events/{eventId}/group")
    suspend fun getGroupForEvent(@Path("eventId") eventId: Int): Response<com.example.loopin.models.GroupResponse>

    @GET("events/creator/{userId}")
    suspend fun getEventsCreatedByUser(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<EventListResponse>

    @GET("events/participants/past/{userId}")
    suspend fun getEventsUserParticipates(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<EventListResponse>

    @GET("events/participants/upcoming/{userId}")
    suspend fun getUpcomingEventsUserParticipates(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<EventListResponse>

    @POST("events/{eventId}/join")
    suspend fun joinEvent(@Body request: JoinEventRequest): Response<JoinEventResponse>

    @POST("events/{eventId}/leave")
    suspend fun leaveEvent(@Body request: LeaveEventRequest): Response<LeaveEventResponse>

    @GET("events/{eventId}/participants")
    suspend fun getEventParticipants(@Path("eventId") eventId: Int): Response<EventParticipantsResponse>

    @GET("events/{eventId}/participant-count")
    suspend fun getEventParticipantCount(@Path("eventId") eventId: Int): Response<ParticipantCountResponse>

    @PUT("events/participant-status")
    suspend fun updateParticipantStatus(@Body request: UpdateParticipantStatusRequest): Response<UpdateParticipantStatusResponse>

    @GET("events/search")
    suspend fun searchEvents(
        @Query("name") name: String? = null,
        @Query("location") location: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("isPrivate") isPrivate: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<EventListResponse>
}