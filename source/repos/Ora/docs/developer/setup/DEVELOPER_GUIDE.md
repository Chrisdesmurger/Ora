# Guide Développeur - Ora Android

## Table des matières

1. [Configuration initiale](#configuration-initiale)
2. [Architecture](#architecture)
3. [Standards de code](#standards-de-code)
4. [Développement UI](#développement-ui)
5. [Tests](#tests)
6. [Débogage](#débogage)
7. [Performance](#performance)
8. [Bonnes pratiques](#bonnes-pratiques)

## Configuration initiale

### Prérequis système

```bash
# Versions minimales
Android Studio Giraffe (2023.2.1) ou plus récent
JDK 17
Android SDK API 26-34
Kotlin 1.9.20+
```

### Setup du projet

```bash
# 1. Cloner le repository
git clone https://github.com/ora-wellbeing/android.git
cd android

# 2. Configuration Git hooks
cp scripts/git-hooks/* .git/hooks/
chmod +x .git/hooks/*

# 3. Variables d'environnement locales
cp local.properties.example local.properties
# Éditer local.properties avec vos configs

# 4. Premier build
./gradlew build
```

### Configuration IDE

**Android Studio settings recommandés :**

```xml
<!-- Code Style > Kotlin -->
<code_scheme name="Ora">
  <option name="RIGHT_MARGIN" value="120" />
  <option name="WRAP_WHEN_TYPING_REACHES_RIGHT_MARGIN" value="true" />

  <!-- Kotlin specific -->
  <option name="PACKAGES_TO_USE_STAR_IMPORTS">
    <value />
  </option>
  <option name="NAME_COUNT_TO_USE_STAR_IMPORT" value="999" />
  <option name="NAME_COUNT_TO_USE_STAR_IMPORT_FOR_MEMBERS" value="999" />
</code_scheme>
```

**Plugins essentiels :**
- Kotlin
- Android
- Detekt
- SonarLint
- GitToolBox

## Architecture

### Vue d'ensemble

Ora utilise Clean Architecture avec MVVM :

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Presentation   │────│     Domain      │────│      Data       │
│   (UI Layer)    │    │ (Business Logic)│    │  (Data Sources) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Structure des packages

```
com.ora.wellbeing/
├── data/
│   ├── local/          # Room, DataStore, Cache
│   ├── remote/         # API, DTOs, Network
│   └── repository/     # Repository implementations
├── domain/
│   ├── entity/         # Business entities
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use cases
├── presentation/
│   ├── ui/            # Composables
│   ├── viewmodel/     # ViewModels
│   ├── navigation/    # Navigation logic
│   └── theme/         # Design system
├── di/                # Dependency injection
└── utils/             # Extensions, utilities
```

### Injection de dépendances

Utilisation de Hilt pour l'injection :

```kotlin
@HiltAndroidApp
class OraApplication : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity()

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel()
```

### Gestion d'état

**ViewModel + StateFlow**

```kotlin
class JournalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    fun updateGratitude(index: Int, value: String) {
        _uiState.update { currentState ->
            when (index) {
                0 -> currentState.copy(gratitude1 = value)
                1 -> currentState.copy(gratitude2 = value)
                2 -> currentState.copy(gratitude3 = value)
                else -> currentState
            }
        }
    }
}
```

## Standards de code

### Conventions de nommage

**Classes et interfaces :**
```kotlin
// Classes
class UserRepository
class JournalViewModel
class ContentDao

// Interfaces
interface UserRepository
interface ApiService

// Composables
@Composable
fun JournalScreen()
@Composable
fun GratitudeInput()
```

**Fonctions et variables :**
```kotlin
// Fonctions
fun getUserById(userId: String): User?
fun calculateCurrentStreak(): Int

// Variables
val currentUser: User?
val isLoading: Boolean
private val _uiState = MutableStateFlow()
```

### Documentation du code

**KDoc pour les APIs publiques :**

```kotlin
/**
 * Repository pour gérer les entrées de journal de l'utilisateur.
 *
 * Fournit des méthodes pour créer, lire, mettre à jour et supprimer
 * les entrées de journal, avec synchronisation cloud optionnelle.
 */
interface JournalRepository {

    /**
     * Sauvegarde une entrée de journal pour la date spécifiée.
     *
     * @param userId ID de l'utilisateur
     * @param date Date de l'entrée
     * @param gratitudes Liste des 3 gratitudes
     * @param mood Humeur sélectionnée
     * @param dayStory Récit optionnel de la journée
     * @return L'entrée sauvegardée
     * @throws UserNotFoundException Si l'utilisateur n'existe pas
     */
    suspend fun saveJournalEntry(
        userId: String,
        date: LocalDate,
        gratitudes: List<String>,
        mood: Mood,
        dayStory: String? = null
    ): JournalEntry
}
```

### Gestion des erreurs

**Sealed classes pour les résultats :**

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// Usage
suspend fun getUser(id: String): Result<User> {
    return try {
        val user = userDao.getUserById(id)
        if (user != null) {
            Result.Success(user)
        } else {
            Result.Error(UserNotFoundException("User $id not found"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

## Développement UI

### Design System

**Utilisation des tokens de design :**

```kotlin
@Composable
fun GratitudeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(OraSpacing.medium),
        colors = CardDefaults.cardColors(
            containerColor = OraColors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = OraElevation.small
        )
    ) {
        Text(
            text = "Ma gratitude",
            style = OraTypography.headlineSmall,
            color = OraColors.onSurface
        )
    }
}
```

### Composables réutilisables

**Structure recommandée :**

```kotlin
@Composable
fun OraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = when (variant) {
                ButtonVariant.Primary -> OraColors.primary
                ButtonVariant.Secondary -> OraColors.secondary
            }
        )
    ) {
        Text(text = text)
    }
}

enum class ButtonVariant { Primary, Secondary }
```

### Accessibilité

**Bonnes pratiques :**

```kotlin
@Composable
fun AccessibleContent() {
    Column(
        modifier = Modifier.semantics {
            contentDescription = "Écran de journal"
        }
    ) {
        OutlinedTextField(
            value = gratitude1,
            onValueChange = { onGratitudeChanged(0, it) },
            label = { Text("Première gratitude") },
            modifier = Modifier.semantics {
                contentDescription = "Champ pour la première gratitude"
            }
        )

        IconButton(
            onClick = onSave,
            modifier = Modifier
                .size(48.dp) // Taille minimale 48dp
                .semantics {
                    contentDescription = "Sauvegarder l'entrée"
                    role = Role.Button
                }
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
        }
    }
}
```

## Tests

### Tests unitaires

**Structure des tests :**

```kotlin
@RunWith(MockitoJUnitRunner::class)
class JournalRepositoryTest {

    @Mock
    private lateinit var journalDao: JournalDao

    @Mock
    private lateinit var apiClient: ApiClient

    private lateinit var repository: JournalRepository

    @Before
    fun setup() {
        repository = JournalRepositoryImpl(journalDao, apiClient)
    }

    @Test
    fun `saveJournalEntry creates new entry when none exists`() = runTest {
        // Given
        val userId = "user123"
        val date = LocalDate.now()
        whenever(journalDao.getEntryByDate(userId, date)).thenReturn(null)

        // When
        val result = repository.saveJournalEntry(
            userId = userId,
            date = date,
            gratitudes = listOf("Test 1", "Test 2", "Test 3"),
            mood = Mood.HAPPY
        )

        // Then
        verify(journalDao).insertEntry(any())
        assertEquals(userId, result.userId)
        assertEquals(date, result.date)
    }
}
```

### Tests UI

**Tests Compose :**

```kotlin
@RunWith(AndroidJUnit4::class)
class JournalScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun journalScreen_displaysGratitudeInputs() {
        composeTestRule.setContent {
            OraTheme {
                JournalScreen(
                    uiState = JournalUiState(),
                    onGratitudeChanged = { _, _ -> },
                    onSave = { }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Première gratitude")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Sauvegarder l'entrée")
            .assertIsDisplayed()
    }

    @Test
    fun gratitudeInput_updatesOnTextChange() {
        var gratitude1 = ""

        composeTestRule.setContent {
            OraTheme {
                GratitudeInput(
                    value = gratitude1,
                    onValueChange = { gratitude1 = it },
                    label = "Test"
                )
            }
        }

        composeTestRule
            .onNodeWithText("Test")
            .performTextInput("Ma famille")

        assertEquals("Ma famille", gratitude1)
    }
}
```

### Tests d'accessibilité

```kotlin
@Test
fun journalScreen_hasProperAccessibility() {
    composeTestRule.setContent {
        OraTheme {
            JournalScreen(/* ... */)
        }
    }

    // Vérifier les content descriptions
    composeTestRule
        .onAllNodesWithContentDescription("")
        .assertCountEquals(0)

    // Vérifier les tailles de cibles tactiles
    composeTestRule
        .onNodeWithContentDescription("Sauvegarder")
        .assertHeightIsAtLeast(48.dp)
        .assertWidthIsAtLeast(48.dp)

    // Vérifier la navigation au clavier
    composeTestRule
        .onNodeWithText("Gratitude 1")
        .performImeAction()

    composeTestRule
        .onNodeWithText("Gratitude 2")
        .assertIsFocused()
}
```

## Débogage

### Logs structurés

**Utilisation de Timber :**

```kotlin
class JournalRepository {

    fun saveEntry(entry: JournalEntry) {
        Timber.d("Saving journal entry for user ${entry.userId}")

        try {
            dao.insert(entry)
            Timber.i("Journal entry saved successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save journal entry")
            throw e
        }
    }
}

// Configuration dans Application
class OraApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }
}
```

### Débogage de base de données

**Inspection Room :**

```kotlin
// Debug uniquement
@Database(
    entities = [User::class, JournalEntry::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OraDatabase : RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): OraDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                OraDatabase::class.java,
                "ora_database"
            )
                .apply {
                    if (BuildConfig.DEBUG) {
                        // Inspection dans Database Inspector
                        fallbackToDestructiveMigration()
                        addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                Timber.d("Database created")
                            }
                        })
                    }
                }
                .build()
        }
    }
}
```

### Network debugging

**Inspection réseau :**

```kotlin
private fun createOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })

                // Inspection dans Network Inspector
                addNetworkInterceptor(FlipperOkhttpInterceptor(networkFlipperPlugin))
            }
        }
        .build()
}
```

## Performance

### Optimisations Compose

**Stabilité des états :**

```kotlin
// ❌ Instable - recomposition à chaque frame
@Composable
fun ContentList(contents: List<Content>) {
    LazyColumn {
        items(contents) { content ->
            ContentItem(
                content = content,
                onClick = { /* handle click */ } // Lambda recréée
            )
        }
    }
}

// ✅ Stable - callbacks mémorisés
@Composable
fun ContentList(
    contents: List<Content>,
    onContentClick: (Content) -> Unit
) {
    LazyColumn {
        items(
            items = contents,
            key = { it.id } // Clé stable
        ) { content ->
            ContentItem(
                content = content,
                onClick = remember(content.id) {
                    { onContentClick(content) }
                }
            )
        }
    }
}
```

**Optimisation des listes :**

```kotlin
@Composable
fun OptimizedContentList(
    contents: List<Content>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = contents,
            key = { it.id },
            contentType = { it.type } // Optimisation type-based
        ) { content ->
            ContentItem(
                content = content,
                modifier = Modifier.animateItemPlacement() // Animation smooth
            )
        }
    }
}
```

### Gestion mémoire

**Images et cache :**

```kotlin
@Composable
fun OptimizedAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .size(300, 200) // Taille spécifique
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        },
        error = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Erreur de chargement"
            )
        }
    )
}
```

## Bonnes pratiques

### Sécurité

**Stockage sécurisé :**

```kotlin
class SecurePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthToken(token: String) {
        encryptedPrefs.edit()
            .putString("auth_token", token)
            .apply()
    }
}
```

**Validation des entrées :**

```kotlin
class InputValidator {

    fun validateGratitude(text: String): ValidationResult {
        return when {
            text.isBlank() -> ValidationResult.Error("La gratitude ne peut pas être vide")
            text.length < 3 -> ValidationResult.Error("Minimum 3 caractères")
            text.length > 200 -> ValidationResult.Error("Maximum 200 caractères")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
```

### Gestion asynchrone

**Coroutines et scopes :**

```kotlin
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository
) : ViewModel() {

    fun saveEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val entry = repository.saveJournalEntry(/* ... */)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
                Timber.e(e, "Failed to save journal entry")
            }
        }
    }
}
```

### Accessibilité

**Support complet :**

```kotlin
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp) // Taille minimale
            .semantics {
                contentDescription = text
                role = Role.Button
                if (!enabled) {
                    disabled()
                }
            },
        enabled = enabled
    ) {
        Text(
            text = text,
            fontSize = 16.sp // Taille minimale lisible
        )
    }
}
```

### Tests et qualité

**Coverage et métriques :**

```bash
# Génération coverage report
./gradlew testDebugUnitTestCoverage

# Analyse qualité complète
./gradlew check

# Tests spécifiques
./gradlew testDebugUnitTest --tests="*Repository*"
./gradlew connectedDebugAndroidTest --tests="*Screen*"
```

---

**Questions fréquentes et support :**

- 💬 **Discord** : [discord.gg/ora-dev](https://discord.gg/ora-dev)
- 📧 **Email** : dev-support@ora-wellbeing.com
- 📚 **Wiki** : [github.com/ora-wellbeing/android/wiki](https://github.com/ora-wellbeing/android/wiki)

**Dernière mise à jour :** 2025-09-28