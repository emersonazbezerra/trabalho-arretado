package br.com.trabalhoarretado.application.auth

import br.com.trabalhoarretado.di.JwtConfig
import br.com.trabalhoarretado.domain.EmailAlreadyExistsException
import br.com.trabalhoarretado.domain.InvalidCredentialsException
import br.com.trabalhoarretado.domain.NotFoundException
import br.com.trabalhoarretado.domain.ValidationException
import br.com.trabalhoarretado.domain.user.User
import br.com.trabalhoarretado.domain.user.UserRepository
import br.com.trabalhoarretado.domain.user.UserRole
import br.com.trabalhoarretado.infra.db.tables.Users.phone
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

class AuthService(
    private val userRepository: UserRepository,
    private val jwtConfig: JwtConfig,
    private val defaultAvatarUrl: String,
) {
    fun register(req: RegisterRequest): AuthResponse {
        val role =
            runCatching { UserRole.valueOf(req.role.uppercase()) }
                .getOrElse { throw ValidationException("Role inválida. Use CLIENT ou PROFESSIONAL") }

        if (userRepository.findByEmail(req.email) != null) {
            throw EmailAlreadyExistsException()
        }

        val passwordHash = BCrypt.hashpw(req.password, BCrypt.gensalt())
        val user =
            userRepository.create(
                name = req.name,
                email = req.email,
                passwordHash = passwordHash,
                role = role,
                city = req.city,
                state = req.state,
                phone = req.phone,
            )
        return AuthResponse(token = generateToken(jwtConfig, user.id, user.role), user = user.toResponse(defaultAvatarUrl))
    }

    fun login(req: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(req.email) ?: throw InvalidCredentialsException()
        if (!BCrypt.checkpw(req.password, user.passwordHash)) throw InvalidCredentialsException()
        return AuthResponse(token = generateToken(jwtConfig, user.id, user.role), user = user.toResponse(defaultAvatarUrl))
    }

    fun getMe(userId: UUID): UserResponse {
        val user = userRepository.findById(userId) ?: throw NotFoundException("Usuário")
        return user.toResponse(defaultAvatarUrl)
    }
}

internal fun User.toResponse(defaultAvatarUrl: String) =
    UserResponse(
        id = id.toString(),
        name = name,
        email = email,
        role = role.name,
        city = city,
        state = state,
        phone = phone,
        avatarUrl = avatarUrl ?: defaultAvatarUrl,
        createdAt = createdAt.toString(),
    )
