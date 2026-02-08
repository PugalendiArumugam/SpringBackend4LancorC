package com.nexus.lancorC.back.appartmentez.entity

import jakarta.persistence.*
import java.time.LocalDateTime

import java.util.UUID
import org.hibernate.annotations.Type

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(name = "user_id")
    val userId: UUID = UUID.randomUUID(),

    @Column(name = "society_id")
    val societyId: UUID,

    @Column(name = "email")
    val email: String,

    @Column(name = "full_name")
    val fullName: String,

    @Column(name = "phone")
    val phone: String,

    @Column(name = "user_type")
    val userType: String,

    @Column(name = "auth_provider")
    @Enumerated(EnumType.STRING)
    val authProvider: AuthProvider = AuthProvider.EMAIL,

    @Column(name = "google_id", unique = true)
    val googleId: String? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "last_login")
    val lastLogin: LocalDateTime? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class AuthProvider {
    EMAIL,
    GOOGLE
}