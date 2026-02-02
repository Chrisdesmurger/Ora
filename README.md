# Ora - Wellbeing Android App

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com)

Ora is a comprehensive Android wellbeing application built with Jetpack Compose, focusing on meditation, yoga, journaling, and personal development programs.

## 🌟 Features

- **🏠 Home Dashboard**: Personalized recommendations and quick access to sessions
- **📚 Content Library**: Extensive catalog of meditation and yoga content
- **📖 Gratitude Journal**: Daily journaling with streak tracking
- **📅 Programs**: Structured challenges and learning paths
- **👤 User Profile**: Track progress, badges, and personal goals
- **🔐 Authentication**: Email/Password and Google Sign-In support
- **☁️ Cloud Sync**: Real-time data synchronization with Firebase

## 🏗️ Architecture

Ora follows **Clean Architecture** principles with **MVVM** pattern:

```
┌─────────────────────────────────────┐
│  Presentation Layer (UI + ViewModels) │
├─────────────────────────────────────┤
│  Domain Layer (Use Cases + Entities)  │
├─────────────────────────────────────┤
│  Data Layer (Repositories + Sources)  │
└─────────────────────────────────────┘
```

### Tech Stack

- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt (Dagger)
- **Navigation**: Navigation Compose
- **Backend**: Firebase (Auth + Firestore)
- **Database**: Room (offline cache) + Firestore (cloud)
- **Media**: ExoPlayer
- **Async**: Kotlin Coroutines + Flow
- **Work Manager**: Background tasks & notifications

## 🚀 Quick Start

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Firebase project configured

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Chrisdesmurger/Ora.git
   cd Ora
   ```

2. **Configure Firebase**
   - Add your `google-services.json` to `app/` directory
   - Update Firebase configuration (see [Firebase Setup Guide](docs/firebase/FIRESTORE_SETUP_GUIDE.md))

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

## 📁 Project Structure

```
Ora/
├── app/                        # Android application module
│   └── src/
│       └── main/java/com/ora/wellbeing/
│           ├── presentation/   # UI layer (Compose + ViewModels)
│           ├── domain/         # Business logic (Use Cases + Entities)
│           ├── data/           # Data layer (Repositories + Sources)
│           └── di/             # Dependency injection modules
├── docs/                       # 📚 Documentation
│   ├── architecture/          # Architecture documents
│   ├── firebase/              # Firebase & Firestore guides
│   ├── development/           # Development guides
│   ├── design/                # Design system documentation
│   ├── qa/                    # QA & testing docs
│   └── archive/               # Archived/historical docs
├── config/                     # ⚙️ Configuration files
│   ├── firebase/              # Firebase config (firebase.json, indexes)
│   ├── design/                # Design tokens & contracts
│   └── feature-flags/         # Feature flags configuration
├── scripts/                    # 🔧 Utility scripts
│   ├── firebase/              # Firebase deployment scripts
│   ├── git/                   # Git automation scripts
│   └── build/                 # Build scripts
├── firebase/                   # Firebase backend (Functions, seed data)
├── qa/                         # QA test scripts
└── gradle/                     # Gradle configuration

```

## 📖 Documentation

### Getting Started
- [Setup Instructions](docs/development/SETUP_INSTRUCTIONS.md)
- [Developer Guide](docs/development/DEVELOPER_GUIDE.md)
- [Build Configuration](docs/development/build_config.md)

### Architecture & Design
- [Application Architecture](docs/architecture/app_architecture.md)
- [Technical Architecture](docs/architecture/technical_architecture.md)
- [Design System](docs/design/ORA_DESIGN_SYSTEM.md)
- [User Flows](docs/architecture/user_flows.md)

### Firebase & Backend
- [Firebase Setup Guide](docs/firebase/FIRESTORE_SETUP_GUIDE.md)
- [Firestore Collections Schema](docs/firebase/FIRESTORE_COLLECTIONS_SCHEMA.md)
- [Firestore Kotlin Mapping](docs/firebase/FIRESTORE_KOTLIN_MAPPING_GUIDE.md)
- [Firestore Troubleshooting](docs/firebase/FIRESTORE_TROUBLESHOOTING.md)

### Development
- [Git Automation](docs/development/GIT_AUTOMATION.md)
- [Feature Flags Guide](docs/development/feature_flags_guide.md)
- [Authentication Setup](docs/development/auth_setup.md)

## 🛠️ Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Lint code
./gradlew lint

# Clean build
./gradlew clean assembleDebug
```

## 🔥 Firebase Commands

```bash
# Login to Firebase
firebase login

# Deploy Firestore rules
firebase deploy --only firestore:rules

# Deploy Firestore indexes
firebase deploy --only firestore:indexes

# Deploy all
firebase deploy
```

## 🤖 Git Automation

Ora includes an automated Git workflow powered by Claude Code.

After completing a feature or fix, simply:
```bash
# Ask Claude to automate the commit and PR
"Create a commit and PR for this feature"

# Or use the PowerShell script manually
.\scripts\git\auto-commit-pr.ps1 -Type "feat" -Scope "profile" -Message "Add profile editing"
```

[Learn more about Git Automation →](docs/development/GIT_AUTOMATION.md)

## 🎨 Design System

Ora uses a warm color palette with Material 3 design:

- **Primary**: #F18D5C (Orange Coral)
- **Secondary**: #F5C9A9 (Peach)
- **Tertiary**: #A8C5B0 (Sage Green)
- **Background**: #F5EFE6 (Warm Beige)

[View full Design System →](docs/design/ORA_DESIGN_SYSTEM.md)

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Unit tests with coverage
./gradlew testDebugUnitTestCoverage

# Instrumented tests
./gradlew connectedAndroidTest

# UI tests
./gradlew connectedCheck
```

## 📦 Dependencies

### Core
- **Kotlin**: 1.9.0
- **Compose BOM**: 2023.10.01
- **Hilt**: 2.48.1
- **Navigation Compose**: 2.7.6

### Firebase
- **Firebase BOM**: 33.7.0
- **Firebase Auth**
- **Firebase Firestore**
- **Google Play Services Auth**: For Credential Manager

### Database & Storage
- **Room**: 2.6.1
- **DataStore**: 1.0.0

### Media & UI
- **ExoPlayer**: 1.2.0
- **Coil**: 2.5.0

### Utilities
- **Work Manager**: 2.9.0
- **Timber**: 5.0.1
- **Retrofit**: 2.9.0

[View complete dependency list →](docs/development/build_config.md)

## 🗺️ Roadmap

### ✅ Completed
- [x] Project setup with Clean Architecture
- [x] Firebase Authentication (Email/Password + Google)
- [x] Firestore integration with real-time sync
- [x] All 5 main screens (Home, Library, Journal, Programs, Profile)
- [x] Material 3 theme with Ora brand colors
- [x] Git automation system

### 🚧 In Progress
- [ ] Content player (ExoPlayer integration)
- [ ] Offline support with Room database
- [ ] Profile editing functionality

### 📋 Planned
- [ ] Google Play Billing for Premium subscriptions
- [ ] WorkManager for daily reminders
- [ ] Push notifications
- [ ] Content management system
- [ ] Advanced analytics
- [ ] iOS version (SwiftUI)

## 🤝 Contributing

We welcome contributions! Please see our contributing guidelines for details.

1. Fork the repository
2. Create your feature branch (`git checkout -b feat/amazing-feature`)
3. Commit your changes using [Conventional Commits](https://www.conventionalcommits.org/)
4. Push to the branch (`git push origin feat/amazing-feature`)
5. Open a Pull Request

### Commit Convention

```
type(scope): description

feat(auth): add Google Sign-In
fix(profile): resolve avatar upload crash
docs(readme): update installation steps
```

## 📄 License

This project is proprietary software. All rights reserved.

## 👥 Team

- **Development**: Chris Desmurger
- **AI Assistant**: Claude (Anthropic)

## 📞 Support

For questions or issues:
- 📧 Email: [your-email@example.com]
- 🐛 Issues: [GitHub Issues](https://github.com/Chrisdesmurger/Ora/issues)
- 📖 Docs: [Documentation](docs/)

---

**Built with ❤️ using Kotlin, Jetpack Compose, and Firebase**

🤖 *This project uses AI-assisted development with [Claude Code](https://claude.com/claude-code)*
