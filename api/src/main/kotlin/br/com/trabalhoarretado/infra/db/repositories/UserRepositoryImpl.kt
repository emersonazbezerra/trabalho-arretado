package br.com.trabalhoarretado.infra.db.repositories

import br.com.trabalhoarretado.domain.NotFoundException
import br.com.trabalhoarretado.domain.user.User
import br.com.trabalhoarretado.domain.user.UserRepository
import br.com.trabalhoarretado.domain.user.UserRole
import br.com.trabalhoarretado.infra.db.tables.Users
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID
import kotlin.time.Clock

class UserRepositoryImpl : UserRepository {
    override fun findByEmail(email: String): User? =
        transaction {
            Users
                .selectAll()
                .where { Users.email eq email }
                .map { it.toUser() }
                .singleOrNull()
        }

    override fun findById(id: UUID): User? =
        transaction {
            Users
                .selectAll()
                .where { Users.id eq id }
                .map { it.toUser() }
                .singleOrNull()
        }

    override fun create(
        name: String,
        email: String,
        passwordHash: String,
        role: UserRole,
        city: String?,
        state: String?,
        phone: String?,
    ): User =
        transaction {
            val now = Clock.System.now()
            val insertResult =
                Users.insert {
                    it[Users.name] = name
                    it[Users.email] = email
                    it[Users.passwordHash] = passwordHash
                    it[Users.role] = role.name
                    it[Users.city] = city
                    it[Users.state] = state
                    it[Users.phone] = phone
                    it[Users.createdAt] = now
                }
            val id = insertResult[Users.id].value
            User(
                id = id,
                name = name,
                email = email,
                passwordHash = passwordHash,
                role = role,
                city = city,
                state = "PB",
                phone = phone,
                avatarUrl = null,
                createdAt = now,
            )
        }

    override fun updateAvatarUrl(
        id: UUID,
        avatarUrl: String,
    ): User =
        transaction {
            val rows =
                Users.update({ Users.id eq id }) {
                    it[Users.avatarUrl] = avatarUrl
                }
            if (rows == 0) throw NotFoundException("Usuário")
            Users
                .selectAll()
                .where { Users.id eq id }
                .map { it.toUser() }
                .single()
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
