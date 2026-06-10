package br.com.trabalhoarretado.application.user

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val city: String? = null,
    val state: String? = null,
    val phone: String? = null,
)
