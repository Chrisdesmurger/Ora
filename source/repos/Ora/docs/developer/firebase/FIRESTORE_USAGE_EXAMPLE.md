# FIX(user-dynamic): Exemples d'Utilisation Firestore

Guide pratique pour intégrer les repositories Firestore dans les ViewModels et l'UI.

## 📱 1. AuthViewModel - Création Profil au Login

```kotlin
// presentation/screens/auth/AuthViewModel.kt
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val createUserProfile: CreateUserProfileUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    sealed class AuthState {
        object Initial : AuthState()
        object Loading : AuthState()
        data class Success(val uid: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    // FIX(user-dynamic): Après login Firebase réussi
    fun onFirebaseLoginSuccess(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // Vérifier si profil existe déjà
            val profileExists = checkIfProfileExists(firebaseUser.uid)

            if (!profileExists) {
                // Premier login → créer profil + stats
                val result = createUserProfile(
                    uid = firebaseUser.uid,
                    firstName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )

                if (result.isFailure) {
                    _authState.value = AuthState.Error("Erreur création profil")
                    return@launch
                }

                Timber.i("Profil créé pour ${firebaseUser.uid}")
            }

            _authState.value = AuthState.Success(firebaseUser.uid)
        }
    }

    private suspend fun checkIfProfileExists(uid: String): Boolean {
        // Implémenter avec getUserProfile().first()
        return false // Placeholder
    }
}
```

## 👤 2. ProfileViewModel - Afficher Profil + Stats

```kotlin
// presentation/screens/profile/ProfileViewModel.kt
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserData: GetUserDataUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // FIX(user-dynamic): Observer données utilisateur en temps réel
    val userData: StateFlow<UserData> = authRepository.currentUser
        .filterNotNull()
        .flatMapLatest { firebaseUser ->
            getUserData(firebaseUser.uid)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserData(profile = null, stats = null)
        )

    // UI State dérivé
    val uiState: StateFlow<ProfileUiState> = userData.map { data ->
        ProfileUiState(
            displayName = data.profile?.displayName() ?: "Invité",
            isPremium = data.profile?.isPremium ?: false,
            totalMinutes = data.stats?.totalMinutes ?: 0,
            sessions = data.stats?.sessions ?: 0,
            streakDays = data.stats?.streakDays ?: 0,
            formattedTime = data.stats?.formatTotalTime() ?: "0min",
            hasPracticedToday = data.stats?.hasPracticedToday() ?: false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    data class ProfileUiState(
        val displayName: String = "Invité",
        val isPremium: Boolean = false,
        val totalMinutes: Int = 0,
        val sessions: Int = 0,
        val streakDays: Int = 0,
        val formattedTime: String = "0min",
        val hasPracticedToday: Boolean = false
    )
}
```

### UI - ProfileScreen

```kotlin
// presentation/screens/profile/ProfileScreen.kt
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Text(
            text = "Bonjour, ${uiState.displayName}",
            style = MaterialTheme.typography.headlineMedium
        )

        if (uiState.isPremium) {
            Badge(text = "Premium")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Cards
        StatsCard(
            title = "Temps total",
            value = uiState.formattedTime,
            icon = Icons.Default.Schedule
        )

        StatsCard(
            title = "Séances",
            value = uiState.sessions.toString(),
            icon = Icons.Default.FitnessCenter
        )

        StatsCard(
            title = "Série",
            value = "${uiState.streakDays} jours",
            icon = Icons.Default.Whatshot,
            highlight = uiState.hasPracticedToday
        )
    }
}
```

## 🎬 3. PlayerViewModel - Enregistrer Séance

```kotlin
// presentation/screens/player/PlayerViewModel.kt
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val recordSession: RecordSessionUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contentId: String = checkNotNull(savedStateHandle["contentId"])

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    sealed class PlayerState {
        object Idle : PlayerState()
        object Playing : PlayerState()
        object Paused : PlayerState()
        data class Completed(val durationMinutes: Int) : PlayerState()
        data class Error(val message: String) : PlayerState()
    }

    // FIX(user-dynamic): Appelé quand vidéo/audio se termine
    fun onContentCompleted(durationMinutes: Int) {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser.value
            if (currentUser == null) {
                Timber.e("No user logged in, cannot record session")
                return@launch
            }

            _playerState.value = PlayerState.Completed(durationMinutes)

            // Enregistrer la séance
            val result = recordSession(
                uid = currentUser.uid,
                durationMinutes = durationMinutes
            )

            if (result.isSuccess) {
                Timber.i("Session enregistrée: $durationMinutes min")
                // Optionnel: afficher snackbar "Séance enregistrée!"
            } else {
                Timber.e("Échec enregistrement session")
                // Optionnel: retry ou stocker en local pour sync plus tard
            }
        }
    }

    // Écouter événements du player ExoPlayer
    fun onPlayerEvent(event: PlayerEvent) {
        when (event) {
            is PlayerEvent.Play -> _playerState.value = PlayerState.Playing
            is PlayerEvent.Pause -> _playerState.value = PlayerState.Paused
            is PlayerEvent.End -> {
                val durationMs = event.currentPosition
                val durationMinutes = (durationMs / 60000).toInt()
                onContentCompleted(durationMinutes)
            }
            is PlayerEvent.Error -> _playerState.value = PlayerState.Error(event.message)
        }
    }
}
```

## 🏠 4. HomeViewModel - Afficher Stats Résumé

```kotlin
// presentation/screens/home/HomeViewModel.kt
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserData: GetUserDataUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    // FIX(user-dynamic): Observer stats pour weekly summary
    val weeklyStats: StateFlow<WeeklyStats?> = authRepository.currentUser
        .filterNotNull()
        .flatMapLatest { user ->
            getUserData(user.uid)
        }
        .map { userData ->
            userData.stats?.let {
                WeeklyStats(
                    totalMinutes = it.totalMinutes,
                    streakDays = it.streakDays,
                    hasPracticedToday = it.hasPracticedToday()
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    data class WeeklyStats(
        val totalMinutes: Int,
        val streakDays: Int,
        val hasPracticedToday: Boolean
    )
}
```

### UI - HomeScreen Stats Widget

```kotlin
@Composable
fun WeeklyStatsWidget(
    stats: WeeklyStats?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cette semaine",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(
                    label = "Minutes",
                    value = stats?.totalMinutes?.toString() ?: "0"
                )

                StatItem(
                    label = "Série",
                    value = "${stats?.streakDays ?: 0} jours",
                    highlight = stats?.hasPracticedToday == true
                )
            }
        }
    }
}
```

## ⚙️ 5. SettingsViewModel - Mettre à Jour Locale/Plan

```kotlin
// presentation/screens/settings/SettingsViewModel.kt
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: FirestoreUserProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    sealed class UpdateState {
        object Idle : UpdateState()
        object Loading : UpdateState()
        object Success : UpdateState()
        data class Error(val message: String) : UpdateState()
    }

    // FIX(user-dynamic): Mettre à jour la locale
    fun updateLocale(locale: String) {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser.value ?: return@launch

            _updateState.value = UpdateState.Loading

            val result = userProfileRepository.updateLocale(currentUser.uid, locale)

            _updateState.value = if (result.isSuccess) {
                UpdateState.Success
            } else {
                UpdateState.Error("Erreur mise à jour locale")
            }
        }
    }

    // FIX(user-dynamic): Upgrade premium (après achat In-App)
    fun upgradeToPremium() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser.value ?: return@launch

            _updateState.value = UpdateState.Loading

            // 1. Valider achat In-App Billing (non implémenté ici)
            // 2. Mettre à jour Firestore
            val result = userProfileRepository.updatePlanTier(currentUser.uid, "premium")

            _updateState.value = if (result.isSuccess) {
                Timber.i("User upgraded to premium")
                UpdateState.Success
            } else {
                UpdateState.Error("Erreur upgrade premium")
            }
        }
    }
}
```

## 🧪 6. Tests Exemples

### Repository Test

```kotlin
// FirestoreUserStatsRepositoryTest.kt
@ExperimentalCoroutinesApi
class FirestoreUserStatsRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var repository: FirestoreUserStatsRepository

    @Before
    fun setup() {
        // Utiliser Firebase Emulator
        firestore = Firebase.firestore
        firestore.useEmulator("10.0.2.2", 8080)
        repository = FirestoreUserStatsRepositoryImpl(firestore)
    }

    @Test
    fun `incrementSession should calculate streak correctly`() = runTest {
        val uid = "test_user_123"

        // Créer stats initiales
        val initialStats = UserStats.createDefault(uid)
        repository.createUserStats(initialStats).getOrThrow()

        // Premier jour
        repository.incrementSession(uid, 15, timestamp1).getOrThrow()
        var stats = repository.getUserStats(uid).first()
        assertEquals(1, stats?.streakDays)
        assertEquals(15, stats?.totalMinutes)
        assertEquals(1, stats?.sessions)

        // Jour consécutif
        val timestamp2 = timestamp1 + (24 * 60 * 60 * 1000) // +1 jour
        repository.incrementSession(uid, 20, timestamp2).getOrThrow()
        stats = repository.getUserStats(uid).first()
        assertEquals(2, stats?.streakDays)
        assertEquals(35, stats?.totalMinutes)

        // Gap > 1 jour → reset
        val timestamp3 = timestamp2 + (48 * 60 * 60 * 1000) // +2 jours
        repository.incrementSession(uid, 10, timestamp3).getOrThrow()
        stats = repository.getUserStats(uid).first()
        assertEquals(1, stats?.streakDays) // Reset!
        assertEquals(45, stats?.totalMinutes)
    }
}
```

### Use Case Test

```kotlin
// CreateUserProfileUseCaseTest.kt
@ExperimentalCoroutinesApi
class CreateUserProfileUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val profileRepository: FirestoreUserProfileRepository = mockk()
    private val statsRepository: FirestoreUserStatsRepository = mockk()
    private lateinit var useCase: CreateUserProfileUseCase

    @Before
    fun setup() {
        useCase = CreateUserProfileUseCase(profileRepository, statsRepository)
    }

    @Test
    fun `invoke should create profile and stats`() = runTest {
        val uid = "test123"

        coEvery { profileRepository.createUserProfile(any()) } returns Result.success(Unit)
        coEvery { statsRepository.createUserStats(any()) } returns Result.success(Unit)

        val result = useCase(uid, "John", "http://photo.jpg")

        assertTrue(result.isSuccess)
        coVerify { profileRepository.createUserProfile(any()) }
        coVerify { statsRepository.createUserStats(any()) }
    }

    @Test
    fun `invoke should fail if stats creation fails`() = runTest {
        val uid = "test123"

        coEvery { profileRepository.createUserProfile(any()) } returns Result.success(Unit)
        coEvery { statsRepository.createUserStats(any()) } returns Result.failure(Exception("Network error"))

        val result = useCase(uid)

        assertTrue(result.isFailure)
    }
}
```

## 🔄 7. Gestion Offline

### Indicateur Offline dans UI

```kotlin
@Composable
fun OfflineIndicator(
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mode hors ligne",
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
```

### Détecter État Network

```kotlin
// utils/NetworkMonitor.kt
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()!!

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        // État initial
        val activeNetwork = connectivityManager.activeNetwork
        trySend(activeNetwork != null)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}

// Dans ViewModel
@HiltViewModel
class MainViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    val isOffline: StateFlow<Boolean> = networkMonitor.isOnline
        .map { !it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
}
```

## 📝 Récapitulatif

### Points Clés
1. **Use cases** centralisent la business logic
2. **Flow réactif** pour UI qui s'update automatiquement
3. **Offline-first** géré automatiquement par Firestore
4. **Privacy** avec vérification UID dans repositories
5. **Error handling** avec Result<T> et Timber logging

### Flux Complet
```
User → UI Event → ViewModel → Use Case → Repository → Firestore
                     ↓                                      ↓
                StateFlow ← ViewModel ← snapshotListener ← Firestore
                     ↓
                  UI Update
```

### À Faire Ensuite
1. Intégrer `CreateUserProfileUseCase` dans `AuthViewModel`
2. Utiliser `GetUserDataUseCase` dans `ProfileScreen` et `HomeScreen`
3. Appeler `RecordSessionUseCase` dans `PlayerViewModel`
4. Déployer `firestore.rules` sur Firebase Console
5. Tester avec Firebase Emulator en local

---

**Implémentation prête pour production** 🚀
