package br.com.trabalhoarretado.ui.professional

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.trabalhoarretado.data.dto.ProfessionalProfileDto
import br.com.trabalhoarretado.data.dto.ReviewDto
import br.com.trabalhoarretado.data.dto.ServiceDto
import br.com.trabalhoarretado.ui.common.UiState
import br.com.trabalhoarretado.ui.common.categoryLabel
import coil3.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalProfileScreen(
    professionalId: String,
    onBack: () -> Unit,
    viewModel: ProfessionalProfileViewModel = koinViewModel(),
) {
    LaunchedEffect(professionalId) { viewModel.load(professionalId) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val isClient by viewModel.isClient.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val favoriteBusy by viewModel.favoriteBusy.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val canReview by viewModel.canReview.collectAsStateWithLifecycle()
    val showReviewForm by viewModel.showReviewForm.collectAsStateWithLifecycle()
    val reviewSubmitting by viewModel.reviewSubmitting.collectAsStateWithLifecycle()
    val reviewError by viewModel.reviewError.collectAsStateWithLifecycle()
    val initialRating by viewModel.initialRating.collectAsStateWithLifecycle()
    val initialComment by viewModel.initialComment.collectAsStateWithLifecycle()

    if (showReviewForm) {
        ReviewFormDialog(
            initialRating = initialRating,
            initialComment = initialComment,
            submitting = reviewSubmitting,
            error = reviewError,
            onDismiss = viewModel::closeReviewForm,
            onSubmit = viewModel::submitReview,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil do profissional") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (isClient) {
                        IconButton(onClick = viewModel::toggleFavorite, enabled = !favoriteBusy) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFavorite) "Desfavoritar" else "Favoritar",
                                tint = if (isFavorite) Color(0xFFE94560) else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (val s = state) {
                is UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Empty -> Text("Profissional não encontrado.", Modifier.align(Alignment.Center))
                is UiState.Error ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(s.message, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = viewModel::retry) { Text("Tentar novamente") }
                    }
                is UiState.Success ->
                    ProfileContent(
                        profile = s.data,
                        reviews = reviews,
                        canReview = canReview,
                        onReviewClick = viewModel::openReviewForm,
                        onRetryReviews = viewModel::loadReviews,
                    )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: ProfessionalProfileDto,
    reviews: UiState<List<ReviewDto>>,
    canReview: Boolean,
    onReviewClick: () -> Unit,
    onRetryReviews: () -> Unit,
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier =
                        Modifier
                            .size(96.dp)
                            .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!profile.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profile.avatarUrl,
                            contentDescription = profile.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Text(
                            text = profile.name.firstOrNull()?.uppercase().orEmpty(),
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(profile.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                val location = listOfNotNull(profile.city, profile.state).joinToString(", ")
                if (location.isNotBlank()) {
                    Text(location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(18.dp))
                    Text(
                        when (profile.reviewCount) {
                            0 -> "Sem avaliações"
                            1 -> "%.1f (1 avaliação)".format(profile.averageRating)
                            else -> "%.1f (%d avaliações)".format(profile.averageRating, profile.reviewCount)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(Modifier.height(16.dp))
                if (!profile.phone.isNullOrBlank()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, whatsappUri(profile.phone).toUri())
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.padding(start = 8.dp))
                        Text("Conversar no WhatsApp")
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "Serviços",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        if (profile.services.isEmpty()) {
            item {
                Text(
                    "Este profissional ainda não publicou serviços.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(profile.services) { service -> ServiceCard(service) }
        }

        item {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Avaliações",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (canReview) {
                    TextButton(onClick = onReviewClick) {
                        Icon(Icons.Filled.RateReview, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.padding(start = 4.dp))
                        Text("Avaliar")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        when (val r = reviews) {
            is UiState.Loading ->
                item { CircularProgressIndicator(Modifier.padding(16.dp)) }
            is UiState.Empty ->
                item {
                    Text(
                        "Nenhuma avaliação ainda. Seja o primeiro!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            is UiState.Error ->
                item {
                    Column {
                        Text(r.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onRetryReviews) { Text("Tentar novamente") }
                    }
                }
            is UiState.Success ->
                items(r.data) { review -> ReviewCard(review) }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ReviewCard(review: ReviewDto) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(review.clientName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                StarRatingDisplay(review.rating)
            }
            if (!review.comment.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(review.comment, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun StarRatingDisplay(rating: Int) {
    Row {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun ReviewFormDialog(
    initialRating: Int,
    initialComment: String,
    submitting: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String) -> Unit,
) {
    var selectedRating by remember(initialRating) { mutableIntStateOf(initialRating) }
    var comment by remember(initialComment) { mutableStateOf(initialComment) }

    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text("Avaliar profissional") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Sua nota:", style = MaterialTheme.typography.bodyMedium)
                Row {
                    repeat(5) { index ->
                        IconButton(onClick = { selectedRating = index + 1 }) {
                            Icon(
                                imageVector = if (index < selectedRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "${index + 1} estrelas",
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentário (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    enabled = !submitting,
                )
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedRating, comment) },
                enabled = selectedRating > 0 && !submitting,
            ) {
                if (submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Enviar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) { Text("Cancelar") }
        },
    )
}

@Composable
private fun ServiceCard(service: ServiceDto) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(service.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(categoryLabel(service.category)) })
            }
            if (!service.description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(service.description, style = MaterialTheme.typography.bodyMedium)
            }
            service.estimatedPrice?.let { price ->
                Spacer(Modifier.height(4.dp))
                Text(
                    "A partir de R$ %.2f".format(price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun whatsappUri(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    val withCountry = if (digits.startsWith("55")) digits else "55$digits"
    return "https://wa.me/$withCountry"
}
