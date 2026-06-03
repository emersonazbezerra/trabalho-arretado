package br.com.trabalhoarretado.application.review

import br.com.trabalhoarretado.domain.ForbiddenException
import br.com.trabalhoarretado.domain.NotFoundException
import br.com.trabalhoarretado.domain.ValidationException
import br.com.trabalhoarretado.domain.review.Review
import br.com.trabalhoarretado.domain.review.ReviewRepository
import br.com.trabalhoarretado.domain.user.UserRepository
import br.com.trabalhoarretado.domain.user.UserRole
import java.util.UUID

class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
) {
    fun list(professionalId: UUID): List<ReviewResponse> {
        ensureProfessional(professionalId)
        return reviewRepository.listByProfessional(professionalId).map { it.toResponse() }
    }

    fun create(
        clientId: UUID,
        callerRole: UserRole,
        professionalId: UUID,
        req: CreateReviewRequest,
    ): ReviewResponse {
        if (callerRole != UserRole.CLIENT) throw ForbiddenException()
        if (req.rating !in 1..5) throw ValidationException("A nota deve estar entre 1 e 5")
        if (clientId == professionalId) throw ValidationException("Você não pode avaliar a si mesmo")
        ensureProfessional(professionalId)
        val review =
            reviewRepository.upsert(
                clientId = clientId,
                professionalId = professionalId,
                rating = req.rating,
                comment = req.comment?.trim()?.takeIf { it.isNotEmpty() },
            )
        return review.toResponse()
    }

    private fun ensureProfessional(professionalId: UUID) {
        val target = userRepository.findById(professionalId) ?: throw NotFoundException("Profissional")
        if (target.role != UserRole.PROFESSIONAL) throw NotFoundException("Profissional")
    }
}

private fun Review.toResponse() =
    ReviewResponse(
        id = id.toString(),
        clientId = clientId.toString(),
        clientName = clientName,
        rating = rating,
        comment = comment,
        createdAt = createdAt.toString(),
    )
