# Ora Wellbeing App - Documentation

Documentation organisée par **cible** (audience) et **sujet**.

## 📁 Structure des dossiers

```
docs/
├── developer/          # 👨‍💻 Développeurs
│   ├── architecture/   # Architecture, schémas, patterns
│   ├── features/       # Guides des fonctionnalités (i18n, offline, onboarding)
│   ├── firebase/       # Intégration Firestore & Auth
│   ├── guides/         # Guides pratiques (journal, player)
│   └── setup/          # Configuration environnement dev
│
├── agent/              # 🤖 Claude Agent
│   ├── context/        # Contexte projet (structure, brief)
│   └── skills/         # Skills Claude Code
│
├── design/             # 🎨 Designers & Product
│   ├── system/         # Design system (couleurs, typo, composants)
│   ├── assets/         # Assets visuels (logos, guides Canva)
│   └── mockups/        # Maquettes et wireframes
│
├── qa/                 # 🧪 QA & DevOps
│   └── testing/        # Rapports de test, screenshots, build reports
│
├── project/            # 📋 Gestion de projet
│   └── status/         # Changelog, statut d'avancement
│
└── archive/            # 📦 Archive
    └── legacy/         # Anciens fichiers (debug, old-structure)
```

## 🚀 Accès rapide par rôle

### 👨‍💻 Développeurs
| Fichier | Description |
|---------|-------------|
| [developer/setup/DEVELOPER_GUIDE.md](developer/setup/DEVELOPER_GUIDE.md) | Guide complet de développement |
| [developer/setup/auth_setup.md](developer/setup/auth_setup.md) | Configuration Firebase Auth |
| [developer/architecture/app_architecture.md](developer/architecture/app_architecture.md) | Architecture Clean + MVVM |
| [developer/firebase/FIRESTORE_SETUP_GUIDE.md](developer/firebase/FIRESTORE_SETUP_GUIDE.md) | Guide Firestore |
| [developer/features/FEATURE_OFFLINE_FIRST_SYNC.md](developer/features/FEATURE_OFFLINE_FIRST_SYNC.md) | Sync offline-first |
| [developer/features/I18N_CONFIGURATION.md](developer/features/I18N_CONFIGURATION.md) | Internationalisation FR/EN/ES |

### 🤖 Claude Agent
| Fichier | Description |
|---------|-------------|
| [agent/skills/CLAUDE_CODE_SKILLS_GUIDE.md](agent/skills/CLAUDE_CODE_SKILLS_GUIDE.md) | Skills d'automatisation |
| [agent/context/PROJECT_STRUCTURE.md](agent/context/PROJECT_STRUCTURE.md) | Structure du projet |
| [agent/context/creator_brief.json](agent/context/creator_brief.json) | Brief produit (cible, ton, features) |

### 🎨 Designers & Product
| Fichier | Description |
|---------|-------------|
| [design/system/ORA_DESIGN_SYSTEM.md](design/system/ORA_DESIGN_SYSTEM.md) | Design system complet |
| [design/system/COLOR_EXTRACTION_REPORT.md](design/system/COLOR_EXTRACTION_REPORT.md) | Palette de couleurs |
| [design/assets/CANVA_INFORMATION_SCREENS_DESIGN_GUIDE.md](design/assets/CANVA_INFORMATION_SCREENS_DESIGN_GUIDE.md) | Guide écrans info Canva |

### 🧪 QA & DevOps
| Fichier | Description |
|---------|-------------|
| [qa/testing/qa_report.md](qa/testing/qa_report.md) | Rapport QA |
| [qa/testing/reports/](qa/testing/reports/) | Build reports et diagnostics |

## 📱 Stack technique

- **Android** : Kotlin, Jetpack Compose, Material 3
- **Architecture** : Clean Architecture + MVVM
- **Backend** : Firebase (Auth, Firestore, Storage)
- **DI** : Hilt
- **Async** : Coroutines + Flow
- **DB locale** : Room
- **Media** : ExoPlayer

## 🔗 Liens utiles

- [CLAUDE.md](../CLAUDE.md) - Guide principal pour Claude Agent (racine projet)
- [README.md](../README.md) - README principal du projet

---
**Dernière mise à jour** : 2026-01-19
