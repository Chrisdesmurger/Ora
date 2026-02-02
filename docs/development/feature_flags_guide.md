# Feature Flags Guide - Ora Android

**Version:** 1.0.0
**Date:** 2025-10-04
**Status:** Production Ready

---

## Vue d'ensemble

Le système de feature flags permet de **contrôler dynamiquement** les fonctionnalités de l'app sans rebuild ni déploiement. Approche **local-first** avec préparation **Remote Config** (Firebase).

### Objectifs
- Déploiement prudent de nouvelles features
- A/B testing et rollout progressif
- Kill switch pour features problématiques
- Personnalisation selon profil utilisateur
- Debug flags pour développement

---

## Architecture

```
config/
  └── flags.json              ← Configuration locale (source de vérité)

app/src/main/java/com/ora/wellbeing/
  ├── data/config/
  │   └── FeatureFlagManager.kt   ← Manager principal
  └── di/
      └── ConfigModule.kt           ← Injection Hilt
```

### Flux de données

```
flags.json → Assets (build) → FeatureFlagManager → Cache → isEnabled()
                                      ↓
                            (Futur: Remote Config override)
```

---

## Flags disponibles

### Données utilisateur (user-dynamic)

| Flag | Default | Risk | Description |
|------|---------|------|-------------|
| `SYNC_STATS_FROM_ROOM` | `true` | Medium | Sync stats Room → Firestore |
| `DYNAMIC_LABELS` | `true` | Low | Labels personnalisés selon profil |
| `OFFLINE_MODE_ENABLED` | `true` | Medium | Cache offline Firestore |
| `AUTO_CREATE_PROFILE` | `true` | Low | Auto-création profil au login |

### Features expérimentales

| Flag | Default | Risk | Description |
|------|---------|------|-------------|
| `ADVANCED_VIDEO_PLAYER` | `false` | High | Lecteur ExoPlayer avancé (PiP, qualité) |
| `NETWORK_SYNC` | `false` | High | Sync auto arrière-plan (WorkManager) |

### Configuration & Debug

| Flag | Default | Risk | Description |
|------|---------|------|-------------|
| `REMOTE_CONFIG_ENABLED` | `false` | Low | Firebase Remote Config |
| `DEBUG_LOGGING` | `true` | Low | Logs Timber verbeux |

### Gamification

| Flag | Default | Risk | Description |
|------|---------|------|-------------|
| `BADGE_SYSTEM` | `true` | Low | Badges et récompenses |
| `GRATITUDE_REMINDERS` | `true` | Low | Rappels quotidiens journal |

---

## Utilisation

### 1. Injection dans un ViewModel

```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val featureFlagManager: FeatureFlagManager,
    private val userRepository: UserRepository
) : ViewModel() {

    fun loadUserProfile() {
        viewModelScope.launch {
            // FIX(user-dynamic): Vérifier si sync Firestore activée
            if (featureFlagManager.isEnabled(FeatureFlag.SYNC_STATS_FROM_ROOM)) {
                syncStatsToFirestore()
            }

            // FIX(user-dynamic): Labels dynamiques selon profil
            val useCustomLabels = featureFlagManager.isEnabled(FeatureFlag.DYNAMIC_LABELS)
            _uiState.update {
                it.copy(useDynamicLabels = useCustomLabels)
            }
        }
    }
}
```

### 2. Vérification dans un Repository

```kotlin
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val roomDao: UserStatsDao,
    private val featureFlagManager: FeatureFlagManager
) : UserRepository {

    override suspend fun saveUserStats(stats: UserStats) {
        // Toujours sauvegarder en local (Room)
        roomDao.insertStats(stats)

        // FIX(user-dynamic): Sync conditionnelle Firestore
        if (featureFlagManager.isEnabled(FeatureFlag.SYNC_STATS_FROM_ROOM)) {
            try {
                firestore.collection("userStats")
                    .document(stats.userId)
                    .set(stats.toFirestoreMap())
            } catch (e: Exception) {
                Timber.e(e, "Firestore sync failed, data safe in Room")
            }
        }
    }
}
```

### 3. UI conditionnelle (Composable)

```kotlin
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    featureFlagManager: FeatureFlagManager = hiltViewModel<ConfigViewModel>().flagManager
) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        // Contenu standard...

        // FIX(user-dynamic): Badges conditionnels
        if (featureFlagManager.isEnabled(FeatureFlag.BADGE_SYSTEM)) {
            BadgeSection(badges = uiState.badges)
        }

        // FIX(user-dynamic): Labels personnalisés
        val levelLabel = if (featureFlagManager.isEnabled(FeatureFlag.DYNAMIC_LABELS)) {
            uiState.userProfile.experienceLevel.customLabel()
        } else {
            "Utilisateur"
        }

        Text("Niveau: $levelLabel")
    }
}
```

### 4. Debug override (debug builds uniquement)

```kotlin
class DebugSettingsScreen {
    fun toggleFeature(flag: FeatureFlag, enabled: Boolean) {
        // FIX(user-dynamic): Override runtime en debug
        featureFlagManager.overrideFlag(flag, enabled)

        // Refresh UI...
    }
}
```

---

## Modification des flags

### Option 1: Local (développement)

1. Éditer `config/flags.json`
2. Modifier `enabled: true/false`
3. Rebuild l'app (pour copie dans assets)

```json
{
  "flags": {
    "SYNC_STATS_FROM_ROOM": {
      "enabled": false,  // ← Désactivé
      "description": "..."
    }
  }
}
```

### Option 2: Remote Config (production - futur)

```kotlin
// À implémenter avec Firebase Remote Config
class RemoteConfigManager {
    suspend fun fetchAndActivate() {
        remoteConfig.fetchAndActivate().await()
        featureFlagManager.reload()  // Recharge avec nouvelles valeurs
    }
}
```

---

## Best practices

### 1. Naming convention
- `UPPER_SNAKE_CASE` pour les noms
- Préfixe selon domaine: `SYNC_*`, `UI_*`, `DEBUG_*`
- Descriptif et non-ambigü

### 2. Default values sécurisés
- Features **risquées** (HIGH): `defaultValue = false`
- Features **stables** (LOW): `defaultValue = true`
- Toujours un fallback safe

### 3. Risk levels
- **LOW**: UI, labels, badges → safe à activer
- **MEDIUM**: Sync réseau, cache → impact performance
- **HIGH**: Lecteur avancé, features complexes → bugs potentiels

### 4. Traçabilité
- Chaque flag a: `added_date`, `comment`, `category`
- Historique des changements dans Git
- Documentation inline (`// FIX(user-dynamic): ...`)

### 5. Cleanup
- Supprimer flags après rollout complet (100%)
- Ne pas accumuler de dead flags
- Review trimestrielle de flags obsolètes

### 6. Testing
- Tester avec flag ON et OFF
- Tests unitaires avec flag override
- Instrumented tests avec configurations multiples

```kotlin
@Test
fun `user stats sync respects feature flag`() = runTest {
    // Arrange
    featureFlagManager.overrideFlag(FeatureFlag.SYNC_STATS_FROM_ROOM, false)

    // Act
    repository.saveUserStats(testStats)

    // Assert
    verify(firestore, never()).collection(any())  // Pas de sync
    verify(roomDao, times(1)).insertStats(testStats)  // Mais Room OK
}
```

---

## Migration vers Remote Config

### Phase 1: Local-first (actuel)
- Flags dans `flags.json`
- Chargement depuis assets
- Modification = rebuild

### Phase 2: Hybrid (futur proche)
- Remote Config comme override
- Fallback sur flags.json si fetch échoue
- Cache local des valeurs Remote

### Phase 3: Remote-first (futur)
- Remote Config source principale
- flags.json = defaults uniquement
- A/B testing, rollouts progressifs

```kotlin
// Exemple implémentation hybrid
class FeatureFlagManager {
    fun isEnabled(flag: FeatureFlag): Boolean {
        // 1. Vérifier Remote Config (si activé)
        if (isRemoteConfigEnabled()) {
            remoteConfig.getBoolean(flag.key)?.let { return it }
        }

        // 2. Fallback local
        return localFlags[flag.key]?.enabled ?: flag.defaultValue
    }
}
```

---

## Écran de debug (developer options)

Créer un écran Settings > Developer pour visualiser/modifier flags en debug:

```kotlin
@Composable
fun DeveloperFlagsScreen(flagManager: FeatureFlagManager) {
    LazyColumn {
        items(FeatureFlag.entries) { flag →
            val metadata = flagManager.getFlagMetadata(flag)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(flag.key, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        metadata?.description ?: "No description",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Badge(metadata?.riskLevel ?: "unknown")
                }

                Switch(
                    checked = flagManager.isEnabled(flag),
                    onCheckedChange = { enabled →
                        flagManager.overrideFlag(flag, enabled)
                    }
                )
            }
        }
    }
}
```

---

## Troubleshooting

### Problème: Flag change non pris en compte
**Cause:** Cache en mémoire
**Solution:**
```kotlin
featureFlagManager.reload()
```

### Problème: flags.json non trouvé
**Cause:** Fichier pas copié dans assets
**Solution:** Vérifier `build.gradle.kts`:
```kotlin
android {
    sourceSets {
        getByName("main") {
            assets.srcDirs("../config")
        }
    }
}
```

### Problème: Override ne fonctionne pas
**Cause:** Mode release
**Solution:** Override uniquement en debug builds
```kotlin
if (BuildConfig.DEBUG) {
    featureFlagManager.overrideFlag(...)
}
```

---

## Exemples d'utilisation réels

### Use case 1: Rollout progressif lecteur vidéo

**Semaine 1:** Beta testers uniquement
```json
{ "ADVANCED_VIDEO_PLAYER": { "enabled": false } }
```

**Semaine 2:** 10% utilisateurs (Remote Config)
```kotlin
remoteConfig.setConfigSettings {
    minimumFetchIntervalInSeconds = 3600
}
remoteConfig.setDefaults(mapOf(
    "ADVANCED_VIDEO_PLAYER" to false
))
```

**Semaine 4:** Rollout complet
```json
{ "ADVANCED_VIDEO_PLAYER": { "enabled": true } }
```

**Semaine 8:** Supprimer flag (feature standard)

### Use case 2: Kill switch urgence

**Incident:** Sync Firestore cause crash
```kotlin
// Remote Config immediate
remoteConfig.fetch(0).await()  // No cache
remoteConfig.activate()

// → SYNC_STATS_FROM_ROOM: false
// App continue de fonctionner (Room only)
```

### Use case 3: A/B testing badges

**Groupe A:** Badges activés
**Groupe B:** Pas de badges
**Métrique:** Engagement (sessions par semaine)

```kotlin
val userId = auth.currentUser.uid
val showBadges = if (userId.hashCode() % 2 == 0) {
    true  // Groupe A
} else {
    false  // Groupe B
}

featureFlagManager.overrideFlag(FeatureFlag.BADGE_SYSTEM, showBadges)
```

---

## Checklist ajout nouveau flag

- [ ] Ajouter dans `FeatureFlag` enum avec default sécurisé
- [ ] Ajouter dans `config/flags.json` avec métadonnées complètes
- [ ] Documenter dans ce guide (table + use case)
- [ ] Tester avec flag ON et OFF
- [ ] Code review: vérifier fallback safe
- [ ] Planifier date de suppression (si feature définitive)

---

## Ressources

- [Firebase Remote Config Docs](https://firebase.google.com/docs/remote-config/android)
- [Feature Flags Best Practices](https://martinfowler.com/articles/feature-toggles.html)
- [Ora Codebase: CLAUDE.md](../CLAUDE.md)

---

**Auteur:** Ora Dev Team
**Contact:** Pour questions, voir README principal
**Dernière mise à jour:** 2025-10-04
