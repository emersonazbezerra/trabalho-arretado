package br.com.trabalhoarretado.infra.db.repositories

import br.com.trabalhoarretado.domain.favorite.FavoriteRepository
import br.com.trabalhoarretado.domain.user.User
import br.com.trabalhoarretado.domain.user.UserRole
import br.com.trabalhoarretado.infra.db.tables.Favorites
import br.com.trabalhoarretado.infra.db.tables.Users
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlin.time.Clock

class FavoriteRepositoryImpl : FavoriteRepository {
    override fun exists(
        clientId: UUID,
        professionalId: UUID,
    ): Boolean =
        transaction {
            Favorites
                .selectAll()
                .where {
                    (Favorites.clientId eq clientId) and (Favorites.professionalId eq professionalId)
                }.limit(1)
                .any()
        }

    override fun add(
        clientId: UUID,
        professionalId: UUID,
    ) {
        transaction {
            Favorites.insert {
                it[Favorites.clientId] = EntityID(clientId, Users)
                it[Favorites.professionalId] = EntityID(professionalId, Users)
                it[Favorites.createdAt] = Clock.System.now()
            }
        }
    }

    override fun remove(
        clientId: UUID,
        professionalId: UUID,
    ): Boolean =
        transaction {
            Favorites.deleteWhere {
                (Favorites.clientId eq clientId) and (Favorites.professionalId eq professionalId)
            } > 0
        }

    override fun listProfessionalsByClient(clientId: UUID): List<User> =
        transaction {
            Favorites
                .join(Users, JoinType.INNER, additionalConstraint = { Favorites.professionalId eq Users.id })
                .selectAll()
                .where {
                    (Favorites.clientId eq clientId) and (Users.role eq UserRole.PROFESSIONAL.name)
                }.orderBy(Favorites.createdAt to SortOrder.DESC)
                .map {
                    User(
                        id = it[Users.id].value,
                        name = it[Users.name],
                        email = it[Users.email],
                        passwordHash = it[Users.passwordHash],
                        role = UserRole.valueOf(it[Users.role]),
                        city = it[Users.city],
                        state = it[Users.state],
                        phone = it[Users.phone],
                        avatarUrl = it[Users.avatarUrl],
                        createdAt = it[Users.createdAt],
                    )
                }
        }
}
