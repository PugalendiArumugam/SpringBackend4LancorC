package com.nexus.lancorC.back.appartmentez.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(name = "user_id")
    val userId: String,

    @Column(name = "society_id")
    val societyId: String,

    val email: String,

    @Column(name = "full_name")
    val fullName: String,

    val phone: String,

    @Column(name = "user_type")
    val userType: String,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)