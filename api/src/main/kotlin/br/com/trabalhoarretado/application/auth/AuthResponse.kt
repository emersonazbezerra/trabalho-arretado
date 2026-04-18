package br.com.trabalhoarretado.application.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse,
)
