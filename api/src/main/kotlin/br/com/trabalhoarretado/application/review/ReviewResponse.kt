package br.com.trabalhoarretado.application.review

import kotlinx.serialization.Serializable

@Serializable
data class ReviewResponse(
    val id: String,
    val clientId: String,
    val clientName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String,
)
