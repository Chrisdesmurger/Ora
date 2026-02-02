# Configuration i18n - Ora Android App

**Date**: 2025-12-20
**Status**: ✅ ACTIF ET OBLIGATOIRE
**Langues supportées**: Français (FR), English (EN), Español (ES)

## 📋 Vue d'ensemble

L'application Ora est **100% internationalisée**. Toute modification de code DOIT suivre les règles i18n ci-dessous. **Aucun code avec hardcoded strings ne sera accepté**.

## 🌍 Langues Supportées

| Langue | Code | Fichier | Statut |
|--------|------|---------|--------|
| **Français** | `fr` | `res/values/strings.xml` | Default / Source |
| **English** | `en` | `res/values-en/strings.xml` | ✅ Complet |
| **Español** | `es` | `res/values-es/strings.xml` | ✅ Complet |

## 🚨 Règles CRITIQUES

### Règle #1: NO Hardcoded Strings
❌ **INTERDIT**:
```kotlin
Text("Échauffement")
error = "Erreur de chargement"
Button { Text("Sauvegarder") }
enum class Side(val label: String) { LEFT("Gauche") }
```

✅ **OBLIGATOIRE**:
```kotlin
Text(stringResource(R.string.yoga_chapter_warmup))
error = getString(R.string.error_loading)
Button { Text(stringResource(R.string.button_save)) }
enum class Side(@StringRes val labelRes: Int) { LEFT(R.string.side_left) }
```

### Règle #2: Toujours 3 Langues
Chaque nouvelle string DOIT être ajoutée dans:
- `res/values/strings.xml` (FR)
- `res/values-en/strings.xml` (EN)
- `res/values-es/strings.xml` (ES)

### Règle #3: Naming Convention
Format: `{feature}_{element}_{description}`

Exemples:
- `yoga_preparing`, `yoga_error_loading`, `yoga_chapter_warmup`
- `meditation_breathing_inhale`, `meditation_ambient_rain`
- `massage_zone_neck`, `massage_pressure_low`
- `home_welcome_message`, `profile_stats_title`

## 📝 Patterns de Code

### Pattern 1: Compose UI
```kotlin
import androidx.compose.ui.res.stringResource
import com.ora.wellbeing.R

@Composable
fun YogaPlayerScreen() {
    Text(
        text = stringResource(R.string.yoga_preparing),
        style = MaterialTheme.typography.bodyLarge
    )

    Button(onClick = onSave) {
        Text(stringResource(R.string.button_save))
    }

    Icon(
        imageVector = Icons.Default.ArrowBack,
        contentDescription = stringResource(R.string.player_back)
    )
}
```

### Pattern 2: ViewModel
```kotlin
import android.app.Application
import androidx.lifecycle.AndroidViewModel

class YogaPlayerViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    fun loadPractice(id: String) {
        viewModelScope.launch {
            try {
                // Load practice
            } catch (e: Exception) {
                val context = getApplication<Application>()
                _uiState.update {
                    it.copy(
                        error = e.message ?: context.getString(R.string.yoga_error_loading)
                    )
                }
            }
        }
    }

    private fun generateChapters(): List<Chapter> {
        val context = getApplication<Application>()
        return listOf(
            Chapter(context.getString(R.string.yoga_chapter_warmup)),
            Chapter(context.getString(R.string.yoga_chapter_sun_salutation)),
            Chapter(context.getString(R.string.yoga_chapter_relaxation))
        )
    }
}
```

### Pattern 3: Enum avec @StringRes
```kotlin
import androidx.annotation.StringRes
import com.ora.wellbeing.R

enum class YogaSide(@StringRes val nameRes: Int) {
    NONE(R.string.yoga_side_none),
    LEFT(R.string.yoga_side_left),
    RIGHT(R.string.yoga_side_right)
}

// Utilisation dans UI
@Composable
fun SideIndicator(side: YogaSide) {
    Text(text = stringResource(side.nameRes))
}
```

### Pattern 4: Data Class avec @StringRes
```kotlin
import androidx.annotation.StringRes

data class BodyZone(
    val id: String,
    @StringRes val nameRes: Int,
    val instructionRes: List<Int>  // List<@StringRes Int>
)

// Création
val zones = listOf(
    BodyZone(
        id = "neck",
        nameRes = R.string.massage_zone_neck,
        instructionRes = listOf(
            R.string.massage_instruction_neck_1,
            R.string.massage_instruction_neck_2
        )
    )
)

// Utilisation dans UI
@Composable
fun ZoneCard(zone: BodyZone) {
    Text(text = stringResource(zone.nameRes))

    zone.instructionRes.forEach { instructionRes ->
        Text(text = stringResource(instructionRes))
    }
}
```

## 🔧 Outils et Hooks

### Hook pre-commit (Automatique)
Le hook `.claude/hooks/pre-commit-i18n-check.sh` s'exécute automatiquement avant chaque commit et:

1. ✅ Scanne tous les fichiers `.kt` pour détecter hardcoded strings
2. ✅ Vérifie que toutes les clés existent dans FR/EN/ES
3. ✅ Compte les traductions manquantes
4. ✅ Vérifie les imports `stringResource()` et `@StringRes`
5. ❌ **BLOQUE le commit** si violations détectées

### Agent i18n-l10n-android
L'agent automatique `.claude/agents/18_i18n-l10n_android.md` peut:

1. Détecter toutes les strings hardcodées
2. Extraire les strings vers `res/values/strings.xml`
3. Générer les traductions EN/ES
4. Modifier le code source (ajouter `stringResource()`, `@StringRes`, etc.)
5. Compiler et valider

**Utilisation**:
```bash
# Via manager-workflow (recommandé)
claude-code --agent manager-workflow --task "Fix i18n violations"

# Directement
claude-code --agent i18n-l10n-android --scan app/src/main/java/
```

### Agent tech-android (Développement)
L'agent tech-android appelle **automatiquement** i18n-l10n-android après chaque modification UI.

Workflow:
1. Développer la feature
2. 🌍 **AUTO**: Validation i18n
3. 🔨 Build et tests
4. 📄 Rapport

## 📊 Statistiques Actuelles

**État au 2025-12-20**:

| Fichier | Nombre de clés | Statut |
|---------|----------------|--------|
| `res/values/strings.xml` (FR) | ~520+ | ✅ Source |
| `res/values-en/strings.xml` (EN) | ~520+ | ✅ Complet |
| `res/values-es/strings.xml` (ES) | ~520+ | ✅ Complet |

**Dernières corrections**:
- ✅ YogaPlayerViewModel.kt - 6 strings corrigées (chapters + error)
- ✅ MassagePlayerViewModel.kt - 15+ instructions corrigées
- ✅ MeditationPlayerScreen.kt - 12+ strings corrigées

## 🚫 Cas Spéciaux (NE PAS traduire)

### Strings Techniques
Ces strings n'ont PAS besoin d'être traduites:

```kotlin
// Logs Timber - OK
Timber.d("Loading practice $id")
Timber.e(error, "Error loading practice for yoga player")

// Analytics events - OK
analytics.logEvent("yoga_player_opened") {
    param("practice_id", practice.id)
}

// Firebase paths - OK
firestore.collection("lessons")
firestore.document("programs/$id")

// IDs techniques - OK
const val PREF_KEY_THEME = "theme_preference"
const val ACTION_PLAY = "com.ora.wellbeing.ACTION_PLAY"
```

### Contenu Dynamique (Backend)
Ces contenus sont gérés côté backend/Firestore:

- Titres de leçons (`lesson.title`)
- Descriptions de programmes (`program.description`)
- Noms d'utilisateurs (`user.displayName`)
- Catégories de contenu (gérées dans Firestore)

## ✅ Checklist Développeur

Avant de commit du code UI, vérifier:

```
[ ] Aucun Text("...") hardcodé
[ ] Aucun error = "..." dans les ViewModels
[ ] Tous les Enum utilisent @StringRes val nameRes: Int
[ ] Strings ajoutées dans res/values/strings.xml (FR)
[ ] Traductions EN ajoutées dans res/values-en/strings.xml
[ ] Traductions ES ajoutées dans res/values-es/strings.xml
[ ] import androidx.compose.ui.res.stringResource présent
[ ] import androidx.annotation.StringRes présent (si @StringRes utilisé)
[ ] ./gradlew.bat assembleDebug compile sans erreur
[ ] Hook pre-commit-i18n-check.sh passe (automatique)
```

## 📚 Documentation Associée

- **CLAUDE.md** - Section "Internationalization (i18n)" (lignes 300-450)
- **`.claude/agents/18_i18n-l10n_android.md`** - Agent i18n complet
- **`.claude/agents/10_tech-android.md`** - Standards Android + i18n
- **`.claude/hooks/pre-commit-i18n-check.sh`** - Validation automatique
- **`.claude/hooks/pre-commit.sh`** - Hook principal (appelle i18n check)

## 🎯 Exemples de Traductions

### Yoga Player
```xml
<!-- FR -->
<string name="yoga_preparing">Préparation de votre séance…</string>
<string name="yoga_error_loading">Erreur de chargement</string>
<string name="yoga_chapter_warmup">Échauffement</string>
<string name="yoga_chapter_sun_salutation">Salutation au soleil</string>

<!-- EN -->
<string name="yoga_preparing">Preparing your session…</string>
<string name="yoga_error_loading">Loading error</string>
<string name="yoga_chapter_warmup">Warm-up</string>
<string name="yoga_chapter_sun_salutation">Sun Salutation</string>

<!-- ES -->
<string name="yoga_preparing">Preparando tu sesión…</string>
<string name="yoga_error_loading">Error de carga</string>
<string name="yoga_chapter_warmup">Calentamiento</string>
<string name="yoga_chapter_sun_salutation">Saludo al Sol</string>
```

### Meditation Player
```xml
<!-- FR -->
<string name="meditation_breathing_inhale">Inspirez</string>
<string name="meditation_breathing_exhale">Expirez</string>
<string name="meditation_ambient_rain">Pluie</string>

<!-- EN -->
<string name="meditation_breathing_inhale">Inhale</string>
<string name="meditation_breathing_exhale">Exhale</string>
<string name="meditation_ambient_rain">Rain</string>

<!-- ES -->
<string name="meditation_breathing_inhale">Inhala</string>
<string name="meditation_breathing_exhale">Exhala</string>
<string name="meditation_ambient_rain">Lluvia</string>
```

## 💡 Bonnes Pratiques

### Ton et Style par Langue

**Français**:
- Ton chaleureux et bienveillant
- Tutoiement pour l'utilisateur ("Prépare ta séance")
- Vouvoiement pour contenu formel ("Votre progression")
- Vocabulaire wellbeing: "gratitude", "sérénité", "bien-être"

**English**:
- Professional yet friendly
- Use "you" consistently
- Wellness vocabulary: "gratitude", "serenity", "wellbeing"
- Keep it concise and clear

**Español**:
- Tono cálido y profesional
- Usar "tú" para el usuario ("Prepara tu sesión")
- Usar "usted" si contexto muy formal
- Vocabulario bienestar: "gratitud", "serenidad", "bienestar"

### Plurals (si nécessaire)
```xml
<!-- FR -->
<plurals name="journal_entries_count">
    <item quantity="one">%d entrée</item>
    <item quantity="other">%d entrées</item>
</plurals>

<!-- EN -->
<plurals name="journal_entries_count">
    <item quantity="one">%d entry</item>
    <item quantity="other">%d entries</item>
</plurals>

<!-- ES -->
<plurals name="journal_entries_count">
    <item quantity="one">%d entrada</item>
    <item quantity="other">%d entradas</item>
</plurals>
```

## 🔒 Politique de Merge

**RÈGLE ABSOLUE**: Aucun PR ne sera mergé s'il contient:
- ❌ Hardcoded strings dans Text()
- ❌ Messages d'erreur hardcodés
- ❌ Enums avec String au lieu de @StringRes
- ❌ Traductions manquantes (EN ou ES)
- ❌ Échec du hook pre-commit-i18n-check

Le hook bloquera automatiquement le commit. Si contourné, la review PR rejettera le code.

---

**Maintenu par**: Agent i18n-l10n-android
**Dernière mise à jour**: 2025-12-20
**Version**: 1.0.0
