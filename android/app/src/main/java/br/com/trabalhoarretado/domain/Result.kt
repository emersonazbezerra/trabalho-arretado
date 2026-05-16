package br.com.trabalhoarretado.domain

import br.com.trabalhoarretado.data.dto.ApiErrorDto
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

sealed class Result<out T> {
    data class Success<T>(
        val data: T,
    ) : Result<T>()

    data class Error(
        val code: String,
        val message: String,
        val httpStatus: Int? = null,
    ) : Result<Nothing>()
}

private val errorJson = Json { ignoreUnknownKeys = true }

suspend fun <T> apiCall(block: suspend () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: HttpException) {
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val parsed = body?.takeIf { it.isNotBlank() }?.let { runCatching { errorJson.decodeFromString<ApiErrorDto>(it) }.getOrNull() }
        Result.Error(
            code = parsed?.error?.code ?: "HTTP_${e.code()}",
            message = parsed?.error?.message ?: "Falha na requisição (HTTP ${e.code()})",
            httpStatus = e.code(),
        )
    } catch (_: IOException) {
        Result.Error("NETWORK", "Sem conexão com o servidor")
    } catch (e: Exception) {
        Result.Error("UNKNOWN", e.message ?: "Erro inesperado")
    }
