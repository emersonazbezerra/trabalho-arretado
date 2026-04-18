package br.com.trabalhoarretado.domain.shared

data class Pagination(
    val page: Int,
    val size: Int,
    val total: Long,
    val totalPages: Int,
)

data class PaginatedResult<T>(
    val data: List<T>,
    val pagination: Pagination,
)
