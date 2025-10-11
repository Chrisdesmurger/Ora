# Ora Android - Application de Bien-être

![Ora Logo](brand/logo-ora.svg)

> Une application mobile dédiée au bien-être, proposant yoga, méditation, respiration et journaling pour accompagner votre quotidien.

[![Android CI](https://github.com/ora-wellbeing/android/workflows/Android%20CI/badge.svg)](https://github.com/ora-wellbeing/android/actions)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ora-android&metric=alert_status)](https://sonarcloud.io/dashboard?id=ora-android)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=26)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Aperçu

Ora est une application Android native développée avec les dernières technologies Android, offrant une expérience complète de bien-être et self-care.

### Fonctionnalités principales

🧘‍♀️ **Bibliothèque de contenu riche**
- Séances de yoga pour tous niveaux
- Méditations guidées
- Exercices de respiration
- Auto-massages et conseils beauté

📖 **Journal de gratitude**
- 3 gratitudes quotidiennes
- Suivi de l'humeur
- Récit du jour libre
- Calendrier et statistiques

🌅 **Parcours personnalisés**
- Routines matin/jour/soir
- Recommandations adaptées
- Sessions flash 5 minutes
- Programmes 21 jours

📊 **Suivi de progression**
- Statistiques détaillées
- Calendrier d'activité
- Système de badges
- Streaks et défis

## Screenshots

| Onboarding | Accueil | Bibliothèque | Journal |
|-----------|---------|-------------|---------|
| ![Onboarding](screenshots/onboarding.png) | ![Home](screenshots/home.png) | ![Library](screenshots/library.png) | ![Journal](screenshots/journal.png) |

## Installation

### Prérequis

- Android 8.0 (API 26) ou supérieur
- 150 MB d'espace libre
- Connexion Internet (pour synchronisation)

### Téléchargement

**Google Play Store** (Recommandé)
```
https://play.google.com/store/apps/details?id=com.ora.wellbeing
```

**Releases GitHub**
```
https://github.com/ora-wellbeing/android/releases
```

**F-Droid** (Version FOSS)
```
https://f-droid.org/packages/com.ora.wellbeing
```

## Guide d'utilisation

### Premier lancement

1. **Onboarding personnalisé** - Définissez vos préférences
2. **Choix du créneau** - Matin, jour ou soir
3. **Niveau d'expérience** - Débutant à avancé
4. **Objectifs** - Relaxation, forme, mindfulness

### Navigation

L'application suit une navigation par onglets :

- **🏠 Accueil** - Recommandations et progression
- **📚 Bibliothèque** - Tous les contenus disponibles
- **📖 Journal** - Gratitudes et humeur quotidienne
- **📅 Programmes** - Défis et programmes structurés
- **👤 Profil** - Statistiques et paramètres

### Fonctionnalités avancées

**Mode hors ligne**
- Contenu téléchargé automatiquement
- Synchronisation en arrière-plan
- Fonctionnement complet sans Internet

**Accessibilité**
- Support TalkBack complet
- Navigation clavier
- Contrastes élevés
- Texte redimensionnable

**Personnalisation**
- Mode sombre/clair automatique
- Notifications personnalisables
- Rappels intelligents
- Filtres par durée/niveau

## Architecture technique

### Stack technologique

- **Language** : Kotlin 100%
- **UI** : Jetpack Compose + Material 3
- **Architecture** : MVVM + Clean Architecture
- **Injection** : Hilt (Dagger)
- **Navigation** : Navigation Compose
- **Base de données** : Room + SQLite
- **Réseau** : Retrofit + OkHttp
- **Media** : ExoPlayer (vidéo/audio)

### Modules principaux

```
app/
├── data/           # Couche données (local + remote)
├── domain/         # Logique métier et use cases
├── presentation/   # UI et ViewModels
├── di/            # Injection de dépendances
└── utils/         # Utilitaires et extensions
```

### Composants clés

**Data Layer**
- Room Database (stockage local)
- Retrofit API Client (synchronisation)
- Repository Pattern (abstraction)
- DataStore (préférences)

**Domain Layer**
- Use Cases (logique métier)
- Entities (modèles de données)
- Repository Interfaces

**Presentation Layer**
- Compose UI (interface)
- ViewModels (état et logique UI)
- Navigation Graph (flux utilisateur)

## Développement

### Configuration de l'environnement

```bash
# Cloner le repository
git clone https://github.com/ora-wellbeing/android.git
cd android

# Importer dans Android Studio
# File > Open > Sélectionner le dossier
```

### Structure des branches

- `main` : Production stable
- `develop` : Développement actif
- `feature/*` : Nouvelles fonctionnalités
- `bugfix/*` : Corrections de bugs
- `release/*` : Préparation releases

### Commandes utiles

```bash
# Build debug
./gradlew assembleDebug

# Tests unitaires
./gradlew testDebugUnitTest

# Tests instrumentés
./gradlew connectedDebugAndroidTest

# Lint check
./gradlew lintDebug

# Analyse Detekt
./gradlew detekt

# Build release
./gradlew assembleRelease
```

### Qualité de code

Le projet maintient des standards élevés :

- **Couverture tests** : >80%
- **Lint score** : 0 erreur, <5 warnings
- **Accessibilité** : Conformité AA
- **Performance** : <2s cold start, 60fps

### Tests

**Tests unitaires** (JUnit + Mockito)
```bash
./gradlew test
```

**Tests UI** (Compose Testing)
```bash
./gradlew connectedAndroidTest
```

**Tests d'accessibilité**
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=AccessibilityTestSuite
```

## Configuration

### Variables d'environnement

```properties
# gradle.properties
ORE_API_BASE_URL=https://api.ora-wellbeing.com/
ORE_ENABLE_LOGGING=false
ORE_ANALYTICS_ENABLED=true
```

### Build variants

- **Debug** : Développement avec logs
- **Staging** : Tests pré-production
- **Release** : Production optimisée

### Flavors

- **Free** : Version gratuite limitée
- **Premium** : Version complète payante

## Déploiement

### CI/CD Pipeline

Le projet utilise GitHub Actions pour :

- ✅ Tests automatisés sur chaque PR
- ✅ Build et déploiement automatique
- ✅ Publication Play Store
- ✅ Analyse de sécurité

### Releases

Les releases suivent le Semantic Versioning :

- `v1.0.0` : Release majeure
- `v1.1.0` : Nouvelle fonctionnalité
- `v1.0.1` : Correction de bug

## Roadmap

### Version 1.1 (T1 2025)
- [ ] Programmes 21 jours
- [ ] Système de badges avancé
- [ ] Mode collaborative (partage)
- [ ] Apple Watch support

### Version 1.2 (T2 2025)
- [ ] IA personnalisée
- [ ] Conseils beauté étendus
- [ ] Oracle post-séance
- [ ] Communauté et défis

### Version 2.0 (T3 2025)
- [ ] Lives instructeurs
- [ ] Marketplace de contenu
- [ ] Intégration santé complète
- [ ] Multi-plateforme (iOS, Web)

## Contribution

Nous accueillons les contributions ! Voir [CONTRIBUTING.md](CONTRIBUTING.md) pour les guidelines.

### Types de contributions

- 🐛 **Bug reports** - Signaler des problèmes
- ✨ **Feature requests** - Proposer des améliorations
- 📝 **Documentation** - Améliorer les docs
- 🔧 **Code contributions** - Développer des fonctionnalités

### Processus

1. Fork le repository
2. Créer une branche feature
3. Développer et tester
4. Soumettre une Pull Request
5. Review et merge

## Support

### Documentation

- [Guide développeur](DEVELOPER_GUIDE.md)
- [Architecture détaillée](docs/ARCHITECTURE.md)
- [API Documentation](docs/API.md)
- [Accessibilité](docs/ACCESSIBILITY.md)

### Aide et support

- **Documentation** : [docs.ora-wellbeing.com](https://docs.ora-wellbeing.com)
- **Issues GitHub** : [github.com/ora-wellbeing/android/issues](https://github.com/ora-wellbeing/android/issues)
- **Email support** : support@ora-wellbeing.com
- **Discord communauté** : [discord.gg/ora-wellbeing](https://discord.gg/ora-wellbeing)

### FAQ

**Q: L'app fonctionne-t-elle hors ligne ?**
R: Oui, toutes les fonctionnalités principales sont disponibles hors ligne après le premier téléchargement.

**Q: Mes données sont-elles privées ?**
R: Absolument. Toutes les données personnelles sont stockées localement et chiffrées. Voir [Politique de confidentialité](PRIVACY.md).

**Q: Comment synchroniser sur plusieurs appareils ?**
R: La synchronisation cloud est optionnelle et sécurisée. Activez-la dans Paramètres > Synchronisation.

## License

Ce projet est sous licence MIT. Voir [LICENSE](LICENSE) pour plus de détails.

## Crédits

### Équipe de développement

- **Lead Developer** : [@johndoe](https://github.com/johndoe)
- **UI/UX Designer** : [@janesmith](https://github.com/janesmith)
- **QA Engineer** : [@testpro](https://github.com/testpro)

### Instructeurs et contenu

- **Yoga** : Sophie Martin, Instructrice certifiée
- **Méditation** : Marc Dubois, Praticien mindfulness
- **Bien-être** : Claire Petit, Coach wellness

### Open Source

Merci aux projets open source qui rendent Ora possible :

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Retrofit](https://square.github.io/retrofit/)
- [Room](https://developer.android.com/training/data-storage/room)
- [ExoPlayer](https://exoplayer.dev/)
- [Hilt](https://dagger.dev/hilt/)

---

**Créé avec ❤️ par l'équipe Ora**

Pour plus d'informations, visitez [ora-wellbeing.com](https://ora-wellbeing.com)