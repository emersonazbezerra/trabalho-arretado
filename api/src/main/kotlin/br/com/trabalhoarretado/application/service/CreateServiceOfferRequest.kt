package br.com.trabalhoarretado.application.service

import kotlinx.serialization.Serializable

@Serializable
data class CreateServiceOfferRequest(
    val title: String,
    val description: String? = null,
    val estimatedPrice: Double? = null,
    val category: String,
)
