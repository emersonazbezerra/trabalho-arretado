package br.com.trabalhoarretado.domain.user

import java.util.UUID
import kotlin.time.Instant

enum class UserRole { CLIENT, PROFESSIONAL }

data class User(
    val id: UUID,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val city: String?,
    val state: String,
    val phone: String?,
    val avatarUrl: String?,
    val createdAt: Instant,
)
