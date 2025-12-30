package com.nexus.lancorC.back.appartmentez.auth.service

import com.nexus.lancorC.back.appartmentez.auth.model.AuthResponse
import com.nexus.lancorC.back.appartmentez.entity.LoginHistory
import com.nexus.lancorC.back.appartmentez.repository.LoginHistoryRepository
import com.nexus.lancorC.back.appartmentez.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

data class OtpEntry(
    val otp: String,
    val expiresAt: Instant
)

@Service
class OtpService(
    private val userRepository: UserRepository,
    private val loginHistoryRepository: LoginHistoryRepository
) {
    private val store = ConcurrentHashMap<String, OtpEntry>()

    // Helper for Controller to get the OTP for email sending
    fun getRecentOtp(email: String): String? = store[email]?.otp

    fun generate(email: String): String {
        val otp = (100000..999999).random().toString()
        store[email] = OtpEntry(otp, Instant.now().plusSeconds(300))
        return otp
    }

    fun verify(email: String, otp: String): Boolean {
        val entry = store[email] ?: return false

        val isValid = if (Instant.now().isAfter(entry.expiresAt)) {
            false
        } else {
            entry.otp == otp
        }

        // Record the login attempt in PostgreSQL
        loginHistoryRepository.save(
            LoginHistory(
                email = email,
                status = if (isValid) "SUCCESS" else "FAILED_INVALID_OTP"
            )
        )

        if (isValid) store.remove(email)
        return isValid
    }

    fun validateAndGenerateOtp(email: String): AuthResponse {
        val userOptional = userRepository.findByEmailIgnoreCase(email)

        if (userOptional.isEmpty) {
            return AuthResponse(success = false, message = "Invalid user: Not found")
        }

        val user = userOptional.get()
        if (!user.isActive) {
            return AuthResponse(success = false, message = "User account is inactive")
        }

        generate(email)
        return AuthResponse(success = true, message = "OTP sent to your email")
    }
}