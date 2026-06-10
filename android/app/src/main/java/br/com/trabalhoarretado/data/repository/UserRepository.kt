package br.com.trabalhoarretado.data.repository

import br.com.trabalhoarretado.data.dto.ProfessionalProfileDto
import br.com.trabalhoarretado.data.dto.UpdateProfessionalRequest
import br.com.trabalhoarretado.data.dto.UpdateUserRequest
import br.com.trabalhoarretado.data.dto.UserDto
import br.com.trabalhoarretado.data.remote.ApiService
import br.com.trabalhoarretado.domain.Result
import br.com.trabalhoarretado.domain.apiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UserRepository(
    private val api: ApiService,
) {
    suspend fun updateProfessional(
        id: String,
        request: UpdateProfessionalRequest,
    ): Result<ProfessionalProfileDto> = apiCall { api.updateProfessional(id, request) }

    suspend fun updateMe(request: UpdateUserRequest): Result<UserDto> = apiCall { api.updateMe(request) }

    suspend fun uploadAvatar(
        bytes: ByteArray,
        mimeType: String,
    ): Result<UserDto> {
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "avatar.jpg", body)
        return apiCall { api.uploadAvatar(part) }
    }
}
