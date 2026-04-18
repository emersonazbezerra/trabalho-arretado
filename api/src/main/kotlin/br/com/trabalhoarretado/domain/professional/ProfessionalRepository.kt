package br.com.trabalhoarretado.domain.professional

import br.com.trabalhoarretado.domain.shared.PaginatedResult
import br.com.trabalhoarretado.domain.user.User
import java.util.UUID

data class ProfessionalFilter(
    val category: String?,
    val city: String?,
    val page: Int,
    val size: Int,
)

data class ProfessionalUpdate(
    val name: String?,
    val city: String?,
    val state: String?,
    val phone: String?,
    val avatarUrl: String?,
)

interface ProfessionalRepository {
    fun search(filter: ProfessionalFilter): PaginatedResult<User>

    fun findById(id: UUID): User?

    fun update(
        id: UUID,
        update: ProfessionalUpdate,
    ): User?
}
