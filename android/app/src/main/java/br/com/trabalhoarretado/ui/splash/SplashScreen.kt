package br.com.trabalhoarretado.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import br.com.trabalhoarretado.data.local.TokenStore
import br.com.trabalhoarretado.data.repository.AuthRepository
import br.com.trabalhoarretado.domain.Result
import org.koin.compose.koinInject

@Composable
fun SplashScreen(
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit,
    tokenStore: TokenStore = koinInject(),
    authRepository: AuthRepository = koinInject(),
) {
    LaunchedEffect(Unit) {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            onUnauthenticated()
            return@LaunchedEffect
        }
        when (authRepository.me()) {
            is Result.Success -> onAuthenticated()
            is Result.Error -> onUnauthenticated()
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Trabalho Arretado!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}
