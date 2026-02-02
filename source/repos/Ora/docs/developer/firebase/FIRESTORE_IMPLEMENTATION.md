# FIX(user-dynamic): Implémentation Firestore - Données Utilisateur Ora

**Date**: 2025-10-04
**Status**: ✅ Complété
**Contrat**: `docs/user_data_contract.yaml`

## 📋 Résumé

Implémentation complète de la persistance Firestore pour les données utilisateur avec architecture Clean, offline-first, et privacy by design.

## 🏗️ Architecture Implémentée

### Domain Layer
**Modèles de domaine** (`domain/model/`):
- ✅ `UserProfile.kt` - Profil utilisateur avec enum `PlanTier`
- ✅ `UserStats.kt` - Statistiques avec business logic du streak

**Interfaces repositories** (`domain/repository/`):
- ✅ `FirestoreUserProfileRepository.kt` - Contrat profil
- ✅ `FirestoreUserStatsRepository.kt` - Contrat stats

**Use cases** (`domain/usecase/`):
- ✅ `CreateUserProfileUseCase.kt` - Création profil + stats (premier login)
- ✅ `GetUserDataUseCase.kt` - Observer profil + stats combinés (Flow réactif)
- ✅ `RecordSessionUseCase.kt` - Enregistrer séance avec calcul streak

### Data Layer
**Mappers** (`data/mapper/`):
- ✅ `UserMapper.kt` - Conversions Firestore Document ↔ Domain Models

**Implémentations repositories** (`data/repository/impl/`):
- ✅ `FirestoreUserProfileRepositoryImpl.kt` - Implémentation profil avec offline cache
- ✅ `FirestoreUserStatsRepositoryImpl.kt` - Implémentation stats avec transactions

### DI Layer
**Modules Hilt** (`di/`):
- ✅ `FirestoreModule.kt` - Fournit Firestore instance + repositories

## 📦 Fichiers Créés

```
app/src/main/java/com/ora/wellbeing/
├── domain/
│   ├── model/
│   │   ├── UserProfile.kt ✅ (avec PlanTier enum)
│   │   └── UserStats.kt ✅ (avec calcul streak)
│   ├── repository/
│   │   ├── FirestoreUserProfileRepository.kt ✅
│   │   └── FirestoreUserStatsRepository.kt ✅
│   └── usecase/
│       ├── CreateUserProfileUseCase.kt ✅
│       ├── GetUserDataUseCase.kt ✅
│       └── RecordSessionUseCase.kt ✅
├── data/
│   ├── mapper/
│   │   └── UserMapper.kt ✅
│   └── repository/impl/
│       ├── FirestoreUserProfileRepositoryImpl.kt ✅
│       └── FirestoreUserStatsRepositoryImpl.kt ✅
└── di/
    └── FirestoreModule.kt ✅

firestore.rules ✅ (règles de sécurité)
```

## 🔐 Collections Firestore

### `users/{uid}`
**Champs**:
- `uid`: String (immutable, source de vérité)
- `firstName`: String? (max 50 chars, PII minimal)
- `photoUrl`: String? (URL photo profil)
- `planTier`: String ("free" | "premium")
- `createdAt`: Long (epoch ms)
- `locale`: String? ("fr" | "en" | null = système)
- `lastSyncAt`: Long? (dernier sync réussi)

**Règles**:
- Lecture/écriture si `request.auth.uid == uid`
- Validation types et bornes

### `stats/{uid}`
**Champs**:
- `uid`: String (référence profil)
- `totalMinutes`: Int (0 - 525600)
- `sessions`: Int (>= 0)
- `streakDays`: Int (0 - 3650)
- `lastPracticeAt`: Long? (epoch ms)
- `updatedAt`: Long (epoch ms)

**Règles**:
- Lecture/écriture si `request.auth.uid == uid`
- Validation valeurs min/max

## 🚀 Configuration

### Dépendances Ajoutées
```kotlin
// app/build.gradle.kts
implementation("com.google.firebase:firebase-firestore") // Via BoM 33.7.0
```

### Configuration Firestore
```kotlin
// FirestoreModule.kt
val settings = FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)  // Cache offline activé
    .setCacheSizeBytes(10 * 1024 * 1024L)  // 10MB cache
    .build()
```

## 💡 Features Clés

### ✅ Offline-First
- Cache persistant 10MB activé
- `snapshotListener` continue de fonctionner offline (retourne cache)
- Écritures en attente sync auto à la reconnexion
- Gestion d'erreurs `UNAVAILABLE` → utilise cache

### ✅ Privacy by Design
- UID vérifié avant chaque opération (`require(uid.isNotBlank())`)
- Firestore Rules: `request.auth.uid == uid`
- PII minimal (firstName, photoUrl optionnels)
- Support GDPR: méthodes `deleteUserProfile()` et `deleteUserStats()`

### ✅ Business Logic du Streak
**Calcul automatique dans `UserStats.incrementSession()`**:
```kotlin
if (lastPracticeAt == null) -> streakDays = 1  // Premier jour
if (même jour) -> streakDays inchangé
if (jour consécutif) -> streakDays + 1
else (gap > 1 jour) -> streakDays = 1  // Reset
```

### ✅ Gestion d'Erreurs
**FirestoreException codes**:
- `PERMISSION_DENIED` → Timber.e, UI devrait logout user (token expiré)
- `UNAVAILABLE` → Timber.w, UI affiche indicateur offline
- Autres → Timber.e, log erreur inattendue

**Result<T> pattern**:
- Write ops retournent `Result<Unit>` (success/failure)
- Read ops retournent `Flow<T?>` (null si n'existe pas)

### ✅ Transactions
**`incrementSession()` utilise Firestore transaction**:
- Garantit cohérence des calculs (totalMinutes, sessions, streakDays)
- Crée stats si n'existent pas
- Atomique (tout ou rien)

## 🎯 Utilisation

### 1. Premier Login (Création Profil)
```kotlin
@Inject lateinit var createUserProfile: CreateUserProfileUseCase

// Dans AuthViewModel après Firebase login success
viewModelScope.launch {
    val result = createUserProfile(
        uid = firebaseUser.uid,
        firstName = firebaseUser.displayName,
        photoUrl = firebaseUser.photoUrl?.toString()
    )

    if (result.isSuccess) {
        // Profil + stats créés
        navigateToHome()
    } else {
        // Gérer erreur
    }
}
```

### 2. Observer Données Utilisateur
```kotlin
@Inject lateinit var getUserData: GetUserDataUseCase

// Dans ProfileViewModel
val userData: StateFlow<UserData> = getUserData(currentUid)
    .stateIn(viewModelScope, SharingStarted.Lazily, UserData(null, null))

// Dans UI
val userData by viewModel.userData.collectAsState()
userData.profile?.displayName()  // "John" ou "Invité"
userData.stats?.formatTotalTime()  // "2h 30min"
```

### 3. Enregistrer Séance
```kotlin
@Inject lateinit var recordSession: RecordSessionUseCase

// Après fin de vidéo méditation
viewModelScope.launch {
    val result = recordSession(
        uid = currentUid,
        durationMinutes = 15
    )

    if (result.isSuccess) {
        // Stats mises à jour (totalMinutes +15, sessions +1, streak calculé)
    }
}
```

## 🔄 Flux de Données

### Nouveau Utilisateur
1. **Firebase Auth** → Login réussi → `firebaseUser.uid`
2. **CreateUserProfileUseCase** → Crée `users/{uid}` + `stats/{uid}`
3. **GetUserDataUseCase** → Flow émet `UserData(profile, stats)`
4. **UI** → Affiche profil + stats

### Utilisateur Existant
1. **Firebase Auth** → Login réussi → `firebaseUser.uid`
2. **GetUserDataUseCase** → Flow écoute `users/{uid}` + `stats/{uid}`
3. **snapshotListener** → Émet changements en temps réel
4. **UI** → Mise à jour automatique

### Séance Terminée
1. **Player** → Vidéo terminée → `durationMinutes`
2. **RecordSessionUseCase** → Transaction Firestore
3. **Calcul streak** → Business logic dans `UserStats`
4. **snapshotListener** → Émet nouvelles stats
5. **UI** → Affiche nouveau streak/totalMinutes

## 📊 Validation Firestore Rules

**Test à faire dans Firebase Console**:
```javascript
// users/{uid}
{
  "uid": "test123",
  "firstName": "John",
  "photoUrl": null,
  "planTier": "free",
  "createdAt": 1728000000000,
  "locale": "fr",
  "lastSyncAt": 1728000000000
}

// stats/{uid}
{
  "uid": "test123",
  "totalMinutes": 150,
  "sessions": 10,
  "streakDays": 5,
  "lastPracticeAt": 1728000000000,
  "updatedAt": 1728000000000
}
```

**Tests de sécurité**:
- ✅ User A peut lire/écrire `users/A` → **Autorisé**
- ❌ User A essaie lire `users/B` → **Interdit (Permission denied)**
- ❌ Unauthenticated essaie lire `users/A` → **Interdit**
- ✅ User A update `totalMinutes` à 200 → **Autorisé** (< 525600)
- ❌ User A update `totalMinutes` à 600000 → **Interdit** (> 525600)

## 🧪 Tests Recommandés

### Unit Tests
- `UserStatsTest.kt` → Tester calcul streak (même jour, consécutif, gap)
- `UserMapperTest.kt` → Tester conversions Document ↔ Model
- `CreateUserProfileUseCaseTest.kt` → Tester orchestration profil + stats

### Integration Tests
- Tester offline → online sync
- Tester transaction `incrementSession` (concurrence)
- Tester Firestore rules avec Firebase Emulator

## 🔜 Prochaines Étapes

### Intégration UI
1. **AuthScreen** → Appeler `CreateUserProfileUseCase` après login
2. **ProfileScreen** → Utiliser `GetUserDataUseCase` pour afficher profil/stats
3. **Player** → Appeler `RecordSessionUseCase` après fin vidéo

### Features Futures
- 🔄 Sync Room ↔ Firestore pour journal offline
- 📊 Analytics des streaks (Cloud Functions)
- 🔔 Notifications streak cassé (WorkManager + Cloud Messaging)
- 💳 In-App Billing → `updatePlanTier("premium")`

## 📝 Notes Importantes

### Différence avec UserRepository Existant
- `UserRepository.kt` (existant) → **Room local** pour offline
- `FirestoreUserProfileRepository` (nouveau) → **Firestore cloud** pour sync
- Les deux coexistent: Room = cache local, Firestore = source de vérité cloud

### Migration Données Locales
Si Room déjà utilisé pour user data:
```sql
-- Ajouter userId aux entités existantes
ALTER TABLE journal_entries ADD COLUMN userId TEXT NOT NULL DEFAULT '';
ALTER TABLE practices ADD COLUMN userId TEXT NOT NULL DEFAULT '';
```

### Coût Firestore
**Estimations** (tiers gratuit 50k reads/20k writes/jour):
- Création profil: 2 writes (profil + stats)
- Observer profil: 1 read initial + writes count si changements
- Session complétée: 1 write (transaction stats)
- **~100 users actifs/jour = ~500 writes = bien dans gratuit**

## ✅ Checklist Déploiement

- [x] Dépendances Firebase Firestore ajoutées
- [x] Modèles domaine créés avec validation
- [x] Repositories interfaces définies
- [x] Repositories implémentations avec offline
- [x] Use cases orchestration
- [x] Module Hilt DI
- [x] Firestore Rules sécurité
- [ ] Tests unitaires
- [ ] Tests intégration avec Emulator
- [ ] Intégration AuthScreen (appeler CreateUserProfileUseCase)
- [ ] Intégration ProfileScreen (afficher GetUserDataUseCase)
- [ ] Intégration Player (appeler RecordSessionUseCase)
- [ ] Déployer firestore.rules sur Firebase Console
- [ ] Monitoring Firestore usage

---

**Implémentation complète et prête pour intégration UI** 🚀
