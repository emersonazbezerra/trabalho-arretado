package br.com.trabalhoarretado.application.user

import br.com.trabalhoarretado.application.auth.UserResponse
import br.com.trabalhoarretado.application.auth.toResponse
import br.com.trabalhoarretado.domain.NotFoundException
import br.com.trabalhoarretado.domain.ValidationException
import br.com.trabalhoarretado.domain.user.UserRepository
import br.com.trabalhoarretado.infra.storage.ImageStorage
import java.util.UUID

class UserService(
    private val userRepository: UserRepository,
    private val imageStorage: ImageStorage,
    private val defaultAvatarUrl: String,
) {
    suspend fun uploadAvatar(
        userId: UUID,
        bytes: ByteArray,
        contentType: String,
    ): UserResponse {
        if (bytes.size > MAX_BYTES) {
            throw ValidationException("Imagem maior que ${MAX_BYTES / 1024 / 1024} MB")
        }
        val extension =
            ALLOWED_TYPES[contentType.lowercase()]
                ?: throw ValidationException("Tipo de imagem não suportado. Use JPEG, PNG ou WEBP")

        userRepository.findById(userId) ?: throw NotFoundException("Usuário")

        val key = "users/$userId/${UUID.randomUUID()}.$extension"
        val url = imageStorage.upload(bytes, contentType, key)
        return userRepository.updateAvatarUrl(userId, url).toResponse(defaultAvatarUrl)
    }

    companion object {
        private const val MAX_BYTES = 2L * 1024 * 1024
        private val ALLOWED_TYPES =
            mapOf(
                "image/jpeg" to "jpg",
                "image/png" to "png",
                "image/webp" to "webp",
            )
    }
}
