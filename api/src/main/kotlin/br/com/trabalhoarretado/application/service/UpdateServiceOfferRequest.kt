package br.com.trabalhoarretado.application.service

import kotlinx.serialization.Serializable

@Serializable
data class UpdateServiceOfferRequest(
    val title: String? = null,
    val description: String? = null,
    val estimatedPrice: Double? = null,
    val category: String? = null,
)
