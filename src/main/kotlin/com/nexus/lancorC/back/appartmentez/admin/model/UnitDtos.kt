package com.nexus.lancorC.back.appartmentez.admin.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.util.UUID

data class UnitResponse(
    @JsonProperty("unit_id")
    val unitId: UUID,

    @JsonProperty("society_id")
    val societyId: UUID,

    @JsonProperty("unit_number")
    val unitNumber: String,

    @JsonProperty("block_name")
    val blockName: String?,

    @JsonProperty("unit_type")
    val unitType: String,

    @JsonProperty("floor_number")
    val floorNumber: Int,

    @JsonProperty("built_up_area")
    val builtUpArea: Double?,

    val status: String
)

data class CreateUnitRequest(
    @field:NotBlank
    @JsonProperty("unit_number")
    val unitNumber: String,

    @JsonProperty("block_name")
    val blockName: String? = null,

    @field:NotBlank
    @JsonProperty("unit_type")
    val unitType: String,

    @field:NotNull
    @field:Positive
    @JsonProperty("floor_number")
    val floorNumber: Int,

    @JsonProperty("built_up_area")
    val builtUpArea: Double? = null,

    val status: String = "vacant"
)

data class UpdateUnitRequest(
    @JsonProperty("unit_number")
    val unitNumber: String? = null,

    @JsonProperty("block_name")
    val blockName: String? = null,

    @JsonProperty("unit_type")
    val unitType: String? = null,

    @JsonProperty("floor_number")
    val floorNumber: Int? = null,

    @JsonProperty("built_up_area")
    val builtUpArea: Double? = null,

    val status: String? = null
)

data class UpdateUnitStatusRequest(
    @field:NotBlank
    val status: String
)
