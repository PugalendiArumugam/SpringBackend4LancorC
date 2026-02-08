package com.nexus.lancorC.back.appartmentez.auth.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException

data class GoogleTokenInfo(
    @JsonProperty("azp") val azp: String,
    val email: String,
    @JsonProperty("email_verified") val emailVerified: Boolean,
    val aud: String,
    val sub: String,
    val name: String,
    val picture: String?,
    @JsonProperty("given_name") val givenName: String,
    @JsonProperty("family_name") val familyName: String,
    val iat: Long,
    val exp: Long
)

@Service
class GoogleTokenVerificationService {
    private val log = LoggerFactory.getLogger(GoogleTokenVerificationService::class.java)
    private val restTemplate = RestTemplate()
    
    companion object {
        private const val GOOGLE_TOKENINFO_URL = "https://www.googleapis.com/oauth2/v3/tokeninfo"
        private const val CLIENT_ID = "833746638979-soqldqmuidc1gnp026ter2no7q1f3cve.apps.googleusercontent.com"
    }
    
    fun verifyGoogleToken(idToken: String): Result<GoogleTokenInfo> {
        return try {
            log.info("Verifying Google ID token")
            
            val url = "$GOOGLE_TOKENINFO_URL?id_token=$idToken"
            val tokenInfo = restTemplate.getForObject(url, GoogleTokenInfo::class.java)
            
            if (tokenInfo == null) {
                log.error("Failed to parse Google token response")
                return Result.failure(Exception("Invalid token response from Google"))
            }
            
            // Verify the token is intended for your app
            if (tokenInfo.aud != CLIENT_ID) {
                log.error("Token audience mismatch. Expected: $CLIENT_ID, Got: ${tokenInfo.aud}")
                return Result.failure(Exception("Token audience mismatch"))
            }
            
            // Check if token is expired
            val currentTime = System.currentTimeMillis() / 1000
            if (tokenInfo.exp < currentTime) {
                log.error("Token expired. Expiration: ${tokenInfo.exp}, Current: $currentTime")
                return Result.failure(Exception("Token expired"))
            }
            
            // Verify email is verified
            if (!tokenInfo.emailVerified) {
                log.error("Email not verified for user: ${tokenInfo.email}")
                return Result.failure(Exception("Email not verified"))
            }
            
            log.info("Google token verification successful for user: ${tokenInfo.email}")
            Result.success(tokenInfo)
            
        } catch (e: HttpClientErrorException) {
            log.error("HTTP error during Google token verification: ${e.statusCode} - ${e.responseBodyAsString}")
            Result.failure(Exception("Google token verification failed: ${e.statusCode}"))
        } catch (e: Exception) {
            log.error("Error during Google token verification", e)
            Result.failure(Exception("Google token verification failed: ${e.message}"))
        }
    }
}