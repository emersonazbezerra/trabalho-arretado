package br.com.trabalhoarretado.ui.professional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trabalhoarretado.data.dto.CreateServiceRequest
import br.com.trabalhoarretado.data.dto.UpdateServiceRequest
import br.com.trabalhoarretado.data.repository.AuthRepository
import br.com.trabalhoarretado.data.repository.ProfessionalRepository
import br.com.trabalhoarretado.data.repository.ServiceRepository
import br.com.trabalhoarretado.domain.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ServiceForm(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val category: String = "",
)

sealed class PublishUiState {
    data object Idle : PublishUiState()

    data object LoadingService : PublishUiState()

    data object Submitting : PublishUiState()

    data class Error(
        val message: String,
    ) : PublishUiState()

    data object Done : PublishUiState()
}

class PublishServiceViewModel(
    private val serviceRepository: ServiceRepository,
    private val professionalRepository: ProfessionalRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _form = MutableStateFlow(ServiceForm())
    val form: StateFlow<ServiceForm> = _form.asStateFlow()

    private val _state = MutableStateFlow<PublishUiState>(PublishUiState.Idle)
    val state: StateFlow<PublishUiState> = _state.asStateFlow()

    private var serviceId: String? = null
    private var loaded = false

    val isEditing: Boolean get() = serviceId != null

    fun load(serviceId: String?) {
        if (loaded) return
        loaded = true
        this.serviceId = serviceId
        if (serviceId == null) return
        // Edição: busca o próprio perfil e localiza o serviço para preencher o formulário.
        viewModelScope.launch {
            _state.value = PublishUiState.LoadingService
            val me = authRepository.me()
            if (me !is Result.Success) {
                _state.value = PublishUiState.Error((me as Result.Error).message)
                return@launch
            }
            when (val profile = professionalRepository.getById(me.data.id)) {
                is Result.Success -> {
                    val service = profile.data.services.find { it.id == serviceId }
                    if (service == null) {
                        _state.value = PublishUiState.Error("Serviço não encontrado")
                    } else {
                        _form.value =
                            ServiceForm(
                                title = service.title,
                                description = service.description.orEmpty(),
                                price = service.estimatedPrice?.let { "%.2f".format(it) }.orEmpty(),
                                category = service.category,
                            )
                        _state.value = PublishUiState.Idle
                    }
                }
                is Result.Error -> _state.value = PublishUiState.Error(profile.message)
            }
        }
    }

    fun setTitle(value: String) {
        _form.value = _form.value.copy(title = value)
    }

    fun setDescription(value: String) {
        _form.value = _form.value.copy(description = value)
    }

    fun setPrice(value: String) {
        _form.value = _form.value.copy(price = value)
    }

    fun setCategory(value: String) {
        _form.value = _form.value.copy(category = value)
    }

    fun submit() {
        val current = _form.value
        if (current.title.isBlank() || current.category.isBlank()) {
            _state.value = PublishUiState.Error("Título e categoria são obrigatórios")
            return
        }
        val price = current.price.trim().replace(',', '.').toDoubleOrNull()
        val description = current.description.trim().takeIf { it.isNotEmpty() }
        viewModelScope.launch {
            _state.value = PublishUiState.Submitting
            val result =
                if (isEditing) {
                    serviceRepository.update(
                        serviceId!!,
                        UpdateServiceRequest(
                            title = current.title.trim(),
                            description = description,
                            estimatedPrice = price,
                            category = current.category,
                        ),
                    )
                } else {
                    serviceRepository.create(
                        CreateServiceRequest(
                            title = current.title.trim(),
                            description = description,
                            estimatedPrice = price,
                            category = current.category,
                        ),
                    )
                }
            _state.value =
                when (result) {
                    is Result.Success -> PublishUiState.Done
                    is Result.Error -> PublishUiState.Error(result.message)
                }
        }
    }
}
