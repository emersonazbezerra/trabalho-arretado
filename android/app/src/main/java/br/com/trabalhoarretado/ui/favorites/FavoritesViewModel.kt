package br.com.trabalhoarretado.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trabalhoarretado.data.dto.ProfessionalSummaryDto
import br.com.trabalhoarretado.data.repository.FavoriteRepository
import br.com.trabalhoarretado.domain.Result
import br.com.trabalhoarretado.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<ProfessionalSummaryDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ProfessionalSummaryDto>>> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value =
                when (val result = favoriteRepository.list()) {
                    is Result.Success ->
                        if (result.data.isEmpty()) UiState.Empty else UiState.Success(result.data)
                    is Result.Error -> UiState.Error(result.message)
                }
        }
    }
}
