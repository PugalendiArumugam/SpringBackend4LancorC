package com.nexus.lancorC.back.appartmentez.admin.controller

import com.nexus.lancorC.back.appartmentez.admin.model.AdminUserResponse
import com.nexus.lancorC.back.appartmentez.admin.model.CreateAdminUserRequest
import com.nexus.lancorC.back.appartmentez.auth.service.JwtTokenService
import com.nexus.lancorC.back.appartmentez.entity.AuthProvider
import com.nexus.lancorC.back.appartmentez.entity.User
import com.nexus.lancorC.back.appartmentez.entity.UserType
import com.nexus.lancorC.back.appartmentez.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/admin/users")
class AdminUserController(
    private val userRepository: UserRepository,
    private val jwtTokenService: JwtTokenService
) {

    private val log = LoggerFactory.getLogger(AdminUserController::class.java)

    @GetMapping
    fun getUsersForAdmin(request: HttpServletRequest): List<AdminUserResponse> {
        val admin = resolveAdminFromRequest(request)
        log.info("Fetching users for admin={} society={}", admin.email, admin.societyId)
        return userRepository.findAllBySocietyId(admin.societyId).map { it.toAdminResponse() }
    }

    @PostMapping
    fun createUserForAdmin(
        request: HttpServletRequest,
        @Valid @RequestBody body: CreateAdminUserRequest
    ): ResponseEntity<AdminUserResponse> {
        val admin = resolveAdminFromRequest(request)

        if (userRepository.existsBySocietyIdAndEmailIgnoreCase(admin.societyId, body.email)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "User with email '${body.email}' already exists in this society"
            )
        }

        val user = User(
            societyId = admin.societyId,
            email = body.email,
            fullName = body.fullName,
            phone = body.phone,
            userType = body.userType,
            authProvider = AuthProvider.EMAIL,
            googleId = null,
            isActive = body.isActive
        )

        return try {
            val saved = userRepository.save(user)
            ResponseEntity.status(HttpStatus.CREATED).body(saved.toAdminResponse())
        } catch (ex: DataIntegrityViolationException) {
            log.error("Failed to create user for society={} email={}", admin.societyId, body.email, ex)
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "User violates unique or check constraints"
            )
        }
    }

    @PatchMapping("/{userId}/status")
    fun toggleUserStatus(
        request: HttpServletRequest,
        @PathVariable userId: UUID
    ): AdminUserResponse {
        val admin = resolveAdminFromRequest(request)

        val user = userRepository.findByUserIdAndSocietyId(userId, admin.societyId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found in admin's society"
                )
            }

        val updated = user.copy(isActive = !user.isActive)
        val saved = userRepository.save(updated)
        return saved.toAdminResponse()
    }

    /**
     * Extracts the authenticated user from the Bearer token and ensures they are an ADMIN.
     * Also provides the `societyId` for data isolation.
     */
    private fun resolveAdminFromRequest(request: HttpServletRequest): User {
        val authHeader = request.getHeader("Authorization")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header")

        if (!authHeader.startsWith("Bearer ", ignoreCase = true)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Authorization header")
        }

        val token = authHeader.substringAfter("Bearer ").trim()
        if (!jwtTokenService.isTokenValid(token)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token")
        }

        val userIdStr = jwtTokenService.extractUserId(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token payload")

        val userId = try {
            UUID.fromString(userIdStr)
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user identifier in token")
        }

        val user = userRepository.findByUserId(userId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
            }

        if (user.userType != UserType.ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN users can access this resource")
        }

        return user
    }

    private fun User.toAdminResponse(): AdminUserResponse =
        AdminUserResponse(
            userId = this.userId,
            email = this.email,
            fullName = this.fullName,
            phone = this.phone,
            userType = this.userType,
            isActive = this.isActive,
            lastLogin = this.lastLogin
        )
}

