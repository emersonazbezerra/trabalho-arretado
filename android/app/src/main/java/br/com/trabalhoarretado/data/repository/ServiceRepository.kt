package br.com.trabalhoarretado.data.repository

import br.com.trabalhoarretado.data.dto.CreateServiceRequest
import br.com.trabalhoarretado.data.dto.ServiceDto
import br.com.trabalhoarretado.data.dto.UpdateServiceRequest
import br.com.trabalhoarretado.data.remote.ApiService
import br.com.trabalhoarretado.domain.Result
import br.com.trabalhoarretado.domain.apiCall
import retrofit2.HttpException
import retrofit2.Response

class ServiceRepository(
    private val api: ApiService,
) {
    suspend fun create(request: CreateServiceRequest): Result<ServiceDto> = apiCall { api.createService(request) }

    suspend fun update(
        id: String,
        request: UpdateServiceRequest,
    ): Result<ServiceDto> = apiCall { api.updateService(id, request) }

    suspend fun delete(id: String): Result<Unit> =
        apiCall {
            val response: Response<Unit> = api.deleteService(id)
            if (!response.isSuccessful) throw HttpException(response)
        }
}
