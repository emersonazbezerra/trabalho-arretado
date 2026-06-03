package br.com.trabalhoarretado.ui.professional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trabalhoarretado.data.dto.ProfessionalProfileDto
import br.com.trabalhoarretado.data.repository.AuthRepository
import br.com.trabalhoarretado.data.repository.FavoriteRepository
import br.com.trabalhoarretado.data.repository.ProfessionalRepository
import br.com.trabalhoarretado.domain.Result
import br.com.trabalhoarretado.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfessionalProfileViewModel(
    private val professionalRepository: ProfessionalRepository,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<ProfessionalProfileDto>>(UiState.Loading)
    val state: StateFlow<UiState<ProfessionalProfileDto>> = _state.asStateFlow()

    // Botão de favoritar só aparece para clientes (favoritos são CLIENT-only no backend).
    private val _isClient = MutableStateFlow(false)
    val isClient: StateFlow<Boolean> = _isClient.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _favoriteBusy = MutableStateFlow(false)
    val favoriteBusy: StateFlow<Boolean> = _favoriteBusy.asStateFlow()

    private var professionalId: String = ""
    private var loaded = false

    fun load(id: String) {
        if (loaded) return
        loaded = true
        professionalId = id
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val result = professionalRepository.getById(id)) {
                is Result.Success -> _state.value = UiState.Success(result.data)
                is Result.Error -> {
                    _state.value = UiState.Error(result.message)
                    return@launch
                }
            }
            // Descobre o papel do usuário e, se cliente, o estado inicial do favorito.
            val me = authRepository.me()
            if (me is Result.Success && me.data.role == "CLIENT") {
                _isClient.value = true
                val favorites = favoriteRepository.list()
                if (favorites is Result.Success) {
                    _isFavorite.value = favorites.data.any { it.id == id }
                }
            }
        }
    }

    fun toggleFavorite() {
        if (!_isClient.value || _favoriteBusy.value) return
        val target = !_isFavorite.value
        viewModelScope.launch {
            _favoriteBusy.value = true
            _isFavorite.value = target // atualização otimista
            val result =
                if (target) {
                    favoriteRepository.add(professionalId)
                } else {
                    favoriteRepository.remove(professionalId)
                }
            if (result is Result.Error) _isFavorite.value = !target // rollback em erro
            _favoriteBusy.value = false
        }
    }

    fun retry() {
        loaded = false
        load(professionalId)
    }
}
