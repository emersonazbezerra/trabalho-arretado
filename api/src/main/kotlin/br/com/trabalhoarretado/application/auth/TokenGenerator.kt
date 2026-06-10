package br.com.trabalhoarretado.application.auth

import br.com.trabalhoarretado.di.JwtConfig
import br.com.trabalhoarretado.domain.user.UserRole
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import java.util.UUID

fun generateToken(
    jwtConfig: JwtConfig,
    userId: UUID,
    role: UserRole,
): String =
    JWT
        .create()
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .withClaim("userId", userId.toString())
        .withClaim("role", role.name)
        .withExpiresAt(Date(System.currentTimeMillis() + jwtConfig.expiresInDays * 24 * 60 * 60 * 1000))
        .sign(Algorithm.HMAC256(jwtConfig.secret))
