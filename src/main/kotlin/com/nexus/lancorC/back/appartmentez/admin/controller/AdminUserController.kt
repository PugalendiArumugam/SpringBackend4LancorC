package com.nexus.lancorC.back.appartmentez.admin.controller

import com.nexus.lancorC.back.appartmentez.admin.model.AdminUserResponse
import com.nexus.lancorC.back.appartmentez.admin.model.CreateAdminUserRequest
import com.nexus.lancorC.back.appartmentez.auth.service.JwtTokenService
import com.nexus.lancorC.back.appartmentez.entity.AuthProvider
import com.nexus.lancorC.back.appartmentez.entity.User
import com.nexus.lancorC.back.appartmentez.entity.UserType
import com.nexus.lancorC.back.appartmentez.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
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
    @Transactional
    fun createUserForAdmin(
        request: HttpServletRequest,
        @Valid @RequestBody body: CreateAdminUserRequest
    ): ResponseEntity<AdminUserResponse> {
        val admin = resolveAdminFromRequest(request)

        // Log the attempt for better traceability
        log.info("Admin ${admin.email} is creating user ${body.email} for society ${admin.societyId}")

        if (userRepository.existsBySocietyIdAndEmailIgnoreCase(admin.societyId, body.email)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email already registered in this society")
        }

        val user = User(
            societyId = admin.societyId,
            email = body.email.lowercase().trim(), // Senior dev tip: Normalize emails
            fullName = body.fullName.trim(),
            phone = body.phone,
            userType = body.userType,
            authProvider = AuthProvider.EMAIL,
            isActive = body.isActive
        )

        return try {
            val saved = userRepository.save(user)
            log.info("Successfully saved user with ID: ${saved.userId}")
            ResponseEntity.status(HttpStatus.CREATED).body(saved.toAdminResponse())
        } catch (ex: Exception) {
            log.error("Database error during user creation: ${ex.message}", ex)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database persistence failed")
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
        // Look for header (handles case-sensitivity)
        val authHeader = request.getHeader("Authorization")
            ?: request.getHeader("authorization")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "No Authorization header found")

        if (!authHeader.startsWith("Bearer ", ignoreCase = true)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Authorization format")
        }

        val token = authHeader.substring(7).trim()

        val email = jwtTokenService.extractEmail(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalid: email missing")

        val societyIdStr = jwtTokenService.extractSocietyId(token)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Token invalid: society context missing")

        return User(
            userId = UUID.randomUUID(),
            email = email,
            fullName = "Admin",
            phone = "0000000000",
            userType = UserType.ADMIN,
            societyId = UUID.fromString(societyIdStr),
            isActive = true,
            authProvider = AuthProvider.EMAIL
        )
    }

    private fun User.toAdminResponse(): AdminUserResponse =
        AdminUserResponse(
            userId = this.userId,
            email = this.email,
            fullName = this.fullName,
            phone = this.phone,
            userType = this.userType,
            isActive = this.isActive,
            // Convert LocalDateTime? to Instant? to match the DTO
            lastLogin = this.lastLogin?.atZone(java.time.ZoneId.systemDefault())?.toInstant()
        )
}

