package br.com.trabalhoarretado.application.favorite

import br.com.trabalhoarretado.application.professional.ProfessionalSummaryResponse
import br.com.trabalhoarretado.domain.ForbiddenException
import br.com.trabalhoarretado.domain.NotFoundException
import br.com.trabalhoarretado.domain.favorite.FavoriteRepository
import br.com.trabalhoarretado.domain.review.RatingStats
import br.com.trabalhoarretado.domain.review.ReviewRepository
import br.com.trabalhoarretado.domain.user.User
import br.com.trabalhoarretado.domain.user.UserRepository
import br.com.trabalhoarretado.domain.user.UserRole
import java.util.UUID

sealed class FavoriteAddResult {
    object Created : FavoriteAddResult()

    object AlreadyExists : FavoriteAddResult()
}

class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository,
    private val defaultAvatarUrl: String,
) {
    fun list(
        clientId: UUID,
        callerRole: UserRole,
    ): List<ProfessionalSummaryResponse> {
        requireClient(callerRole)
        val professionals = favoriteRepository.listProfessionalsByClient(clientId)
        val stats = reviewRepository.statsFor(professionals.map { it.id })
        return professionals.map { it.toSummary(stats[it.id] ?: RatingStats.EMPTY, defaultAvatarUrl) }
    }

    fun add(
        clientId: UUID,
        callerRole: UserRole,
        professionalId: UUID,
    ): FavoriteAddResult {
        requireClient(callerRole)
        val target = userRepository.findById(professionalId) ?: throw NotFoundException("Profissional")
        if (target.role != UserRole.PROFESSIONAL) throw NotFoundException("Profissional")
        if (favoriteRepository.exists(clientId, professionalId)) return FavoriteAddResult.AlreadyExists
        favoriteRepository.add(clientId, professionalId)
        return FavoriteAddResult.Created
    }

    fun remove(
        clientId: UUID,
        callerRole: UserRole,
        professionalId: UUID,
    ) {
        requireClient(callerRole)
        favoriteRepository.remove(clientId, professionalId)
    }

    private fun requireClient(role: UserRole) {
        if (role != UserRole.CLIENT) throw ForbiddenException()
    }
}

private fun User.toSummary(stats: RatingStats, defaultAvatarUrl: String) =
    ProfessionalSummaryResponse(
        id = id.toString(),
        name = name,
        city = city,
        state = state,
        phone = phone,
        avatarUrl = avatarUrl ?: defaultAvatarUrl,
        averageRating = stats.average,
        reviewCount = stats.count,
    )
