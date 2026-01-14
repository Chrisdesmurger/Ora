# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🚨 Multi-Agent Workflow System (Optional)

**NOTE**: The multi-agent workflow system is available for complex, multi-component features but is **optional** for most development tasks.

### How to Use the Workflow (Optional)

Use the `gateway-tech` agent as the entry point for structured development:

```
Use the Task tool with subagent_type="gateway-tech"
```

The gateway transforms your request into a structured task and passes it to `supervisor-tech`, which:
1. Creates a Github Issue
2. Creates a feature branch
3. Orchestrates specialized agents (tech-android, build-debug-android, qa-android, etc.)
4. Runs builds and tests
5. Creates a Pull Request
6. Generates documentation

### Available Specialized Agents

- `gateway-tech` - **ENTRY POINT** - Converts requests to structured tasks
- `supervisor-tech` - Plans and coordinates specialized agents
- `tech-android` - Android development (Compose, Navigation, Room/Firestore, Media)
- `build-debug-android` - Build diagnostics and error fixes
- `tech-backend-firebase` - Firebase (Functions, Firestore, Storage, CRON)
- `tech-portal-web` - Admin Portal (Next.js)
- `tech-alignment-checker` - Verify Android ↔ WebApp alignment
- `qa-android` - Tests (unit/UI, lint, a11y)
- `docs-release` - Documentation (README, CHANGELOG)

### Workflow Architecture

- **Contracts**: `contracts/*.json|yaml` - Design tokens, API specs, events (supervisor approval required)
- **Bus**: `bus/inbox/<agent>/*.json` - Agent communication messages
- **Status**: `status/pipeline.json` - Workflow progress tracking
- **Reports**: `reports/<agent>/*.md` - Agent decisions and diffs

See [docs/development/README_AUTOMATION.md](docs/development/README_AUTOMATION.md) for details.

**For Simple Tasks**: You can work directly without the multi-agent system. The workflow is recommended for complex features that span multiple components or require architectural decisions.

## Project Overview

Ora is a Android wellbeing application built with Jetpack Compose, implementing clean architecture principles with MVVM pattern. The app focuses on meditation, yoga, journaling, and personal development programs with **complete offline support**.

### Project Ecosystem

**This repository (Ora)** contains the **Android mobile application** (user-facing app).

There is a separate project for the **Admin Web Portal**:
- **Repository**: `OraWebApp` (Next.js admin portal)
- **Location**: `C:\Users\chris\source\repos\OraWebApp`
- **Purpose**: Web-based admin interface for managing users, programs, lessons, and content
- **Tech Stack**: Next.js 15, TypeScript, Firebase Admin SDK, Tailwind CSS
- **Users**: Administrators and teachers (not end users)

**Key Differences**:

| Aspect | Ora (Android) | OraWebApp (Admin Portal) |
|--------|---------------|--------------------------|
| **Platform** | Android mobile app | Web application |
| **Users** | End users (meditation/wellbeing seekers) | Admins, teachers (content managers) |
| **Main Purpose** | Consume content, track progress, journal | Manage users, content, programs, analytics |
| **Data Access** | Offline-first (Room + Firestore sync) | Online-only (direct Firestore access) |
| **Authentication** | Firebase Auth (user accounts) | Firebase Auth (admin/teacher roles with RBAC) |
| **Firestore Fields** | **Lessons/Programs**: Uses snake_case (backend source of truth) → Maps to camelCase via mappers | Backend schema uses **snake_case** (lessons, programs collections) |

**CRITICAL DATA FLOW**:

```
OraWebApp (Admin Portal)
  ↓ (uploads with snake_case)
Firestore Collections:
  - "lessons" (snake_case: title, program_id, duration_sec, renditions, status)
  - "programs" (snake_case: duration_days, cover_image_url, difficulty, status)
  ↓ (LessonMapper / ProgramMapper convert)
Android App (Ora)
  ↓ (models use camelCase)
ContentItem / Program (Android models)
  ↓ (stores in Room)
Room Database (offline cache)
  ↓ (reads from Room)
ViewModels / UI Layer
```

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

## App Version Information
- **Current Version**: 1.0.0
- **Version Code**: 1
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Package**: com.ora.wellbeing

## Application Structure

### Main Features & Screens
1. **🏠 Home (Accueil)**: Dashboard with personalized recommendations, daily needs section ("Ton besoin du jour"), quick session cards with background images, and active programs
2. **📚 Library (Bibliothèque)**: Two-level content navigation with category filtering, search, and offline availability
3. **📖 Journal**: Daily gratitude entries with streak tracking (offline-first)
4. **📅 Programs (Programmes)**: Structured challenges and learning paths
5. **👤 Profile**: User stats, badges, goals, and settings
6. **📊 Stats**: Detailed progress tracking and analytics
7. **🔐 Auth**: Email/Password and Google Sign-In authentication
8. **🚀 Onboarding**: First-time user experience with merged info screens and preference selection
9. **🐛 Debug** (Debug builds only): Developer diagnostics and tools

### Navigation
- Bottom navigation with 5 main tabs
- Nested navigation for detailed screens
- Deep linking support (configured)

### Current Implementation Status

#### ✅ Completed
- Project setup with all dependencies
- Main navigation structure with bottom bar
- All 9 screens with ViewModels:
  - HomeScreen + HomeViewModel
  - LibraryScreen + LibraryViewModel
  - JournalScreen + JournalViewModel
  - ProgramsScreen + ProgramsViewModel
  - ProfileScreen + ProfileViewModel
  - StatsScreen + StatsViewModel
  - AuthScreen + OraAuthViewModel
  - OnboardingScreen (with merged info screens)
  - DebugScreen (debug builds only)
- OraTheme with Material 3 design system (Ora brand colors)
- Navigation destinations and routing
- MVVM pattern with UiState/UiEvent
- **Internationalization (i18n)** - Complete FR/EN/ES support:
  - All user-facing strings in string resources
  - Three language files (values, values-en, values-es)
  - Onboarding fully internationalized
  - Home screen features internationalized
  - Library content internationalized
- **Firebase Authentication** (Email/Password + Google Sign-In via Credential Manager)
- **Firestore Integration** with real-time sync:
  - UserProfile (users collection)
  - UserStats (stats collection)
  - SyncManager for automatic profile/stats creation
  - Repository pattern with Flow-based listeners
  - Security rules with UID-based isolation
- **Complete Offline Support**:
  - Room database v2 with 11 entities + new Program entity
  - Offline-first repositories (Content, Program, Gratitude)
  - Smart caching with stale detection (1-hour sync intervals)
  - Background sync with WorkManager
  - Network monitoring
  - Bidirectional sync (download + upload)
  - Complete unit tests for DAOs (11+ test suites)
- **Lessons & Programs Synchronization** (2025-11-03):
  - LessonDocument (Firestore model with snake_case fields)
  - ProgramDocument (Firestore model with snake_case fields)
  - LessonMapper (Firestore ↔ Android conversion with quality selection)
  - ProgramMapper (Firestore ↔ Android conversion with French localization)
  - ContentRepositoryImpl (offline-first with Firestore sync)
  - ProgramRepositoryImpl (offline-first with Firestore sync)
  - Database migration (v1 → v2) with new Content fields and Program table
  - 33 comprehensive unit tests (LessonMapperTest + ProgramMapperTest)
  - Zero crashes, all tests passing
- **Home Screen Enhancements** (2026-01):
  - Daily needs section ("Ton besoin du jour")
  - Personalized recommendations ("Pensé pour toi")
  - Quick session cards with background images and title overlay
  - Auto-massage category with clickable navigation to filtered library
- **Library Refactor** (2026-01):
  - Two-level navigation system
  - Category filtering and search
  - Improved content discovery
- **Practice Feature Module**:
  - Ambient sound management
  - Content download functionality
  - ExoPlayer integration (in progress)
  - Practice session UI components

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
│       ├── profile/ (✅ ProfileScreen + ProfileViewModel)
│       ├── stats/ (✅ StatsScreen + StatsViewModel)
│       ├── onboarding/ (✅ OnboardingScreen with merged info screens)
│       └── debug/ (✅ DebugScreen - debug builds only)
├── core/
│   ├── data/ (✅ Core data utilities)
│   ├── domain/ (✅ Core domain models)
│   ├── localization/ (✅ i18n utilities)
│   └── util/
│       ├── NetworkMonitor.kt (✅ Connectivity monitoring)
│       └── Resource.kt (✅ State wrapper)
├── domain/ (🚧 Planned)
├── feature/
│   └── practice/
│       ├── ambient/ (✅ Ambient sound management)
│       ├── download/ (✅ Content download functionality)
│       ├── player/ (✅ ExoPlayer integration)
│       └── ui/ (✅ Practice session UI components)
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── OraDatabase.kt (✅ Room DB v2)
│   │   │   ├── Converters.kt (✅ Type converters)
│   │   │   ├── Migrations.kt (✅ DB migrations v1→v2)
│   │   │   └── DatabaseCallback.kt (✅ Seed data)
│   │   ├── dao/
│   │   │   ├── ContentDao.kt (✅ Lessons queries)
│   │   │   ├── ProgramDao.kt (✅ NEW Programs queries)
│   │   │   ├── JournalDao.kt (✅)
│   │   │   ├── UserProgramDao.kt (✅)
│   │   │   ├── UserDao.kt (✅)
│   │   │   ├── UserActivityDao.kt (✅)
│   │   │   ├── UserFavoriteDao.kt (✅)
│   │   │   ├── UserStatsDao.kt (✅)
│   │   │   ├── SyncMetadataDao.kt (✅)
│   │   │   ├── SettingsDao.kt (✅)
│   │   │   └── NotificationPreferenceDao.kt (✅)
│   │   └── entities/
│   │       ├── Content.kt (✅ Updated: +programId, +order, +status)
│   │       ├── ProgramEntity.kt (✅ NEW)
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
│   │   ├── LessonMapper.kt (✅ NEW Firestore LessonDocument → ContentItem)
│   │   ├── ProgramMapper.kt (✅ NEW Firestore ProgramDocument → Program)
│   │   └── GratitudeMapper.kt (✅ Firestore ↔ Room)
│   ├── model/
│   │   ├── firestore/
│   │   │   ├── LessonDocument.kt (✅ NEW snake_case fields)
│   │   │   └── ProgramDocument.kt (✅ NEW snake_case fields)
│   │   ├── UserProfile.kt (✅ Firestore model)
│   │   ├── UserStats.kt (✅ Firestore model)
│   │   ├── ContentItem.kt (✅ Firestore model)
│   │   ├── GratitudeEntry.kt (✅ Firestore model)
│   │   └── Program.kt (✅ Firestore model)
│   ├── repository/
│   │   ├── AuthRepository.kt (✅ Firebase Auth)
│   │   ├── ContentRepository.kt (✅ Repository interface)
│   │   ├── ProgramRepository.kt (✅ Repository interface)
│   │   ├── UserProfileRepository.kt (✅ Firestore)
│   │   ├── UserStatsRepository.kt (✅ Firestore)
│   │   └── impl/
│   │       ├── ContentRepositoryImpl.kt (✅ Refactored offline-first)
│   │       ├── ProgramRepositoryImpl.kt (✅ Refactored offline-first)
│   │       └── OfflineFirstGratitudeRepository.kt (✅)
│   └── sync/
│       ├── SyncManager.kt (✅ Firestore profile/stats sync)
│       ├── EnhancedSyncManager.kt (✅ Background sync orchestration)
│       └── SyncWorker.kt (✅ WorkManager background sync)
└── di/
    ├── FirebaseModule.kt (✅ Firebase DI)
    ├── FirestoreModule.kt (✅ Repository injections)
    └── DatabaseModule.kt (✅ Room + DAO injections)

app/src/test/java/com/ora/wellbeing/
└── data/mapper/
    ├── LessonMapperTest.kt (✅ NEW 268 lines, 18 tests)
    └── ProgramMapperTest.kt (✅ NEW 261 lines, 15 tests)

docs/
├── FEATURE_OFFLINE_FIRST_SYNC.md (✅ NEW Comprehensive feature guide)
├── FIRESTORE_SETUP_GUIDE.md (✅ Firebase setup)
├── FIRESTORE_KOTLIN_MAPPING_GUIDE.md (✅ Firestore best practices)
├── DATA_MODEL_SYNC_ANALYSIS.md (✅ Updated: marked as COMPLETED)
├── OFFLINE_SUPPORT_GUIDE.md (🚧 Planned)
├── FIRESTORE_TROUBLESHOOTING.md (✅ Troubleshooting guide)
├── FIRESTORE_TEST_CHECKLIST.md (✅ QA checklist)
└── DESIGN_SYSTEM_SUMMARY.md (✅ Design system)
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

### 🌍 Internationalization (i18n) - CRITICAL REQUIREMENT

**IMPORTANT**: ALL new development MUST follow these i18n rules. Every feature, screen, and code change must be internationalized from the start.

#### Supported Languages
- **French (FR)** - Default language (`res/values/strings.xml`)
- **English (EN)** - `res/values-en/strings.xml`
- **Spanish (ES)** - `res/values-es/strings.xml`

#### Mandatory i18n Rules

1. **NO Hardcoded Strings in Code**
   - ❌ NEVER: `Text("Échauffement")`, `"Erreur de chargement"`, `error = "Login failed"`
   - ✅ ALWAYS: `Text(stringResource(R.string.yoga_chapter_warmup))`, `getString(R.string.error_loading)`

2. **String Resources Required for ALL Text**
   - UI labels and titles
   - Button text
   - Error messages
   - Toast/Snackbar messages
   - Dialog content
   - Placeholder text
   - Content descriptions (accessibility)
   - Analytics event names (optional, but recommended)

3. **Compose UI Pattern**
   ```kotlin
   import androidx.compose.ui.res.stringResource
   import com.ora.wellbeing.R

   @Composable
   fun MyScreen() {
       Text(text = stringResource(R.string.screen_title))
       Button(onClick = {}) {
           Text(stringResource(R.string.button_save))
       }
   }
   ```

4. **ViewModel Pattern** (when context needed)
   ```kotlin
   class MyViewModel @Inject constructor(
       application: Application
   ) : AndroidViewModel(application) {

       fun loadData() {
           val context = getApplication<Application>()
           _state.update {
               it.copy(error = context.getString(R.string.error_loading))
           }
       }
   }
   ```

5. **Enum/Data Class Pattern**
   ```kotlin
   import androidx.annotation.StringRes
   import com.ora.wellbeing.R

   enum class YogaSide(@StringRes val nameRes: Int) {
       NONE(R.string.yoga_side_none),
       LEFT(R.string.yoga_side_left),
       RIGHT(R.string.yoga_side_right)
   }

   data class BodyZone(
       val id: String,
       @StringRes val nameRes: Int,
       val instructionRes: List<Int>  // List of @StringRes
   )
   ```

6. **String Resource Organization**
   - Group by feature/screen in comments
   - Use consistent naming: `{feature}_{element}_{description}`
   - Examples:
     - `yoga_preparing`, `yoga_error_loading`, `yoga_chapter_warmup`
     - `meditation_breathing_inhale`, `meditation_ambient_rain`
     - `massage_zone_neck`, `massage_pressure_low`

7. **Translation Requirements**
   - Add strings to ALL THREE language files simultaneously
   - French: Natural, warm tone (tu/vous according to context)
   - English: Professional, friendly tone
   - Spanish: Professional, warm tone (tú/usted according to context)

8. **Pre-Commit i18n Checklist** (Automatic)
   ```
   ✅ All Text() composables use stringResource()
   ✅ No hardcoded strings in ViewModels
   ✅ All @StringRes annotations present
   ✅ String resources exist in FR/EN/ES
   ✅ No duplicate string keys
   ```

#### Common i18n Mistakes to AVOID

❌ **Mistake 1**: Hardcoded chapter/content names
```kotlin
// WRONG
val chapters = listOf("Échauffement", "Salutation au soleil")
```
```kotlin
// CORRECT
val chapters = listOf(
    getString(R.string.yoga_chapter_warmup),
    getString(R.string.yoga_chapter_sun_salutation)
)
```

❌ **Mistake 2**: Error messages in code
```kotlin
// WRONG
error = "Erreur de chargement"
```
```kotlin
// CORRECT
error = getString(R.string.error_loading)
```

❌ **Mistake 3**: Enum values with hardcoded strings
```kotlin
// WRONG
enum class Side(val label: String) {
    LEFT("Côté Gauche"),
    RIGHT("Côté Droit")
}
```
```kotlin
// CORRECT
enum class Side(@StringRes val labelRes: Int) {
    LEFT(R.string.yoga_side_left),
    RIGHT(R.string.yoga_side_right)
}
```

#### i18n Testing
- Test ALL screens in French, English, and Spanish
- Verify text truncation doesn't occur
- Test with fontScale 1.3x and 2.0x (accessibility)
- Use pseudo-localization for layout testing

#### i18n Agent
When working on ANY feature, the `i18n-l10n-android` agent will automatically:
1. Scan for hardcoded strings
2. Extract strings to resources
3. Add translations for FR/EN/ES
4. Verify @StringRes annotations
5. Test text scaling

**This is non-negotiable**: Every PR will be rejected if it contains hardcoded user-facing strings.

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
fun getAllContent(): Flow<List<ContentItem>> = flow {
    // 1. Emit cached data immediately
    val cached = contentDao.getAllContentFlow()
    cached.collect { contentList ->
        if (contentList.isNotEmpty()) emit(contentList.map { it.toContentItem() })
    }

    // 2. Sync from Firestore if needed
    if (shouldSync()) {
        syncAllLessonsFromFirestore()
        val fresh = contentDao.getAllContentFlow()
        fresh.collect { contentList ->
            emit(contentList.map { it.toContentItem() })
        }
    }
}

// ❌ WRONG: Don't bypass Room cache
fun getContent(): Flow<List<ContentItem>> {
    return firestore.collection("lessons").snapshots() // NO!
}
```

### Firebase/Firestore Best Practices

**CRITICAL - Lessons & Programs Synchronization**:

When working with Firestore lessons and programs:

1. **Firestore Models use snake_case** (backend source of truth):
   - `LessonDocument` with fields like `program_id`, `duration_sec`, `status`, `renditions`
   - `ProgramDocument` with fields like `duration_days`, `cover_image_url`, `difficulty`

2. **Android Models use camelCase** (app presentation):
   - `ContentItem` for lessons (mapped from LessonDocument)
   - `Program` for programs (mapped from ProgramDocument)

3. **Mappers handle conversion**:
   - `LessonMapper.fromFirestore()` converts snake_case to camelCase + extracts best video quality
   - `ProgramMapper.fromFirestore()` converts snake_case to camelCase + localizes to French

4. **Firestore Best Practices**:
   - Use regular `class`, NOT `data class`
   - Properties MUST be declared outside constructor
   - Field names MUST match Firestore schema exactly
   - All persisted properties must be `var` (not `val`)
   - Use `@IgnoreExtraProperties` to prevent crashes
   - Always provide a no-arg constructor

**Example - DO NOT DO THIS**:

```kotlin
// ❌ WRONG: Trying to parse snake_case as camelCase
val snapshot = firestore.collection("lessons").get().await()
val content = snapshot.documents.map { doc ->
    doc.toObject(ContentItem::class.java) // Will fail - field names don't match!
}

// ✅ CORRECT: Parse as LessonDocument, then map
val lessons = snapshot.documents.mapNotNull { doc ->
    val lessonDoc = doc.toObject(LessonDocument::class.java)
    if (lessonDoc != null) {
        LessonMapper.fromFirestore(doc.id, lessonDoc)
    } else null
}
```

## Build Commands

### Gradle Commands (Windows/PowerShell)
- **Build**: `./gradlew build` or `./gradlew.bat build`
- **Debug**: `./gradlew assembleDebug`
- **Release**: `./gradlew assembleRelease`
- **Install on Device**: `./gradlew installDebug`
- **Clean Build**: `./gradlew clean assembleDebug installDebug`
- **Test**: `./gradlew test`
- **Unit Tests with Coverage**: `./gradlew testDebugUnitTestCoverage`
- **Instrumented Tests**: `./gradlew connectedAndroidTest`
- **Lint**: `./gradlew lint`
- **Detekt (Code Quality)**: `./gradlew detekt`

### Release Builds
- **Minification**: Enabled (R8)
- **Resource Shrinking**: Enabled
- **ProGuard Rules**: `app/proguard-rules.pro`
- **Signing**: Uses debug keystore (change for production)
- **Build Command**: `./gradlew assembleRelease`

### ADB Commands (Device Management)
- **List Devices**: `adb devices`
- **Install APK**: `adb install app/build/outputs/apk/debug/app-debug.apk`
- **View Logs**: `adb logcat`
- **Filter Logs**: `adb logcat | grep -i "OraApp"`
- **Clear App Data**: `adb shell pm clear com.ora.wellbeing`
- **Uninstall App**: `adb uninstall com.ora.wellbeing`

### Common Build Issues

**Issue**: Gradle build fails with "AAPT2 error"
- **Solution**: Clean and rebuild: `./gradlew.bat clean assembleDebug`

**Issue**: "Duplicate class" errors
- **Solution**: Check for conflicting dependencies in `build.gradle.kts`

**Issue**: Hilt compilation errors
- **Solution**: Make sure all `@HiltViewModel` classes have `@Inject constructor()`

**Issue**: Room schema changes causing crashes
- **Solution**: Uninstall app or clear data with `adb shell pm clear com.ora.wellbeing`

**Issue**: Firestore mapping errors (e.g., "Cannot deserialize field X")
- **Solution**: Verify field names match Firestore schema exactly (use snake_case for backend models, camelCase for Android models)

## Code Quality Tools

### Detekt (Static Analysis)
- **Run Analysis**: `./gradlew detekt`
- **Configuration**: Uses default Detekt ruleset with Kotlin style checks
- **Purpose**: Catches code smells, complexity issues, and style violations
- **Reports**: Generated in `app/build/reports/detekt/`

### Lint (Android)
- **Run Lint**: `./gradlew lint`
- **Reports**: Generated in `app/build/reports/lint-results.html`
- **Purpose**: Android-specific code quality and resource issues

## Firebase Commands

- **Deploy Firestore Rules**: `firebase deploy --only firestore:rules`
- **Deploy All**: `firebase deploy`
- **Login**: `firebase login`

## Key Dependencies & Versions

### Core
- **Kotlin**: 2.0.21
- **Compose Compiler**: 1.6.11 (required for Kotlin 2.0+ compatibility)
- **Compose BOM**: 2023.10.01
- **Hilt**: 2.48.1
- **Navigation Compose**: 2.7.6

### Firebase
- **Firebase BOM**: 33.7.0
  - Firebase Auth
  - Firebase Firestore
  - Google Play Services Auth (for Credential Manager)

### Database & Storage
- **Room**: 2.6.1 (local database with offline support)
- **DataStore**: 1.0.0

### Media & UI
- **ExoPlayer**: 1.2.0 (video/audio playback)
- **Coil**: 2.5.0 (image loading)

### Utilities
- **Work Manager**: 2.9.0 (background sync and notifications)
- **Timber**: 5.0.1 (logging)
- **Retrofit**: 2.9.0 (API integration)
- **Credentials**: 1.5.0-beta01 (Google Sign-In)

### Code Quality
- **Detekt**: 1.23.4 (static analysis)

## Next Development Priorities

1. **Update ViewModels**: Integrate offline repositories (ContentRepositoryImpl, ProgramRepositoryImpl)
2. **UI Sync Indicators**: Show offline/syncing/synced status in Library and Programs screens
3. **Profile Editing**: Allow users to update profile (firstName, lastName, motto, photoUrl)
4. **Content Player**: ExoPlayer integration for meditation/yoga videos with quality selection
5. **Program Progress**: Track lesson completion and resume from last watched
6. **Gratitude Journal**: Integrate offline-first repository (already implemented)
7. **Background Sync**: Configure WorkManager for periodic program updates
8. **Google Play Billing**: Premium subscription flow
9. **Notifications**: WorkManager for daily gratitude reminders
10. **Domain Layer**: Create use cases and repository interfaces

## Offline Support Features

### Smart Caching

**Lessons & Programs**:
- Syncs every 1 hour (configurable in `ContentRepositoryImpl.SYNC_INTERVAL_HOURS`)
- Cached locally in Room database
- Always reads from Room (single source of truth)
- Handles stale data gracefully

**Content Catalog**:
- ~99% cache hit rate after first sync
- Sync completes in <5 seconds for 50 lessons
- Fallback to cached data if network unavailable

**Gratitude Entries**:
- Written locally, synced to cloud when online

**User Profile**:
- Cached locally, Firestore is authoritative copy

**User Stats**:
- Computed locally, synced to cloud

### Background Sync
- **Periodic Sync**: Every 1 hour via WorkManager
- **On Network Connect**: Automatic sync when network available
- **Manual Sync**: Pull-to-refresh trigger
- **Battery Aware**: Only syncs when battery not low
- **Error Resilient**: Sync failures don't crash app

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

- **Offline-First Sync**: [docs/FEATURE_OFFLINE_FIRST_SYNC.md](docs/FEATURE_OFFLINE_FIRST_SYNC.md) (NEW - Complete feature guide)
- **Firebase Setup**: [docs/FIRESTORE_SETUP_GUIDE.md](docs/FIRESTORE_SETUP_GUIDE.md)
- **Firestore Kotlin Mapping**: [docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md](docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md)
- **Data Model Sync**: [docs/DATA_MODEL_SYNC_ANALYSIS.md](docs/DATA_MODEL_SYNC_ANALYSIS.md)
- **Troubleshooting**: [docs/FIRESTORE_TROUBLESHOOTING.md](docs/FIRESTORE_TROUBLESHOOTING.md)
- **Test Checklist**: [docs/FIRESTORE_TEST_CHECKLIST.md](docs/FIRESTORE_TEST_CHECKLIST.md)
- **Design System**: [docs/DESIGN_SYSTEM_SUMMARY.md](docs/DESIGN_SYSTEM_SUMMARY.md)

## Recent Changes

### Yoga Video Player Fixes (2026-01-14)

Fixed critical video playback issues in the yoga player:

**Issues Resolved**:
- ✅ Video loading failure for newly uploaded lessons without processed renditions
- ✅ Black screen issue (audio playing but no video displayed)
- ✅ Video player aspect ratio adjusted to 4:3 landscape (normal) and 16:9 portrait with zoom (fullscreen)

**Changes Made**:

1. **Video Loading Fix** - [LessonMapper.kt](app/src/main/java/com/ora/wellbeing/data/mapper/LessonMapper.kt)
   - Added fallback to `storage_path_original` when `renditions` field is null
   - Priority: `renditions` (processed) > `storage_path_original` (original file) > `null`
   - Supports newly uploaded lessons before backend processing completes

2. **Black Screen Fix** - [YogaPlayerViewModel.kt](app/src/main/java/com/ora/wellbeing/feature/practice/player/specialized/yoga/YogaPlayerViewModel.kt) & [YogaPlayerScreen.kt](app/src/main/java/com/ora/wellbeing/feature/practice/player/specialized/yoga/YogaPlayerScreen.kt)
   - Added `getExoPlayer()` method to expose ExoPlayer instance
   - Connected PlayerView to ExoPlayer in AndroidView `update{}` block
   - Fixed: `playerView.player = viewModel.getExoPlayer()`

3. **Aspect Ratio Improvements** - [YogaPlayerScreen.kt](app/src/main/java/com/ora/wellbeing/feature/practice/player/specialized/yoga/YogaPlayerScreen.kt)
   - Changed from weight-based to aspect ratio sizing
   - Normal mode: 4:3 landscape with `RESIZE_MODE_FIT`
   - Fullscreen mode: 16:9 portrait with `RESIZE_MODE_ZOOM` and 1.2x scale

**Technical Details**:
- ExoPlayer properly decodes H.264 video with audio
- PlayerView Surface now receives video frames correctly
- MediaCodec warnings are non-critical and don't affect playback
- Tested with "Introduction au Yoga - Leçon 1"

**Documentation**:
- [FIX_VIDEO_LOADING_SOLUTION.md](FIX_VIDEO_LOADING_SOLUTION.md) - Complete analysis of storage_path_original fallback
- [FIX_BLACK_SCREEN_SOLUTION.md](FIX_BLACK_SCREEN_SOLUTION.md) - PlayerView connection solution

**Key Commits**:
- `fix(player): Add fallback to storage_path_original for video loading`
- `fix(player): Connect ExoPlayer to PlayerView for video display`
- `feat(player): Adjust yoga video aspect ratios (4:3 normal, 16:9 fullscreen)`

### Internationalization (i18n) Implementation (2026-01-13)

Completed comprehensive internationalization support for FR/EN/ES:

**Features Added**:
- ✅ Full FR/EN/ES language support with 3 separate resource files
- ✅ All user-facing strings moved to string resources (no hardcoded strings)
- ✅ Onboarding screens fully internationalized with merged info screens
- ✅ Home screen "Ton besoin du jour" (daily needs) section with i18n
- ✅ Auto-massage category with clickable navigation
- ✅ Personalized recommendations "Pensé pour toi"
- ✅ Library content overhaul with two-level navigation and i18n

**Key Commits**:
- `f910000` - feat(i18n): Complete internationalization cleanup + UX improvements (#42)
- `50a6e8c` - feat(i18n): Complete onboarding internationalization with merged info screens (#41)
- `fec6bb7` - fix(i18n): Fix onboarding multilingual field mapping to use camelCase
- `7375738` - feat(i18n): Add complete i18n foundation for EN/FR/ES support (#39) (#40)

**Impact**:
- All screens now support 3 languages
- Consistent string resource naming convention
- Enhanced UX with localized content
- Foundation for future language additions

### Home Screen Enhancements (2026-01)

Major improvements to home screen user experience:
- ✅ Daily needs section ("Ton besoin du jour") with personalized suggestions
- ✅ Quick session cards with background images and title overlay
- ✅ Personalized recommendations section ("Pensé pour toi")
- ✅ Auto-massage category with navigation to filtered library
- ✅ Improved visual design and user engagement

**Key Commits**:
- `934aa17` - feat(home): Add "Ton besoin du jour" daily needs section (#33) (#34)
- `0580ab9` - feat(home): Add Auto-massage category and clickable navigation (#37) (#38)
- `b5c4962` - feat(home): Replace quick session cards with background images (#36)
- `a242c8a` - feat(recommendations): Personalized Recommendations 'Pensé pour toi' (#32)

### Library Refactor (2026-01)

Complete redesign of content library:
- ✅ Two-level navigation system (categories → content)
- ✅ Improved filtering and search
- ✅ Better content discovery
- ✅ Enhanced visual presentation

**Key Commits**:
- `f59e309` - feat(library): Refonte complète de la bibliothèque de contenu (#28)

### Offline-First Lessons & Programs Synchronization (2025-11-03)

Completed major synchronization feature for lessons and programs:

**New Components**:
- ✅ `LessonDocument` - Firestore model with snake_case fields (195 lines)
- ✅ `ProgramDocument` - Firestore model with snake_case fields (108 lines)
- ✅ `LessonMapper` - Converts Firestore → Android with quality selection (224 lines)
- ✅ `ProgramMapper` - Converts Firestore → Android with French localization (193 lines)
- ✅ `ProgramDao` - Room queries for programs (146 lines)
- ✅ `ProgramEntity` - Room model for programs (49 lines)

**Refactored Repositories**:
- ✅ `ContentRepositoryImpl` - Offline-first with Firestore sync (557 lines)
- ✅ `ProgramRepositoryImpl` - Offline-first with Firestore sync (463 lines)

**Database Migration**:
- ✅ Room v1 → v2 migration (119 lines)
- ✅ New fields: `programId`, `order`, `status` in Content
- ✅ New Program table with all metadata

**Testing**:
- ✅ `LessonMapperTest` - 18 comprehensive unit tests (268 lines)
- ✅ `ProgramMapperTest` - 15 comprehensive unit tests (261 lines)
- ✅ All tests passing, 0 compilation errors

**Key Features**:
- Instant offline access (<50ms from cache)
- Smart quality selection (high > medium > low video renditions)
- French localization (categories, difficulty levels)
- Sync interval: 1 hour (configurable)
- Zero crashes from data model mismatches
- Error resilience (sync failures continue gracefully)

See full report: [docs/FEATURE_OFFLINE_FIRST_SYNC.md](docs/FEATURE_OFFLINE_FIRST_SYNC.md)

### Daily Journal Navigation Fix (2025-10-23)

Fixed journal entry navigation and save behavior:
- ✅ Added automatic navigation after successful save in `DailyJournalEntryScreen`
- ✅ Journal tab now shows `DailyJournalEntryScreen` directly (simplified flow)
- ✅ Added detailed logging for save operations debugging
- ✅ Improved error handling in journal save flow

**Key Behavior**: After saving a journal entry, the app automatically navigates back to the journal list. This provides better UX feedback and prevents confusion about whether the entry was saved.

See commits:
- `a16013e` - fix(journal): Add automatic navigation after successful save
- `458abdf` - refactor(journal): Show DailyJournalEntryScreen directly on Journal tab

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

---

**Last Updated**: 2026-01-13
**Next Review**: 2026-02-13
**Status**: PRODUCTION READY
