package br.com.trabalhoarretado.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

private val ROLES = listOf("CLIENT" to "Cliente", "PROFESSIONAL" to "Profissional")

private val BRAZILIAN_STATES =
    listOf(
        "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO",
        "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI",
        "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO",
    )

// Prefixo literal inserido imediatamente antes de cada dígito, no formato (XX) XXXXX-XXXX
private val PHONE_DIGIT_PREFIXES = listOf("(", "", ") ", "", "", "", "", "-", "", "", "")

private val PhoneVisualTransformation =
    VisualTransformation { text ->
        val digits = text.text.take(11)
        val formatted = StringBuilder()
        val digitPositions = IntArray(digits.length)
        for (i in digits.indices) {
            formatted.append(PHONE_DIGIT_PREFIXES[i])
            digitPositions[i] = formatted.length
            formatted.append(digits[i])
        }

        val offsetMapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val clamped = offset.coerceIn(0, digits.length)
                    return if (clamped == digits.length) formatted.length else digitPositions[clamped]
                }

                override fun transformedToOriginal(offset: Int): Int = digitPositions.count { it < offset }
            }

        TransformedText(AnnotatedString(formatted.toString()), offsetMapping)
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("CLIENT") }
    var stateExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onRegistered()
            viewModel.reset()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Criar conta", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Text("Eu sou:")
        Row(verticalAlignment = Alignment.CenterVertically) {
            ROLES.forEach { (value, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = role == value, onClick = { role = value })
                    Text(label, modifier = Modifier.padding(end = 12.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            supportingText = { Text("A senha precisa ter pelo menos 6 caracteres") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Cidade") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = stateExpanded,
            onExpandedChange = { stateExpanded = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = state,
                onValueChange = {},
                readOnly = true,
                label = { Text("UF") },
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
            )
            DropdownMenu(
                expanded = stateExpanded,
                onDismissRequest = { stateExpanded = false },
                modifier = Modifier.exposedDropdownSize(),
            ) {
                BRAZILIAN_STATES.forEach { uf ->
                    DropdownMenuItem(
                        text = { Text(uf) },
                        onClick = {
                            state = uf
                            stateExpanded = false
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { input ->
                val digits = input.filter { it.isDigit() }.take(11)
                phone = digits
            },
            label = { Text("Telefone") },
            singleLine = true,
            visualTransformation = PhoneVisualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))
        when (val s = uiState) {
            is AuthUiState.Loading -> CircularProgressIndicator()
            is AuthUiState.Error -> {
                Text(s.message, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.register(name, email, password, role, city, state, phone) },
                ) { Text("Cadastrar") }
            }
            else ->
                Button(
                    onClick = { viewModel.register(name, email, password, role, city, state, phone) },
                ) { Text("Cadastrar") }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBackToLogin) { Text("Já tenho conta") }

        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.height(16.dp)) // espaço extra para teclado
        Spacer(Modifier.height(16.dp))
    }
}
