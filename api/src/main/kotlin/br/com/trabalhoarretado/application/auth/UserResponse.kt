package br.com.trabalhoarretado.application.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val city: String?,
    val state: String,
    val phone: String?,
    val avatarUrl: String?,
    val createdAt: String,
)
