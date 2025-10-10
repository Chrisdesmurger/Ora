# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Ora is a Android wellbeing application built with Jetpack Compose, implementing clean architecture principles with MVVM pattern. The app focuses on meditation, yoga, journaling, and personal development programs.

## Architecture

### Clean Architecture Layers
- **Presentation Layer**: UI (Compose) + ViewModels + Navigation
- **Domain Layer**: Use Cases + Repository Interfaces + Entities (planned)
- **Data Layer**: Repository Implementations + Data Sources + Room Database (planned)

### Tech Stack
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **DI**: Hilt (Dagger)
- **Navigation**: Navigation Compose
- **Backend**: Firebase (Authentication + Firestore)
- **Database**: Room (configured) + Firestore (active)
- **Networking**: Retrofit + OkHttp (configured)
- **Media**: ExoPlayer (configured)
- **Async**: Coroutines + Flow
- **Work Manager**: For background tasks and notifications

## Application Structure

### Main Features
1. **🏠 Home (Accueil)**: Recommendations, quick sessions, active programs, weekly stats
2. **📚 Library (Bibliothèque)**: Content catalog with filtering and search
3. **📖 Journal**: Daily gratitudes with streak tracking
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

#### 🚧 In Progress / TODO
- Domain layer (Use Cases, Repository interfaces)
- Data layer (Room database for offline cache)
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
├── domain/ (🚧 Planned)
├── data/
│   ├── model/
│   │   ├── UserProfile.kt (✅ Firestore model)
│   │   └── UserStats.kt (✅ Firestore model)
│   ├── repository/
│   │   ├── AuthRepository.kt (✅ Firebase Auth)
│   │   ├── UserProfileRepository.kt (✅ Firestore)
│   │   └── UserStatsRepository.kt (✅ Firestore)
│   └── sync/
│       └── SyncManager.kt (✅ Auto profile/stats creation)
└── di/
    ├── FirebaseModule.kt (✅ Firebase DI)
    └── FirestoreModule.kt (✅ Firestore DI)
```

## Design Principles

### UI/UX
- Material 3 Design with custom Ora color scheme
- Warm color palette: Orange coral (#F18D5C), Peach (#F5C9A9), Warm beige (#F5EFE6)
- Accessible components with proper contrast
- Smooth animations and transitions
- Responsive layout for different screen sizes

### Code Quality
- SOLID principles
- Clean Architecture
- Separation of concerns
- Testable code structure
- Proper error handling
- Timber logging

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
- Room: 2.6.1
- ExoPlayer: 1.2.0
- Retrofit: 2.9.0
- Work Manager: 2.9.0
- Timber: 5.0.1
- Credentials: 1.5.0-beta01 (Google Sign-In)

## Next Development Priorities

1. **Profile Editing**: Allow users to update their profile (firstName, lastName, motto, photoUrl)
2. **Content Management**: Add meditation sessions, yoga videos to Firestore
3. **Content Player**: ExoPlayer integration for meditation/yoga videos
4. **Gratitude Journal**: Persist gratitudes to Firestore with real-time sync
5. **Programs**: Structured challenges and learning paths in Firestore
6. **Google Play Billing**: Premium subscription flow
7. **Notifications**: WorkManager for daily gratitude reminders
8. **Domain Layer**: Create use cases and repository interfaces
9. **Offline Support**: Room database for offline caching

## Git Automation

Ce projet inclut un système d'automatisation Git complet via Claude Code pour faciliter le workflow de développement.

### 🤖 Agent Git Automation

Un agent spécialisé (`git-automation`) est disponible dans `.claude/agents/55_git-automation.md` pour automatiser :
- ✅ Création de commits avec messages conventionnels (Conventional Commits)
- ✅ Création automatique de branches feature/fix
- ✅ Push vers le dépôt GitHub (remote `ora`)
- ✅ Création de Pull Requests avec template complet

### 📝 Utilisation

Après avoir terminé une fonctionnalité ou un fix, demandez simplement à Claude :

```
"Peux-tu créer un commit et une PR pour cette fonctionnalité ?"
"Automatise le commit et push pour ce fix"
"Crée une PR pour les changements que je viens de faire"
```

Claude analysera automatiquement les changements et créera :
1. Une branche appropriée (ex: `feat/profile-editing`, `fix/auth-crash`)
2. Un commit avec message conventionnel (ex: `feat(profile): add profile editing`)
3. Un push vers GitHub
4. Une Pull Request avec description complète

### 🛠️ Script PowerShell

Un script helper est disponible pour automatisation manuelle :

```powershell
# Nouvelle fonctionnalité
.\scripts\auto-commit-pr.ps1 -Type "feat" -Scope "profile" -Message "Add profile editing"

# Correction de bug
.\scripts\auto-commit-pr.ps1 -Type "fix" -Scope "auth" -Message "Fix login crash"

# Avec issue GitHub
.\scripts\auto-commit-pr.ps1 -Type "fix" -Scope "firestore" -Message "Fix sync" -IssueNumber "42"
```

### 📚 Convention de commits

| Type | Emoji | Description | Exemple |
|------|-------|-------------|---------|
| `feat` | 🚀 | Nouvelle fonctionnalité | `feat(auth): add Google Sign-In` |
| `fix` | 🐛 | Correction de bug | `fix(profile): resolve crash` |
| `refactor` | 🔧 | Refactoring | `refactor(home): simplify logic` |
| `test` | ✅ | Tests | `test(auth): add unit tests` |
| `docs` | 📚 | Documentation | `docs(readme): update steps` |
| `style` | 🎨 | UI/Formatage | `style(theme): update colors` |
| `perf` | ⚡ | Performance | `perf(library): lazy load images` |
| `chore` | 🛠️ | Maintenance | `chore(deps): update Firebase` |

**Scopes disponibles** : `auth`, `profile`, `home`, `library`, `journal`, `programs`, `firestore`, `ui`, `theme`, `di`, `build`, `navigation`

### 📖 Documentation complète

Voir le guide complet : [docs/GIT_AUTOMATION.md](docs/GIT_AUTOMATION.md)

## Documentation

- **Git Automation**: [docs/GIT_AUTOMATION.md](docs/GIT_AUTOMATION.md) 🆕
- **Firebase Setup**: [docs/FIRESTORE_SETUP_GUIDE.md](docs/FIRESTORE_SETUP_GUIDE.md)
- **Firestore Kotlin Mapping**: [docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md](docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md)
- **Troubleshooting**: [docs/FIRESTORE_TROUBLESHOOTING.md](docs/FIRESTORE_TROUBLESHOOTING.md)
- **Test Checklist**: [docs/FIRESTORE_TEST_CHECKLIST.md](docs/FIRESTORE_TEST_CHECKLIST.md)
- **Design System**: [docs/DESIGN_SYSTEM_SUMMARY.md](docs/DESIGN_SYSTEM_SUMMARY.md)
