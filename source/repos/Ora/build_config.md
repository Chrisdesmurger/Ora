# Configuration de Build - Ora Android

## Vue d'ensemble
Ce document décrit la configuration de build pour l'application Ora Android, incluant les outils DevOps, les pipelines CI/CD et les bonnes pratiques de développement.

## Structure du projet

```
Ora/
├── app/                          # Module principal Android
│   ├── src/main/                # Code source principal
│   ├── src/test/                # Tests unitaires
│   ├── src/androidTest/         # Tests instrumentés
│   ├── build.gradle.kts         # Configuration Gradle module
│   └── lint.xml                 # Configuration Lint
├── gradle/
│   └── libs.versions.toml       # Catalog des versions
├── .github/workflows/           # Workflows GitHub Actions
│   ├── android-ci.yml          # CI/CD Pipeline
│   └── android-release.yml     # Release Pipeline
├── build.gradle.kts            # Configuration Gradle racine
└── gradle.properties           # Propriétés Gradle
```

## Configuration Gradle

### Version Catalog (libs.versions.toml)
Le projet utilise Gradle Version Catalog pour centraliser la gestion des dépendances :

**Avantages :**
- ✅ Versions centralisées et cohérentes
- ✅ Autocomplétion dans l'IDE
- ✅ Type safety pour les dépendances
- ✅ Facilite les mises à jour

**Bundles définis :**
- `compose` : UI Jetpack Compose
- `architecture` : MVVM + Lifecycle
- `room` : Base de données locale
- `network` : Retrofit + OkHttp
- `testing` : Framework de tests
- `debug` : Outils de développement

### Configuration Build Types

```kotlin
buildTypes {
    debug {
        isDebuggable = true
        applicationIdSuffix = ".debug"
        versionNameSuffix = "-debug"
        isMinifyEnabled = false

        // Configuration pour développement
        buildConfigField("String", "API_BASE_URL", "\"http://localhost:8080/\"")
        buildConfigField("boolean", "ENABLE_LOGGING", "true")
    }

    release {
        isDebuggable = false
        isMinifyEnabled = true
        isShrinkResources = true

        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )

        // Configuration pour production
        buildConfigField("String", "API_BASE_URL", "\"https://api.ora-wellbeing.com/\"")
        buildConfigField("boolean", "ENABLE_LOGGING", "false")
    }

    create("staging") {
        initWith(getByName("debug"))
        applicationIdSuffix = ".staging"
        versionNameSuffix = "-staging"

        // Configuration pour tests
        buildConfigField("String", "API_BASE_URL", "\"https://staging-api.ora-wellbeing.com/\"")
    }
}
```

### Flavors de produit

```kotlin
productFlavors {
    create("free") {
        dimension = "version"
        applicationIdSuffix = ".free"
        versionNameSuffix = "-free"

        // Fonctionnalités limitées
        buildConfigField("boolean", "IS_PREMIUM", "false")
    }

    create("premium") {
        dimension = "version"
        // Version complète
        buildConfigField("boolean", "IS_PREMIUM", "true")
    }
}
```

## Pipelines CI/CD

### Pipeline CI (android-ci.yml)

**Déclencheurs :**
- Push sur `main` ou `develop`
- Pull requests vers `main` ou `develop`
- Modifications dans les dossiers `app/`, `gradle/` ou fichiers Gradle

**Jobs exécutés :**

1. **Test** (Ubuntu, 30min)
   - Tests unitaires
   - Génération de rapports
   - Upload des résultats

2. **Lint** (Ubuntu, 20min)
   - Vérification qualité code
   - Règles d'accessibilité
   - Upload rapports Lint

3. **Detekt** (Ubuntu, 15min)
   - Analyse statique Kotlin
   - Vérification style de code
   - Détection code smells

4. **Tests instrumentés** (macOS, 45min)
   - Matrix API levels : 26, 30, 34
   - Tests UI sur émulateurs
   - Cache AVD pour performance

5. **Build** (Ubuntu, 25min)
   - Build APK Debug/Release
   - Upload artefacts
   - Validation builds

6. **Sécurité** (Ubuntu, 15min)
   - Scan vulnérabilités Trivy
   - Upload vers GitHub Security
   - Analyse dépendances

### Pipeline Release (android-release.yml)

**Déclencheurs :**
- Tags `v*` (ex: v1.0.0)
- Déclenchement manuel avec version

**Étapes :**

1. **Pre-Release Checks**
   - Tests complets
   - Validation CHANGELOG.md
   - Vérifications qualité

2. **Build Release**
   - APK et AAB signés
   - Versioning automatique
   - Release GitHub avec notes

3. **Publish Play Store** (conditionnel)
   - Upload automatique AAB
   - Publication en production
   - Métadonnées Play Store

## Outils de qualité

### Lint Android
Configuration dans `app/lint.xml` :

**Règles critiques (erreur) :**
- Texte hardcodé
- Content descriptions manquantes
- Taille de texte < 12sp
- Cibles tactiles < 48dp
- Problèmes de sécurité

**Règles importantes (warning) :**
- Ressources inutilisées
- Optimisations de performance
- Hiérarchie de layout trop profonde

### Detekt (Kotlin)
Analyse statique pour :
- Style de code Kotlin
- Complexité des fonctions
- Détection de code smells
- Patterns anti-patterns

### Tests automatisés

**Tests unitaires :**
- Repositories, ViewModels, UseCases
- Mocking avec Mockito
- Coroutines avec TestDispatchers
- Couverture cible : 80%

**Tests UI :**
- Compose UI Testing
- Tests d'accessibilité
- Tests de navigation
- Tests de régression

**Tests instrumentés :**
- Tests d'intégration
- Tests sur vrais appareils
- Tests de performance
- Matrix API levels

## Configuration des secrets

### GitHub Secrets requis :

```yaml
# Signature APK
KEYSTORE_FILE          # Keystore encodé base64
KEYSTORE_PASSWORD      # Mot de passe keystore
KEY_ALIAS             # Alias de la clé
KEY_PASSWORD          # Mot de passe de la clé

# Play Store
PLAY_STORE_SERVICE_ACCOUNT  # JSON service account

# Notifications (optionnel)
SLACK_WEBHOOK_URL      # Webhook Slack
```

### Sécurisation du keystore :

```bash
# Encoder le keystore pour GitHub Secrets
base64 -i keystore.jks | pbcopy

# Dans le workflow
echo ${{ secrets.KEYSTORE_FILE }} | base64 -d > app/keystore.jks
```

## Optimisations de performance

### Cache Gradle
- Cache des dépendances
- Cache de build Gradle
- Réutilisation entre builds

### Cache AVD
- Snapshot d'émulateurs
- Réduction temps setup tests
- Cache par API level

### Parallelisation
- Jobs CI en parallèle
- Tests matrix parallèles
- Build variants simultanés

## Monitoring et reporting

### Métriques suivies :
- Temps de build
- Taux de succès des tests
- Couverture de code
- Taille APK/AAB
- Performance des tests

### Rapports générés :
- Test coverage reports
- Lint reports (HTML/XML)
- Detekt reports
- Dependency update reports
- Security scan reports

### Notifications :
- GitHub Status Checks
- GitHub Releases automatiques
- Slack/Discord (optionnel)
- Email sur échecs

## Bonnes pratiques

### Commits et PR
- Conventional Commits
- Pull Request obligatoires
- Review requise avant merge
- Tests passants obligatoires

### Versioning
- Semantic Versioning (SemVer)
- Tags Git pour releases
- Incrémentation automatique versionCode
- CHANGELOG.md maintenu

### Sécurité
- Scan automatique vulnérabilités
- Secrets dans GitHub Secrets
- Permissions minimales
- Validation des entrées

### Performance
- Builds incrémentaux
- Cache agressif
- Parallélisation maximale
- Timeout appropriés

## Déploiement

### Environnements :
- **Development** : Branches feature
- **Staging** : Branche develop
- **Production** : Branche main + tags

### Stratégie de release :
- Release candidates avec tags RC
- Tests beta avec Play Store Internal Testing
- Production avec validation manuelle
- Rollback possible via Play Store

## Troubleshooting

### Problèmes courants :

**Build lent :**
```bash
# Augmenter mémoire Gradle
export GRADLE_OPTS="-Xmx4g -XX:MaxMetaspaceSize=512m"

# Utiliser build daemon
./gradlew --daemon build
```

**Tests flaky :**
```bash
# Désactiver animations émulateur
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.disableAnalytics=true
```

**Cache corrompu :**
```bash
# Nettoyer cache Gradle
./gradlew clean
rm -rf ~/.gradle/caches/
```

## Roadmap DevOps

### À court terme :
- [ ] Intégration Firebase App Distribution
- [ ] Tests de performance automatisés
- [ ] Analyse de crash automatique

### À moyen terme :
- [ ] Déploiement multi-environnements
- [ ] Tests de charge API
- [ ] Monitoring APM

### À long terme :
- [ ] Infrastructure as Code
- [ ] ML pour prédiction de bugs
- [ ] Déploiement continu avancé

---

**Maintenu par :** Équipe DevOps Ora
**Dernière mise à jour :** 2025-09-28
**Version :** 1.0.0