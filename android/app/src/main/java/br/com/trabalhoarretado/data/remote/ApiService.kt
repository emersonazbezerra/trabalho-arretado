package br.com.trabalhoarretado.data.remote

import br.com.trabalhoarretado.data.dto.AuthResponse
import br.com.trabalhoarretado.data.dto.CreateServiceRequest
import br.com.trabalhoarretado.data.dto.LoginRequest
import br.com.trabalhoarretado.data.dto.PaginatedProfessionalsDto
import br.com.trabalhoarretado.data.dto.ProfessionalProfileDto
import br.com.trabalhoarretado.data.dto.ProfessionalSummaryDto
import br.com.trabalhoarretado.data.dto.RegisterRequest
import br.com.trabalhoarretado.data.dto.ServiceDto
import br.com.trabalhoarretado.data.dto.UpdateProfessionalRequest
import br.com.trabalhoarretado.data.dto.UpdateServiceRequest
import br.com.trabalhoarretado.data.dto.UserDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("/api/professionals")
    suspend fun listProfessionals(
        @Query("category") category: String? = null,
        @Query("city") city: String? = null,
        @Query("page") page: Int = 1,
    ): PaginatedProfessionalsDto

    @GET("/api/professionals/{id}")
    suspend fun getProfessional(
        @Path("id") id: String,
    ): ProfessionalProfileDto

    @GET("/api/favorites")
    suspend fun listFavorites(): List<ProfessionalSummaryDto>

    @POST("/api/favorites/{profId}")
    suspend fun addFavorite(
        @Path("profId") profId: String,
    ): Response<Unit>

    @DELETE("/api/favorites/{profId}")
    suspend fun removeFavorite(
        @Path("profId") profId: String,
    ): Response<Unit>

    @PUT("/api/professionals/{id}")
    suspend fun updateProfessional(
        @Path("id") id: String,
        @Body request: UpdateProfessionalRequest,
    ): ProfessionalProfileDto

    @POST("/api/services")
    suspend fun createService(
        @Body request: CreateServiceRequest,
    ): ServiceDto

    @PUT("/api/services/{id}")
    suspend fun updateService(
        @Path("id") id: String,
        @Body request: UpdateServiceRequest,
    ): ServiceDto

    @DELETE("/api/services/{id}")
    suspend fun deleteService(
        @Path("id") id: String,
    ): Response<Unit>

    @Multipart
    @POST("/api/users/me/avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part,
    ): UserDto
}
