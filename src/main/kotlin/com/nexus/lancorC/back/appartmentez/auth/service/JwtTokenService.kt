package com.nexus.lancorC.back.appartmentez.auth.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import javax.crypto.SecretKey
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Key
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class JwtTokenService {
    private val log = LoggerFactory.getLogger(JwtTokenService::class.java)
    
    @Value("\${app.jwt.secret:mySecretKey}")
    private lateinit var jwtSecret: String
    
    @Value("\${app.jwt.expiration-days:10}")
    private var expirationDays: Long = 10
    
    // Use a secure key for signing
    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray(Charsets.UTF_8))
    }
    
    fun generateToken(userId: java.util.UUID, email: String): String {
        log.info("Generating JWT token for user: {}", email)
        
        val expirationTime = Instant.now().plus(expirationDays, ChronoUnit.DAYS)
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("email", email)
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(expirationTime))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()
    }
    
    fun extractUserId(token: String): String? {
        return try {
            val claims = extractAllClaims(token)
            claims.subject
        } catch (e: Exception) {
            log.error("Error extracting user ID from token", e)
            null
        }
    }
    
    fun extractEmail(token: String): String? {
        return try {
            val claims = extractAllClaims(token)
            claims["email"] as String?
        } catch (e: Exception) {
            log.error("Error extracting email from token", e)
            null
        }
    }
    
    fun isTokenValid(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)
            val expiration = claims.expiration
            !expiration.before(Date())
        } catch (e: Exception) {
            log.error("Error validating token", e)
            false
        }
    }
    
    fun extractExpiration(token: String): Date? {
        return try {
            val claims = extractAllClaims(token)
            claims.expiration
        } catch (e: Exception) {
            log.error("Error extracting expiration from token", e)
            null
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}