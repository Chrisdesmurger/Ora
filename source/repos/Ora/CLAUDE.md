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
- **Database**: Room (configured)
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
- OraTheme with Material 3 design system
- Navigation destinations and routing
- Mock data for all screens
- MVVM pattern with UiState/UiEvent

#### 🚧 In Progress / TODO
- Domain layer (Use Cases, Repository interfaces)
- Data layer (Room database, Repository implementations)
- API integration (Retrofit setup exists)
- ExoPlayer integration for video/audio content
- WorkManager for evening gratitude reminders
- Detailed screens (ContentDetail, ProgramDetail, etc.)
- User authentication
- Real data persistence
- Push notifications
- Settings screens

## File Structure

```
app/src/main/java/com/ora/wellbeing/
├── MainActivity.kt (✅ Updated with navigation)
├── OraApplication.kt (✅ Hilt setup)
├── presentation/
│   ├── theme/
│   │   ├── OraTheme.kt (✅ Material 3 colors)
│   │   └── Typography.kt (✅ Text styles)
│   ├── navigation/
│   │   ├── OraDestinations.kt (✅ Route definitions)
│   │   └── OraNavigation.kt (✅ NavHost + BottomBar)
│   └── screens/
│       ├── home/ (✅ Complete)
│       ├── library/ (✅ Complete)
│       ├── journal/ (✅ Complete)
│       ├── programs/ (✅ Complete)
│       └── profile/ (✅ Complete)
├── domain/ (🚧 Planned)
└── data/ (🚧 Planned)
```

## Design Principles

### UI/UX
- Material 3 Design with custom Ora color scheme
- Zen-focused color palette (purple, teal, soft rose)
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

## Build Commands

- **Build**: `./gradlew build`
- **Debug**: `./gradlew assembleDebug`
- **Test**: `./gradlew test`
- **Lint**: `./gradlew lint`

## Key Dependencies

- Compose BOM: 2023.10.01
- Hilt: 2.48.1
- Navigation Compose: 2.7.6
- Room: 2.6.1
- ExoPlayer: 1.2.0
- Retrofit: 2.9.0
- Work Manager: 2.9.0
- Timber: 5.0.1

## Next Development Priorities

1. **Domain Layer**: Create use cases and repository interfaces
2. **Data Layer**: Implement Room database and repositories
3. **Content Player**: ExoPlayer integration for meditation/yoga videos
4. **Notifications**: WorkManager for daily gratitude reminders
5. **Authentication**: User sign-up/sign-in flow
6. **Real Data**: Replace mock data with actual backend integration