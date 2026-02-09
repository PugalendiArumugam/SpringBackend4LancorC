package com.nexus.lancorC.back.appartmentez.auth.service

import com.nexus.lancorC.back.appartmentez.auth.model.GoogleAuthRequest
import com.nexus.lancorC.back.appartmentez.entity.AuthProvider
import com.nexus.lancorC.back.appartmentez.entity.User
import com.nexus.lancorC.back.appartmentez.entity.UserType
import com.nexus.lancorC.back.appartmentez.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class GoogleAuthService(
    private val userRepository: UserRepository,
    private val googleTokenVerificationService: GoogleTokenVerificationService
) {
    private val log = LoggerFactory.getLogger(GoogleAuthService::class.java)
    
    fun authenticateWithGoogle(request: GoogleAuthRequest): User {
        log.info("Google authentication request for email: {}", request.email)
        
        // First verify the Google ID token
        val tokenInfoResult = googleTokenVerificationService.verifyGoogleToken(request.idToken)
        if (tokenInfoResult.isFailure) {
            log.error("Google token verification failed: {}", tokenInfoResult.exceptionOrNull()?.message)
            throw IllegalArgumentException("Invalid Google token: ${tokenInfoResult.exceptionOrNull()?.message}")
        }
        
        val tokenInfo = tokenInfoResult.getOrThrow()
        
        // Verify that the email in the request matches the token
        if (request.email != tokenInfo.email) {
            log.error("Email mismatch. Request: {}, Token: {}", request.email, tokenInfo.email)
            throw IllegalArgumentException("Email mismatch between request and Google token")
        }
        
        // Create or update user
        val user = createOrUpdateUser(request.email, request.name, tokenInfo)
        log.info("Google authentication successful for user: {}", user.email)
        
        return user
    }
    
    private fun createOrUpdateUser(email: String, name: String, tokenInfo: com.nexus.lancorC.back.appartmentez.auth.service.GoogleTokenInfo): User {
        val existingUser = userRepository.findByEmailIgnoreCase(email)
        
        return if (existingUser.isPresent) {
            // Update existing user with Google info
            val user = existingUser.get()
            if (user.authProvider != AuthProvider.GOOGLE) {
                log.info("Updating existing user to Google auth: {}", email)
                // User exists with EMAIL provider, update to GOOGLE
                user.copy(
                    authProvider = AuthProvider.GOOGLE,
                    googleId = tokenInfo.sub,
                    fullName = name, // Update name from Google
                    lastLogin = LocalDateTime.now()
                )
            } else {
                // User already has Google auth, just update last login
                user.copy(
                    fullName = name, // Update name from Google
                    lastLogin = LocalDateTime.now()
                )
            }.also { updatedUser ->
                userRepository.save(updatedUser)
            }
        } else {
            // Create new user
            log.info("Creating new Google user: {}", email)
            val newUser = User(
                userId = UUID.randomUUID(),
                societyId = UUID.randomUUID(), // TODO: handle society assignment appropriately
                email = email,
                fullName = name,
                phone = "", // Empty phone for Google users initially
                userType = UserType.TENANT, // Default user type constrained by DB enum
                authProvider = AuthProvider.GOOGLE,
                googleId = tokenInfo.sub,
                lastLogin = LocalDateTime.now()
            )
            userRepository.save(newUser)
        }
    }
}