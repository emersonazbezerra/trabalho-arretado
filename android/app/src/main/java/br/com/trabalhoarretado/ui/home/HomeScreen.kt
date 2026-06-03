package br.com.trabalhoarretado.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.trabalhoarretado.data.repository.AuthRepository
import br.com.trabalhoarretado.ui.common.PROFESSIONAL_CATEGORIES
import br.com.trabalhoarretado.ui.common.ProfessionalCard
import br.com.trabalhoarretado.ui.common.UiState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearch: (category: String?) -> Unit,
    onProfessionalClick: (String) -> Unit,
    onFavorites: () -> Unit,
    onMyProfile: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    authRepository: AuthRepository = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trabalho Arretado") },
                actions = {
                    IconButton(onClick = { onSearch(null) }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = onFavorites) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favoritos")
                    }
                    IconButton(onClick = onMyProfile) {
                        Icon(Icons.Filled.Person, contentDescription = "Meu perfil")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            authRepository.logout()
                            onLoggedOut()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            item {
                Text("Categorias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PROFESSIONAL_CATEGORIES) { category ->
                        AssistChip(
                            onClick = { onSearch(category) },
                            label = { Text(category) },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Destaques", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
            }

            when (val s = state) {
                is UiState.Loading -> item { CenteredBox { CircularProgressIndicator() } }
                is UiState.Error -> item {
                    CenteredBox {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(s.message, color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = viewModel::load) { Text("Tentar novamente") }
                        }
                    }
                }
                is UiState.Empty -> item {
                    CenteredBox { Text("Nenhum profissional cadastrado ainda.") }
                }
                is UiState.Success ->
                    items(s.data) { professional ->
                        ProfessionalCard(
                            professional = professional,
                            onClick = { onProfessionalClick(professional.id) },
                        )
                    }
            }
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(180.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
