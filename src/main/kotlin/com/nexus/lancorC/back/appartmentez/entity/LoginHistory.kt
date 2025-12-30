package com.nexus.lancorC.back.appartmentez.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "login_history")
class LoginHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "email", nullable = false)
    val email: String,

    @Column(name = "login_time")
    val loginTime: LocalDateTime = LocalDateTime.now(),

    @Column(name = "status")
    val status: String // e.g., "SUCCESS" or "FAILED"
)