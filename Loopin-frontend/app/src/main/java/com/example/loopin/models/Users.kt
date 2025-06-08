package com.example.loopin.models

import java.io.Serializable

data class RegisterRequest(
    val fullName: String,
    val username: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val success: Boolean,
    val userId: Int? = null,
    val error: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val user: UserInfo? = null,
    val message: String? = null,
    val error: String? = null
)

data class UserInfo(
    val userId: Int,
    val username: String
)

data class CheckUsernameRequest(
    val username: String
)

data class CheckUsernameResponse(
    val exists: Boolean,
    val error: String? = null
)

data class CheckEmailRequest(
    val email: String
)

data class CheckEmailResponse(
    val exists: Boolean,
    val error: String? = null
)

data class UpdateProfileRequest(
    val userId: Int,
    val fullName: String? = null,
    val username: String? = null,
    val phoneNumber: String? = null,
    val location: String? = null,
    val bio: String? = null,
     val email: String? = null,
)


data class UpdateProfileResponse(
    val success: Boolean,
    val affectedRows: Int? = null,
    val message: String? = null,
    val error: String? = null
)

data class UserProfileResponse(
    val success: Boolean,
    val user: UserProfile? = null,
    val message: String? = null,
    val error: String? = null
)

data class UserProfile(
    val userId: Int,
    val fullName: String,
    val username: String,
    val email: String,
    val phoneNumber: String? = null,
    val location: String? = null,
    val bio: String? = null,
    val profileImage: String? = null
): Serializable

data class ChangePasswordRequest(
    val userId: Int,
    val currentPassword: String,
    val newPassword: String
)

data class ChangePasswordResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class DeleteAccountResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class UpdateFcmTokenResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class UpdateFcmTokenRequest(
    val userId: Int,
    val fcmToken: String
)