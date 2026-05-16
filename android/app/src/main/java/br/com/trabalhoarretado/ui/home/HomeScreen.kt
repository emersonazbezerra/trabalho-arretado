package br.com.trabalhoarretado.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.trabalhoarretado.data.dto.UserDto
import br.com.trabalhoarretado.data.repository.AuthRepository
import br.com.trabalhoarretado.domain.Result
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
    onLoggedOut: () -> Unit,
    authRepository: AuthRepository = koinInject(),
) {
    var user by remember { mutableStateOf<UserDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        when (val r = authRepository.me()) {
            is Result.Success -> user = r.data
            is Result.Error -> error = r.message
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            user != null -> {
                Text("Olá, ${user!!.name}", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("Papel: ${user!!.role}", style = MaterialTheme.typography.bodyMedium)
            }
            error != null -> Text("Erro: $error", color = MaterialTheme.colorScheme.error)
            else -> CircularProgressIndicator()
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = {
            scope.launch {
                authRepository.logout()
                onLoggedOut()
            }
        }) { Text("Sair") }
    }
}
