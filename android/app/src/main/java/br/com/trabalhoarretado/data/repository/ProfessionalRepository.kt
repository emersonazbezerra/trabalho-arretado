package br.com.trabalhoarretado.data.repository

import br.com.trabalhoarretado.data.dto.PaginatedProfessionalsDto
import br.com.trabalhoarretado.data.dto.ProfessionalProfileDto
import br.com.trabalhoarretado.data.remote.ApiService
import br.com.trabalhoarretado.domain.Result
import br.com.trabalhoarretado.domain.apiCall

class ProfessionalRepository(
    private val api: ApiService,
) {
    suspend fun list(
        category: String?,
        city: String?,
        page: Int,
    ): Result<PaginatedProfessionalsDto> = apiCall { api.listProfessionals(category, city, page) }

    suspend fun getById(id: String): Result<ProfessionalProfileDto> = apiCall { api.getProfessional(id) }
}
