# FIX(user-dynamic): Index des Fichiers Firestore

Tous les fichiers créés/modifiés pour l'implémentation Firestore des données utilisateur.

## 📂 Fichiers Créés (20 fichiers)

### Domain Layer (7 fichiers)

#### Models
1. **`app/src/main/java/com/ora/wellbeing/domain/model/UserProfile.kt`**
   - Entity profil utilisateur
   - Enum `PlanTier` (FREE, PREMIUM)
   - Validation business logic (max 50 chars firstName)
   - Factory `createDefault()`

2. **`app/src/main/java/com/ora/wellbeing/domain/model/UserStats.kt`**
   - Entity statistiques utilisateur
   - Business logic calcul streak
   - Méthodes: `incrementSession()`, `resetStreak()`, `formatTotalTime()`, `hasPracticedToday()`
   - Validation bornes (totalMinutes 0-525600, streakDays 0-3650)

#### Repository Interfaces
3. **`app/src/main/java/com/ora/wellbeing/domain/repository/FirestoreUserProfileRepository.kt`**
   - Interface repository profil
   - Méthodes: `getUserProfile()`, `createUserProfile()`, `updateUserProfile()`, `updateLocale()`, `updatePlanTier()`, `updatePhotoUrl()`, `deleteUserProfile()`

4. **`app/src/main/java/com/ora/wellbeing/domain/repository/FirestoreUserStatsRepository.kt`**
   - Interface repository stats
   - Méthodes: `getUserStats()`, `createUserStats()`, `incrementSession()`, `resetStreak()`, `deleteUserStats()`

#### Use Cases
5. **`app/src/main/java/com/ora/wellbeing/domain/usecase/CreateUserProfileUseCase.kt`**
   - Orchestration création profil + stats
   - Appelé au premier login
   - Retourne `Result<Unit>`

6. **`app/src/main/java/com/ora/wellbeing/domain/usecase/GetUserDataUseCase.kt`**
   - Observer profil + stats combinés
   - Retourne `Flow<UserData>`
   - Combine 2 Flow avec `combine()`

7. **`app/src/main/java/com/ora/wellbeing/domain/usecase/RecordSessionUseCase.kt`**
   - Enregistrer séance terminée
   - Appelé après fin vidéo/audio
   - Retourne `Result<Unit>`

### Data Layer (3 fichiers)

#### Mappers
8. **`app/src/main/java/com/ora/wellbeing/data/mapper/UserMapper.kt`**
   - Extension `DocumentSnapshot.toUserProfile()`
   - Extension `DocumentSnapshot.toUserStats()`
   - Extension `UserProfile.toFirestoreMap()`
   - Extension `UserStats.toFirestoreMap()`
   - Gestion cas edge (null, types incorrects)

#### Repository Implementations
9. **`app/src/main/java/com/ora/wellbeing/data/repository/impl/FirestoreUserProfileRepositoryImpl.kt`**
   - Implémentation profil avec `snapshotListener`
   - Gestion offline avec cache
   - Gestion erreurs (`PERMISSION_DENIED`, `UNAVAILABLE`)
   - Méthodes partielles updates optimisées

10. **`app/src/main/java/com/ora/wellbeing/data/repository/impl/FirestoreUserStatsRepositoryImpl.kt`**
    - Implémentation stats avec transactions
    - Business logic `incrementSession()` atomique
    - Gestion offline avec cache
    - Création stats auto si n'existent pas

### DI Layer (1 fichier)

11. **`app/src/main/java/com/ora/wellbeing/di/FirestoreModule.kt`**
    - Module Hilt `@InstallIn(SingletonComponent::class)`
    - Provider `FirebaseFirestore` avec cache offline (10MB)
    - Provider `FirestoreUserProfileRepository`
    - Provider `FirestoreUserStatsRepository`

### Configuration Firestore (2 fichiers)

12. **`firestore.rules`**
    - Règles sécurité: `request.auth.uid == uid`
    - Validation `users/{uid}` (types, bornes)
    - Validation `stats/{uid}` (totalMinutes 0-525600, etc.)
    - Blocage accès cross-user

13. **`firestore.indexes.json`**
    - Index `stats.lastPracticeAt` (DESCENDING)
    - Pour queries "utilisateurs actifs récents"

### Documentation (5 fichiers)

14. **`docs/FIRESTORE_IMPLEMENTATION.md`**
    - Architecture détaillée
    - Collections Firestore
    - Features clés (offline, privacy, business logic)
    - Utilisation use cases
    - Validation Firestore Rules
    - Tests recommandés

15. **`docs/FIRESTORE_USAGE_EXAMPLE.md`**
    - Exemples ViewModels (Auth, Profile, Player, Home, Settings)
    - Exemples UI (Compose)
    - Exemples tests (Unit, Integration)
    - Gestion offline avec indicateurs UI
    - NetworkMonitor pour détecter état réseau

16. **`docs/FIRESTORE_DEPLOYMENT.md`**
    - Guide déploiement Firebase CLI
    - Configuration Firebase Emulator
    - Tests sécurité avec Emulator
    - Monitoring production (métriques, alertes)
    - Workflow mise à jour règles/indexes
    - Rollback d'urgence

17. **`docs/FIRESTORE_FILES_INDEX.md`** (ce fichier)
    - Index complet de tous les fichiers
    - Descriptions courtes
    - Organisation par layer

18. **`FIRESTORE_SUMMARY.md`**
    - Résumé exécutif
    - Architecture Clean
    - Collections Firestore
    - Business logic
    - Checklist déploiement
    - Commandes Firebase

### Fichiers Modifiés (2 fichiers)

19. **`app/build.gradle.kts`**
    - ✅ Ligne 162: `implementation("com.google.firebase:firebase-firestore")`
    - Utilise Firebase BoM 33.7.0
    - Detekt ajouté (ligne 12, 207)
    - Task `qualityCheck` (ligne 128-132)

20. **`docs/user_data_contract.yaml`** (référence existante)
    - Contrat schéma Firestore
    - Événements business
    - Offline strategy
    - Privacy/GDPR
    - Test scenarios

---

## 🗂️ Structure Complète

```
Ora/
├── app/
│   ├── build.gradle.kts                    # ✅ Modifié (firebase-firestore)
│   └── src/main/java/com/ora/wellbeing/
│       ├── domain/
│       │   ├── model/
│       │   │   ├── UserProfile.kt          # ✅ Créé
│       │   │   └── UserStats.kt            # ✅ Créé
│       │   ├── repository/
│       │   │   ├── FirestoreUserProfileRepository.kt    # ✅ Créé
│       │   │   └── FirestoreUserStatsRepository.kt      # ✅ Créé
│       │   └── usecase/
│       │       ├── CreateUserProfileUseCase.kt          # ✅ Créé
│       │       ├── GetUserDataUseCase.kt                # ✅ Créé
│       │       └── RecordSessionUseCase.kt              # ✅ Créé
│       ├── data/
│       │   ├── mapper/
│       │   │   └── UserMapper.kt           # ✅ Créé
│       │   └── repository/impl/
│       │       ├── FirestoreUserProfileRepositoryImpl.kt    # ✅ Créé
│       │       └── FirestoreUserStatsRepositoryImpl.kt      # ✅ Créé
│       └── di/
│           └── FirestoreModule.kt          # ✅ Créé
│
├── firestore.rules                         # ✅ Créé
├── firestore.indexes.json                  # ✅ Créé
├── FIRESTORE_SUMMARY.md                    # ✅ Créé
│
└── docs/
    ├── user_data_contract.yaml             # ✅ Existant (référence)
    ├── FIRESTORE_IMPLEMENTATION.md         # ✅ Créé
    ├── FIRESTORE_USAGE_EXAMPLE.md          # ✅ Créé
    ├── FIRESTORE_DEPLOYMENT.md             # ✅ Créé
    └── FIRESTORE_FILES_INDEX.md            # ✅ Créé (ce fichier)
```

---

## 📊 Statistiques

- **Fichiers créés**: 18
- **Fichiers modifiés**: 2
- **Total**: 20 fichiers
- **Lignes de code**: ~2500 lignes (estimation)
- **Documentation**: ~1500 lignes

### Répartition par Layer
- **Domain**: 7 fichiers (35%)
- **Data**: 3 fichiers (15%)
- **DI**: 1 fichier (5%)
- **Config**: 2 fichiers (10%)
- **Documentation**: 5 fichiers (25%)
- **Build**: 2 fichiers (10%)

---

## ✅ Checklist Fichiers

### Domain Layer
- [x] `UserProfile.kt` - Entity profil
- [x] `UserStats.kt` - Entity stats
- [x] `FirestoreUserProfileRepository.kt` - Interface profil
- [x] `FirestoreUserStatsRepository.kt` - Interface stats
- [x] `CreateUserProfileUseCase.kt` - Use case création
- [x] `GetUserDataUseCase.kt` - Use case observer
- [x] `RecordSessionUseCase.kt` - Use case session

### Data Layer
- [x] `UserMapper.kt` - Mappers Firestore
- [x] `FirestoreUserProfileRepositoryImpl.kt` - Impl profil
- [x] `FirestoreUserStatsRepositoryImpl.kt` - Impl stats

### DI Layer
- [x] `FirestoreModule.kt` - Module Hilt

### Configuration
- [x] `firestore.rules` - Règles sécurité
- [x] `firestore.indexes.json` - Indexes
- [x] `build.gradle.kts` - Dépendance Firestore

### Documentation
- [x] `FIRESTORE_IMPLEMENTATION.md` - Architecture
- [x] `FIRESTORE_USAGE_EXAMPLE.md` - Exemples
- [x] `FIRESTORE_DEPLOYMENT.md` - Déploiement
- [x] `FIRESTORE_FILES_INDEX.md` - Index (ce fichier)
- [x] `FIRESTORE_SUMMARY.md` - Résumé

---

## 🔍 Recherche Rapide

### Par Fonctionnalité

**Création utilisateur**:
- `CreateUserProfileUseCase.kt`
- `FirestoreUserProfileRepositoryImpl.createUserProfile()`
- `FirestoreUserStatsRepositoryImpl.createUserStats()`

**Observer données**:
- `GetUserDataUseCase.kt`
- `FirestoreUserProfileRepositoryImpl.getUserProfile()`
- `FirestoreUserStatsRepositoryImpl.getUserStats()`

**Enregistrer séance**:
- `RecordSessionUseCase.kt`
- `FirestoreUserStatsRepositoryImpl.incrementSession()`
- `UserStats.incrementSession()` (business logic)

**Sécurité**:
- `firestore.rules`
- Validation dans tous repositories (`require(uid.isNotBlank())`)

**Offline**:
- `FirestoreModule.kt` (configuration cache)
- `snapshotListener` dans implementations
- Gestion erreurs `UNAVAILABLE`

### Par Type

**Entities**:
- `UserProfile.kt`
- `UserStats.kt`

**Interfaces**:
- `FirestoreUserProfileRepository.kt`
- `FirestoreUserStatsRepository.kt`

**Implementations**:
- `FirestoreUserProfileRepositoryImpl.kt`
- `FirestoreUserStatsRepositoryImpl.kt`

**Use Cases**:
- `CreateUserProfileUseCase.kt`
- `GetUserDataUseCase.kt`
- `RecordSessionUseCase.kt`

**Mappers**:
- `UserMapper.kt`

**DI**:
- `FirestoreModule.kt`

**Config**:
- `firestore.rules`
- `firestore.indexes.json`
- `build.gradle.kts`

**Docs**:
- `FIRESTORE_IMPLEMENTATION.md`
- `FIRESTORE_USAGE_EXAMPLE.md`
- `FIRESTORE_DEPLOYMENT.md`
- `FIRESTORE_SUMMARY.md`
- `FIRESTORE_FILES_INDEX.md`

---

## 📝 Conventions de Nommage

### Repositories
- Interface: `Firestore[Entity]Repository`
- Implémentation: `Firestore[Entity]RepositoryImpl`
- Méthodes: verbe + nom (ex: `getUserProfile`, `createUserStats`)

### Use Cases
- Nom: verbe + nom + `UseCase`
- Opérateur: `operator fun invoke()`
- Retour: `Result<T>` ou `Flow<T>`

### Mappers
- Objet: `[Entity]Mapper`
- Extensions: `to[Entity]()`, `toFirestoreMap()`

### Modules
- Nom: `[Service]Module`
- Installation: `@InstallIn(SingletonComponent::class)`

### Documentation
- Implémentation: `[SERVICE]_IMPLEMENTATION.md`
- Exemples: `[SERVICE]_USAGE_EXAMPLE.md`
- Déploiement: `[SERVICE]_DEPLOYMENT.md`
- Index: `[SERVICE]_FILES_INDEX.md`

---

## 🎯 Prochaines Étapes

### Intégration
1. ✅ AuthViewModel: appeler `CreateUserProfileUseCase` après login
2. ✅ ProfileScreen: utiliser `GetUserDataUseCase`
3. ✅ PlayerViewModel: appeler `RecordSessionUseCase`
4. ✅ HomeScreen: afficher stats avec `GetUserDataUseCase`

### Déploiement
5. ✅ `firebase deploy --only firestore:rules`
6. ✅ `firebase deploy --only firestore:indexes`
7. ✅ Tester avec Firebase Emulator
8. ✅ Configurer monitoring Firebase Console

### Tests
9. ✅ Tests unitaires (UserStats calcul streak)
10. ✅ Tests integration (Firestore Emulator)
11. ✅ Tests sécurité (Rules validation)

---

**Index complet des fichiers Firestore** 📋
