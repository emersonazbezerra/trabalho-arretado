package br.com.trabalhoarretado.data.remote

import br.com.trabalhoarretado.data.dto.AuthResponse
import br.com.trabalhoarretado.data.dto.LoginRequest
import br.com.trabalhoarretado.data.dto.RegisterRequest
import br.com.trabalhoarretado.data.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): AuthResponse

    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): AuthResponse

    @GET("/api/auth/me")
    suspend fun getMe(): UserDto
}
