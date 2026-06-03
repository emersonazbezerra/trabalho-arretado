package br.com.trabalhoarretado.domain.review

import java.util.UUID

interface ReviewRepository {
    /** Cria ou atualiza a avaliação do cliente para o profissional (uma por par). */
    fun upsert(
        clientId: UUID,
        professionalId: UUID,
        rating: Int,
        comment: String?,
    ): Review

    fun listByProfessional(professionalId: UUID): List<Review>

    fun stats(professionalId: UUID): RatingStats

    fun statsFor(professionalIds: List<UUID>): Map<UUID, RatingStats>
}
