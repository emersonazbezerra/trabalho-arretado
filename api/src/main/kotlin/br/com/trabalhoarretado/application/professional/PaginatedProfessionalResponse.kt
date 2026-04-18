package br.com.trabalhoarretado.application.professional

import kotlinx.serialization.Serializable

@Serializable
data class PaginationResponse(
    val page: Int,
    val size: Int,
    val total: Long,
    val totalPages: Int,
)

@Serializable
data class PaginatedProfessionalResponse(
    val data: List<ProfessionalSummaryResponse>,
    val pagination: PaginationResponse,
)
