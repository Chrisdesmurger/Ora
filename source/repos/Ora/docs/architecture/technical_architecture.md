# Architecture Technique Android - Ora

## Vue d'ensemble

L'application Ora Android suit une architecture **MVVM (Model-View-ViewModel)** moderne utilisant **Jetpack Compose** pour l'interface utilisateur et **Hilt** pour l'injection de dépendances.

## Stack Technique

### Framework UI
- **Jetpack Compose** - Interface utilisateur déclarative
- **Material 3** - Design system moderne et accessible
- **Navigation Compose** - Navigation entre écrans
- **Compose Animation** - Animations fluides

### Architecture
- **MVVM Pattern** - Séparation claire des responsabilités
- **Repository Pattern** - Abstraction de la couche données
- **UseCase Pattern** - Logique métier encapsulée
- **Hilt** - Injection de dépendances

### Gestion des données
- **Room Database** - Base de données locale SQLite
- **DataStore** - Préférences utilisateur
- **StateFlow** - Gestion d'état réactive
- **Coroutines** - Programmation asynchrone

### Media & Contenu
- **ExoPlayer (Media3)** - Lecture vidéo/audio performante
- **Coil** - Chargement d'images optimisé
- **WorkManager** - Tâches en arrière-plan

## Structure du Projet

```
app/
├── src/main/
│   ├── java/com/ora/wellbeing/
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── database/
│   │   │   │   │   ├── entities/
│   │   │   │   │   ├── dao/
│   │   │   │   │   └── OraDatabase.kt
│   │   │   │   ├── datastore/
│   │   │   │   └── preferences/
│   │   │   └── repository/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   ├── presentation/
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   ├── components/
│   │   │   │   ├── screens/
│   │   │   │   │   ├── onboarding/
│   │   │   │   │   ├── home/
│   │   │   │   │   ├── library/
│   │   │   │   │   ├── journal/
│   │   │   │   │   ├── profile/
│   │   │   │   │   └── player/
│   │   │   │   └── navigation/
│   │   │   └── viewmodel/
│   │   ├── di/
│   │   └── util/
│   └── res/
├── build.gradle.kts
└── proguard-rules.pro
```

## Couches Architecture

### 1. Presentation Layer (UI + ViewModel)

#### Composables UI
- **Screens** - Écrans complets (HomeScreen, JournalScreen, etc.)
- **Components** - Composants réutilisables (Cards, Buttons, etc.)
- **Theme** - Tokens de design Material 3

#### ViewModels
- Gestion d'état avec **StateFlow/MutableStateFlow**
- Communication avec la couche Domain via UseCases
- Gestion du cycle de vie automatique

#### Navigation
- **NavHost** central avec routes typées
- **Bottom Navigation** pour navigation principale
- **Deep Links** pour accès direct aux contenus

### 2. Domain Layer (Business Logic)

#### Models
- **Data Classes** pures sans dépendances Android
- **Sealed Classes** pour les états d'UI
- **Enums** pour les types fixes

#### UseCases
- **Single Responsibility** - Une action métier par UseCase
- **Testables** - Logique métier isolée
- **Réutilisables** - Partagés entre ViewModels

#### Repository Interfaces
- **Abstractions** de la couche données
- **Contrats** clairs pour l'accès aux données

### 3. Data Layer (Repository + Sources)

#### Repository Implementations
- **Single Source of Truth** - Room comme source principale
- **Cache Strategy** - Données en mémoire pour performance
- **Offline First** - Fonctionnement sans réseau

#### Local Data Sources
- **Room Database** - Stockage persistant
- **DataStore** - Préférences utilisateur
- **File System** - Assets media locaux

## Patterns Clés

### State Management
```kotlin
// ViewModel avec StateFlow
class HomeViewModel @Inject constructor(
    private val getDailyRecommendationUseCase: GetDailyRecommendationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadDailyRecommendation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val recommendation = getDailyRecommendationUseCase()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                dailyRecommendation = recommendation
            )
        }
    }
}
```

### Repository Pattern
```kotlin
@Singleton
class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao,
    private val dataStore: UserPreferencesDataStore
) : JournalRepository {

    override suspend fun saveJournalEntry(entry: JournalEntry): Result<Unit> {
        return try {
            journalDao.insertJournalEntry(entry.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getJournalEntries(): Flow<List<JournalEntry>> {
        return journalDao.getAllJournalEntries()
            .map { entities -> entities.map { it.toDomain() } }
    }
}
```

### UseCase Pattern
```kotlin
@Singleton
class SaveJournalEntryUseCase @Inject constructor(
    private val journalRepository: JournalRepository
) {
    suspend operator fun invoke(
        gratitudes: List<String>,
        mood: Mood,
        dayStory: String
    ): Result<Unit> {
        val entry = JournalEntry(
            id = UUID.randomUUID().toString(),
            date = LocalDate.now(),
            gratitudes = gratitudes,
            mood = mood,
            dayStory = dayStory,
            createdAt = LocalDateTime.now()
        )

        return journalRepository.saveJournalEntry(entry)
    }
}
```

## Gestion des Médias

### Video Player Architecture
- **ExoPlayer** pour lecture vidéo haute performance
- **Custom Controls** avec timer de session
- **Picture-in-Picture** pour multitâche
- **Background Audio** pour méditations

### Content Management
- **Local Storage** pour contenu hors ligne
- **Progressive Download** pour optimiser l'espace
- **Quality Adaptation** selon la connexion

## Performance & Optimisations

### Memory Management
- **Lazy Loading** des listes avec LazyColumn/LazyRow
- **Image Caching** avec Coil et cache LRU
- **Database Indexing** pour requêtes rapides

### Battery Optimization
- **WorkManager** pour tâches différées
- **Doze Mode** compatibility
- **Background Limits** respectées

### Startup Performance
- **App Startup** library pour initialisation
- **Hilt** compilation rapide
- **Baseline Profiles** pour optimisation ART

## Accessibilité (AA Compliance)

### Implementation
- **Content Descriptions** sur tous les éléments interactifs
- **Semantic Roles** appropriés
- **Focus Management** pour navigation clavier
- **High Contrast** support
- **Large Text** scaling

### Testing
- **Accessibility Scanner** intégré
- **TalkBack** testing mandatory
- **Switch Access** compatibility

## Sécurité & Confidentialité

### Data Protection
- **Room Encryption** avec SQLCipher (optionnel)
- **DataStore Encryption** pour données sensibles
- **Biometric Authentication** pour journal privé

### Privacy by Design
- **Minimal Permissions** - uniquement nécessaires
- **Local First** - pas de tracking par défaut
- **Transparent Consent** - permissions explicites

## Configuration Build

### Gradle Setup
- **Version Catalogs** pour dépendances centralisées
- **Build Variants** (debug, release, staging)
- **Proguard/R8** pour obfuscation release
- **Baseline Profiles** pour performance

### CI/CD Pipeline
- **GitHub Actions** pour build automatisé
- **Unit Tests** mandatory avant merge
- **UI Tests** sur Firebase Test Lab
- **Code Quality** avec Detekt/Ktlint

## Monitoring & Analytics

### Performance Monitoring
- **Firebase Performance** pour métriques temps réel
- **Custom Metrics** pour actions utilisateur
- **Crash Reporting** avec Firebase Crashlytics

### User Analytics
- **Privacy-First** analytics optionnelles
- **Local Metrics** pour insights sans tracking
- **User Consent** requis pour télémétrie

## Migration & Versioning

### Database Migrations
- **Room Migrations** versionnées
- **Backward Compatibility** maintenue
- **Data Integrity** checks automatiques

### App Updates
- **In-App Updates** avec Play Core
- **Feature Flags** pour rollout progressif
- **Graceful Degradation** si features indisponibles

## Testing Strategy

### Unit Tests
- **ViewModels** - logique d'état
- **UseCases** - logique métier
- **Repositories** - accès données

### UI Tests
- **Compose Tests** - interface utilisateur
- **Integration Tests** - flux complets
- **Accessibility Tests** - compliance AA

Cette architecture garantit une application robuste, performante et maintenable, respectant les meilleures pratiques Android modernes.