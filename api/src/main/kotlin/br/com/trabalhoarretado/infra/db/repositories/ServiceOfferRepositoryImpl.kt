package br.com.trabalhoarretado.infra.db.repositories

import br.com.trabalhoarretado.domain.service.NewServiceOffer
import br.com.trabalhoarretado.domain.service.ServiceOffer
import br.com.trabalhoarretado.domain.service.ServiceOfferRepository
import br.com.trabalhoarretado.domain.service.ServiceOfferUpdate
import br.com.trabalhoarretado.infra.db.tables.Services
import br.com.trabalhoarretado.infra.db.tables.Users
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.time.Clock

class ServiceOfferRepositoryImpl : ServiceOfferRepository {
    override fun create(offer: NewServiceOffer): ServiceOffer =
        transaction {
            val now = Clock.System.now()
            val insertResult =
                Services.insert {
                    it[professionalId] = EntityID(offer.professionalId, Users)
                    it[title] = offer.title
                    it[description] = offer.description
                    it[estimatedPrice] = offer.estimatedPrice
                    it[category] = offer.category
                    it[createdAt] = now
                }
            ServiceOffer(
                id = insertResult[Services.id].value,
                professionalId = offer.professionalId,
                title = offer.title,
                description = offer.description,
                estimatedPrice = offer.estimatedPrice,
                category = offer.category,
                createdAt = now,
            )
        }

    override fun findById(id: UUID): ServiceOffer? =
        transaction {
            Services
                .selectAll()
                .where { Services.id eq id }
                .map { it.toServiceOffer() }
                .singleOrNull()
        }

    override fun findByProfessionalId(professionalId: UUID): List<ServiceOffer> =
        transaction {
            Services
                .selectAll()
                .where { Services.professionalId eq professionalId }
                .map { it.toServiceOffer() }
        }

    override fun update(
        id: UUID,
        update: ServiceOfferUpdate,
    ): ServiceOffer? =
        transaction {
            Services.update({ Services.id eq id }) {
                update.title?.let { title -> it[Services.title] = title }
                update.description?.let { desc -> it[Services.description] = desc }
                update.estimatedPrice?.let { price -> it[Services.estimatedPrice] = price }
                update.category?.let { cat -> it[Services.category] = cat }
            }
            Services
                .selectAll()
                .where { Services.id eq id }
                .map { it.toServiceOffer() }
                .singleOrNull()
        }

    override fun delete(id: UUID) {
        transaction {
            Services.deleteWhere { Services.id eq id }
        }
    }

    private fun ResultRow.toServiceOffer() =
        ServiceOffer(
            id = this[Services.id].value,
            professionalId = this[Services.professionalId].value,
            title = this[Services.title],
            description = this[Services.description],
            estimatedPrice = this[Services.estimatedPrice],
            category = this[Services.category],
            createdAt = this[Services.createdAt],
        )
}
