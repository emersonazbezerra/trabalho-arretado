package br.com.trabalhoarretado.ui.professional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trabalhoarretado.data.dto.ProfessionalProfileDto
import br.com.trabalhoarretado.data.dto.ReviewDto
import br.com.trabalhoarretado.data.repository.AuthRepository
import br.com.trabalhoarretado.data.repository.FavoriteRepository
import br.com.trabalhoarretado.data.repository.ProfessionalRepository
import br.com.trabalhoarretado.data.repository.ReviewRepository
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
    private val reviewRepository: ReviewRepository,
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

    // ID do usuário logado — usado para impedir que profissional avalie a si mesmo.
    private val _currentUserId = MutableStateFlow<String?>(null)

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
            val me = authRepository.me()
            if (me is Result.Success) {
                _currentUserId.value = me.data.id
                if (me.data.role == "CLIENT") {
                    _isClient.value = true
                    val favorites = favoriteRepository.list()
                    if (favorites is Result.Success) {
                        _isFavorite.value = favorites.data.any { it.id == id }
                    }
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

    fun canReview(): Boolean {
        // Apenas clientes podem avaliar; e o profissional não pode se auto-avaliar.
        val profId = (_state.value as? UiState.Success)?.data?.id ?: return false
        return _isClient.value && _currentUserId.value != profId
    }

    fun retry() {
        loaded = false
        load(professionalId)
    }
}
