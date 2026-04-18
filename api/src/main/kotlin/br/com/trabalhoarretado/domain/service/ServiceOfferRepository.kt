package br.com.trabalhoarretado.domain.service

import java.util.UUID

interface ServiceOfferRepository {
    fun create(offer: NewServiceOffer): ServiceOffer

    fun findById(id: UUID): ServiceOffer?

    fun findByProfessionalId(professionalId: UUID): List<ServiceOffer>

    fun update(
        id: UUID,
        update: ServiceOfferUpdate,
    ): ServiceOffer?

    fun delete(id: UUID)
}
