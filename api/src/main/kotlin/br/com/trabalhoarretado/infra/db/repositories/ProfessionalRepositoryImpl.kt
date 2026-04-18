package br.com.trabalhoarretado.infra.db.repositories

import br.com.trabalhoarretado.domain.professional.ProfessionalFilter
import br.com.trabalhoarretado.domain.professional.ProfessionalRepository
import br.com.trabalhoarretado.domain.professional.ProfessionalUpdate
import br.com.trabalhoarretado.domain.shared.PaginatedResult
import br.com.trabalhoarretado.domain.shared.Pagination
import br.com.trabalhoarretado.domain.user.User
import br.com.trabalhoarretado.domain.user.UserRole
import br.com.trabalhoarretado.infra.db.tables.Services
import br.com.trabalhoarretado.infra.db.tables.Users
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.exists
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.math.ceil

class ProfessionalRepositoryImpl : ProfessionalRepository {
    override fun search(filter: ProfessionalFilter): PaginatedResult<User> =
        transaction {
            val offset = ((filter.page - 1) * filter.size).toLong()
            val condition = buildCondition(filter)

            val total = Users.selectAll().where { condition }.count()
            val data =
                Users
                    .selectAll()
                    .where { condition }
                    .limit(filter.size)
                    .offset(offset)
                    .map { it.toUser() }

            PaginatedResult(
                data = data,
                pagination =
                    Pagination(
                        page = filter.page,
                        size = filter.size,
                        total = total,
                        totalPages = ceil(total.toDouble() / filter.size).toInt().coerceAtLeast(1),
                    ),
            )
        }

    override fun findById(id: UUID): User? =
        transaction {
            Users
                .selectAll()
                .where { (Users.id eq id) and (Users.role eq UserRole.PROFESSIONAL.name) }
                .map { it.toUser() }
                .singleOrNull()
        }

    override fun update(
        id: UUID,
        update: ProfessionalUpdate,
    ): User? =
        transaction {
            Users.update({ Users.id eq id }) {
                update.name?.let { name -> it[Users.name] = name }
                update.city?.let { city -> it[Users.city] = city }
                update.state?.let { state -> it[Users.state] = state }
                update.phone?.let { phone -> it[Users.phone] = phone }
                update.avatarUrl?.let { url -> it[Users.avatarUrl] = url }
            }
            Users
                .selectAll()
                .where { Users.id eq id }
                .map { it.toUser() }
                .singleOrNull()
        }

    @Suppress("ComplexMethod")
    private fun buildCondition(filter: ProfessionalFilter): Op<Boolean> {
        var condition: Op<Boolean> = Users.role eq UserRole.PROFESSIONAL.name

        filter.city?.let { city ->
            condition = condition and (Users.city like "%$city%")
        }

        filter.category?.let { category ->
            condition =
                condition and
                exists(
                    Services
                        .selectAll()
                        .where {
                            (Services.professionalId eq Users.id) and (Services.category eq category)
                        },
                )
        }

        return condition
    }

    private fun ResultRow.toUser() =
        User(
            id = this[Users.id].value,
            name = this[Users.name],
            email = this[Users.email],
            passwordHash = this[Users.passwordHash],
            role = UserRole.valueOf(this[Users.role]),
            city = this[Users.city],
            state = this[Users.state],
            phone = this[Users.phone],
            avatarUrl = this[Users.avatarUrl],
            createdAt = this[Users.createdAt],
        )
}
