package com.nexus.lancorC.back.AppartmentEz.auth.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory


@Service
class EmailService(
    private val mailSender: JavaMailSender
) {

    private val log = LoggerFactory.getLogger(EmailService::class.java)

    fun sendOtp(email: String, otp: String) {

        log.info("Preparing to send OTP email to {}", email)

        val message = SimpleMailMessage().apply {
            setTo(email)
            subject = "Your OTP – Lancor Courtyard"
            text = "Your OTP is $otp. Valid for 5 minutes."
        }

        mailSender.send(message)
        log.info("OTP email sent to {}", email)
    }
}