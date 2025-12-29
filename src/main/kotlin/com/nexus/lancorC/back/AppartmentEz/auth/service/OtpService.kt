package com.nexus.lancorC.back.AppartmentEz.auth.service

import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class OtpService {

    private data class OtpEntry(
        val otp: String,
        val expiresAt: Instant
    )

    private val store = ConcurrentHashMap<String, OtpEntry>()

    fun generate(email: String): String {
        val otp = (100000..999999).random().toString()
        store[email] = OtpEntry(
            otp,
            Instant.now().plusSeconds(300)
        )
        return otp
    }

    fun verify(email: String, otp: String): Boolean {
        val entry = store[email] ?: return false
        if (Instant.now().isAfter(entry.expiresAt)) {
            store.remove(email)
            return false
        }
        val valid = entry.otp == otp
        if (valid) store.remove(email)
        return valid
    }
}