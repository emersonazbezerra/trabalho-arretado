package br.com.trabalhoarretado.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trabalhoarretado.data.dto.ProfessionalSummaryDto
import br.com.trabalhoarretado.data.repository.ProfessionalRepository
import br.com.trabalhoarretado.domain.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchFilters(
    val cityQuery: String = "",
    val category: String? = null,
)

sealed class SearchUiState {
    data object Idle : SearchUiState()

    data object Loading : SearchUiState()

    data object LoadingMore : SearchUiState()

    data object Success : SearchUiState()

    data object Empty : SearchUiState()

    data class Error(
        val message: String,
    ) : SearchUiState()
}

class SearchViewModel(
    private val professionalRepository: ProfessionalRepository,
) : ViewModel() {
    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters.asStateFlow()

    private val _items = MutableStateFlow<List<ProfessionalSummaryDto>>(emptyList())
    val items: StateFlow<List<ProfessionalSummaryDto>> = _items.asStateFlow()

    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var currentPage = 0
    private var totalPages = 1
    private var debounceJob: Job? = null
    private var loadJob: Job? = null

    fun initialize(category: String?) {
        if (_state.value != SearchUiState.Idle) return
        _filters.value = _filters.value.copy(category = category)
        reload()
    }

    fun setCityQuery(query: String) {
        _filters.value = _filters.value.copy(cityQuery = query)
        debounceJob?.cancel()
        debounceJob =
            viewModelScope.launch {
                delay(300)
                reload()
            }
    }

    fun toggleCategory(category: String) {
        val current = _filters.value.category
        _filters.value = _filters.value.copy(category = if (current == category) null else category)
        reload()
    }

    fun loadMore() {
        if (_state.value == SearchUiState.Loading || _state.value == SearchUiState.LoadingMore) return
        if (currentPage >= totalPages) return
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                _state.value = SearchUiState.LoadingMore
                fetch(page = currentPage + 1, append = true)
            }
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                _isRefreshing.value = true
                currentPage = 0
                totalPages = 1
                fetch(page = 1, append = false)
                _isRefreshing.value = false
            }
    }

    private fun reload() {
        loadJob?.cancel()
        currentPage = 0
        totalPages = 1
        _items.value = emptyList()
        loadJob =
            viewModelScope.launch {
                _state.value = SearchUiState.Loading
                fetch(page = 1, append = false)
            }
    }

    private suspend fun fetch(
        page: Int,
        append: Boolean,
    ) {
        val city = _filters.value.cityQuery.trim().takeIf { it.isNotEmpty() }
        val category = _filters.value.category
        when (val result = professionalRepository.list(category, city, page)) {
            is Result.Success -> {
                currentPage = result.data.pagination.page
                totalPages = result.data.pagination.totalPages
                _items.value = if (append) _items.value + result.data.data else result.data.data
                _state.value = if (_items.value.isEmpty()) SearchUiState.Empty else SearchUiState.Success
            }
            is Result.Error -> _state.value = SearchUiState.Error(result.message)
        }
    }
}
