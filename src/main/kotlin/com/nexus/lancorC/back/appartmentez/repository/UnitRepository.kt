package com.nexus.lancorC.back.appartmentez.repository

import com.nexus.lancorC.back.appartmentez.entity.Unit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UnitRepository : JpaRepository<Unit, UUID> {

    fun findAllBySocietyId(societyId: UUID): List<Unit>

    fun findByUnitIdAndSocietyId(unitId: UUID, societyId: UUID): Unit?

    fun existsBySocietyIdAndUnitNumberIgnoreCase(societyId: UUID, unitNumber: String): Boolean
}
