# Architecture Application Android Ora

## Vue d'ensemble

L'application Ora Android suit une architecture **Clean Architecture** avec les principes **MVVM** et utilise **Jetpack Compose** pour l'interface utilisateur. Cette approche garantit une séparation claire des responsabilités, une testabilité optimale et une maintenabilité à long terme.

## Structure des Couches

### 1. Presentation Layer (UI + ViewModel)

#### 1.1 UI Layer - Jetpack Compose
```kotlin
presentation/
├── ui/
│   ├── screens/           # Écrans principaux
│   │   ├── home/         # Dashboard principal
│   │   ├── library/      # Bibliothèque de contenu
│   │   ├── journal/      # Journal personnel
│   │   ├── profile/      # Profil et statistiques
│   │   ├── player/       # Lecteur vidéo/audio
│   │   └── onboarding/   # Première utilisation
│   ├── components/       # Composants réutilisables
│   │   ├── cards/        # Cartes de contenu
│   │   ├── buttons/      # Boutons customisés
│   │   ├── inputs/       # Champs de saisie
│   │   └── common/       # Composants communs
│   └── theme/           # Thème Material 3
├── navigation/          # Navigation Compose
└── viewmodel/          # ViewModels MVVM
```

#### 1.2 ViewModels
Chaque écran principal possède son ViewModel :
- **HomeViewModel** : Gestion dashboard et recommandations
- **LibraryViewModel** : Gestion bibliothèque et filtres
- **JournalViewModel** : Gestion journal et saisies
- **ProfileViewModel** : Gestion profil et statistiques
- **PlayerViewModel** : Gestion lecture média

**Pattern utilisé :**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDailyRecommendationUseCase: GetDailyRecommendationUseCase,
    private val getQuickActionsUseCase: GetQuickActionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadHomeData() {
        viewModelScope.launch {
            // Logique de chargement avec UseCases
        }
    }
}
```

### 2. Domain Layer (Business Logic)

#### 2.1 Structure Domain
```kotlin
domain/
├── model/              # Modèles métier purs
│   ├── User.kt
│   ├── Content.kt
│   ├── JournalEntry.kt
│   ├── UserActivity.kt
│   └── Program.kt
├── repository/         # Interfaces repository
│   ├── UserRepository.kt
│   ├── ContentRepository.kt
│   ├── JournalRepository.kt
│   └── MediaRepository.kt
└── usecase/           # Cas d'usage métier
    ├── user/          # Use cases utilisateur
    ├── content/       # Use cases contenu
    ├── journal/       # Use cases journal
    └── media/         # Use cases média
```

#### 2.2 Use Cases
Chaque action métier est encapsulée dans un UseCase :

```kotlin
@Singleton
class GetDailyRecommendationUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(): Result<Content> {
        return try {
            val userPreferences = userRepository.getUserPreferences()
            val recommendation = contentRepository.getPersonalizedRecommendation(
                preferences = userPreferences,
                timeOfDay = LocalTime.now()
            )
            Result.success(recommendation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 3. Data Layer (Repository + Sources)

#### 3.1 Structure Data
```kotlin
data/
├── local/             # Sources de données locales
│   ├── database/      # Room Database
│   │   ├── entities/  # Entités Room
│   │   ├── dao/       # Data Access Objects
│   │   └── OraDatabase.kt
│   ├── datastore/     # Préférences DataStore
│   └── files/         # Stockage fichiers
├── remote/            # Sources de données distantes (futur)
│   ├── api/           # API REST
│   └── dto/           # Data Transfer Objects
└── repository/        # Implémentations Repository
    ├── UserRepositoryImpl.kt
    ├── ContentRepositoryImpl.kt
    └── JournalRepositoryImpl.kt
```

#### 3.2 Repository Pattern
```kotlin
@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val contentDao: ContentDao,
    private val contentPreferences: ContentPreferences
) : ContentRepository {

    override fun getAllContent(): Flow<List<Content>> {
        return contentDao.getAllContent()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getContentById(id: String): Content? {
        return contentDao.getContentById(id)?.toDomain()
    }

    override suspend fun addToFavorites(contentId: String): Result<Unit> {
        return try {
            contentDao.addToFavorites(contentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Injection de Dépendances avec Hilt

### Configuration Modules
```kotlin
di/
├── DatabaseModule.kt    # Configuration Room
├── RepositoryModule.kt  # Binding Repository
├── UseCaseModule.kt     # Configuration UseCases
├── NetworkModule.kt     # Configuration réseau (futur)
└── PreferencesModule.kt # Configuration DataStore
```

### Exemple de Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    abstract fun bindContentRepository(
        contentRepositoryImpl: ContentRepositoryImpl
    ): ContentRepository
}
```

## Navigation avec Compose

### Architecture Navigation
```kotlin
@Composable
fun OraNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { OraBottomNavigation(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = OraDestinations.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Définition des destinations
        }
    }
}
```

### Destinations Typées
```kotlin
sealed class OraDestinations(val route: String) {
    object Home : OraDestinations("home")
    object Library : OraDestinations("library")

    object ContentDetail : OraDestinations("content_detail/{contentId}") {
        fun createRoute(contentId: String) = "content_detail/$contentId"
    }
}
```

## Gestion des États UI

### Pattern State/Event
```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val dailyRecommendation: Content? = null,
    val quickActions: List<QuickAction> = emptyList(),
    val error: String? = null
)

sealed class HomeUiEvent {
    object LoadData : HomeUiEvent()
    data class ContentClicked(val contentId: String) : HomeUiEvent()
    data class QuickActionClicked(val actionId: String) : HomeUiEvent()
}
```

### Gestion Reactive avec Flow
```kotlin
class HomeViewModel : ViewModel() {

    val uiState = combine(
        getUserRecommendationFlow(),
        getQuickActionsFlow(),
        getProgressFlow()
    ) { recommendation, actions, progress ->
        HomeUiState(
            dailyRecommendation = recommendation,
            quickActions = actions,
            progressOverview = progress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}
```

## Persistance des Données

### Room Database
```kotlin
@Database(
    entities = [
        UserEntity::class,
        ContentEntity::class,
        JournalEntryEntity::class,
        UserActivityEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class OraDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contentDao(): ContentDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun userActivityDao(): UserActivityDao
}
```

### DataStore pour Préférences
```kotlin
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val userPreferences = dataStore.data.map { preferences ->
        UserPreferences(
            onboardingCompleted = preferences[ONBOARDING_COMPLETED] ?: false,
            preferredTimeSlots = preferences[PREFERRED_TIME_SLOTS]?.split(",") ?: emptyList(),
            notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true
        )
    }
}
```

## Lecteur Multimédia

### ExoPlayer Integration
```kotlin
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private var exoPlayer: ExoPlayer? = null

    fun initializePlayer(context: Context, contentId: String) {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(getContentUri(contentId))
            setMediaItem(mediaItem)
            prepare()
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer?.release()
    }
}
```

## Testing Strategy

### Structure des Tests
```
test/
├── unit/              # Tests unitaires
│   ├── viewmodel/     # Tests ViewModels
│   ├── usecase/       # Tests Use Cases
│   └── repository/    # Tests Repository
└── integration/       # Tests d'intégration

androidTest/
├── ui/                # Tests UI Compose
├── database/          # Tests Room
└── navigation/        # Tests navigation
```

### Exemple Test ViewModel
```kotlin
@Test
fun `when loading home data, should emit loading then success state`() = runTest {
    // Given
    val mockRecommendation = createMockContent()
    whenever(getDailyRecommendationUseCase()).thenReturn(Result.success(mockRecommendation))

    // When
    viewModel.loadHomeData()

    // Then
    viewModel.uiState.test {
        val initialState = awaitItem()
        assertThat(initialState.isLoading).isTrue()

        val loadedState = awaitItem()
        assertThat(loadedState.isLoading).isFalse()
        assertThat(loadedState.dailyRecommendation).isEqualTo(mockRecommendation)
    }
}
```

## Performance & Optimisations

### Lazy Loading
- **LazyColumn/LazyRow** pour les listes
- **Paging 3** pour la pagination (futur)
- **Image loading** avec Coil et cache LRU

### Memory Management
- **ViewModel scope** pour les coroutines
- **Proper lifecycle management** pour ExoPlayer
- **Database indexing** pour les requêtes fréquentes

### Offline-First
- **Room comme source de vérité**
- **Synchronisation différée** avec WorkManager
- **Cache stratégique** pour les médias

Cette architecture garantit une application robuste, scalable et maintenir tout en respectant les meilleures pratiques Android modernes.