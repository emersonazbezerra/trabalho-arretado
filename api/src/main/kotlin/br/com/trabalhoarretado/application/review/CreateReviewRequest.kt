package br.com.trabalhoarretado.application.review

import kotlinx.serialization.Serializable

@Serializable
data class CreateReviewRequest(
    val rating: Int,
    val comment: String? = null,
)
