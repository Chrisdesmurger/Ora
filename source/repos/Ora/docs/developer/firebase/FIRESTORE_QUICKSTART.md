# 🚀 Firestore Quick Start - Intégration Rapide

Guide rapide pour intégrer Firestore dans vos ViewModels et UI.

## ⚡ Snippets Essentiels

### 1. AuthViewModel - Créer Profil au Login

```kotlin
// presentation/screens/auth/AuthViewModel.kt
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val createUserProfile: CreateUserProfileUseCase // ← Injecter
) : ViewModel() {

    fun onGoogleSignInSuccess(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            // FIX(user-dynamic): Créer profil + stats au premier login
            val result = createUserProfile(
                uid = firebaseUser.uid,
                firstName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString()
            )

            if (result.isSuccess) {
                Timber.i("User profile created successfully")
                navigateToHome()
            } else {
                Timber.e("Failed to create user profile")
                showError("Erreur création profil")
            }
        }
    }
}
```

### 2. ProfileViewModel - Observer Profil + Stats

```kotlin
// presentation/screens/profile/ProfileViewModel.kt
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserData: GetUserDataUseCase, // ← Injecter
    private val authRepository: AuthRepository
) : ViewModel() {

    // FIX(user-dynamic): Flow réactif profil + stats
    val userData: StateFlow<UserData> = authRepository.currentUser
        .filterNotNull()
        .flatMapLatest { user -> getUserData(user.uid) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserData(null, null)
        )

    // UI State dérivé
    val displayName: StateFlow<String> = userData
        .map { it.profile?.displayName() ?: "Invité" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "Invité")

    val isPremium: StateFlow<Boolean> = userData
        .map { it.profile?.isPremium ?: false }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val streakDays: StateFlow<Int> = userData
        .map { it.stats?.streakDays ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)
}
```

### 3. ProfileScreen - Afficher Stats

```kotlin
// presentation/screens/profile/ProfileScreen.kt
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val userData by viewModel.userData.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Text(
            text = "Bonjour, ${userData.profile?.displayName() ?: "Invité"}",
            style = MaterialTheme.typography.headlineMedium
        )

        if (userData.profile?.isPremium == true) {
            Badge { Text("Premium") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats
        userData.stats?.let { stats ->
            StatsCard(
                title = "Temps total",
                value = stats.formatTotalTime(),
                icon = Icons.Default.Schedule
            )
            StatsCard(
                title = "Séances",
                value = stats.sessions.toString(),
                icon = Icons.Default.FitnessCenter
            )
            StatsCard(
                title = "Série",
                value = "${stats.streakDays} jours",
                icon = Icons.Default.Whatshot,
                highlight = stats.hasPracticedToday()
            )
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    highlight: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodySmall)
                Text(value, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
```

### 4. PlayerViewModel - Enregistrer Séance

```kotlin
// presentation/screens/player/PlayerViewModel.kt
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val recordSession: RecordSessionUseCase, // ← Injecter
    private val authRepository: AuthRepository
) : ViewModel() {

    // FIX(user-dynamic): Appelé quand vidéo/audio se termine
    fun onVideoCompleted(durationMinutes: Int) {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser.value ?: return@launch

            val result = recordSession(
                uid = currentUser.uid,
                durationMinutes = durationMinutes
            )

            if (result.isSuccess) {
                Timber.i("Session recorded: $durationMinutes min")
                showSnackbar("Séance enregistrée! 🎉")
            } else {
                Timber.e("Failed to record session")
                // Optionnel: retry ou stocker localement
            }
        }
    }
}
```

### 5. HomeViewModel - Weekly Stats Widget

```kotlin
// presentation/screens/home/HomeViewModel.kt
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserData: GetUserDataUseCase, // ← Injecter
    private val authRepository: AuthRepository
) : ViewModel() {

    // FIX(user-dynamic): Stats pour widget "Cette semaine"
    val weeklyStats: StateFlow<WeeklyStats?> = authRepository.currentUser
        .filterNotNull()
        .flatMapLatest { user -> getUserData(user.uid) }
        .map { userData ->
            userData.stats?.let {
                WeeklyStats(
                    totalMinutes = it.totalMinutes,
                    streakDays = it.streakDays,
                    hasPracticedToday = it.hasPracticedToday()
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    data class WeeklyStats(
        val totalMinutes: Int,
        val streakDays: Int,
        val hasPracticedToday: Boolean
    )
}
```

### 6. SettingsViewModel - Mettre à Jour Locale

```kotlin
// presentation/screens/settings/SettingsViewModel.kt
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: FirestoreUserProfileRepository, // ← Injecter
    private val authRepository: AuthRepository
) : ViewModel() {

    fun updateLocale(locale: String) {
        viewModelScope.launch {
            val uid = authRepository.currentUser.value?.uid ?: return@launch

            val result = userProfileRepository.updateLocale(uid, locale)

            if (result.isSuccess) {
                Timber.i("Locale updated to $locale")
                showSnackbar("Langue mise à jour")
            } else {
                Timber.e("Failed to update locale")
                showError("Erreur mise à jour langue")
            }
        }
    }

    fun upgradeToPremium() {
        viewModelScope.launch {
            val uid = authRepository.currentUser.value?.uid ?: return@launch

            // 1. Valider achat In-App Billing (non implémenté)
            // 2. Mettre à jour Firestore
            val result = userProfileRepository.updatePlanTier(uid, "premium")

            if (result.isSuccess) {
                Timber.i("User upgraded to premium")
                showSnackbar("Bienvenue en Premium! 🌟")
            }
        }
    }
}
```

---

## 🔧 Configuration Rapide

### 1. Module Hilt (déjà fait)

```kotlin
// di/FirestoreModule.kt
@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = Firebase.firestore
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // Offline cache
            .setCacheSizeBytes(10 * 1024 * 1024L) // 10MB
            .build()
        firestore.firestoreSettings = settings
        return firestore
    }

    @Provides @Singleton
    fun provideUserProfileRepo(firestore: FirebaseFirestore): FirestoreUserProfileRepository =
        FirestoreUserProfileRepositoryImpl(firestore)

    @Provides @Singleton
    fun provideUserStatsRepo(firestore: FirebaseFirestore): FirestoreUserStatsRepository =
        FirestoreUserStatsRepositoryImpl(firestore)
}
```

### 2. Gradle (déjà fait)

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-firestore") // ← Ajouté
}
```

---

## 🧪 Test Local avec Emulator

### 1. Démarrer Emulator

```bash
firebase emulators:start --only firestore
# Firestore Emulator running at http://localhost:8080
# Emulator UI at http://localhost:4000
```

### 2. Configurer App (Debug uniquement)

```kotlin
// di/FirestoreModule.kt
@Provides
@Singleton
fun provideFirebaseFirestore(): FirebaseFirestore {
    val firestore = Firebase.firestore

    if (BuildConfig.DEBUG) {
        firestore.useEmulator("10.0.2.2", 8080) // Android emulator
        Timber.d("Using Firestore Emulator")
    }

    // ... reste de la config
    return firestore
}
```

### 3. Voir Données en Temps Réel

Ouvrir http://localhost:4000/firestore et observer:
- Collection `users/{uid}`
- Collection `stats/{uid}`
- Mises à jour en temps réel

---

## 📝 Checklist Intégration

### AuthScreen
- [ ] Injecter `CreateUserProfileUseCase`
- [ ] Appeler `createUserProfile()` après Firebase login success
- [ ] Gérer Result (success → navigate, failure → error)

### ProfileScreen
- [ ] Injecter `GetUserDataUseCase`
- [ ] Observer `userData` Flow
- [ ] Afficher `profile.displayName()`, `stats.formatTotalTime()`, etc.

### PlayerScreen
- [ ] Injecter `RecordSessionUseCase`
- [ ] Appeler `recordSession()` après vidéo terminée
- [ ] Afficher snackbar confirmation

### HomeScreen
- [ ] Injecter `GetUserDataUseCase`
- [ ] Créer widget "Stats de la semaine"
- [ ] Afficher streak, total minutes

### SettingsScreen
- [ ] Injecter `FirestoreUserProfileRepository`
- [ ] Bouton changer langue → `updateLocale()`
- [ ] Bouton upgrade premium → `updatePlanTier()`

---

## 🚨 Gestion Erreurs

### Pattern Recommandé

```kotlin
// Dans ViewModel
suspend fun performFirestoreOperation() {
    val result = repository.someOperation()

    result.fold(
        onSuccess = {
            Timber.i("Operation successful")
            _uiState.value = UiState.Success
        },
        onFailure = { error ->
            Timber.e(error, "Operation failed")
            when (error) {
                is FirebaseFirestoreException -> {
                    when (error.code) {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                            // Token expiré → logout
                            logoutUser()
                        }
                        FirebaseFirestoreException.Code.UNAVAILABLE -> {
                            // Offline → afficher indicateur
                            _uiState.value = UiState.Offline
                        }
                        else -> {
                            _uiState.value = UiState.Error(error.message)
                        }
                    }
                }
                else -> {
                    _uiState.value = UiState.Error("Erreur inattendue")
                }
            }
        }
    )
}
```

---

## 🔄 Indicateur Offline

### NetworkMonitor

```kotlin
// utils/NetworkMonitor.kt
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val isOnline: Flow<Boolean> = callbackFlow {
        val cm = context.getSystemService<ConnectivityManager>()!!
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = trySend(true).let {}
            override fun onLost(network: Network) = trySend(false).let {}
        }
        cm.registerDefaultNetworkCallback(callback)
        trySend(cm.activeNetwork != null)
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
```

### UI Component

```kotlin
@Composable
fun OfflineIndicator(isOffline: Boolean) {
    AnimatedVisibility(
        visible = isOffline,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CloudOff, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mode hors ligne")
            }
        }
    }
}

// Dans MainActivity ou TopAppBar
@Composable
fun MainScreen(networkMonitor: NetworkMonitor) {
    val isOffline by networkMonitor.isOnline
        .map { !it }
        .collectAsState(initial = false)

    Scaffold(
        topBar = {
            Column {
                TopAppBar(...)
                OfflineIndicator(isOffline)
            }
        }
    ) { ... }
}
```

---

## 📊 Logs Utiles

### Timber Configuration

```kotlin
// OraApplication.kt
override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
        Timber.plant(Timber.DebugTree())
    } else {
        Timber.plant(CrashReportingTree())
    }
}

class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= Log.ERROR) {
            // Envoyer à Firebase Crashlytics
            // FirebaseCrashlytics.getInstance().recordException(t ?: Exception(message))
        }
    }
}
```

### Logs Firestore

Tous les repositories utilisent déjà Timber:
```kotlin
Timber.d("getUserProfile: Listening to profile $uid")
Timber.i("createUserProfile: Profile $uid créé avec succès")
Timber.e(e, "updateUserProfile: Erreur Firestore ${e.code}")
```

---

## 🎯 Commandes Firebase

### Déployer Rules

```bash
firebase deploy --only firestore:rules
```

### Déployer Indexes

```bash
firebase deploy --only firestore:indexes
```

### Voir Logs

```bash
firebase firestore:logs
```

### Emulator

```bash
firebase emulators:start --only firestore
```

---

## ✅ Checklist Finale

### Développement
- [x] Use cases créés et injectés
- [ ] AuthViewModel intégré
- [ ] ProfileScreen intégré
- [ ] PlayerViewModel intégré
- [ ] HomeScreen widget stats
- [ ] SettingsScreen locale/premium

### Testing
- [ ] Tests unitaires (UserStats calcul streak)
- [ ] Tests Firestore Emulator
- [ ] Tests offline/online sync
- [ ] Tests sécurité Rules

### Déploiement
- [ ] `firebase deploy --only firestore:rules`
- [ ] `firebase deploy --only firestore:indexes`
- [ ] Monitoring Firebase Console
- [ ] Alertes configurées (quota, erreurs)

---

**Quick start prêt - Intégration en 30 minutes!** ⚡
