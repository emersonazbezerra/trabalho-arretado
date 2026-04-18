package br.com.trabalhoarretado.application.professional

import br.com.trabalhoarretado.application.service.ServiceOfferResponse
import kotlinx.serialization.Serializable

@Serializable
data class ProfessionalProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val city: String?,
    val state: String,
    val phone: String?,
    val avatarUrl: String?,
    val services: List<ServiceOfferResponse>,
    val averageRating: Double,
    val reviewCount: Int,
    val createdAt: String,
)
