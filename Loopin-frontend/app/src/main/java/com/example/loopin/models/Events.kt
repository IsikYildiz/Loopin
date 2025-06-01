package com.example.loopin.models

data class CreateEventRequest(
    val creatorId: Int,
    val eventName: String,
    val eventLocation: String? = null,
    val startTime: String,
    val endTime: String,
    val description: String? = null,
    val maxParticipants: Int,
    val isPrivate: Boolean = false,
    val password: String? = null
)

data class CreateEventResponse(
    val success: Boolean,
    val eventId: Int? = null,
    val message: String? = null,
    val error: String? = null
)

data class UpdateEventRequest(
    val eventName: String? = null,
    val eventLocation: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val description: String? = null,
    val maxParticipants: Int? = null,
    val isPrivate: Boolean? = null,
    val password: String? = null
)

data class UpdateEventResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class DeleteEventRequest(
    val userId: Int
)

data class DeleteEventResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class EventResponse(
    val success: Boolean,
    val event: Event? = null,
    val message: String? = null,
    val error: String? = null
)

data class Event(
    val eventId: Int,
    val creatorId: Int,
    val eventName: String,
    val eventLocation: String? = null,
    val startTime: String,
    val endTime: String,
    val description: String? = null,
    val createdAt: String,
    val maxParticipants: Int,
    val isPrivate: Boolean,
    val password: String? = null
)

data class EventListResponse(
    val success: Boolean,
    val events: List<Event> = emptyList(),
    val pagination: PaginationInfo,
    val error: String? = null
)

data class JoinEventRequest(
    val eventId: Int,
    val userId: Int,
    val password: String? = null
)

data class JoinEventResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class LeaveEventRequest(
    val eventId: Int,
    val userId: Int
)

data class LeaveEventResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class EventParticipantsResponse(
    val success: Boolean,
    val participants: List<Participant> = emptyList(),
    val error: String? = null
)

data class Participant(
    val id: Int,
    val name: String,
    val status: String,
    val joinedAt: String
)

data class ParticipantCountResponse(
    val success: Boolean,
    val participantCount: Int,
    val error: String? = null
)

data class UpdateParticipantStatusRequest(
    val eventId: Int,
    val userId: Int,
    val status: String
)

data class UpdateParticipantStatusResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class SearchEventsRequest(
    val name: String? = null,
    val location: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val isPrivate: Boolean? = null,
    val page: Int = 1,
    val limit: Int = 10
)