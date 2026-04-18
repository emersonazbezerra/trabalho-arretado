package br.com.trabalhoarretado.application.service

import kotlinx.serialization.Serializable

@Serializable
data class ServiceOfferResponse(
    val id: String,
    val professionalId: String,
    val title: String,
    val description: String?,
    val estimatedPrice: Double?,
    val category: String,
    val createdAt: String,
)
