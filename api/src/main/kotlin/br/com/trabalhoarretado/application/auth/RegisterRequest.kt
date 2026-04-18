package br.com.trabalhoarretado.application.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val city: String? = null,
    val phone: String? = null,
)
