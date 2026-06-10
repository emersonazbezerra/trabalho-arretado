package br.com.trabalhoarretado.domain.user

import java.util.UUID

interface UserRepository {
    fun findByEmail(email: String): User?

    fun findById(id: UUID): User?

    fun create(
        name: String,
        email: String,
        passwordHash: String,
        role: UserRole,
        city: String?,
        state: String?,
        phone: String?,
    ): User

    fun updateAvatarUrl(
        id: UUID,
        avatarUrl: String,
    ): User

    fun updateRole(
        id: UUID,
        role: UserRole,
    ): User
}
