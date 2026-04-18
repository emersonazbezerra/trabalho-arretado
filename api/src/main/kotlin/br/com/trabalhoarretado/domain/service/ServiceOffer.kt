package br.com.trabalhoarretado.domain.service

import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Instant

data class ServiceOffer(
    val id: UUID,
    val professionalId: UUID,
    val title: String,
    val description: String?,
    val estimatedPrice: BigDecimal?,
    val category: String,
    val createdAt: Instant,
)

data class NewServiceOffer(
    val professionalId: UUID,
    val title: String,
    val description: String?,
    val estimatedPrice: BigDecimal?,
    val category: String,
)

data class ServiceOfferUpdate(
    val title: String?,
    val description: String?,
    val estimatedPrice: BigDecimal?,
    val category: String?,
)
