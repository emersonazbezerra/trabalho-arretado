package br.com.trabalhoarretado.domain.review

import java.util.UUID
import kotlin.time.Instant

data class Review(
    val id: UUID,
    val clientId: UUID,
    val clientName: String,
    val professionalId: UUID,
    val rating: Int,
    val comment: String?,
    val createdAt: Instant,
)

data class RatingStats(
    val average: Double,
    val count: Int,
) {
    companion object {
        val EMPTY = RatingStats(average = 0.0, count = 0)
    }
}
