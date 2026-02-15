package com.nexus.lancorC.back.appartmentez.auth.controller

import com.nexus.lancorC.back.appartmentez.auth.model.AuthResponse
import com.nexus.lancorC.back.appartmentez.auth.model.GoogleAuthRequest
import com.nexus.lancorC.back.appartmentez.auth.model.OtpRequest
import com.nexus.lancorC.back.appartmentez.auth.model.OtpVerifyRequest
import com.nexus.lancorC.back.appartmentez.auth.service.EmailService
import com.nexus.lancorC.back.appartmentez.auth.service.GoogleAuthService
import com.nexus.lancorC.back.appartmentez.auth.service.JwtTokenService
import com.nexus.lancorC.back.appartmentez.auth.service.OtpService
import com.nexus.lancorC.back.appartmentez.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import jakarta.validation.Valid

@RestController
@RequestMapping("/auth")
class AuthController(
    private val otpService: OtpService,
    private val emailService: EmailService,
    private val googleAuthService: GoogleAuthService,
    private val jwtTokenService: JwtTokenService,
    private val userRepository: com.nexus.lancorC.back.appartmentez.repository.UserRepository
) {
    private val log = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/request-otp")
    fun requestOtp(@Valid @RequestBody request: OtpRequest): AuthResponse {
        log.info("OTP request for email={}", request.email)

        // Use the combined validation and generation method
        val response = otpService.validateAndGenerateOtp(request.email)

        if (response.success) {
            // Change 'getOtpForEmail' to 'getRecentOtp' to match your Service
            val otp = otpService.getRecentOtp(request.email)
            if (otp != null) {
                emailService.sendOtp(request.email, otp)
            }
        }
        return response

    }

    @PostMapping("/verify-otp")
    fun verifyOtp(@Valid @RequestBody request: OtpVerifyRequest): AuthResponse {
        log.info("=== VERIFY OTP CALLED ===")
        val isValid = otpService.verify(request.email, request.otp)
        return if (isValid) {
            val user = userRepository.findByEmailIgnoreCase(request.email)
                .orElseThrow { IllegalArgumentException("User not found") }

            log.info("=== USER FOUND: userId={}, societyId={}, email={}", user.userId, user.societyId, user.email)

            // Pass both UUIDs directly
            val token = jwtTokenService.generateToken(user.userId, user.societyId, request.email)

            // Convert to String only for the AuthResponse JSON
            val response = AuthResponse.success("Login successful", token, user.userId.toString(), user.societyId.toString())
            log.info("=== AUTH RESPONSE: {}", response)
            response
        } else {
            AuthResponse.failure("Invalid OTP")
        }
    }

    @PostMapping("/google-auth")
    fun authenticateWithGoogle(@Valid @RequestBody request: GoogleAuthRequest): ResponseEntity<AuthResponse> {
        return try {
            log.info("Google authentication request for email: {}", request.email)
            val user = googleAuthService.authenticateWithGoogle(request)

            // FIX: Added user.societyId (UUID) to match the new signature
            val token = jwtTokenService.generateToken(user.userId, user.societyId, user.email)

            log.info("Google authentication successful for user: {}", user.email)
            ResponseEntity.ok(AuthResponse.success("Authentication successful", token, user.userId.toString(), user.societyId.toString()))

        } catch (e: IllegalArgumentException) {
            log.error("Google authentication failed: {}", e.message)
            ResponseEntity.badRequest().body(AuthResponse.failure(e.message ?: "Google authentication failed"))
        } catch (e: Exception) {
            log.error("Unexpected error during Google authentication", e)
            ResponseEntity.internalServerError().body(AuthResponse.failure("Internal server error"))
        }
    }
}