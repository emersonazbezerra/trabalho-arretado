package br.com.trabalhoarretado.data.repository

import br.com.trabalhoarretado.data.dto.CreateReviewRequest
import br.com.trabalhoarretado.data.dto.ReviewDto
import br.com.trabalhoarretado.data.remote.ApiService
import br.com.trabalhoarretado.domain.Result
import br.com.trabalhoarretado.domain.apiCall

class ReviewRepository(private val api: ApiService) {
    suspend fun list(professionalId: String): Result<List<ReviewDto>> =
        apiCall { api.listReviews(professionalId) }

    suspend fun create(
        professionalId: String,
        rating: Int,
        comment: String?,
    ): Result<ReviewDto> =
        apiCall { api.createReview(professionalId, CreateReviewRequest(rating, comment?.ifBlank { null })) }
}
