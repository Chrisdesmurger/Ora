# Firestore Troubleshooting Guide

## Table des Matières

1. [CustomClassMapper Warnings](#customclassmapper-warnings)
2. [Profile Returns Null](#profile-returns-null)
3. [PERMISSION_DENIED Errors](#permission_denied-errors)
4. [Collections Not Creating](#collections-not-creating)
5. [Real-time Updates Not Working](#real-time-updates-not-working)
6. [JobCancellationException](#jobcancellationexception)

---

## CustomClassMapper Warnings

### Symptôme

```
W Firestore: No setter/field for firstName found on class UserProfile
W Firestore: No setter/field for lastName found on class UserProfile
```

### Diagnostic

```bash
adb logcat | grep CustomClassMapper
```

### Causes Possibles

1. **Propriétés dans le constructeur de data class**
   ```kotlin
   // ❌ INCORRECT
   data class UserProfile(
       @PropertyName("first_name")
       var firstName: String? = null
   )
   ```

2. **Annotations manquantes sur getter/setter**
   ```kotlin
   // ❌ INCORRECT
   @PropertyName("first_name")
   var firstName: String? = null
   ```

3. **Propriétés en val au lieu de var**
   ```kotlin
   // ❌ INCORRECT
   val firstName: String? = null
   ```

### Solution

Utiliser la structure correcte :

```kotlin
@IgnoreExtraProperties
class UserProfile {
    @get:PropertyName("first_name")
    @set:PropertyName("first_name")
    var firstName: String? = null
}
```

### Vérification

Après rebuild :
```bash
adb logcat -c
adb logcat | grep CustomClassMapper
```

✅ Aucun warning = problème résolu

---

## Profile Returns Null

### Symptôme

```
D UserProfileRepository: Profile updated: null
D SyncManager: Profile received: null
```

### Diagnostic

1. **Vérifier les logs de mapping :**
   ```bash
   adb logcat | grep -E "(CustomClassMapper|Profile updated)"
   ```

2. **Vérifier le document dans Firestore Console :**
   - Aller sur Firebase Console → Firestore Database
   - Chercher `users/{uid}`
   - Vérifier la structure des champs

### Causes Possibles

1. **Structure du document incompatible**
   - Document créé avec ancienne structure de classe
   - Noms de champs ne correspondent pas aux `@PropertyName`

2. **CustomClassMapper warnings présents**
   - Voir section précédente

3. **Document n'existe pas**
   ```bash
   adb logcat | grep "Profile updated"
   ```

   Si aucun log → listener ne reçoit rien

### Solution

#### Option 1 : Supprimer et recréer (Recommandé)

1. Firebase Console → Firestore Database
2. Supprimer `users/{uid}`
3. Supprimer `stats/{uid}`
4. Relancer l'app
5. SyncManager recrée avec bonne structure

#### Option 2 : Migration manuelle

Dans Firebase Console, modifier le document pour correspondre à :

```json
{
  "uid": "...",
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

### Vérification

```bash
adb logcat | grep "Profile updated"
```

✅ `Profile updated: UserProfile@<hash>` avec données

---

## PERMISSION_DENIED Errors

### Symptôme

```
E Firestore: PERMISSION_DENIED: Missing or insufficient permissions
```

### Diagnostic

```bash
adb logcat | grep PERMISSION_DENIED
```

### Causes Possibles

1. **Firestore Rules non déployées**
   ```bash
   firebase deploy --only firestore:rules
   ```

2. **Collection names ne correspondent pas aux rules**

   Code utilise `user_profiles` mais rules attendent `users`

3. **User non authentifié**
   ```bash
   adb logcat | grep "Auth state"
   ```

4. **UID mismatch dans les rules**

### Solution

1. **Vérifier les rules déployées :**

   Firebase Console → Firestore Database → Rules

   Doit contenir :
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{uid} {
         allow read, write: if request.auth != null && request.auth.uid == uid;
       }
       match /stats/{uid} {
         allow read, write: if request.auth != null && request.auth.uid == uid;
       }
     }
   }
   ```

2. **Vérifier collection names dans code :**

   UserProfileRepository.kt :
   ```kotlin
   private const val COLLECTION_USER_PROFILES = "users"  // Pas "user_profiles"
   ```

   UserStatsRepository.kt :
   ```kotlin
   private const val COLLECTION_USER_STATS = "stats"  // Pas "user_stats"
   ```

3. **Vérifier authentification :**
   ```bash
   adb logcat | grep OraAuthViewModel
   ```

   ✅ Doit montrer `User authenticated, uid=...`

### Vérification

Tester écriture :
```bash
adb logcat | grep -E "(Firestore|PERMISSION)"
```

✅ Aucune erreur PERMISSION_DENIED

---

## Collections Not Creating

### Symptôme

Firebase Console → Firestore Database vide après création d'utilisateur

### Diagnostic

```bash
adb logcat | grep SyncManager
```

### Causes Possibles

1. **PERMISSION_DENIED** (voir section précédente)

2. **SyncManager non démarré**
   ```bash
   adb logcat | grep "Starting sync"
   ```

   ❌ Aucun log → SyncManager pas appelé

3. **Erreur lors de la création**
   ```bash
   adb logcat | grep "Error creating"
   ```

4. **JobCancellationException** (voir section dédiée)

### Solution

1. **Vérifier que SyncManager démarre :**

   OraAuthViewModel.kt doit appeler :
   ```kotlin
   SyncManager.startSync(uid)
   ```

2. **Vérifier les logs de création :**
   ```bash
   adb logcat | grep -E "(Default profile|Default stats)"
   ```

   ✅ `Default profile created successfully`
   ✅ `Default stats created successfully`

3. **Forcer recréation :**

   Supprimer et réinstaller l'app :
   ```bash
   adb uninstall com.ora.wellbeing
   ./gradlew installDebug
   ```

### Vérification

Firebase Console → Firestore Database doit montrer :
- Collection `users` avec document `{uid}`
- Collection `stats` avec document `{uid}`

---

## Real-time Updates Not Working

### Symptôme

Modifications dans Firebase Console ne se reflètent pas dans l'app

### Diagnostic

```bash
adb logcat | grep -E "(getUserProfileFlow|Profile updated)"
```

### Causes Possibles

1. **Listener non actif**

   ❌ Aucun log "Setting up listener"

2. **Profile désérialisé en null**

   Voir section "Profile Returns Null"

3. **ViewModel ne s'abonne pas au Flow**

### Solution

1. **Vérifier le listener :**
   ```bash
   adb logcat | grep "Setting up listener"
   ```

   ✅ Doit apparaître au démarrage

2. **Vérifier les updates :**
   ```bash
   adb logcat | grep "Profile updated"
   ```

   ✅ Doit apparaître à chaque changement

3. **Vérifier le ViewModel :**

   ProfileViewModel.kt :
   ```kotlin
   init {
       observeUserData()
   }

   private fun observeUserData() {
       viewModelScope.launch {
           userProfileRepository.getUserProfileFlow().collect { profile ->
               // ...
           }
       }
   }
   ```

### Test

1. Modifier `first_name` dans Firebase Console
2. Observer les logs :
   ```bash
   adb logcat | grep -E "(Profile updated|ProfileViewModel)"
   ```

✅ `Profile updated: ...` puis `ProfileViewModel: UI State updated`

---

## JobCancellationException

### Symptôme

```
E SyncManager: Error syncing profile
E SyncManager: kotlinx.coroutines.JobCancellationException:
  StandaloneCoroutine was cancelled
```

### Diagnostic

```bash
adb logcat | grep JobCancellationException
```

### Causes

1. **Sync redémarré trop rapidement**

   Flow émet plusieurs fois → startSync appelé plusieurs fois → jobs s'annulent

2. **stopSync appelé immédiatement après startSync**

### Impact

⚠️ **En général, ce n'est PAS un problème bloquant**

- Collections se créent quand même
- Profile/Stats se synchronisent quand même
- C'est une "race condition" bénigne

### Solution (Optionnelle)

Si vous voulez éliminer ces logs, ajouter un debounce :

```kotlin
// OraAuthViewModel.kt
private fun observeAuthState() {
    viewModelScope.launch {
        authRepository.getAuthStateFlow()
            .debounce(300) // Attend 300ms avant d'émettre
            .collect { authState ->
                // ...
            }
    }
}
```

### Vérification

Même avec JobCancellationException, vérifier que :

```bash
adb logcat | grep -E "(Profile updated|Stats received)"
```

✅ Les données arrivent quand même

---

## Commandes de Debug Utiles

### Logs Firestore Complets

```bash
adb logcat | grep -E "(Firestore|SyncManager|AuthViewModel|UserProfileRepository)"
```

### Clear et Watch

```bash
adb logcat -c && adb logcat | grep -E "(Firestore|Profile|Stats)"
```

### Vérifier Mapping Uniquement

```bash
adb logcat | grep CustomClassMapper
```

### Vérifier Authentification

```bash
adb logcat | grep -E "(Auth|Firebase)"
```

### Filtrer les Erreurs

```bash
adb logcat *:E | grep Firestore
```

---

## Workflow de Debug

1. **Clear logs**
   ```bash
   adb logcat -c
   ```

2. **Lancer l'app et collecter les logs**
   ```bash
   adb logcat > logs.txt
   ```

3. **Chercher dans l'ordre :**
   - CustomClassMapper warnings
   - PERMISSION_DENIED errors
   - "Profile updated: null"
   - "Default profile created"
   - "Setting up listener"

4. **Vérifier Firebase Console**
   - Collections existent ?
   - Documents ont bonne structure ?
   - Rules déployées ?

5. **Tester modification**
   - Modifier un champ dans Console
   - Vérifier logs : "Profile updated"
   - Vérifier UI de l'app

---

## Checklist de Santé Firestore

- [ ] Aucun warning CustomClassMapper dans logs
- [ ] `Profile updated: UserProfile@...` (pas null)
- [ ] `Stats received: X sessions` (pas null)
- [ ] Collections `users` et `stats` visibles dans Console
- [ ] Rules déployées et visibles dans Console
- [ ] Aucune erreur PERMISSION_DENIED
- [ ] Modifications dans Console se reflètent dans app (< 1s)
- [ ] Modifications dans app persistent dans Console

---

## Contacts & Ressources

- [Firebase Console](https://console.firebase.google.com/)
- [Firestore Rules Playground](https://firebase.google.com/docs/rules/simulator)
- [Mapping Guide](./FIRESTORE_KOTLIN_MAPPING_GUIDE.md)
- [Setup Guide](./FIRESTORE_SETUP_GUIDE.md)
