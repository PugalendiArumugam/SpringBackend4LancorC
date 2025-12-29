package com.nexus.lancorC.back.AppartmentEz.auth.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class OtpVerifyRequest(

    @field:NotBlank(message = "Email is required!!")
    val email: String,

    @field:NotBlank(message = "OTP is required!!")
    @field:Size(min = 6, max = 6, message = "OTP must be 6 digits")
    val otp: String
)