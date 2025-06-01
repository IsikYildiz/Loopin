package com.example.loopin.network.Api
import com.example.loopin.models.ChatByIdResponse
import com.example.loopin.models.ChatMessagesResponse
import com.example.loopin.models.CreateChatRequest
import com.example.loopin.models.CreateChatResponse
import com.example.loopin.models.DeleteChatRequest
import com.example.loopin.models.DeleteChatResponse
import com.example.loopin.models.DeleteMessageRequest
import com.example.loopin.models.DeleteMessageResponse
import com.example.loopin.models.SendMessageRequest
import com.example.loopin.models.SendMessageResponse
import com.example.loopin.models.UserChatsResponse
import retrofit2.Response
import retrofit2.http.*

interface ChatApi {
    @POST("chats")
    suspend fun createChat(@Body request: CreateChatRequest): Response<CreateChatResponse>

    @GET("chats/user/{userId}")
    suspend fun getUserChats(@Path("userId") userId: Int): Response<UserChatsResponse>

    @GET("chats/{chatId}")
    suspend fun getChatById(@Path("chatId") chatId: Int): Response<ChatByIdResponse>

    @GET("chats/{chatId}/messages")
    suspend fun getChatMessages(
        @Path("chatId") chatId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ChatMessagesResponse>

    @POST("chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: Int,
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>

    @DELETE("chats/{chatId}")
    suspend fun deleteChat(
        @Path("chatId") chatId: Int,
        @Body request: DeleteChatRequest
    ): Response<DeleteChatResponse>

    @DELETE("chats/{chatId}/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("chatId") chatId: Int,
        @Path("messageId") messageId: Int,
        @Body request: DeleteMessageRequest
    ): Response<DeleteMessageResponse>
}