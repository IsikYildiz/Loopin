package com.example.loopin

import com.example.loopin.network.ApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module // Marks this class as a Dagger module
@InstallIn(SingletonComponent::class) // Specifies the scope/component where these bindings are installed
object NetworkModule { // Use 'object' if ApiClient is truly a singleton object

    @Singleton // Makes ApiClient a singleton in the Hilt graph
    @Provides // Tells Dagger Hilt how to provide an instance of ApiClient
    fun provideApiClient(): ApiClient {
        return ApiClient // Since ApiClient is a Kotlin 'object', we just return its instance
    }
}