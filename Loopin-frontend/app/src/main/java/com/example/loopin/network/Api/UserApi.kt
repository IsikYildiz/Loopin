package com.example.loopin.network.Api
import com.example.loopin.models.ChangePasswordRequest
import com.example.loopin.models.ChangePasswordResponse
import com.example.loopin.models.CheckEmailRequest
import com.example.loopin.models.CheckEmailResponse
import com.example.loopin.models.CheckUsernameRequest
import com.example.loopin.models.CheckUsernameResponse
import com.example.loopin.models.DeleteAccountResponse
import com.example.loopin.models.LoginRequest
import com.example.loopin.models.LoginResponse
import com.example.loopin.models.RegisterRequest
import com.example.loopin.models.RegisterResponse
import com.example.loopin.models.UpdateProfileRequest
import com.example.loopin.models.UpdateProfileResponse
import com.example.loopin.models.UserProfileResponse
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @POST("users/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("users/check-username")
    suspend fun checkUsername(@Body request: CheckUsernameRequest): Response<CheckUsernameResponse>

    @POST("users/check-email")
    suspend fun checkEmail(@Body request: CheckEmailRequest): Response<CheckEmailResponse>

    @PUT("users/update-profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UpdateProfileResponse>

    @GET("users/get-profile")
    suspend fun getUserProfile(@Path("id") userId: Int): Response<UserProfileResponse>

    @DELETE("users/delete-profile")
    suspend fun deleteAccount(@Path("id") userId: Int): Response<DeleteAccountResponse>

    @PUT("users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ChangePasswordResponse>
}