package com.nexus.lancorC.back.appartmentez.auth.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class OtpRequest(

    @field:Email(message = "Invalid email address")
    @field:NotBlank(message = "Email is required")
    val email: String
)