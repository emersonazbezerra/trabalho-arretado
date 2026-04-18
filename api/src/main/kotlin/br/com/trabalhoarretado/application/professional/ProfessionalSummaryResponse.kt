package br.com.trabalhoarretado.application.professional

import kotlinx.serialization.Serializable

@Serializable
data class ProfessionalSummaryResponse(
    val id: String,
    val name: String,
    val city: String?,
    val state: String,
    val phone: String?,
    val avatarUrl: String?,
    val averageRating: Double,
    val reviewCount: Int,
)
