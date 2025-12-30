package com.nexus.lancorC.back.appartmentez.auth.service
//@Service
//class EmailService {
//
//    private val log = LoggerFactory.getLogger(EmailService::class.java)
//
//    fun sendOtp(email: String, otp: String) {
//        // This replaces the real mail sender with a console log
//        log.info("**************************************************")
//        log.info("DUMMY EMAIL SERVICE: Sending OTP to {}", email)
//        log.info("YOUR OTP IS: {}", otp)
//        log.info("**************************************************")
//    }
//}

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.util.Properties

@Service
class EmailService { // Removed the constructor parameter to avoid auto-config conflicts

    private val log = LoggerFactory.getLogger(EmailService::class.java)

    fun sendOtp(email: String, otp: String) {
        try {
            log.info("Starting hardcoded SMTP send to {}", email)

            val mailSender = JavaMailSenderImpl()
            mailSender.host = "smtp.gmail.com"
            mailSender.port = 465
            mailSender.username = "pugal.a@gmail.com"
            mailSender.password = "lhbqksvoltkodabi" // Your App Password

            val props: Properties = mailSender.javaMailProperties
            props["mail.transport.protocol"] = "smtp"
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.ssl.enable"] = "true" // SSL for port 465
            props["mail.smtp.socketFactory.port"] = "465"
            props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
            props["mail.debug"] = "true" // This will print the handshake in your console

            val message = SimpleMailMessage()
            message.from = "pugal.a@gmail.com"
            message.setTo(email)
            message.subject = "LancorC Apartment Login OTP"
            message.text = "Your OTP is: $otp"

            mailSender.send(message)
            log.info("SUCCESS! Email sent to {}", email)

        } catch (e: Exception) {
            log.error("HARDCODED SEND FAILED: {}", e.message)
            e.printStackTrace() // This will show exactly why it failed
        }
    }
}