package com.nexus.lancorC.back.appartmentez.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "units")
data class Unit(
    @Id
    @Column(name = "unit_id")
    val unitId: UUID = UUID.randomUUID(),

    @Column(name = "society_id")
    val societyId: UUID,

    @Column(name = "unit_number", nullable = false)
    val unitNumber: String,

    @Column(name = "block_name")
    val blockName: String? = null,

    @Column(name = "unit_type")
    val unitType: String,

    @Column(name = "floor_number")
    val floorNumber: Int,

    @Column(name = "built_up_area", columnDefinition = "DOUBLE PRECISION")
    val builtUpArea: Double? = null,

    @Column(name = "status")
    val status: String = "VACANT"
)
