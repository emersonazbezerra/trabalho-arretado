package br.com.trabalhoarretado.application.service

import br.com.trabalhoarretado.domain.ForbiddenException
import br.com.trabalhoarretado.domain.NotFoundException
import br.com.trabalhoarretado.domain.ValidationException
import br.com.trabalhoarretado.domain.service.NewServiceOffer
import br.com.trabalhoarretado.domain.service.ServiceCategory
import br.com.trabalhoarretado.domain.service.ServiceOffer
import br.com.trabalhoarretado.domain.service.ServiceOfferRepository
import br.com.trabalhoarretado.domain.service.ServiceOfferUpdate
import java.math.BigDecimal
import java.util.UUID

class ServiceOfferService(
    private val serviceOfferRepository: ServiceOfferRepository,
) {
    fun create(
        req: CreateServiceOfferRequest,
        professionalId: UUID,
    ): ServiceOfferResponse {
        val offer =
            serviceOfferRepository.create(
                NewServiceOffer(
                    professionalId = professionalId,
                    title = req.title,
                    description = req.description,
                    estimatedPrice = req.estimatedPrice?.let { BigDecimal.valueOf(it) },
                    category = parseCategory(req.category),
                ),
            )
        return offer.toResponse()
    }

    fun update(
        id: UUID,
        callerId: UUID,
        req: UpdateServiceOfferRequest,
    ): ServiceOfferResponse {
        val existing = serviceOfferRepository.findById(id) ?: throw NotFoundException("Serviço")
        if (existing.professionalId != callerId) throw ForbiddenException()
        val updated =
            serviceOfferRepository.update(
                id = id,
                update =
                    ServiceOfferUpdate(
                        title = req.title,
                        description = req.description,
                        estimatedPrice = req.estimatedPrice?.let { BigDecimal.valueOf(it) },
                        category = req.category?.let { parseCategory(it) },
                    ),
            ) ?: throw NotFoundException("Serviço")
        return updated.toResponse()
    }

    fun delete(
        id: UUID,
        callerId: UUID,
    ) {
        val existing = serviceOfferRepository.findById(id) ?: throw NotFoundException("Serviço")
        if (existing.professionalId != callerId) throw ForbiddenException()
        serviceOfferRepository.delete(id)
    }

    private fun parseCategory(raw: String): ServiceCategory =
        runCatching { ServiceCategory.valueOf(raw.uppercase()) }
            .getOrElse {
                throw ValidationException(
                    "Categoria inválida. Use uma das: ${ServiceCategory.entries.joinToString { it.name }}",
                )
            }
}

internal fun ServiceOffer.toResponse() =
    ServiceOfferResponse(
        id = id.toString(),
        professionalId = professionalId.toString(),
        title = title,
        description = description,
        estimatedPrice = estimatedPrice?.toDouble(),
        category = category.name,
        createdAt = createdAt.toString(),
    )
