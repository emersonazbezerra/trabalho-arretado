package br.com.trabalhoarretado.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val city: String? = null,
    val state: String? = null,
    val phone: String? = null,
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto,
)

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val city: String? = null,
    val state: String? = null,
    val phone: String? = null,
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val city: String? = null,
    val state: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String,
)
