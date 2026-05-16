package br.com.trabalhoarretado.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfessionalSummaryDto(
    val id: String,
    val name: String,
    val city: String? = null,
    val state: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
)

@Serializable
data class PaginationDto(
    val page: Int,
    val size: Int,
    val total: Long,
    val totalPages: Int,
)

@Serializable
data class PaginatedProfessionalsDto(
    val data: List<ProfessionalSummaryDto>,
    val pagination: PaginationDto,
)

@Serializable
data class ServiceDto(
    val id: String,
    val professionalId: String,
    val title: String,
    val description: String? = null,
    val estimatedPrice: Double? = null,
    val category: String,
    val createdAt: String,
)

@Serializable
data class ProfessionalProfileDto(
    val id: String,
    val name: String,
    val email: String,
    val city: String? = null,
    val state: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val services: List<ServiceDto> = emptyList(),
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val createdAt: String,
)
