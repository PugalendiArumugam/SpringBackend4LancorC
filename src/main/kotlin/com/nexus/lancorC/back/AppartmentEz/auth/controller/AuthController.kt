package com.nexus.lancorC.back.AppartmentEz.auth.controller

import com.nexus.lancorC.back.AppartmentEz.auth.model.AuthResponse
import com.nexus.lancorC.back.AppartmentEz.auth.model.OtpRequest
import com.nexus.lancorC.back.AppartmentEz.auth.model.OtpVerifyRequest
import com.nexus.lancorC.back.AppartmentEz.auth.service.EmailService
import com.nexus.lancorC.back.AppartmentEz.auth.service.OtpService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val otpService: OtpService
    // private val emailService: EmailService   // keep commented for now
) {

    private val log = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/request-otp")
    fun requestOtp(
        @Valid @RequestBody request: OtpRequest
    ): AuthResponse {

        log.info("Received OTP request for email={}", request.email)

        val otp = otpService.generate(request.email)

        log.info("OTP generated for email={} otp={}", request.email, otp)

        // Uncomment later when mail is enabled
        // emailService.sendOtp(request.email, otp)

        return AuthResponse(
            success = true,
            message = "OTP sent to email"
        )
    }

    @PostMapping("/verify-otp")
    fun verifyOtp(
        @Valid @RequestBody request: OtpVerifyRequest
    ): AuthResponse {

        log.info("Verifying OTP for email={}", request.email)

        val isValid = otpService.verify(
            request.email,
            request.otp
        )

        log.info(
            "OTP verification result for email={} success={}",
            request.email,
            isValid
        )

        return AuthResponse(
            success = isValid,
            message = if (isValid) "Login successful" else "Invalid OTP"
        )
    }
}