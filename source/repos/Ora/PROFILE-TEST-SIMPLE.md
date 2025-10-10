# Test Simple - ProfileScreen

Ce document fournit un test minimal pour vérifier que ProfileScreen fonctionne correctement.

## Option 1: Test via l'application complète (Recommandé)

### Étapes:
1. Compiler et installer l'APK:
   ```bash
   ./gradlew :app:assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

2. Lancer l'application sur device/émulateur

3. Cliquer sur l'onglet **Profil** (icône personne en bas à droite)

4. **Vérifier que l'écran affiche:**
   - Logo "☀️ ORA" en haut à gauche
   - Bouton Edit (crayon) en haut à droite
   - Photo de profil circulaire verte avec initiale "C"
   - Nom "Clara" en grand
   - Motto "Je prends soin de moi chaque jour"
   - Section "MON TEMPS PAR PRATIQUE" avec 4 cartes:
     - Yoga (orange) - 3h45 ce mois-ci
     - Pilates (orange clair) - 2h15 ce mois-ci
     - Méditation (vert) - 4h30 ce mois-ci
     - Respiration (vert clair) - 1h20 ce mois-ci
   - Carte GRATITUDES (gauche) avec "✓ Complété"
   - Carte OBJECTIFS (droite) avec 3 objectifs cochés/non cochés
   - Stats en bas:
     - "5 jours d'affilée"
     - "24h10 en tout"
     - "Dernière activité: Yoga doux - 25 min"

5. **Tester les interactions:**
   - Cliquer sur une carte de pratique → devrait naviguer vers PracticeStatsScreen
   - Retour → devrait revenir au Profile
   - Cocher/décocher un objectif → devrait se mettre à jour visuellement

---

## Option 2: Test via Preview Compose (Dev only)

Si vous développez dans Android Studio:

1. Ouvrir `ProfileScreen.kt`

2. Chercher la fonction `@Preview`:
   ```kotlin
   @Preview(showBackground = true)
   @Composable
   fun ProfileScreenPreview()
   ```

3. Dans Android Studio:
   - Onglet "Split" ou "Design" à droite
   - Le preview devrait montrer l'écran Profile complet

4. **Avantages:**
   - Pas besoin de build/install
   - Modifications en temps réel
   - Isolation des composants

---

## Option 3: Test unitaire du ViewModel

Créer un test pour vérifier la logique métier:

```kotlin
// app/src/test/java/com/ora/wellbeing/presentation/screens/profile/ProfileViewModelTest.kt

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProfileViewModel()
    }

    @Test
    fun `initial state is loading`() = runTest {
        val initialState = viewModel.uiState.value
        assertThat(initialState.isLoading).isFalse()
        assertThat(initialState.userProfile).isNull()
    }

    @Test
    fun `load profile data shows mock data`() = runTest {
        viewModel.uiState.test {
            // Initial state
            assertThat(awaitItem().isLoading).isFalse()

            // Trigger load
            viewModel.onEvent(ProfileUiEvent.LoadProfileData)

            // Loading state
            assertThat(awaitItem().isLoading).isTrue()

            // Loaded state
            val loadedState = awaitItem()
            assertThat(loadedState.isLoading).isFalse()
            assertThat(loadedState.userProfile?.name).isEqualTo("Clara")
            assertThat(loadedState.practiceTimes).hasSize(4)
            assertThat(loadedState.goals).hasSize(3)
        }
    }

    @Test
    fun `toggle goal updates state`() = runTest {
        // Load data first
        viewModel.onEvent(ProfileUiEvent.LoadProfileData)

        viewModel.uiState.test {
            // Skip to loaded state
            skipItems(2)

            val initialGoals = awaitItem().goals
            val firstGoal = initialGoals.first()

            // Toggle first goal
            viewModel.onEvent(ProfileUiEvent.ToggleGoal(firstGoal.id))

            val updatedGoals = awaitItem().goals
            val updatedFirstGoal = updatedGoals.first()

            assertThat(updatedFirstGoal.isCompleted).isNotEqualTo(firstGoal.isCompleted)
        }
    }
}
```

**Exécuter le test:**
```bash
./gradlew test --tests ProfileViewModelTest
```

---

## Logs utiles pour debug

### Filtrer les logs Timber pour Profile:
```bash
adb logcat | grep "ProfileViewModel"
```

### Logs attendus:
```
D/ProfileViewModel: Profile data loaded successfully
D/ProfileViewModel: Goal toggled: goalId=goal_1
D/ProfileViewModel: Navigate to edit profile
D/ProfileViewModel: Navigate to practice stats: yoga
```

### En cas d'erreur:
```bash
# Tous les logs app
adb logcat | grep "com.ora.wellbeing"

# Crash logs
adb logcat | grep "AndroidRuntime"

# Hilt errors
adb logcat | grep "Hilt"
```

---

## Checklist rapide de dépannage

Si ProfileScreen ne s'affiche toujours pas:

- [ ] Build clean effectué (`./gradlew clean`)
- [ ] APK réinstallé (`adb install -r`)
- [ ] Cache Android Studio vidé (File → Invalidate Caches)
- [ ] Pas d'erreur de compilation visible
- [ ] Logcat montre bien "ProfileViewModel created" (si ajouté dans init)
- [ ] Les autres écrans fonctionnent bien (Home, Library, etc.)
- [ ] Hilt configuré correctement (@HiltAndroidApp, @AndroidEntryPoint)
- [ ] MainActivity.kt a les imports corrects (presentation.* pas ui.*)

---

## État attendu de ProfileUiState (mock data)

```kotlin
ProfileUiState(
    isLoading = false,
    error = null,
    userProfile = UserProfile(
        name = "Clara",
        motto = "Je prends soin de moi chaque jour",
        photoUrl = null
    ),
    practiceTimes = [
        PracticeTime("yoga", "Yoga", "3h45 ce mois-ci", Color(0xFFF4845F), Icons.Default.SelfImprovement),
        PracticeTime("pilates", "Pilates", "2h15 ce mois-ci", Color(0xFFFDB5A0), Icons.Default.FitnessCenter),
        PracticeTime("meditation", "Méditation", "4h30 ce mois-ci", Color(0xFF7BA089), Icons.Default.Spa),
        PracticeTime("breathing", "Respiration", "1h20 ce mois-ci", Color(0xFFB4D4C3), Icons.Default.Air)
    ],
    streak = 5,
    totalTime = "24h10",
    lastActivity = "Yoga doux - 25 min",
    hasGratitudeToday = true,
    goals = [
        Goal("goal_1", "Lire plus", true),
        Goal("goal_2", "Arrêter l'alcool", true),
        Goal("goal_3", "10 min de réseaux sociaux max", false)
    ]
)
```

---

**Si tout fonctionne correctement, vous devriez voir l'écran Profile s'afficher avec toutes les données mockées ci-dessus.**