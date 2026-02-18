package com.nexus.lancorC.back.appartmentez.repository

import com.nexus.lancorC.back.appartmentez.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {

    // Existing method
    fun findByEmailIgnoreCase(email: String): Optional<User>

    // Add this line to fix the "UserAuthService" error
    fun existsByEmailAndIsActiveTrue(email: String): Boolean

    fun findAllBySocietyId(societyId: UUID): List<User>

    fun findByUserId(userId: UUID): Optional<User>

    fun findByUserIdAndSocietyId(userId: UUID, societyId: UUID): Optional<User>

    fun existsBySocietyIdAndEmailIgnoreCase(societyId: UUID, email: String): Boolean
}