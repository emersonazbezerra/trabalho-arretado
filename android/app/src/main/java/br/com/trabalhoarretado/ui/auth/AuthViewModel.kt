package br.com.trabalhoarretado.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trabalhoarretado.data.dto.RegisterRequest
import br.com.trabalhoarretado.data.dto.UserDto
import br.com.trabalhoarretado.data.repository.AuthRepository
import br.com.trabalhoarretado.domain.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()

    data object Loading : AuthUiState()

    data class Success(
        val user: UserDto,
    ) : AuthUiState()

    data class Error(
        val message: String,
    ) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(
        email: String,
        password: String,
    ) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Preencha email e senha")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = authRepository.login(email.trim(), password)) {
                    is Result.Success -> AuthUiState.Success(result.data.user)
                    is Result.Error ->
                        AuthUiState.Error(
                            if (result.httpStatus == 401) "Usuário e/ou senha inválidos" else result.message,
                        )
                }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        city: String?,
        state: String?,
        phone: String?,
    ) {
        if (name.isBlank() || email.isBlank() || password.length < 6) {
            _uiState.value = AuthUiState.Error("Nome, email e senha (≥6 caracteres) são obrigatórios")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val req =
                RegisterRequest(
                    name = name.trim(),
                    email = email.trim(),
                    password = password,
                    role = role,
                    city = city?.takeIf { it.isNotBlank() },
                    state = state?.takeIf { it.isNotBlank() },
                    phone = phone?.takeIf { it.isNotBlank() },
                )
            _uiState.value =
                when (val result = authRepository.register(req)) {
                    is Result.Success -> AuthUiState.Success(result.data.user)
                    is Result.Error -> AuthUiState.Error(result.message)
                }
        }
    }

    fun reset() {
        _uiState.value = AuthUiState.Idle
    }
}
