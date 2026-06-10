package br.com.trabalhoarretado.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.trabalhoarretado.data.dto.ServiceDto
import br.com.trabalhoarretado.data.dto.UserDto
import br.com.trabalhoarretado.ui.common.UiState
import br.com.trabalhoarretado.ui.common.categoryLabel
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onBack: () -> Unit,
    onPublishNew: () -> Unit,
    onEditService: (String) -> Unit,
    viewModel: MyProfileViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.loadUser() }
    LifecycleResumeEffect(Unit) {
        viewModel.refreshServices()
        onPauseOrDispose { }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val avatarPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                scope.launch {
                    val bytes = decodeResized(context, uri)
                    if (bytes != null) viewModel.uploadAvatar(bytes, "image/jpeg")
                }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (val s = state) {
                is UiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Empty -> Text("Não foi possível carregar o perfil.", Modifier.align(Alignment.Center))
                is UiState.Error ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(s.message, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = viewModel::loadUser) { Text("Tentar novamente") }
                    }
                is UiState.Success ->
                    ProfileForm(
                        user = s.data,
                        services = services,
                        saving = saving,
                        onPickAvatar = {
                            avatarPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        onSave = viewModel::save,
                        onPublishNew = onPublishNew,
                        onEditService = onEditService,
                        onDeleteService = viewModel::deleteService,
                        onBecomeProfessional = viewModel::becomeProfessional,
                    )
            }
        }
    }
}

@Composable
private fun ProfileForm(
    user: UserDto,
    services: List<ServiceDto>,
    saving: Boolean,
    onPickAvatar: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
    onPublishNew: () -> Unit,
    onEditService: (String) -> Unit,
    onDeleteService: (String) -> Unit,
    onBecomeProfessional: () -> Unit,
) {
    val isProfessional = user.role == "PROFESSIONAL"
    var name by remember(user.id) { mutableStateOf(user.name) }
    var city by remember(user.id) { mutableStateOf(user.city.orEmpty()) }
    var state by remember(user.id) { mutableStateOf(user.state.orEmpty()) }
    var phone by remember(user.id) { mutableStateOf(user.phone.orEmpty()) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier =
                    Modifier
                        .size(104.dp)
                        .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (!user.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        text = user.name.firstOrNull()?.uppercase().orEmpty(),
                        style = MaterialTheme.typography.displaySmall,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        FilledTonalButton(onClick = onPickAvatar, enabled = !saving) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Trocar foto")
        }
        Spacer(Modifier.height(16.dp))
        Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        if (isProfessional) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Cidade") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("Estado (UF)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefone (WhatsApp)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onSave(name, city, state, phone) },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Salvar perfil")
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Meus serviços", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onPublishNew) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Novo")
                }
            }
            if (services.isEmpty()) {
                Text(
                    "Você ainda não publicou serviços.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                services.forEach { service ->
                    ServiceRow(
                        service = service,
                        onClick = { onEditService(service.id) },
                        onDelete = { onDeleteService(service.id) },
                    )
                }
            }
        } else {
            // CLIENT: dados básicos somente leitura nesta fase.
            ReadOnlyField("Nome", user.name)
            ReadOnlyField("Cidade", user.city)
            ReadOnlyField("Estado", user.state)
            ReadOnlyField("Telefone", user.phone)

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBecomeProfessional,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Fornecer serviços")
            }
        }
    }
}

@Composable
private fun ServiceRow(
    service: ServiceDto,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(categoryLabel(service.category), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Remover serviço", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ReadOnlyField(
    label: String,
    value: String?,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value?.takeIf { it.isNotBlank() } ?: "—", style = MaterialTheme.typography.bodyLarge)
    }
}

private suspend fun decodeResized(
    context: Context,
    uri: Uri,
): ByteArray? =
    withContext(Dispatchers.IO) {
        val bitmap =
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                ?: return@withContext null
        val maxSide = 1024
        val scale = maxSide.toFloat() / maxOf(bitmap.width, bitmap.height)
        val scaled =
            if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true,
                )
            } else {
                bitmap
            }
        ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.toByteArray()
        }
    }
