package br.com.trabalhoarretado.data.repository

import br.com.trabalhoarretado.data.dto.ProfessionalSummaryDto
import br.com.trabalhoarretado.data.remote.ApiService
import br.com.trabalhoarretado.domain.Result
import br.com.trabalhoarretado.domain.apiCall
import retrofit2.HttpException
import retrofit2.Response

class FavoriteRepository(
    private val api: ApiService,
) {
    suspend fun list(): Result<List<ProfessionalSummaryDto>> = apiCall { api.listFavorites() }

    suspend fun add(profId: String): Result<Unit> = apiCall { api.addFavorite(profId).requireSuccess() }

    suspend fun remove(profId: String): Result<Unit> = apiCall { api.removeFavorite(profId).requireSuccess() }
}

private fun Response<Unit>.requireSuccess() {
    if (!isSuccessful) throw HttpException(this)
}
