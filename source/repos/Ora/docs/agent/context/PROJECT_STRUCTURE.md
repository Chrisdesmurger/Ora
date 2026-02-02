# Ora - Structure du Projet

Ce document décrit l'organisation des fichiers et dossiers du projet Ora.

## 📋 Table des Matières

- [Structure Racine](#structure-racine)
- [Dossier Documentation (docs/)](#dossier-documentation-docs)
- [Dossier Configuration (config/)](#dossier-configuration-config)
- [Dossier Scripts (scripts/)](#dossier-scripts-scripts)
- [Dossier Application (app/)](#dossier-application-app)
- [Conventions de Nommage](#conventions-de-nommage)

## 📁 Structure Racine

```
Ora/
├── .github/              # GitHub workflows et configuration CI/CD
├── .gradle/              # Cache Gradle (ignoré par Git)
├── .idea/                # Configuration Android Studio (partiellement ignoré)
├── app/                  # Module application Android
├── config/               # ⚙️ Fichiers de configuration
├── docs/                 # 📚 Toute la documentation
├── firebase/             # Backend Firebase (Functions, seed data)
├── gradle/               # Gradle wrapper
├── qa/                   # Scripts de tests QA
├── reports/              # Rapports générés (builds, tests, etc.)
├── scripts/              # 🔧 Scripts utilitaires
│
├── .gitignore            # Fichiers à ignorer par Git
├── build.gradle.kts      # Configuration Gradle du projet
├── CHANGELOG.md          # Journal des changements
├── CLAUDE.md             # Instructions pour Claude Code
├── firebase.rules        # Règles Firestore (copie pour référence)
├── gradle.properties     # Propriétés Gradle
├── gradlew              # Gradle wrapper (Linux/Mac)
├── gradlew.bat          # Gradle wrapper (Windows)
├── local.properties     # Configuration locale (ignoré par Git)
├── README.md            # Documentation principale du projet
└── settings.gradle.kts  # Configuration des modules Gradle
```

### Fichiers Essentiels à la Racine

| Fichier | Description |
|---------|-------------|
| `README.md` | Documentation principale du projet |
| `CLAUDE.md` | Instructions pour Claude Code (AI assistant) |
| `CHANGELOG.md` | Historique des versions et changements |
| `.gitignore` | Patterns de fichiers à ignorer par Git |
| `build.gradle.kts` | Configuration Gradle racine |
| `settings.gradle.kts` | Définition des modules Gradle |
| `gradle.properties` | Propriétés globales Gradle |
| `gradlew` / `gradlew.bat` | Gradle wrapper |
| `firestore.rules` | Règles de sécurité Firestore |

### ❌ Fichiers Interdits à la Racine

Pour maintenir une racine propre, ces types de fichiers doivent être dans leurs dossiers respectifs :

- **Documentation** → `docs/`
- **Configuration** → `config/`
- **Scripts** → `scripts/`
- **Code Kotlin** → `app/src/`
- **Fichiers temporaires** → `docs/archive/`

## 📚 Dossier Documentation (docs/)

Organisation de toute la documentation du projet.

```
docs/
├── architecture/         # Documents d'architecture
│   ├── app_architecture.md
│   ├── technical_architecture.md
│   ├── navigation_graph.yaml
│   ├── feature_breakdown.yaml
│   └── user_flows.md
│
├── firebase/            # Documentation Firebase & Firestore
│   ├── FIREBASE_AUTH_INTEGRATION.md
│   ├── FIRESTORE_SETUP_GUIDE.md
│   ├── FIRESTORE_COLLECTIONS_SCHEMA.md
│   ├── FIRESTORE_KOTLIN_MAPPING_GUIDE.md
│   ├── FIRESTORE_QUICKSTART.md
│   ├── FIRESTORE_IMPLEMENTATION.md
│   ├── FIRESTORE_USAGE_EXAMPLE.md
│   ├── FIRESTORE_TEST_CHECKLIST.md
│   ├── FIRESTORE_TROUBLESHOOTING.md
│   ├── FIRESTORE_DEPLOYMENT.md
│   ├── FIRESTORE_FILES_INDEX.md
│   ├── firestore_rules_notes.md
│   └── TODO_FIREBASE_SETUP.md
│
├── development/         # Guides de développement
│   ├── DEVELOPER_GUIDE.md
│   ├── SETUP_INSTRUCTIONS.md
│   ├── build_config.md
│   ├── README_ANDROID.md
│   ├── README_AUTOMATION.md
│   ├── GIT_AUTOMATION.md
│   ├── feature_flags_guide.md
│   └── auth_setup.md
│
├── design/              # Design system et UI/UX
│   ├── ORA_DESIGN_SYSTEM.md
│   ├── DESIGN_SYSTEM_SUMMARY.md
│   └── COLOR_EXTRACTION_REPORT.md
│
├── qa/                  # Documentation QA et tests
│   ├── qa_report.md
│   ├── TESTS_LIVRABLES_SUMMARY.md
│   ├── cases_user_data.md
│   └── test_summary_user_data.md
│
├── archive/             # Documents historiques/temporaires
│   ├── DIAGNOSTIC-PROFILE-SCREEN.md
│   ├── diagnostic-homescreen-fix.md
│   ├── PROFILE-TEST-SIMPLE.md
│   ├── IMPLEMENTATION_SUMMARY.md
│   ├── SYNC_IMPLEMENTATION_SUMMARY.md
│   ├── FIRESTORE_SUMMARY.md
│   ├── FIRESTORE_FIX_SUMMARY.md
│   ├── FIRESTORE_INTEGRATION_COMPLETE.md
│   └── FIRESTORE_MAPPING_FIX_SUMMARY.md
│
├── README.md            # Index de la documentation
└── PROJECT_STRUCTURE.md # Ce fichier
```

### Organisation de la Documentation

| Catégorie | Dossier | Contenu |
|-----------|---------|---------|
| **Architecture** | `architecture/` | Diagrammes, flux, structure technique |
| **Firebase** | `firebase/` | Guides Firestore, authentification, déploiement |
| **Développement** | `development/` | Setup, build, automation, feature flags |
| **Design** | `design/` | Design system, couleurs, composants UI |
| **QA** | `qa/` | Rapports de tests, cas de test |
| **Archive** | `archive/` | Docs obsolètes, diagnostics, historique |

## ⚙️ Dossier Configuration (config/)

Centralise tous les fichiers de configuration du projet.

```
config/
├── firebase/            # Configuration Firebase
│   ├── firebase.json           # Config Firebase principale
│   └── firestore.indexes.json  # Index Firestore
│
├── design/              # Tokens et contrats de design
│   ├── design_tokens.yaml      # Tokens de design (couleurs, typo, etc.)
│   └── user_data_contract.yaml # Contrat de données utilisateur
│
├── feature-flags/       # Feature flags
│   └── flags.json              # Configuration des feature flags
│
├── detekt.yml           # Configuration Detekt (linter Kotlin)
└── README.md            # Index de configuration
```

### Types de Configuration

| Type | Fichier | Description |
|------|---------|-------------|
| **Firebase** | `firebase/firebase.json` | Configuration Firebase générale |
| **Firestore** | `firebase/firestore.indexes.json` | Index de base de données |
| **Design** | `design/design_tokens.yaml` | Tokens de design (couleurs, spacing, etc.) |
| **Contrats** | `design/user_data_contract.yaml` | Schéma de données utilisateur |
| **Feature Flags** | `feature-flags/flags.json` | Activation/désactivation de fonctionnalités |
| **Lint** | `detekt.yml` | Règles de linting Kotlin |

## 🔧 Dossier Scripts (scripts/)

Scripts utilitaires pour automatisation et déploiement.

```
scripts/
├── firebase/            # Scripts Firebase
│   ├── deploy-firestore-rules.sh
│   └── watch-firestore-logs.ps1
│
├── git/                 # Scripts Git et automatisation
│   └── auto-commit-pr.ps1
│
├── build/               # Scripts de build
│   └── (à venir)
│
└── README.md            # Index des scripts
```

### Catégories de Scripts

| Catégorie | Dossier | Utilisation |
|-----------|---------|-------------|
| **Firebase** | `firebase/` | Déploiement Firestore, logs, seed data |
| **Git** | `git/` | Automatisation commit/PR, workflows |
| **Build** | `build/` | Scripts de build, CI/CD |

### Exemples d'Utilisation

```bash
# Déployer les règles Firestore
bash scripts/firebase/deploy-firestore-rules.sh

# Automatiser commit et PR
powershell scripts/git/auto-commit-pr.ps1 -Type "feat" -Message "Add feature"

# Watcher les logs Firestore
powershell scripts/firebase/watch-firestore-logs.ps1
```

## 📱 Dossier Application (app/)

Structure du module Android principal.

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/ora/wellbeing/
│   │   │   ├── presentation/       # Couche UI (Compose + ViewModels)
│   │   │   │   ├── theme/         # Thème Material 3
│   │   │   │   ├── navigation/    # Navigation Compose
│   │   │   │   └── screens/       # Écrans de l'app
│   │   │   │       ├── auth/
│   │   │   │       ├── home/
│   │   │   │       ├── library/
│   │   │   │       ├── journal/
│   │   │   │       ├── programs/
│   │   │   │       └── profile/
│   │   │   │
│   │   │   ├── domain/             # Couche métier
│   │   │   │   ├── model/         # Entités métier
│   │   │   │   ├── repository/    # Interfaces de repository
│   │   │   │   └── usecase/       # Cas d'utilisation
│   │   │   │
│   │   │   ├── data/               # Couche données
│   │   │   │   ├── model/         # Modèles de données (DTOs)
│   │   │   │   ├── repository/    # Implémentations de repository
│   │   │   │   ├── local/         # Sources de données locales (Room)
│   │   │   │   ├── remote/        # Sources de données distantes (Firestore)
│   │   │   │   └── sync/          # Gestion de synchronisation
│   │   │   │
│   │   │   ├── di/                 # Injection de dépendances (Hilt)
│   │   │   │   ├── AppModule.kt
│   │   │   │   ├── FirebaseModule.kt
│   │   │   │   ├── FirestoreModule.kt
│   │   │   │   └── ConfigModule.kt
│   │   │   │
│   │   │   ├── core/               # Utilitaires et classes communes
│   │   │   │   ├── data/
│   │   │   │   └── domain/
│   │   │   │
│   │   │   ├── feature/            # Fonctionnalités transverses
│   │   │   │   └── practice/      # Module de pratique (player, etc.)
│   │   │   │
│   │   │   ├── MainActivity.kt
│   │   │   └── OraApplication.kt
│   │   │
│   │   ├── res/                    # Ressources Android
│   │   │   ├── values/
│   │   │   ├── values-en/
│   │   │   ├── drawable/
│   │   │   └── mipmap/
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   ├── test/                       # Tests unitaires
│   └── androidTest/                # Tests d'instrumentation
│
└── build.gradle.kts                # Configuration Gradle du module
```

### Architecture en Couches

| Couche | Dossier | Responsabilité |
|--------|---------|----------------|
| **Presentation** | `presentation/` | UI (Compose), ViewModels, Navigation |
| **Domain** | `domain/` | Logique métier, Use Cases, Entités |
| **Data** | `data/` | Repositories, Sources de données |
| **DI** | `di/` | Injection de dépendances (Hilt) |

## 📏 Conventions de Nommage

### Fichiers Markdown

| Type | Pattern | Exemple |
|------|---------|---------|
| Guide principal | `UPPERCASE.md` | `README.md`, `CLAUDE.md` |
| Documentation | `PascalCase.md` | `FirestoreSetupGuide.md` |
| Archive/Temp | `DIAGNOSTIC-*.md`, `*_SUMMARY.md` | `DIAGNOSTIC-AUTH.md` |

### Fichiers de Configuration

| Type | Pattern | Exemple |
|------|---------|---------|
| YAML | `snake_case.yaml` | `design_tokens.yaml` |
| JSON | `kebab-case.json` ou `camelCase.json` | `firebase.json`, `feature-flags.json` |

### Scripts

| Type | Pattern | Exemple |
|------|---------|---------|
| Shell | `kebab-case.sh` | `deploy-firestore-rules.sh` |
| PowerShell | `PascalCase.ps1` ou `kebab-case.ps1` | `auto-commit-pr.ps1` |

### Code Kotlin

| Type | Pattern | Exemple |
|------|---------|---------|
| Classes | `PascalCase` | `UserProfileRepository.kt` |
| Fonctions | `camelCase` | `getUserProfile()` |
| Constantes | `SCREAMING_SNAKE_CASE` | `MAX_RETRY_COUNT` |

## 🚫 Fichiers à Ne Jamais Commiter

Voir `.gitignore` pour la liste complète. Principaux :

```gitignore
# Secrets
google-services.json
local.properties
*.keystore
*.jks

# Builds
build/
*.apk
*.aab

# IDE
.idea/workspace.xml
.idea/tasks.xml

# Temporaires
DIAGNOSTIC-*.md
*_SUMMARY.md
*.backup
```

## 🔄 Workflow de Gestion des Fichiers

### Ajout d'une Nouvelle Fonctionnalité

1. **Code** : Ajouter dans `app/src/main/java/com/ora/wellbeing/`
2. **Tests** : Ajouter dans `app/src/test/` ou `app/src/androidTest/`
3. **Documentation** : Ajouter dans `docs/development/`
4. **Config** : Si nécessaire, ajouter dans `config/`
5. **Scripts** : Si nécessaire, ajouter dans `scripts/`

### Ajout de Documentation

- **Architecture** → `docs/architecture/`
- **Guide de développement** → `docs/development/`
- **Firebase/Firestore** → `docs/firebase/`
- **Design** → `docs/design/`
- **Tests/QA** → `docs/qa/`
- **Temporaire/Diagnostic** → `docs/archive/`

### Ajout de Configuration

- **Firebase** → `config/firebase/`
- **Design tokens** → `config/design/`
- **Feature flags** → `config/feature-flags/`
- **Linter** → `config/` (racine)

## 📚 Ressources

- [README Principal](../README.md)
- [Guide Développeur](development/DEVELOPER_GUIDE.md)
- [Architecture de l'App](architecture/app_architecture.md)
- [Configuration Firebase](firebase/FIRESTORE_SETUP_GUIDE.md)

---

**Dernière mise à jour** : 2025-10-11
**Version** : 1.0
**Mainteneur** : @Chrisdesmurger

🤖 *Généré avec [Claude Code](https://claude.com/claude-code)*
