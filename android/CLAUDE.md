# CLAUDE.md — android/ (App Android Kotlin/Compose)

Este é o app Android do **Trabalho Arretado**. Leia também `../docs/DOMAIN.md` e
`../docs/DATA_GUIDE.md` antes de qualquer tarefa de modelagem ou desenvolvimento.

---

## Stack

| Tecnologia | Versão | Papel |
|---|---|---|
| Kotlin | 2.x | Linguagem principal |
| Jetpack Compose | latest stable | UI declarativa nativa Android |
| Jetpack Navigation (Compose) | 2.x | Navegação entre telas |
| ViewModel + StateFlow | — | Gerenciamento de estado (MVVM) |
| Retrofit | 2.x | Consumo da API REST |
| OkHttp | 4.x | HTTP client (interceptors, logging) |
| Room | 2.x | Persistência local de dados |
| DataStore (Preferences) | 1.x | Armazenamento do token JWT |
| Coil | 3.x | Carregamento assíncrono de imagens |
| Koin | 4.x | Injeção de dependência |
| kotlinx.serialization | — | Serialização JSON |
| Android Studio | latest stable | IDE de desenvolvimento |

---

## Estrutura de pacotes

```
android/app/src/main/java/com/trabalhoarretado/
├── MainActivity.kt                  ← entry point, NavHost raiz
├── ui/
│   ├── theme/
│   │   ├── Color.kt                 ← paleta de cores
│   │   ├── Theme.kt                 ← MaterialTheme config
│   │   └── Type.kt                  ← tipografia (Roboto)
│   ├── splash/
│   │   └── SplashScreen.kt
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   └── AuthViewModel.kt
│   ├── home/
│   │   ├── HomeScreen.kt            ← feed de profissionais em destaque
│   │   └── HomeViewModel.kt
│   ├── search/
│   │   ├── SearchScreen.kt          ← busca com filtros
│   │   └── SearchViewModel.kt
│   ├── professional/
│   │   ├── ProfessionalProfileScreen.kt
│   │   ├── ProfessionalProfileViewModel.kt
│   │   └── PublishServiceScreen.kt
│   ├── favorites/
│   │   ├── FavoritesScreen.kt
│   │   └── FavoritesViewModel.kt
│   └── profile/
│       ├── MyProfileScreen.kt
│       └── MyProfileViewModel.kt
├── data/
│   ├── remote/
│   │   ├── ApiService.kt            ← interface Retrofit com todos os endpoints
│   │   └── dto/                     ← data classes para request/response JSON
│   ├── local/
│   │   ├── AppDatabase.kt           ← Room database
│   │   └── dao/                     ← DAOs Room
│   └── repository/
│       ├── AuthRepository.kt
│       ├── ProfessionalRepository.kt
│       ├── FavoritesRepository.kt
│       └── ReviewRepository.kt
└── di/
    └── AppModule.kt                 ← módulo Koin com todas as dependências
```

---

## Telas do aplicativo

| Tela | Rota de navegação | Descrição |
|------|------------------|-----------|
| Splash / Onboarding | `splash` | Tela de abertura com logo; redireciona para login ou home |
| Login | `auth/login` | Autenticação por e-mail e senha |
| Cadastro | `auth/register` | Cadastro com seleção de perfil (cliente ou profissional) |
| Home (Cliente) | `home` | Feed com profissionais em destaque e categorias |
| Busca e Filtros | `search` | Busca por nome, categoria ou cidade; filtros por avaliação |
| Perfil do Profissional | `professional/{id}` | Foto, bio, serviços, avaliações e botão de contato |
| Publicar Serviço | `professional/service/new` | Formulário para cadastrar/editar serviço |
| Meu Perfil | `profile` | Visualização e edição de dados pessoais |
| Favoritos | `favorites` | Lista de profissionais marcados como favoritos |

---

## Paleta de cores

```kotlin
// ui/theme/Color.kt
val NavyDark   = Color(0xFF1A1A2E)  // primária — confiança e profissionalismo
val OrangeRed  = Color(0xFFE94560)  // secundária — CTAs e elementos de ação
val Background = Color(0xFFFFFFFF)  // fundo principal
val Surface    = Color(0xFFF4F4F8)  // fundo de cards e superfícies alternadas
val OnSurface  = Color(0xFF333333)  // texto principal
```

O design segue **Material Design 3 (Material You)** com cards arredondados, campos de texto
com bordas suaves e botões com estados visuais claros. Tipografia: família **Roboto** (padrão
Android). Navegação principal via **Bottom Navigation Bar**.

---

## Convenções de código

- **Arquitetura:** MVVM — UI (Composables) → ViewModel → Repository → DataSource
- **Estado:** `StateFlow` + `UiState` sealed class por tela; nunca expor `MutableStateFlow` para a UI
- **Coroutines:** `viewModelScope.launch` nos ViewModels; `suspend fun` nos repositórios
- **Nulabilidade:** evitar `!!` — usar `?: return` ou `?: throw` com mensagem descritiva
- **Composables:** funções sem estado (`stateless`) sempre que possível; elevar estado para o ViewModel
- **Nomenclatura:** telas em `NomeScreen.kt`, ViewModels em `NomeViewModel.kt`

### Exemplo de ViewModel

```kotlin
// ui/home/HomeViewModel.kt
class HomeViewModel(
    private val professionalRepo: ProfessionalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadProfessionals() }

    private fun loadProfessionals() {
        viewModelScope.launch {
            _uiState.value = when (val result = professionalRepo.getFeatured()) {
                is Result.Success -> HomeUiState.Success(result.data)
                is Result.Error   -> HomeUiState.Error(result.message)
            }
        }
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val professionals: List<Professional>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
```

### Exemplo de Composable consumindo estado

```kotlin
// ui/home/HomeScreen.kt
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is HomeUiState.Loading -> CircularProgressIndicator()
        is HomeUiState.Error   -> ErrorMessage(state.message)
        is HomeUiState.Success -> ProfessionalList(state.professionals)
    }
}
```

---

## Autenticação e token JWT

O token recebido no login é salvo no **DataStore** e injetado automaticamente pelo Retrofit
via `AuthInterceptor`:

```kotlin
// data/remote/AuthInterceptor.kt
class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenStore.getToken() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else chain.request()
        return chain.proceed(request)
    }
}
```

---

## Comandos Gradle

```bash
# Build debug
./gradlew assembleDebug

# Instalar no emulador/dispositivo conectado
./gradlew installDebug

# Rodar testes unitários
./gradlew test

# Rodar testes instrumentados (emulador necessário)
./gradlew connectedAndroidTest

# Verificar lint
./gradlew lint

# Limpar build
./gradlew clean
```

---

## Variáveis de ambiente necessárias

```
# android/local.properties  (nunca commitar)
API_BASE_URL=http://10.0.2.2:8080   # emulador aponta para localhost da máquina host
```

Em produção, `API_BASE_URL` aponta para o servidor Ktor hospedado no Railway. Usar `BuildConfig` para injetar
a URL no build:

```kotlin
// build.gradle.kts (app)
buildConfigField("String", "API_BASE_URL", "\"${localProperties["API_BASE_URL"]}\"")
```
