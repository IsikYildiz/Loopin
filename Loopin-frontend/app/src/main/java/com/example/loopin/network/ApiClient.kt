package com.example.loopin.network

import com.example.loopin.network.Api.ChatApi
import com.example.loopin.network.Api.EventApi
import com.example.loopin.network.Api.FriendApi
import com.example.loopin.network.Api.GroupApi
import com.example.loopin.network.Api.NotificationApi
import com.example.loopin.network.Api.UserApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://localhost:3000/"

    private val retrofit: Retrofit by lazy {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val chatApi: ChatApi by lazy {
        retrofit.create(ChatApi::class.java)
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }

    val groupApi: GroupApi by lazy {
        retrofit.create(GroupApi::class.java)
    }

    val friendApi: FriendApi by lazy {
        retrofit.create(FriendApi::class.java)
    }

    val eventApi: EventApi by lazy {
        retrofit.create(EventApi::class.java)
    }

    val notificationApi: NotificationApi by lazy {
        retrofit.create(NotificationApi::class.java)
    }
}