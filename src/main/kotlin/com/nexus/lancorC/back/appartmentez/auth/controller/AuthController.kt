package com.nexus.lancorC.back.appartmentez.auth.controller

import com.nexus.lancorC.back.appartmentez.auth.model.AuthResponse
import com.nexus.lancorC.back.appartmentez.auth.model.OtpRequest
import com.nexus.lancorC.back.appartmentez.auth.model.OtpVerifyRequest
import com.nexus.lancorC.back.appartmentez.auth.service.EmailService
import com.nexus.lancorC.back.appartmentez.auth.service.OtpService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

import jakarta.validation.Valid

@RestController
@RequestMapping("/auth")
class AuthController(
    private val otpService: OtpService,
    private val emailService: EmailService
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

//        if (response.success) {
//            // Retrieve the OTP from the store to send the email
//            // (Or modify validateAndGenerateOtp to return the OTP string)
//            val otp = otpService.getRecentOtp(request.email)
//            if (otp != null) {
//                emailService.sendOtp(request.email, otp)
//            }
//        }
//        return response
    }

    @PostMapping("/verify-otp")
    fun verifyOtp(@Valid @RequestBody request: OtpVerifyRequest): AuthResponse {
        val isValid = otpService.verify(request.email, request.otp)
        return AuthResponse(
            success = isValid,
            message = if (isValid) "Login successful" else "Invalid OTP"
        )
    }
}