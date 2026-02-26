package com.nexus.lancorC.back.appartmentez.admin.controller

import com.nexus.lancorC.back.appartmentez.admin.model.CreateUnitRequest
import com.nexus.lancorC.back.appartmentez.admin.model.UnitResponse
import com.nexus.lancorC.back.appartmentez.admin.model.UpdateUnitRequest
import com.nexus.lancorC.back.appartmentez.admin.model.UpdateUnitStatusRequest
import com.nexus.lancorC.back.appartmentez.auth.service.JwtTokenService
import com.nexus.lancorC.back.appartmentez.entity.Unit
import com.nexus.lancorC.back.appartmentez.entity.User
import com.nexus.lancorC.back.appartmentez.entity.UserType
import com.nexus.lancorC.back.appartmentez.repository.UnitRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/admin/units")
class AdminUnitController(
    private val unitRepository: UnitRepository,
    private val jwtTokenService: JwtTokenService
) {

    private val log = LoggerFactory.getLogger(AdminUnitController::class.java)

    @GetMapping
    fun getUnitsForAdmin(request: HttpServletRequest): List<UnitResponse> {
        val admin = resolveAdminFromRequest(request)
        log.info("Fetching units for admin={} society={}", admin.email, admin.societyId)
        return unitRepository.findAllBySocietyId(admin.societyId).map { it.toUnitResponse() }
    }

    @PostMapping
    @Transactional
    fun createUnitForAdmin(
        request: HttpServletRequest,
        @Valid @RequestBody body: CreateUnitRequest
    ): ResponseEntity<UnitResponse> {
        val admin = resolveAdminFromRequest(request)

        log.info("Admin ${admin.email} is creating unit ${body.unitNumber} for society ${admin.societyId}")

        if (unitRepository.existsBySocietyIdAndUnitNumberIgnoreCase(admin.societyId, body.unitNumber.trim())) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Unit number already exists in this society")
        }

        val unit = Unit(
            societyId = admin.societyId,
            unitNumber = body.unitNumber.trim(),
            blockName = body.blockName?.trim(),
            unitType = body.unitType,
            floorNumber = body.floorNumber,
            builtUpArea = body.builtUpArea,
            status = body.status
        )

        return try {
            val saved = unitRepository.save(unit)
            log.info("Successfully saved unit with ID: ${saved.unitId}")
            ResponseEntity.status(HttpStatus.CREATED).body(saved.toUnitResponse())
        } catch (ex: Exception) {
            log.error("Database error during unit creation: ${ex.message}", ex)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database persistence failed")
        }
    }

    @PutMapping("/{unitId}")
    @Transactional
    fun updateUnitForAdmin(
        request: HttpServletRequest,
        @PathVariable unitId: UUID,
        @Valid @RequestBody body: UpdateUnitRequest
    ): ResponseEntity<UnitResponse> {
        val admin = resolveAdminFromRequest(request)

        val existingUnit = unitRepository.findByUnitIdAndSocietyId(unitId, admin.societyId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found in admin's society")

        if (body.unitNumber != null && body.unitNumber.trim() != existingUnit.unitNumber) {
            if (unitRepository.existsBySocietyIdAndUnitNumberIgnoreCase(admin.societyId, body.unitNumber.trim())) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Unit number already exists in this society")
            }
        }

        val updatedUnit = existingUnit.copy(
            unitNumber = body.unitNumber?.trim() ?: existingUnit.unitNumber,
            blockName = body.blockName?.trim() ?: existingUnit.blockName,
            unitType = body.unitType ?: existingUnit.unitType,
            floorNumber = body.floorNumber ?: existingUnit.floorNumber,
            builtUpArea = body.builtUpArea ?: existingUnit.builtUpArea,
            status = body.status ?: existingUnit.status
        )

        val saved = unitRepository.save(updatedUnit)
        log.info("Successfully updated unit with ID: ${saved.unitId}")
        return ResponseEntity.ok(saved.toUnitResponse())
    }

    @PatchMapping("/{unitId}/status")
    fun updateUnitStatus(
        request: HttpServletRequest,
        @PathVariable unitId: UUID,
        @Valid @RequestBody body: UpdateUnitStatusRequest
    ): UnitResponse {
        val admin = resolveAdminFromRequest(request)

        val unit = unitRepository.findByUnitIdAndSocietyId(unitId, admin.societyId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found in admin's society")

        val updated = unit.copy(status = body.status)
        val saved = unitRepository.save(updated)
        log.info("Successfully updated unit status for unit ID: ${saved.unitId}")
        return saved.toUnitResponse()
    }

    @DeleteMapping("/{unitId}")
    @Transactional
    fun deleteUnit(
        request: HttpServletRequest,
        @PathVariable unitId: UUID
    ): ResponseEntity<Unit> {
        val admin = resolveAdminFromRequest(request)

        val unit = unitRepository.findByUnitIdAndSocietyId(unitId, admin.societyId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found in admin's society")

        unitRepository.delete(unit)
        log.info("Successfully deleted unit with ID: ${unitId}")
        return ResponseEntity.noContent().build()
    }

    private fun resolveAdminFromRequest(request: HttpServletRequest): User {
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
            authProvider = com.nexus.lancorC.back.appartmentez.entity.AuthProvider.EMAIL
        )
    }
}

private fun Unit.toUnitResponse(): UnitResponse =
    UnitResponse(
        unitId = this.unitId,
        societyId = this.societyId,
        unitNumber = this.unitNumber,
        blockName = this.blockName,
        unitType = this.unitType,
        floorNumber = this.floorNumber,
        builtUpArea = this.builtUpArea,
        status = this.status
    )
