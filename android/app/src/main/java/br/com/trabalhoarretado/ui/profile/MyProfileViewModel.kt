package br.com.trabalhoarretado.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trabalhoarretado.data.dto.ServiceDto
import br.com.trabalhoarretado.data.dto.UpdateProfessionalRequest
import br.com.trabalhoarretado.data.dto.UserDto
import br.com.trabalhoarretado.data.repository.AuthRepository
import br.com.trabalhoarretado.data.repository.ProfessionalRepository
import br.com.trabalhoarretado.data.repository.ServiceRepository
import br.com.trabalhoarretado.data.repository.UserRepository
import br.com.trabalhoarretado.domain.Result
import br.com.trabalhoarretado.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyProfileViewModel(
    private val authRepository: AuthRepository,
    private val professionalRepository: ProfessionalRepository,
    private val userRepository: UserRepository,
    private val serviceRepository: ServiceRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<UserDto>>(UiState.Loading)
    val state: StateFlow<UiState<UserDto>> = _state.asStateFlow()

    private val _services = MutableStateFlow<List<ServiceDto>>(emptyList())
    val services: StateFlow<List<ServiceDto>> = _services.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    // Feedback transitório (snackbar). A tela consome e chama clearMessage().
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val isProfessional: Boolean
        get() = (_state.value as? UiState.Success)?.data?.role == "PROFESSIONAL"

    private val userId: String?
        get() = (_state.value as? UiState.Success)?.data?.id

    fun loadUser() {
        viewModelScope.launch {
            if (_state.value !is UiState.Success) _state.value = UiState.Loading
            when (val result = authRepository.me()) {
                is Result.Success -> {
                    _state.value = UiState.Success(result.data)
                    if (result.data.role == "PROFESSIONAL") fetchServices(result.data.id)
                }
                is Result.Error -> _state.value = UiState.Error(result.message)
            }
        }
    }

    fun refreshServices() {
        val id = userId ?: return
        if (!isProfessional) return
        viewModelScope.launch { fetchServices(id) }
    }

    private suspend fun fetchServices(professionalId: String) {
        when (val result = professionalRepository.getById(professionalId)) {
            is Result.Success -> _services.value = result.data.services
            is Result.Error -> { /* mantém lista anterior; perfil já carregou */ }
        }
    }

    fun save(
        name: String,
        city: String,
        state: String,
        phone: String,
    ) {
        val id = userId ?: return
        if (!isProfessional) return
        if (name.isBlank()) {
            _message.value = "Nome é obrigatório"
            return
        }
        viewModelScope.launch {
            _saving.value = true
            val result =
                userRepository.updateProfessional(
                    id,
                    UpdateProfessionalRequest(
                        name = name.trim(),
                        city = city.trim().takeIf { it.isNotEmpty() },
                        state = state.trim().takeIf { it.isNotEmpty() },
                        phone = phone.trim().takeIf { it.isNotEmpty() },
                    ),
                )
            _saving.value = false
            when (result) {
                is Result.Success -> {
                    _message.value = "Perfil atualizado"
                    loadUser()
                }
                is Result.Error -> _message.value = result.message
            }
        }
    }

    fun uploadAvatar(
        bytes: ByteArray,
        mimeType: String,
    ) {
        viewModelScope.launch {
            _saving.value = true
            val result = userRepository.uploadAvatar(bytes, mimeType)
            _saving.value = false
            when (result) {
                is Result.Success -> {
                    _state.value = UiState.Success(result.data)
                    _message.value = "Foto atualizada"
                }
                is Result.Error -> _message.value = result.message
            }
        }
    }

    fun becomeProfessional() {
        viewModelScope.launch {
            _saving.value = true
            val result = authRepository.becomeProfessional()
            _saving.value = false
            when (result) {
                is Result.Success -> {
                    _message.value = "Agora você é um profissional"
                    loadUser()
                }
                is Result.Error -> _message.value = result.message
            }
        }
    }

    fun deleteService(id: String) {
        viewModelScope.launch {
            when (val result = serviceRepository.delete(id)) {
                is Result.Success -> {
                    _services.value = _services.value.filterNot { it.id == id }
                    _message.value = "Serviço removido"
                }
                is Result.Error -> _message.value = result.message
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
