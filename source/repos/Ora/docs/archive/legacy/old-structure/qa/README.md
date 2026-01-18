# QA Documentation - Ora Wellbeing

Documentation des tests et procédures QA pour l'application Ora Android.

---

## Structure des fichiers QA

```
qa/
├── README.md                      # Ce fichier
├── cases_user_data.md            # Test cases manuels (User Data)
└── test_summary_user_data.md     # Résumé des tests automatisés
```

---

## Tests automatisés

### Localisation des tests

```
app/src/test/java/com/ora/wellbeing/
├── domain/model/
│   ├── UserStatsTest.kt          # 35 tests unitaires
│   └── UserProfileTest.kt        # 28 tests unitaires
└── presentation/screens/profile/
    └── ProfileViewModelTest.kt   # 25 tests ViewModel

Total: 88 tests automatisés
```

### Lancer tous les tests

```bash
# Depuis la racine du projet
./gradlew test

# Avec logs détaillés
./gradlew test --info

# Mode watch (relance auto après modifications)
./gradlew test --continuous
```

### Lancer des tests spécifiques

```bash
# Tous les tests du domaine
./gradlew test --tests "com.ora.wellbeing.domain.*"

# UserStatsTest uniquement
./gradlew test --tests "com.ora.wellbeing.domain.model.UserStatsTest"

# Un test précis
./gradlew test --tests "UserStatsTest.incrementSession for new user sets streak to 1"
```

### Générer les rapports HTML

```bash
# Lancer tests et générer rapport
./gradlew test

# Rapport disponible ici:
# app/build/reports/tests/testDebugUnitTest/index.html

# Ouvrir dans le navigateur (Windows)
start app/build/reports/tests/testDebugUnitTest/index.html

# Linux/Mac
open app/build/reports/tests/testDebugUnitTest/index.html
```

---

## Tests manuels

### Référence

Voir `cases_user_data.md` pour les scénarios détaillés.

### Scénarios critiques (P0)

1. **TC-001**: Création automatique de profil pour nouveau utilisateur
2. **TC-002**: Affichage du profil existant après logout/login
3. **TC-003**: Incrément des stats après une séance
4. **TC-016**: Cold start < 2,5s

### Checklist rapide

```
[ ] Nouveau user → profil créé auto
[ ] Stats incrémentées après séance
[ ] Streak calculé correctement
[ ] Offline mode fonctionne
[ ] Cold start < 2,5s
[ ] Premium tier détecté
[ ] Locale fr/en fonctionne
```

---

## Outils de qualité

### Lint (détection de bugs)

```bash
# Lancer lint
./gradlew lint

# Rapport HTML
# app/build/reports/lint-results-debug.html
```

### Detekt (style Kotlin)

```bash
# Installer detekt (si pas déjà fait)
# Ajouter dans build.gradle.kts:
# plugins {
#     id("io.gitlab.arturbosch.detekt") version "1.23.4"
# }

# Lancer detekt
./gradlew detekt

# Rapport HTML
# app/build/reports/detekt/detekt.html
```

### Accessibilité (TalkBack)

```bash
# Activer TalkBack sur device/emulator:
# Settings > Accessibility > TalkBack > ON

# Tester navigation avec:
# - Swipe right/left: navigation entre éléments
# - Double-tap: activation
# - Vérifier labels descriptifs sur tous les boutons/images
```

### Performance

```bash
# Mesurer cold start time
adb shell am start -W com.ora.wellbeing/.MainActivity

# Sortie attendue:
# TotalTime: < 2500ms (2,5s)

# Profiler avec Android Studio:
# Run > Profile 'app' > CPU/Memory/Network
```

---

## CI/CD Integration

### GitHub Actions (exemple)

```yaml
name: Android CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'

      - name: Run Unit Tests
        run: ./gradlew test

      - name: Upload Test Reports
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-reports
          path: app/build/reports/tests/

      - name: Run Lint
        run: ./gradlew lint
```

### Bitrise / CircleCI

```bash
# step: test
./gradlew test lint

# step: upload reports
cp app/build/reports/tests/testDebugUnitTest/index.html $BITRISE_DEPLOY_DIR/unit.html
cp app/build/reports/lint-results-debug.html $BITRISE_DEPLOY_DIR/lint.html
```

---

## Debugging des tests

### Test échoue localement

```bash
# 1. Nettoyer le cache Gradle
./gradlew clean

# 2. Relancer avec stack trace
./gradlew test --stacktrace

# 3. Vérifier versions dépendances
./gradlew dependencies
```

### MockK issues

```kotlin
// Si "no answer found" error:
every { mock.method() } returns expectedValue // Ajouter stub

// Si "missing stub" warning:
@RelaxedMockK // Utiliser relaxed mock
lateinit var repository: UserRepository
```

### Coroutines tests

```kotlin
// Si test freeze/timeout:
@OptIn(ExperimentalCoroutinesApi::class)
@Before
fun setup() {
    Dispatchers.setMain(StandardTestDispatcher()) // ← Dispatcher de test
}

@After
fun tearDown() {
    Dispatchers.resetMain() // ← Ne pas oublier
}

// Avancer le dispatcher manuellement
testDispatcher.scheduler.advanceUntilIdle()
```

---

## Conventions de test

### Nommage

```kotlin
// Pattern: `methodName behavior expectedResult`
@Test
fun `incrementSession on same day keeps streak unchanged`() { ... }

// ✅ Bon: descriptif, lisible
// ❌ Éviter: testIncrementSession1(), test_increment_session()
```

### Structure AAA

```kotlin
@Test
fun `test example`() {
    // Arrange (Given)
    val stats = UserStats.createDefault("uid")

    // Act (When)
    val updated = stats.incrementSession(20, timestamp)

    // Assert (Then)
    assertEquals(1, updated.streakDays)
}
```

### Commentaires FIX

```kotlin
// FIX(user-dynamic): Description du test
@Test
fun `test name`() { ... }
```

---

## Métriques de qualité

### Objectifs

| Métrique | Cible | Actuel | Statut |
|----------|-------|--------|--------|
| Couverture tests | 90% | ~92% | ✅ |
| Cold start | < 2,5s | ⚠️ À mesurer | 🔄 |
| Violations a11y | 0 critique | ⚠️ À vérifier | 🔄 |
| Lint warnings | < 10 | ⚠️ À mesurer | 🔄 |
| Detekt issues | < 5 | ⚠️ À mesurer | 🔄 |

### Dashboard de santé

```bash
# Générer rapport complet
./gradlew test lint

# Créer répertoire reports/
mkdir -p reports

# Copier tous les rapports
cp app/build/reports/tests/testDebugUnitTest/index.html reports/unit.html
cp app/build/reports/lint-results-debug.html reports/lint.html

# Ouvrir dashboard
start reports/unit.html
```

---

## Contacts

- **QA Lead**: qa-android@ora.app
- **Agent QA**: Claude Agent SDK (qa-android)
- **Documentation**: `qa/` directory

---

## Ressources

### Documentation officielle

- [Android Testing](https://developer.android.com/training/testing)
- [JUnit 4](https://junit.org/junit4/)
- [MockK](https://mockk.io/)
- [Turbine (Flow testing)](https://github.com/cashapp/turbine)
- [Truth (assertions)](https://truth.dev/)

### Best practices

- [Test-Driven Development (TDD)](https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-basics)
- [FIRST principles](https://github.com/ghsukumar/SFDC_Best_Practices/wiki/F.I.R.S.T-Principles-of-Unit-Testing)
- [Testing Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html)

---

**Dernière mise à jour**: 2025-10-04
**Version**: 1.0
