# Quick Start - Tests QA

Guide rapide pour lancer les tests du système de données utilisateur.

---

## Installation rapide

### 1. Sync Gradle

```bash
# Depuis Android Studio: File > Sync Project with Gradle Files
# OU en ligne de commande:
./gradlew --refresh-dependencies
```

**Nouvelles dépendances ajoutées**:
- MockK 1.13.8
- Turbine 1.0.0
- Truth 1.1.4
- Detekt 1.23.4

---

## Lancer les tests

### Tests unitaires (88 tests)

```bash
# Tous les tests
./gradlew test

# Tests en mode watch (relance auto)
./gradlew test --continuous

# Avec logs détaillés
./gradlew test --info --stacktrace
```

**Durée estimée**: ~5 secondes

**Sortie attendue**:
```
> Task :app:testDebugUnitTest
UserStatsTest > createDefault creates stats with zero values PASSED
UserStatsTest > incrementSession for new user sets streak to 1 PASSED
...
BUILD SUCCESSFUL in 5s
88 tests, 88 passed
```

---

### Tests par module

```bash
# Seulement UserStats (35 tests)
./gradlew test --tests "com.ora.wellbeing.domain.model.UserStatsTest"

# Seulement UserProfile (28 tests)
./gradlew test --tests "com.ora.wellbeing.domain.model.UserProfileTest"

# Seulement ProfileViewModel (25 tests)
./gradlew test --tests "com.ora.wellbeing.presentation.screens.profile.ProfileViewModelTest"
```

---

### Qualité globale (test + lint + detekt)

```bash
# Commande complète (peut prendre 30s-1min)
./gradlew qualityCheck

# OU manuellement:
./gradlew test lint detekt
```

**Rapports générés**:
- `app/build/reports/tests/testDebugUnitTest/index.html`
- `app/build/reports/lint-results-debug.html`
- `app/build/reports/detekt/detekt.html`

---

## Vérifier les rapports

### Windows

```cmd
start app\build\reports\tests\testDebugUnitTest\index.html
```

### Linux/Mac

```bash
open app/build/reports/tests/testDebugUnitTest/index.html
# OU
xdg-open app/build/reports/tests/testDebugUnitTest/index.html
```

### Android Studio

1. Ouvrir l'onglet **Build** (en bas)
2. Cliquer sur le lien **See full report**
3. OU: Naviguer manuellement vers `app/build/reports/tests/`

---

## Debugging

### Test échoue

```bash
# 1. Nettoyer
./gradlew clean

# 2. Relancer avec stack trace complète
./gradlew test --tests "TestClassName.test name" --stacktrace --info
```

### Problèmes de dépendances

```bash
# Voir l'arbre de dépendances
./gradlew app:dependencies

# Forcer refresh
./gradlew clean build --refresh-dependencies
```

### MockK "no answer found"

```kotlin
// Dans le test, ajouter stub manquant:
every { mockObject.method() } returns expectedValue

// OU utiliser relaxed mock:
@RelaxedMockK
lateinit var mockObject: SomeInterface
```

---

## Tests manuels (checklist rapide)

### Scénario critique: Nouveau utilisateur

1. **Désinstaller l'app** (supprimer données)
   ```bash
   adb uninstall com.ora.wellbeing
   ```

2. **Installer et lancer**
   ```bash
   ./gradlew installDebug
   adb shell am start -n com.ora.wellbeing/.MainActivity
   ```

3. **S'inscrire** avec un nouveau compte

4. **Vérifier**:
   - ✅ Profil créé automatiquement
   - ✅ Nom affiché "Invité" ou nom Google
   - ✅ Streak = 0
   - ✅ Temps total = "0min"

### Scénario critique: Incrément stats

1. **Compléter une séance** de 20 minutes

2. **Aller dans Profile**

3. **Vérifier**:
   - ✅ Temps total augmenté (+20 min)
   - ✅ Sessions +1
   - ✅ Streak = 1 (premier jour)

---

## Performance

### Mesurer cold start

```bash
# Fermer l'app complètement
adb shell am force-stop com.ora.wellbeing

# Mesurer démarrage
adb shell am start -W com.ora.wellbeing/.MainActivity

# Sortie attendue:
# TotalTime: < 2500 (2,5s max)
```

**Exemple sortie**:
```
Starting: Intent { cmp=com.ora.wellbeing/.MainActivity }
Status: ok
LaunchState: COLD
Activity: com.ora.wellbeing/.MainActivity
TotalTime: 1847
WaitTime: 1856
```

---

## CI/CD

### GitHub Actions

Ajouter dans `.github/workflows/android.yml`:

```yaml
name: Android CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run tests
        run: ./gradlew test

      - name: Upload test reports
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-reports
          path: app/build/reports/tests/
```

### Pre-commit hook

Créer `.git/hooks/pre-commit`:

```bash
#!/bin/sh
echo "Running quality checks..."
./gradlew test lint detekt

if [ $? -ne 0 ]; then
  echo "Tests failed. Commit aborted."
  exit 1
fi

echo "All checks passed!"
```

Rendre exécutable:
```bash
chmod +x .git/hooks/pre-commit
```

---

## Métriques de succès

### Tests doivent passer

- ✅ **88/88 tests** passent
- ✅ **Durée** < 10s
- ✅ **Couverture** > 90%

### Qualité code

- ✅ **Lint warnings** < 10
- ✅ **Detekt issues** < 5
- ✅ **Cold start** < 2,5s

---

## Ressources

### Documentation complète

- `qa/README.md` - Guide complet
- `qa/cases_user_data.md` - Test cases manuels
- `qa/test_summary_user_data.md` - Résumé des tests

### Tests

- `app/src/test/java/com/ora/wellbeing/domain/model/UserStatsTest.kt`
- `app/src/test/java/com/ora/wellbeing/domain/model/UserProfileTest.kt`
- `app/src/test/java/com/ora/wellbeing/presentation/screens/profile/ProfileViewModelTest.kt`

### Configuration

- `config/detekt.yml` - Règles Detekt
- `app/build.gradle.kts` - Configuration Gradle

---

## Aide

### Commandes utiles

```bash
# Lister toutes les tasks disponibles
./gradlew tasks

# Voir les tests disponibles
./gradlew test --dry-run

# Nettoyer complètement le projet
./gradlew clean

# Rebuild complet
./gradlew clean build
```

### Raccourcis Android Studio

- **Run tests**: `Ctrl+Shift+F10` (sur le test)
- **Run avec couverture**: `Ctrl+Shift+F9`
- **Voir rapports**: `Build` > `Analyze Test Results`

---

**Contact QA**: qa-android@ora.app

**Dernière mise à jour**: 2025-10-04
