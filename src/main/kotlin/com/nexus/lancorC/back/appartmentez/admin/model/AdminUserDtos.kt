package com.nexus.lancorC.back.appartmentez.admin.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.nexus.lancorC.back.appartmentez.entity.UserType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

//data class AdminUserResponse(
//    val userId: UUID,
//    val email: String,
//    val fullName: String,
//    val phone: String,
//    val userType: UserType,
//    val isActive: Boolean,
//    val lastLogin: LocalDateTime?
//)

data class AdminUserResponse(
    @JsonProperty("user_id")
    val userId: UUID,

    val email: String,

    @JsonProperty("full_name")
    val fullName: String,

    val phone: String?,

    @JsonProperty("user_type")
    val userType: UserType,

    @JsonProperty("is_active")
    val isActive: Boolean,

    @JsonProperty("last_login")
    val lastLogin: Instant?
)

data class CreateAdminUserRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    val fullName: String, // 'full_name' from JSON will now map here automatically

    @field:NotBlank
    val phone: String,

    @field:NotNull
    val userType: UserType,

    val isActive: Boolean = true
)
