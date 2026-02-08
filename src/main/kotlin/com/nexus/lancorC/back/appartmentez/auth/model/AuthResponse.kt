package com.nexus.lancorC.back.appartmentez.auth.model

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null, // JWT token for authenticated sessions
    val userId: String? = null // User ID for client reference
) {
    companion object {
        fun success(message: String, token: String? = null, userId: String? = null) = 
            AuthResponse(true, message, token, userId)
        
        fun failure(message: String) = 
            AuthResponse(false, message)
    }
}