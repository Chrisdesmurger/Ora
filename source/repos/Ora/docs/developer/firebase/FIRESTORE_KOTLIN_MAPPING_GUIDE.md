# Guide Firestore Kotlin Mapping - Bonnes Pratiques

## Problème Rencontré

Lors de l'implémentation de Firestore avec Kotlin, nous avons rencontré des erreurs de mapping qui empêchaient la désérialisation des documents :

```
W Firestore: No setter/field for firstName found on class UserProfile
W Firestore: No setter/field for lastName found on class UserProfile
```

Résultat : Les objets retournaient `null` malgré la présence de données dans Firestore.

## Solution : Structure Correcte pour Firestore

### ✅ CORRECT - Regular Class avec Propriétés en Dehors du Constructeur

```kotlin
@IgnoreExtraProperties
class UserProfile {
    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid: String = ""

    @get:PropertyName("first_name")
    @set:PropertyName("first_name")
    var firstName: String? = null

    @get:PropertyName("last_name")
    @set:PropertyName("last_name")
    var lastName: String? = null

    @get:PropertyName("email")
    @set:PropertyName("email")
    var email: String? = null

    @get:PropertyName("photo_url")
    @set:PropertyName("photo_url")
    var photoUrl: String? = null

    @get:PropertyName("plan_tier")
    @set:PropertyName("plan_tier")
    var planTier: String = "FREE"

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    @ServerTimestamp
    var createdAt: Date? = null

    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    @ServerTimestamp
    var updatedAt: Date? = null

    /**
     * Méthodes calculées DOIVENT avoir @Exclude
     */
    @Exclude
    fun getDisplayName(): String {
        return when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() -> "$firstName $lastName"
            !firstName.isNullOrBlank() -> firstName!!
            !lastName.isNullOrBlank() -> lastName!!
            else -> "Invité"
        }
    }

    @Exclude
    fun isPremium(): Boolean {
        return planTier == "PREMIUM" || planTier == "LIFETIME"
    }
}
```

### ❌ INCORRECT - Data Class avec Paramètres dans le Constructeur

```kotlin
// NE FONCTIONNE PAS - Firestore ne trouve pas les setters
@IgnoreExtraProperties
data class UserProfile(
    @PropertyName("uid")
    var uid: String = "",

    @PropertyName("first_name")
    var firstName: String? = null,
    // ...
)
```

### ❌ INCORRECT - Data Class sans Paramètres

```kotlin
// NE COMPILE PAS - Data class nécessite au moins 1 paramètre
@IgnoreExtraProperties
data class UserProfile() {
    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid: String = ""
}
```

## Règles Essentielles

### 1. Structure de la Classe

- ✅ Utiliser `class` (pas `data class`)
- ✅ Constructeur vide par défaut
- ✅ Toutes les propriétés déclarées **dans le corps de la classe** (pas dans le constructeur)
- ✅ Utiliser `var` (pas `val`) pour toutes les propriétés persistées

### 2. Annotations PropertyName

Pour mapper snake_case (Firestore) ↔ camelCase (Kotlin) :

```kotlin
@get:PropertyName("first_name")  // Pour la lecture (Firestore → Kotlin)
@set:PropertyName("first_name")  // Pour l'écriture (Kotlin → Firestore)
var firstName: String? = null
```

**Important :** Les annotations doivent être sur les getter/setter, PAS directement sur la propriété.

### 3. Annotations de Classe

```kotlin
@IgnoreExtraProperties  // Ignore les champs Firestore non mappés
class UserProfile {
    // ...
}
```

### 4. Méthodes Calculées

Toutes les méthodes qui ne doivent PAS être sérialisées dans Firestore doivent avoir `@Exclude` :

```kotlin
@Exclude
fun getDisplayName(): String {
    return "$firstName $lastName"
}
```

### 5. Timestamps Automatiques

```kotlin
@get:PropertyName("created_at")
@set:PropertyName("created_at")
@ServerTimestamp  // Firestore remplit automatiquement
var createdAt: Date? = null
```

### 6. DocumentId

Pour le UID du document :

```kotlin
@DocumentId
var uid: String = ""
```

## Instantiation avec apply

Puisque nous n'avons plus de constructeur avec paramètres, utiliser `apply` :

```kotlin
val profile = UserProfile().apply {
    this.uid = "user123"
    this.firstName = "Jean"
    this.lastName = "Dupont"
    this.email = "jean@example.com"
    this.planTier = "FREE"
}
```

## Convention de Nommage Firestore vs Kotlin

| Firestore (snake_case) | Kotlin (camelCase) | Type |
|------------------------|-------------------|------|
| `first_name` | `firstName` | `String?` |
| `last_name` | `lastName` | `String?` |
| `photo_url` | `photoUrl` | `String?` |
| `plan_tier` | `planTier` | `String` |
| `created_at` | `createdAt` | `Date?` |
| `updated_at` | `updatedAt` | `Date?` |
| `total_sessions` | `totalSessions` | `Int` |
| `current_streak` | `currentStreak` | `Int` |
| `has_gratitude_today` | `hasGratitudeToday` | `Boolean` |

## Collection Names et Security Rules

Les noms de collections dans le code **doivent correspondre** aux règles Firestore :

### UserProfileRepository.kt
```kotlin
companion object {
    private const val COLLECTION_USER_PROFILES = "users"  // Pas "user_profiles"
}
```

### UserStatsRepository.kt
```kotlin
companion object {
    private const val COLLECTION_USER_STATS = "stats"  // Pas "user_stats"
}
```

### firestore.rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {  // "users" correspond au code
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
    match /stats/{uid} {  // "stats" correspond au code
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

## Debugging - Comment Vérifier que ça Fonctionne

### 1. Vérifier les Warnings CustomClassMapper

Après build, vérifier les logs :

```bash
adb logcat | grep CustomClassMapper
```

✅ **Aucun warning** = mapping correct

❌ **"No setter/field found"** = problème de structure

### 2. Vérifier la Désérialisation

```bash
adb logcat | grep "Profile updated"
```

✅ `Profile updated: UserProfile@...` avec données

❌ `Profile updated: null` = mapping échoue

### 3. Vérifier la Structure dans Firestore Console

Le document dans Firestore doit avoir :

```json
{
  "uid": "OCqFs1Q7zYV9uNUVYbPAnzoQV0N2",
  "first_name": "Jean",
  "last_name": "Dupont",
  "email": "jean@example.com",
  "photo_url": null,
  "motto": "Je prends soin de moi chaque jour",
  "plan_tier": "FREE",
  "created_at": Timestamp,
  "updated_at": Timestamp
}
```

## Migration de Données Existantes

Si vous avez des documents Firestore avec l'ancienne structure :

1. **Option 1 - Console Firebase :**
   - Supprimer les documents `users/{uid}` et `stats/{uid}`
   - Relancer l'app → SyncManager recrée automatiquement

2. **Option 2 - Migration Script :**
   - Lire tous les documents
   - Transformer les champs
   - Réécrire avec la nouvelle structure

## Checklist Avant Commit

- [ ] Classe utilise `class` (pas `data class`)
- [ ] Toutes propriétés sont `var` dans le corps de classe
- [ ] `@get:PropertyName` et `@set:PropertyName` sur chaque propriété snake_case
- [ ] `@IgnoreExtraProperties` sur la classe
- [ ] `@Exclude` sur toutes les méthodes calculées
- [ ] Nom de collection correspond aux Firestore rules
- [ ] Testé sans warnings CustomClassMapper dans logs
- [ ] Testé que la désérialisation retourne des objets (pas null)

## Références

- [Firebase Firestore Kotlin Custom Objects](https://firebase.google.com/docs/firestore/manage-data/add-data#custom_objects)
- [PropertyName Annotation](https://firebase.google.com/docs/reference/android/com/google/firebase/firestore/PropertyName)
- [Exclude Annotation](https://firebase.google.com/docs/reference/android/com/google/firebase/firestore/Exclude)
- [IgnoreExtraProperties](https://firebase.google.com/docs/reference/android/com/google/firebase/firestore/IgnoreExtraProperties)

## Historique des Erreurs Résolues

### Erreur 1 : PERMISSION_DENIED
- **Cause :** Collection names (`user_profiles`, `user_stats`) ne correspondaient pas aux rules (`users`, `stats`)
- **Solution :** Renommer les constantes dans les repositories

### Erreur 2 : CustomClassMapper warnings
- **Cause :** Data class avec propriétés dans le constructeur
- **Solution :** Convertir en `class` régulière avec propriétés dans le corps

### Erreur 3 : Profile deserialized as null
- **Cause :** Documents Firestore créés avec ancienne structure incompatible
- **Solution :** Supprimer et recréer les documents
