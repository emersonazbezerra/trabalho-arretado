package br.com.trabalhoarretado.ui.search

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.trabalhoarretado.ui.common.ServiceCategory
import br.com.trabalhoarretado.ui.common.ProfessionalCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialCategory: String?,
    onBack: () -> Unit,
    onProfessionalClick: (String) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    LaunchedEffect(initialCategory) { viewModel.initialize(initialCategory) }

    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalCount = listState.layoutInfo.totalItemsCount
            totalCount > 0 && lastVisible >= totalCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && state == SearchUiState.Success) viewModel.loadMore()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar profissionais") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            OutlinedTextField(
                value = filters.cityQuery,
                onValueChange = viewModel::setCityQuery,
                label = { Text("Cidade") },
                singleLine = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ServiceCategory.entries) { category ->
                    FilterChip(
                        selected = filters.category == category.name,
                        onClick = { viewModel.toggleCategory(category.name) },
                        label = { Text(category.label) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f),
            ) {
                when (val s = state) {
                    is SearchUiState.Idle, SearchUiState.Loading ->
                        CenteredBox { CircularProgressIndicator() }
                    is SearchUiState.Empty ->
                        CenteredBox { Text("Nenhum profissional encontrado.") }
                    is SearchUiState.Error ->
                        CenteredBox {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(s.message, color = MaterialTheme.colorScheme.error)
                                TextButton(onClick = { viewModel.toggleCategory(filters.category ?: "") }) {
                                    Text("Tentar novamente")
                                }
                            }
                        }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            items(items, key = { it.id }) { professional ->
                                ProfessionalCard(
                                    professional = professional,
                                    onClick = { onProfessionalClick(professional.id) },
                                )
                            }
                            if (state == SearchUiState.LoadingMore) {
                                item {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(64.dp),
                                        contentAlignment = Alignment.Center,
                                    ) { CircularProgressIndicator() }
                                }
                            }
                        }
                    }
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
                .fillMaxSize()
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
