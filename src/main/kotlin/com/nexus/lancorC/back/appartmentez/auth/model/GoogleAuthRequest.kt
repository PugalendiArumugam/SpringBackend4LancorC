package com.nexus.lancorC.back.appartmentez.auth.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class GoogleAuthRequest(
    @field:NotBlank(message = "ID token is required")
    val idToken: String,
    
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String
) {
    override fun toString(): String {
        return "GoogleAuthRequest{" +
                "idToken='" + (if (idToken.isNotEmpty()) "***REDACTED***" else "null") + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}'
    }
}