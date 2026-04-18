package br.com.trabalhoarretado.application.professional

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfessionalRequest(
    val name: String? = null,
    val city: String? = null,
    val state: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
)
