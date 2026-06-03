package br.com.trabalhoarretado.infra.db.repositories

import br.com.trabalhoarretado.domain.review.RatingStats
import br.com.trabalhoarretado.domain.review.Review
import br.com.trabalhoarretado.domain.review.ReviewRepository
import br.com.trabalhoarretado.infra.db.tables.Reviews
import br.com.trabalhoarretado.infra.db.tables.Users
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.time.Clock

class ReviewRepositoryImpl : ReviewRepository {
    override fun upsert(
        clientId: UUID,
        professionalId: UUID,
        rating: Int,
        comment: String?,
    ): Review =
        transaction {
            val exists =
                Reviews
                    .selectAll()
                    .where { (Reviews.clientId eq clientId) and (Reviews.professionalId eq professionalId) }
                    .limit(1)
                    .any()

            if (exists) {
                Reviews.update({ (Reviews.clientId eq clientId) and (Reviews.professionalId eq professionalId) }) {
                    it[Reviews.rating] = rating
                    it[Reviews.comment] = comment
                }
            } else {
                Reviews.insert {
                    it[Reviews.clientId] = EntityID(clientId, Users)
                    it[Reviews.professionalId] = EntityID(professionalId, Users)
                    it[Reviews.rating] = rating
                    it[Reviews.comment] = comment
                    it[Reviews.createdAt] = Clock.System.now()
                }
            }

            reviewsWithClient()
                .where { (Reviews.clientId eq clientId) and (Reviews.professionalId eq professionalId) }
                .map { it.toReview() }
                .single()
        }

    override fun listByProfessional(professionalId: UUID): List<Review> =
        transaction {
            reviewsWithClient()
                .where { Reviews.professionalId eq professionalId }
                .orderBy(Reviews.createdAt to SortOrder.DESC)
                .map { it.toReview() }
        }

    override fun stats(professionalId: UUID): RatingStats = statsFor(listOf(professionalId))[professionalId] ?: RatingStats.EMPTY

    override fun statsFor(professionalIds: List<UUID>): Map<UUID, RatingStats> {
        if (professionalIds.isEmpty()) return emptyMap()
        // Agregação em memória — suficiente na escala atual (poucos reviews por profissional).
        return transaction {
            Reviews
                .selectAll()
                .where { Reviews.professionalId inList professionalIds }
                .map { it[Reviews.professionalId].value to it[Reviews.rating] }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, ratings) -> RatingStats(average = ratings.average(), count = ratings.size) }
        }
    }

    private fun reviewsWithClient() =
        Reviews
            .join(Users, JoinType.INNER, additionalConstraint = { Reviews.clientId eq Users.id })
            .selectAll()

    private fun ResultRow.toReview() =
        Review(
            id = this[Reviews.id].value,
            clientId = this[Reviews.clientId].value,
            clientName = this[Users.name],
            professionalId = this[Reviews.professionalId].value,
            rating = this[Reviews.rating],
            comment = this[Reviews.comment],
            createdAt = this[Reviews.createdAt],
        )
}
