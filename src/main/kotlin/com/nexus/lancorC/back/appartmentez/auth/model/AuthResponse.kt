package com.nexus.lancorC.back.appartmentez.auth.model

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val userId: String? = null,
    val societyId: String? = null
) {
    companion object {
        fun success(message: String, token: String? = null, userId: String? = null, societyId: String? = null) = 
            AuthResponse(true, message, token, userId, societyId)
        
        fun failure(message: String) = 
            AuthResponse(false, message)
    }
}