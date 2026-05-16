package br.com.trabalhoarretado.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
    val error: ApiErrorBody,
)

@Serializable
data class ApiErrorBody(
    val code: String,
    val message: String,
)
