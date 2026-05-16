package br.com.trabalhoarretado.application.favorite

import br.com.trabalhoarretado.application.professional.ProfessionalSummaryResponse
import br.com.trabalhoarretado.domain.ForbiddenException
import br.com.trabalhoarretado.domain.NotFoundException
import br.com.trabalhoarretado.domain.favorite.FavoriteRepository
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
) {
    fun list(
        clientId: UUID,
        callerRole: UserRole,
    ): List<ProfessionalSummaryResponse> {
        requireClient(callerRole)
        return favoriteRepository.listProfessionalsByClient(clientId).map { it.toSummary() }
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

private fun User.toSummary() =
    ProfessionalSummaryResponse(
        id = id.toString(),
        name = name,
        city = city,
        state = state,
        phone = phone,
        avatarUrl = avatarUrl,
        averageRating = 0.0,
        reviewCount = 0,
    )
