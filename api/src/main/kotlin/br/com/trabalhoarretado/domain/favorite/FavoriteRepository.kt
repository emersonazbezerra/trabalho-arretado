package br.com.trabalhoarretado.domain.favorite

import br.com.trabalhoarretado.domain.user.User
import java.util.UUID

interface FavoriteRepository {
    fun exists(
        clientId: UUID,
        professionalId: UUID,
    ): Boolean

    fun add(
        clientId: UUID,
        professionalId: UUID,
    )

    fun remove(
        clientId: UUID,
        professionalId: UUID,
    ): Boolean

    fun listProfessionalsByClient(clientId: UUID): List<User>
}
