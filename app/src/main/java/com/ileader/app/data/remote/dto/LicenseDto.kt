package com.ileader.app.data.remote.dto

import com.ileader.app.data.models.License
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LicenseDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val number: String? = null,
    val category: String? = null,
    @SerialName("class") val licenseClass: String? = null,
    val federation: String? = null,
    val status: String? = null,
    @SerialName("issue_date") val issueDate: String? = null,
    @SerialName("expiry_date") val expiryDate: String? = null,
    @SerialName("medical_check_date") val medicalCheckDate: String? = null,
    @SerialName("medical_check_expiry") val medicalCheckExpiry: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    fun toDomain(): License = License(
        number = number ?: "",
        category = category ?: "",
        issueDate = issueDate ?: "",
        expiryDate = expiryDate ?: "",
        status = status ?: "",
        className = licenseClass ?: "",
        federation = federation ?: "",
        medicalCheckDate = medicalCheckDate ?: "",
        medicalCheckExpiry = medicalCheckExpiry ?: ""
    )
}
