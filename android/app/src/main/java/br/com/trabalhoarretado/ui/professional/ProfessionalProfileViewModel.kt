package br.com.trabalhoarretado.ui.professional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trabalhoarretado.data.dto.ProfessionalProfileDto
import br.com.trabalhoarretado.data.dto.ReviewDto
import br.com.trabalhoarretado.data.local.TokenStore
import br.com.trabalhoarretado.data.repository.FavoriteRepository
import br.com.trabalhoarretado.data.repository.ProfessionalRepository
import br.com.trabalhoarretado.data.repository.ReviewRepository
import br.com.trabalhoarretado.domain.Result
import br.com.trabalhoarretado.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfessionalProfileViewModel(
    private val professionalRepository: ProfessionalRepository,
    private val favoriteRepository: FavoriteRepository,
    private val reviewRepository: ReviewRepository,
    private val tokenStore: TokenStore,
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

    private val _reviews = MutableStateFlow<UiState<List<ReviewDto>>>(UiState.Loading)
    val reviews: StateFlow<UiState<List<ReviewDto>>> = _reviews.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)

    // Derivado reativo: atualiza automaticamente quando _state, _isClient ou _currentUserId mudam.
    val canReview: StateFlow<Boolean> =
        combine(_state, _isClient, _currentUserId) { state, isClient, userId ->
            val profId = (state as? UiState.Success)?.data?.id ?: return@combine false
            isClient && userId != profId
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val showReviewForm = MutableStateFlow(false)

    private val _reviewSubmitting = MutableStateFlow(false)
    val reviewSubmitting: StateFlow<Boolean> = _reviewSubmitting.asStateFlow()

    private val _reviewError = MutableStateFlow<String?>(null)
    val reviewError: StateFlow<String?> = _reviewError.asStateFlow()

    // Nota e comentário iniciais ao abrir o formulário (pré-populado da avaliação existente).
    private val _initialRating = MutableStateFlow(0)
    val initialRating: StateFlow<Int> = _initialRating.asStateFlow()

    private val _initialComment = MutableStateFlow("")
    val initialComment: StateFlow<String> = _initialComment.asStateFlow()

    private var professionalId: String = ""
    private var loaded = false

    init {
        // Lê role e userId do cache imediatamente ao criar o ViewModel,
        // antes mesmo de load() ser chamado pela tela.
        viewModelScope.launch {
            val role = tokenStore.getUserRole()
            _currentUserId.value = tokenStore.getUserId()
            if (role == "CLIENT") _isClient.value = true
        }
    }

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
            if (_isClient.value) {
                val favorites = favoriteRepository.list()
                if (favorites is Result.Success) {
                    _isFavorite.value = favorites.data.any { it.id == id }
                }
            }
        }
        loadReviews()
    }

    fun loadReviews() {
        viewModelScope.launch {
            _reviews.value = UiState.Loading
            _reviews.value = when (val result = reviewRepository.list(professionalId)) {
                is Result.Success ->
                    if (result.data.isEmpty()) UiState.Empty else UiState.Success(result.data)
                is Result.Error -> UiState.Error(result.message)
            }
        }
    }

    fun toggleFavorite() {
        if (!_isClient.value || _favoriteBusy.value) return
        val target = !_isFavorite.value
        viewModelScope.launch {
            _favoriteBusy.value = true
            _isFavorite.value = target
            val result =
                if (target) favoriteRepository.add(professionalId) else favoriteRepository.remove(professionalId)
            if (result is Result.Error) _isFavorite.value = !target
            _favoriteBusy.value = false
        }
    }

    fun openReviewForm() {
        _reviewError.value = null
        val existing = (_reviews.value as? UiState.Success)?.data
            ?.firstOrNull { it.clientId == _currentUserId.value }
        _initialRating.value = existing?.rating ?: 0
        _initialComment.value = existing?.comment.orEmpty()
        showReviewForm.value = true
    }

    fun closeReviewForm() {
        showReviewForm.value = false
    }

    fun submitReview(
        rating: Int,
        comment: String,
    ) {
        viewModelScope.launch {
            _reviewSubmitting.value = true
            _reviewError.value = null
            when (val result = reviewRepository.create(professionalId, rating, comment)) {
                is Result.Success -> {
                    showReviewForm.value = false
                    loadReviews()
                }
                is Result.Error -> _reviewError.value = result.message
            }
            _reviewSubmitting.value = false
        }
    }

    fun retry() {
        loaded = false
        load(professionalId)
    }
}
