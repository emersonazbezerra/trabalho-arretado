package br.com.trabalhoarretado.data.repository

import br.com.trabalhoarretado.data.dto.AuthResponse
import br.com.trabalhoarretado.data.dto.LoginRequest
import br.com.trabalhoarretado.data.dto.RegisterRequest
import br.com.trabalhoarretado.data.dto.UserDto
import br.com.trabalhoarretado.data.local.TokenStore
import br.com.trabalhoarretado.data.remote.ApiService
import br.com.trabalhoarretado.domain.Result
import br.com.trabalhoarretado.domain.apiCall

class AuthRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) {
    suspend fun login(
        email: String,
        password: String,
    ): Result<AuthResponse> {
        val result = apiCall { api.login(LoginRequest(email, password)) }
        if (result is Result.Success) {
            tokenStore.setSession(result.data.token, result.data.user.id, result.data.user.role)
        }
        return result
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        val result = apiCall { api.register(request) }
        if (result is Result.Success) {
            tokenStore.setSession(result.data.token, result.data.user.id, result.data.user.role)
        }
        return result
    }

    suspend fun me(): Result<UserDto> = apiCall { api.getMe() }

    suspend fun logout() {
        tokenStore.clear()
    }
}
