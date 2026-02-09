package com.nexus.lancorC.back.appartmentez.admin.model

import com.nexus.lancorC.back.appartmentez.entity.UserType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID

data class AdminUserResponse(
    val userId: UUID,
    val email: String,
    val fullName: String,
    val phone: String,
    val userType: UserType,
    val isActive: Boolean,
    val lastLogin: LocalDateTime?
)

data class CreateAdminUserRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    val fullName: String,

    @field:NotBlank
    val phone: String,

    @field:NotNull
    val userType: UserType,

    val isActive: Boolean = true
)

