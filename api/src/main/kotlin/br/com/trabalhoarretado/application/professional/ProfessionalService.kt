package br.com.trabalhoarretado.application.professional

import br.com.trabalhoarretado.application.service.ServiceOfferResponse
import br.com.trabalhoarretado.application.service.toResponse
import br.com.trabalhoarretado.domain.ForbiddenException
import br.com.trabalhoarretado.domain.NotFoundException
import br.com.trabalhoarretado.domain.professional.ProfessionalFilter
import br.com.trabalhoarretado.domain.professional.ProfessionalRepository
import br.com.trabalhoarretado.domain.professional.ProfessionalUpdate
import br.com.trabalhoarretado.domain.service.ServiceOfferRepository
import br.com.trabalhoarretado.domain.user.User
import br.com.trabalhoarretado.domain.user.UserRole
import java.util.UUID

class ProfessionalService(
    private val professionalRepository: ProfessionalRepository,
    private val serviceOfferRepository: ServiceOfferRepository,
) {
    fun search(
        category: String?,
        city: String?,
        page: Int,
    ): PaginatedProfessionalResponse {
        val size = 20
        val result = professionalRepository.search(ProfessionalFilter(category, city, page, size))
        return PaginatedProfessionalResponse(
            data = result.data.map { it.toSummary() },
            pagination =
                PaginationResponse(
                    page = result.pagination.page,
                    size = result.pagination.size,
                    total = result.pagination.total,
                    totalPages = result.pagination.totalPages,
                ),
        )
    }

    fun findById(id: UUID): ProfessionalProfileResponse {
        val professional = professionalRepository.findById(id) ?: throw NotFoundException("Profissional")
        val services = serviceOfferRepository.findByProfessionalId(id).map { it.toResponse() }
        return professional.toProfile(services)
    }

    fun update(
        id: UUID,
        callerId: UUID,
        req: UpdateProfessionalRequest,
    ): ProfessionalProfileResponse {
        val professional = professionalRepository.findById(id) ?: throw NotFoundException("Profissional")
        if (professional.id != callerId || professional.role != UserRole.PROFESSIONAL) throw ForbiddenException()
        val updated =
            professionalRepository.update(
                id = id,
                update =
                    ProfessionalUpdate(
                        name = req.name,
                        city = req.city,
                        state = req.state,
                        phone = req.phone,
                        avatarUrl = req.avatarUrl,
                    ),
            ) ?: throw NotFoundException("Profissional")
        val services = serviceOfferRepository.findByProfessionalId(id).map { it.toResponse() }
        return updated.toProfile(services)
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

private fun User.toProfile(services: List<ServiceOfferResponse>) =
    ProfessionalProfileResponse(
        id = id.toString(),
        name = name,
        email = email,
        city = city,
        state = state,
        phone = phone,
        avatarUrl = avatarUrl,
        services = services,
        averageRating = 0.0,
        reviewCount = 0,
        createdAt = createdAt.toString(),
    )
