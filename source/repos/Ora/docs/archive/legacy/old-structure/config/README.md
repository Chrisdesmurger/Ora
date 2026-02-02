# Feature Flags Configuration

Ce dossier contient la configuration des **feature flags** pour Ora Android.

## Fichiers

### `flags.json`
Configuration principale des feature flags. Ce fichier est copié dans les **assets** de l'app au build.

**Format:**
```json
{
  "version": "1.0.0",
  "flags": {
    "FLAG_NAME": {
      "enabled": true/false,
      "description": "Description du flag",
      "category": "data|ui|auth|...",
      "risk_level": "low|medium|high",
      "comment": "Notes additionnelles"
    }
  }
}
```

## Modification des flags

### Développement local
1. Éditer `config/flags.json`
2. Modifier `enabled: true/false`
3. Rebuild l'app (`./gradlew assembleDebug`)
4. Les changements sont copiés dans `app/build/intermediates/assets/`

### Production (futur)
- Utiliser **Firebase Remote Config** pour changements sans rebuild
- `flags.json` servira de fallback/defaults

## Flags disponibles

Voir [`docs/feature_flags_guide.md`](../docs/feature_flags_guide.md) pour la liste complète et exemples d'utilisation.

### Principaux flags (user-dynamic)
- `SYNC_STATS_FROM_ROOM`: Synchronisation Room → Firestore
- `DYNAMIC_LABELS`: Labels personnalisés selon profil
- `OFFLINE_MODE_ENABLED`: Cache offline Firestore
- `AUTO_CREATE_PROFILE`: Auto-création profil utilisateur

## Best practices

1. **Naming**: `UPPER_SNAKE_CASE`
2. **Risk levels**:
   - `low`: Features stables, UI, labels → `enabled: true` OK
   - `medium`: Sync réseau, cache → tester avant d'activer
   - `high`: Features complexes → `enabled: false` par défaut
3. **Traçabilité**: Toujours ajouter `comment` et `added_date`
4. **Cleanup**: Supprimer flags après rollout complet (100%)

## Utilisation dans le code

```kotlin
@Inject lateinit var featureFlagManager: FeatureFlagManager

// Vérifier un flag
if (featureFlagManager.isEnabled(FeatureFlag.SYNC_STATS_FROM_ROOM)) {
    // Fonctionnalité activée
}
```

## Architecture

```
config/flags.json
    ↓ (build copy)
app/build/intermediates/assets/flags.json
    ↓ (runtime)
FeatureFlagManager (Hilt singleton)
    ↓
ViewModels / Repositories / UI
```

## Tests

Voir `app/src/test/java/com/ora/wellbeing/data/config/FeatureFlagManagerTest.kt` pour exemples de tests avec feature flags.

---

**Dernière mise à jour:** 2025-10-04
**Maintenu par:** Ora Dev Team
