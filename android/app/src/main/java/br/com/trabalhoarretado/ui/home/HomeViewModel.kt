package br.com.trabalhoarretado.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trabalhoarretado.data.dto.ProfessionalSummaryDto
import br.com.trabalhoarretado.data.repository.ProfessionalRepository
import br.com.trabalhoarretado.domain.Result
import br.com.trabalhoarretado.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val professionalRepository: ProfessionalRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<ProfessionalSummaryDto>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ProfessionalSummaryDto>>> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = fetch()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _state.value = fetch()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetch(): UiState<List<ProfessionalSummaryDto>> =
        when (val result = professionalRepository.list(category = null, city = null, page = 1)) {
            is Result.Success ->
                if (result.data.data.isEmpty()) UiState.Empty else UiState.Success(result.data.data)
            is Result.Error -> UiState.Error(result.message)
        }
}
