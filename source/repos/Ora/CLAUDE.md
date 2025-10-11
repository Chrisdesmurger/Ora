# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ora is a Android wellbeing application built with Jetpack Compose, implementing clean architecture principles with MVVM pattern. The app focuses on meditation, yoga, journaling, and personal development programs with **complete offline support**.

## Architecture

### Clean Architecture Layers
- **Presentation Layer**: UI (Compose) + ViewModels + Navigation
- **Domain Layer**: Use Cases + Repository Interfaces + Entities (in progress)
- **Data Layer**: Repository Implementations + Data Sources + Room Database (offline cache) + Firestore (cloud sync)

### Tech Stack
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture + Offline-First
- **DI**: Hilt (Dagger)
- **Navigation**: Navigation Compose
- **Backend**: Firebase (Authentication + Firestore)
- **Database**: Room (local cache) + Firestore (cloud sync)
- **Networking**: Retrofit + OkHttp (configured)
- **Media**: ExoPlayer (configured)
- **Async**: Coroutines + Flow
- **Work Manager**: For background sync and notifications
- **Offline Support**: Complete offline-first architecture

## Application Structure

### Main Features
1. **🏠 Home (Accueil)**: Recommendations, quick sessions, active programs, weekly stats
2. **📚 Library (Bibliothèque)**: Content catalog with filtering and search (offline available)
3. **📖 Journal**: Daily gratitudes with streak tracking (offline-first)
4. **📅 Programs (Programmes)**: Structured challenges and learning paths
5. **👤 Profile**: User stats, badges, goals, and settings

### Navigation
- Bottom navigation with 5 main tabs
- Nested navigation for detailed screens
- Deep linking support (configured)

### Current Implementation Status

#### ✅ Completed
- Project setup with all dependencies
- Main navigation structure with bottom bar
- All 5 main screens with ViewModels:
  - HomeScreen + HomeViewModel
  - LibraryScreen + LibraryViewModel
  - JournalScreen + JournalViewModel
  - ProgramsScreen + ProgramsViewModel
  - ProfileScreen + ProfileViewModel
- OraTheme with Material 3 design system (Ora brand colors)
- Navigation destinations and routing
- Mock data for all screens
- MVVM pattern with UiState/UiEvent
- **Firebase Authentication** (Email/Password + Google Sign-In via Credential Manager)
- **Firestore Integration** with real-time sync:
  - UserProfile (users collection)
  - UserStats (stats collection)
  - SyncManager for automatic profile/stats creation
  - Repository pattern with Flow-based listeners
  - Security rules with UID-based isolation
- **Complete Offline Support** (NEW):
  - Room database v2 with 11 entities
  - Offline-first repositories
  - Smart caching with stale detection
  - Background sync with WorkManager
  - Network monitoring
  - Bidirectional sync (upload + download)
  - Complete unit tests for DAOs

#### 🚧 In Progress / TODO
- Integration of offline repositories into ViewModels
- Domain layer (Use Cases, Repository interfaces)
- API integration (Retrofit setup exists)
- ExoPlayer integration for video/audio content
- WorkManager for evening gratitude reminders
- Detailed screens (ContentDetail, ProgramDetail, etc.)
- Google Play Billing for Premium subscriptions
- Push notifications
- Settings screens (Profile editing, preferences)
- Content management (meditation sessions, yoga videos)

## File Structure

```
app/src/main/java/com/ora/wellbeing/
├── MainActivity.kt (✅ Updated with navigation)
├── OraApplication.kt (✅ Hilt setup)
├── presentation/
│   ├── theme/
│   │   └── OraTheme.kt (✅ Material 3 with Ora brand colors)
│   ├── navigation/
│   │   ├── OraDestinations.kt (✅ Route definitions)
│   │   ├── OraNavigation.kt (✅ NavHost + BottomBar)
│   │   └── OraAuthViewModel.kt (✅ Auth state management)
│   └── screens/
│       ├── auth/ (✅ AuthScreen with Email/Password + Google)
│       ├── home/ (✅ HomeScreen + HomeViewModel)
│       ├── library/ (✅ LibraryScreen + LibraryViewModel)
│       ├── journal/ (✅ JournalScreen + JournalViewModel)
│       ├── programs/ (✅ ProgramsScreen + ProgramsViewModel)
│       └── profile/ (✅ ProfileScreen + ProfileViewModel)
├── core/
│   └── util/
│       ├── NetworkMonitor.kt (✅ Connectivity monitoring)
│       └── Resource.kt (✅ State wrapper)
├── domain/ (🚧 Planned)
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── OraDatabase.kt (✅ Room DB v2)
│   │   │   ├── Converters.kt (✅ Type converters)
│   │   │   ├── Migrations.kt (✅ DB migrations)
│   │   │   └── DatabaseCallback.kt (✅ Seed data)
│   │   ├── dao/
│   │   │   ├── ContentDao.kt (✅)
│   │   │   ├── JournalDao.kt (✅)
│   │   │   ├── ProgramDao.kt (✅)
│   │   │   ├── UserProgramDao.kt (✅)
│   │   │   ├── UserDao.kt (✅)
│   │   │   ├── UserActivityDao.kt (✅)
│   │   │   ├── UserFavoriteDao.kt (✅)
│   │   │   ├── UserStatsDao.kt (✅)
│   │   │   ├── SyncMetadataDao.kt (✅)
│   │   │   ├── SettingsDao.kt (✅)
│   │   │   └── NotificationPreferenceDao.kt (✅)
│   │   └── entities/
│   │       ├── Content.kt (✅)
│   │       ├── JournalEntry.kt (✅)
│   │       ├── Program.kt (✅)
│   │       ├── UserProgram.kt (✅)
│   │       ├── User.kt (✅)
│   │       ├── UserActivity.kt (✅)
│   │       ├── UserFavorite.kt (✅)
│   │       ├── UserStats.kt (✅)
│   │       ├── SyncMetadata.kt (✅)
│   │       ├── Settings.kt (✅)
│   │       └── NotificationPreference.kt (✅)
│   ├── mapper/
│   │   ├── ContentMapper.kt (✅ Firestore ↔ Room)
│   │   ├── ProgramMapper.kt (✅ Firestore ↔ Room)
│   │   └── GratitudeMapper.kt (✅ Firestore ↔ Room)
│   ├── model/
│   │   ├── UserProfile.kt (✅ Firestore model)
│   │   ├── UserStats.kt (✅ Firestore model)
│   │   ├── ContentItem.kt (✅ Firestore model)
│   │   ├── GratitudeEntry.kt (✅ Firestore model)
│   │   └── Program.kt (✅ Firestore model)
│   ├── repository/
│   │   ├── AuthRepository.kt (✅ Firebase Auth)
│   │   ├── UserProfileRepository.kt (✅ Firestore)
│   │   ├── UserStatsRepository.kt (✅ Firestore)
│   │   └── impl/
│   │       ├── OfflineFirstContentRepository.kt (✅)
│   │       └── OfflineFirstGratitudeRepository.kt (✅)
│   └── sync/
│       ├── SyncManager.kt (✅ Firestore profile/stats sync)
│       ├── EnhancedSyncManager.kt (✅ Background sync orchestration)
│       └── SyncWorker.kt (✅ WorkManager background sync)
└── di/
    ├── FirebaseModule.kt (✅ Firebase DI)
    ├── FirestoreModule.kt (✅ Firestore DI)
    └── DatabaseModule.kt (✅ Room DI)
```

## Design Principles

### UI/UX
- Material 3 Design with custom Ora color scheme
- Warm color palette: Orange coral (#F18D5C), Peach (#F5C9A9), Warm beige (#F5EFE6)
- Accessible components with proper contrast
- Smooth animations and transitions
- Responsive layout for different screen sizes
- Offline indicators and sync status

### Code Quality
- SOLID principles
- Clean Architecture
- Separation of concerns
- Testable code structure
- Proper error handling
- Timber logging
- Offline-first design

### Offline-First Architecture

**CRITICAL:** The app now follows an offline-first architecture:

1. **Room Database is Single Source of Truth**: All UI reads from Room
2. **Immediate Writes**: User actions write to Room first
3. **Background Sync**: Firestore sync happens asynchronously
4. **Smart Caching**: Time-based sync intervals with stale detection
5. **Network Awareness**: Automatic sync when network available

**Repository Pattern**:
```kotlin
// ✅ CORRECT: Offline-first
fun getAllContent(): Flow<Resource<List<Content>>> = flow {
    // 1. Emit cached data immediately
    val cached = contentDao.getAllPrograms()
    if (cached.isNotEmpty()) emit(Resource.success(cached))

    // 2. Sync from Firestore if needed
    if (shouldSync()) {
        syncFromFirestore()
        val fresh = contentDao.getAllPrograms()
        emit(Resource.success(fresh))
    }
}

// ❌ WRONG: Don't bypass Room cache
fun getContent(): Flow<List<Content>> {
    return firestore.collection("content").snapshots() // NO!
}
```

### Firebase/Firestore Best Practices

**IMPORTANT:** When working with Firestore models in Kotlin:

1. **Use regular `class`, NOT `data class`**
2. **Properties MUST be declared outside constructor**
3. **Field names MUST match Firestore schema exactly (camelCase)**
4. **Only use `@PropertyName` if Firestore uses snake_case (our schema uses camelCase)**
5. **All persisted properties must be `var` (not `val`)**
6. **Computed methods must have `@Exclude`**
7. **Always provide a no-arg constructor**

**CRITICAL:** Our Firestore schema uses **camelCase** field names (firstName, photoUrl, planTier, etc.) so Kotlin models should match exactly without `@PropertyName` annotations.

See detailed guide: [docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md](docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md)

For troubleshooting: [docs/FIRESTORE_TROUBLESHOOTING.md](docs/FIRESTORE_TROUBLESHOOTING.md)

For offline support: [docs/OFFLINE_SUPPORT_GUIDE.md](docs/OFFLINE_SUPPORT_GUIDE.md)

## Build Commands

- **Build**: `./gradlew build`
- **Debug**: `./gradlew assembleDebug`
- **Install**: `./gradlew installDebug`
- **Clean Build**: `./gradlew clean assembleDebug installDebug`
- **Test**: `./gradlew test`
- **Lint**: `./gradlew lint`

## Firebase Commands

- **Deploy Firestore Rules**: `firebase deploy --only firestore:rules`
- **Deploy All**: `firebase deploy`
- **Login**: `firebase login`

## Key Dependencies

- Compose BOM: 2023.10.01
- Hilt: 2.48.1
- Navigation Compose: 2.7.6
- **Firebase BOM: 33.7.0**
  - Firebase Auth
  - Firestore
  - Google Play Services Auth (for Credential Manager)
- **Room: 2.6.1** (local database)
- ExoPlayer: 1.2.0
- Retrofit: 2.9.0
- **Work Manager: 2.9.0** (background sync)
- Timber: 5.0.1
- Credentials: 1.5.0-beta01 (Google Sign-In)

## Next Development Priorities

1. **Integrate Offline Repositories**: Update ViewModels to use offline-first repositories
2. **UI Sync Indicators**: Show offline/syncing/synced status in UI
3. **Profile Editing**: Allow users to update their profile (firstName, lastName, motto, photoUrl)
4. **Content Management**: Add meditation sessions, yoga videos to Firestore
5. **Content Player**: ExoPlayer integration for meditation/yoga videos
6. **Gratitude Journal**: Use offline-first repository (already implemented)
7. **Programs**: Implement offline program tracking
8. **Google Play Billing**: Premium subscription flow
9. **Notifications**: WorkManager for daily gratitude reminders
10. **Domain Layer**: Create use cases and repository interfaces

## Offline Support Features

### Smart Caching
- **Content Catalog**: Syncs every hour, cached locally
- **Gratitude Entries**: Written locally, synced to cloud when online
- **User Profile**: Cached locally, Firestore is source of truth
- **User Stats**: Computed locally, synced to cloud

### Background Sync
- **Periodic Sync**: Every 1 hour via WorkManager
- **On Network Connect**: Automatic sync when network available
- **Manual Sync**: Pull-to-refresh trigger
- **Battery Aware**: Only syncs when battery not low

### Network Monitoring
- Real-time connectivity detection
- Network type identification (WiFi, Cellular, Ethernet)
- Reactive Flow API for UI updates

### Sync Status
```kotlin
sealed class SyncState {
    object Idle, Syncing, Synced, Offline
    data class Error(val message: String)
}
```

## Documentation

- **Firebase Setup**: [docs/FIRESTORE_SETUP_GUIDE.md](docs/FIRESTORE_SETUP_GUIDE.md)
- **Firestore Kotlin Mapping**: [docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md](docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md)
- **Offline Support**: [docs/OFFLINE_SUPPORT_GUIDE.md](docs/OFFLINE_SUPPORT_GUIDE.md)
- **Troubleshooting**: [docs/FIRESTORE_TROUBLESHOOTING.md](docs/FIRESTORE_TROUBLESHOOTING.md)
- **Test Checklist**: [docs/FIRESTORE_TEST_CHECKLIST.md](docs/FIRESTORE_TEST_CHECKLIST.md)
- **Design System**: [docs/DESIGN_SYSTEM_SUMMARY.md](docs/DESIGN_SYSTEM_SUMMARY.md)

## Recent Changes

### Offline Support Implementation (2025-10-11)

Added complete offline support:
- ✅ Room database v2 with 11 entities
- ✅ All DAOs with comprehensive queries
- ✅ Type converters for all data types
- ✅ Database migrations (v1 → v2)
- ✅ Offline-first repositories (Content, Gratitude)
- ✅ Background sync with WorkManager
- ✅ Network monitoring
- ✅ Smart caching with sync intervals
- ✅ Unit tests for DAOs
- ✅ Complete documentation

See full report: [reports/tech-android/offline-support-implementation.md](reports/tech-android/offline-support-implementation.md)
