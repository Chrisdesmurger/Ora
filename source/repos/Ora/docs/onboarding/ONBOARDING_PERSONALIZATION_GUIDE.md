# Guide d'Onboarding de Personnalisation ORA

Documentation complète de l'onboarding de personnalisation pour l'application Ora.

## 📋 Vue d'ensemble

L'onboarding de personnalisation est un questionnaire de 21 écrans (18 écrans de contenu + 3 écrans de transition) qui permet de personnaliser l'expérience utilisateur en fonction de:

- Profil personnel (prénom, date de naissance, genre, situation)
- Relation au mouvement et au bien-être
- Intentions et objectifs
- Niveau de pratique (yoga, méditation, respiration)
- Préférences de temps et d'horaires
- État émotionnel actuel
- Zones corporelles nécessitant attention
- Qualité du sommeil
- Style d'expérience préféré
- Fréquence de pratique souhaitée
- Type de rappels

## 🏗️ Architecture

### Structure des Écrans

**Écrans d'Information (5):**
1. Écran 1: Bienvenue
2. Écran 2: Construisons ton profil ORA
3. Écran 7: C'est noté !
4. Écran 14: Merci !
5. Écran 21: Ton espace ORA est prêt

**Écrans de Questions (16):**
- Profil: prénom, date de naissance, genre, situation (4 questions)
- Mouvement & bien-être: relation mouvement, rapport bien-être (2 questions)
- Intentions & niveau: pourquoi es-tu ici, niveau de pratique (2 questions)
- Temps: engagement temps, moments préférés (2 questions)
- État actuel: énergie, corps, sommeil (3 questions)
- Préférences: style expérience, fréquence, rappels (3 questions)

### Types de Questions Utilisés

| Type | Usage | Exemples |
|------|-------|----------|
| `information_screen` | Écrans de transition sans collecte de données | Écrans 1, 2, 7, 14, 21 |
| `text_input` | Saisie de texte libre | Prénom, Date de naissance |
| `multiple_choice` | Sélection unique | Genre, Situation, Niveau |
| `toggle_list` | Sélection multiple avec switches | Intentions, Moments préférés, Zones corps |
| `grid_selection` | Sélection visuelle en grille | Énergie actuelle |
| `multiple_choice` (grid) | Alternative à grid_selection | - |

## 📂 Fichiers Impliqués

### Android App (Ora)

**Modèles de données:**
- `app/src/main/java/com/ora/wellbeing/data/model/onboarding/OnboardingConfig.kt`
- `app/src/main/java/com/ora/wellbeing/data/model/onboarding/OnboardingQuestion.kt`
- `app/src/main/java/com/ora/wellbeing/data/model/onboarding/QuestionType.kt` (✅ mis à jour avec `INFORMATION_SCREEN`)
- `app/src/main/java/com/ora/wellbeing/data/model/onboarding/AnswerOption.kt`
- `app/src/main/java/com/ora/wellbeing/data/model/onboarding/UserOnboardingResponse.kt`

**Repository:**
- `app/src/main/java/com/ora/wellbeing/data/repository/OnboardingRepository.kt`

**UI:**
- `app/src/main/java/com/ora/wellbeing/presentation/screens/onboarding/OnboardingScreen.kt` (✅ mis à jour avec `InformationScreenContent`)
- `app/src/main/java/com/ora/wellbeing/presentation/screens/onboarding/OnboardingViewModel.kt`
- `app/src/main/java/com/ora/wellbeing/presentation/screens/onboarding/OnboardingCelebrationScreen.kt`

**Configuration:**
- `onboarding_personalization_config.json` - Configuration complète (✅ créé)

**Scripts:**
- `import_onboarding_to_firebase.js` - Script d'import Firebase (✅ créé)

### OraWebApp (Admin Portal)

**À créer:**
- Interface de gestion des onboardings
- Éditeur de questions
- Prévisualisation des configurations
- Activation/désactivation de configurations

## 🚀 Déploiement

### 1. Prérequis

**Node.js et Firebase Admin SDK:**
```bash
npm install firebase-admin
```

**Service Account Key:**
1. Aller sur Firebase Console → Project Settings → Service Accounts
2. Cliquer "Generate new private key"
3. Télécharger le fichier JSON
4. Le renommer en `serviceAccountKey.json`
5. Le placer à la racine du projet Ora (⚠️ **NE PAS** commit ce fichier!)

**Alternative: Variable d'environnement:**
```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/serviceAccountKey.json"
```

### 2. Importer la Configuration

**Exécuter le script d'import:**
```bash
node import_onboarding_to_firebase.js
```

**Sortie attendue:**
```
═══════════════════════════════════════════════════════════
  ORA Onboarding Configuration Import
═══════════════════════════════════════════════════════════

✅ Firebase Admin SDK initialized with service account key
📖 Loaded configuration: Onboarding de Personnalisation Ora
   Version: 1.0
   Questions: 21

✅ Successfully imported onboarding configuration!
   Document ID: abc123xyz
   Collection: onboarding_configs
   Status: active

📊 Configuration Summary:
   - Total questions: 21
   - Questions by category:
     • personalization: 10
     • experience: 4
     • goals: 1
     • preferences: 6
   - Questions by type:
     • information_screen: 5
     • text_input: 2
     • multiple_choice: 8
     • toggle_list: 4
     • grid_selection: 1
   - Required questions: 16
   - Optional questions: 5

🔍 Verifying import...
✅ Verification passed!
   - Document exists: ✓
   - Questions count: 21 ✓
   - Status: active ✓

🎉 Import completed successfully!
```

### 3. Vérification dans Firebase Console

1. Ouvrir [Firebase Console](https://console.firebase.google.com/)
2. Sélectionner votre projet ORA
3. Aller dans **Firestore Database**
4. Naviguer vers la collection `onboarding_configs`
5. Vérifier que le document existe avec:
   - `status: "active"`
   - `questions: [21 items]`
   - `version: "1.0"`

## 🧪 Tests

### Test de l'Onboarding dans l'App Android

**Prérequis:**
1. Configuration importée dans Firestore (status = "active")
2. App Android compilée et installée
3. Compte utilisateur créé

**Flow de test:**

```kotlin
// 1. Créer un nouveau compte
// L'app détecte automatiquement qu'il n'y a pas d'onboarding complété

// 2. OnboardingViewModel charge la config active
OnboardingRepository.getActiveOnboardingConfig()
  .onSuccess { config ->
    // Config chargée: 21 questions
  }

// 3. Navigation séquentielle à travers les écrans
Écran 1 (information_screen) → Auto-acknowledged → Suivant
Écran 2 (information_screen) → Auto-acknowledged → Suivant
Écran 3 (text_input: prénom) → Saisie requise → Suivant
Écran 4 (text_input: date) → Saisie requise → Suivant
Écran 5 (multiple_choice: genre) → Sélection requise → Suivant
...
Écran 21 (information_screen) → Terminer

// 4. Sauvegarde dans Firestore
users/{uid}.onboarding {
  uid: "abc123",
  config_version: "v1_personalization",
  completed: true,
  completed_at: Timestamp,
  started_at: Timestamp,
  answers: [16 answers], // Only questions with user input
  metadata: {
    device_type: "Android 14",
    app_version: "1.0.0",
    total_time_seconds: 180,
    locale: "fr"
  }
}
```

**Points de vérification:**

✅ Tous les écrans d'information s'affichent correctement
✅ Les questions obligatoires bloquent la navigation si non répondues
✅ La barre de progression avance correctement (1/21, 2/21, etc.)
✅ Le bouton "Précédent" fonctionne et restaure les réponses
✅ Le bouton "Terminer" apparaît sur le dernier écran
✅ Les réponses sont sauvegardées dans Firestore après complétion
✅ L'app navigue vers l'écran principal après l'onboarding

### Tests Unitaires

**À créer:**

```kotlin
// OnboardingViewModelTest.kt
@Test
fun testLoadActiveConfig_Success() {
  // Given: Active config in Firestore
  // When: ViewModel loads config
  // Then: Config is loaded with 21 questions
}

@Test
fun testAnswerQuestion_InformationScreen() {
  // Given: Information screen question
  // When: Question is displayed
  // Then: Auto-acknowledged, can proceed to next
}

@Test
fun testAnswerQuestion_TextInput_Required() {
  // Given: Required text input question
  // When: User leaves it empty
  // Then: Cannot proceed to next
}

@Test
fun testCompleteOnboarding_SavesResponses() {
  // Given: All questions answered
  // When: User completes onboarding
  // Then: Responses saved to Firestore
}
```

## 📊 Schéma Firestore

### Collection: `onboarding_configs`

**Document ID:** Auto-généré par Firestore

**Structure:**
```typescript
{
  id: string, // Document ID
  title: string, // "Onboarding de Personnalisation Ora"
  description: string,
  status: "draft" | "active" | "archived",
  version: string, // "1.0"
  questions: OnboardingQuestion[], // 21 questions
  created_at: Timestamp,
  updated_at: Timestamp,
  created_by: string, // "admin"
  published_at: Timestamp | null,
  published_by: string | null
}
```

### Collection: `users` → Sous-champ: `onboarding`

**Structure:**
```typescript
users/{uid} {
  // ... autres champs profil ...

  onboarding: {
    uid: string,
    config_version: string,
    completed: boolean,
    completed_at: Timestamp,
    started_at: Timestamp,
    answers: [
      {
        question_id: string,
        selected_options: string[],
        text_answer: string | null,
        answered_at: Timestamp
      }
    ],
    metadata: {
      device_type: string,
      app_version: string,
      total_time_seconds: number,
      locale: string
    }
  }
}
```

**Note:** Les écrans `information_screen` génèrent une réponse `["acknowledged"]` mais ne sont pas sauvegardés dans Firestore (filtrés côté app).

## 🎨 Design & UX

### Écrans d'Information

**Layout:**
- Icône centrale (🪷 lotus ORA)
- Titre en gras
- Sous-titre descriptif
- Bouton "Suivant" pour continuer

**Exemple:**
```
┌─────────────────────────┐
│                         │
│         🪷              │
│                         │
│  Bienvenue sur ORA      │
│                         │
│  ORA est ton espace de  │
│  bien-être. 2 minutes   │
│  pour personnaliser     │
│  ton expérience.        │
│                         │
│    [    Suivant    ]    │
│                         │
└─────────────────────────┘
```

### Écrans de Questions

**Layout:**
- Badge de catégorie (ex: "✨ Personnalisation")
- Titre de la question
- Sous-titre optionnel
- Composant de réponse (selon le type)
- Indicateur "* Cette question est obligatoire"
- Boutons de navigation (Précédent / Suivant)

### Barre de Progression

**Format:**
- Barre linéaire en haut de l'écran
- Texte: "Question X sur 21" + "Y%"
- Couleur: Primary (ORA coral)

## 🔧 Configuration des Questions

### Ajouter une Nouvelle Question

1. **Ouvrir** `onboarding_personalization_config.json`
2. **Ajouter** dans le tableau `questions`:

```json
{
  "id": "unique_question_id",
  "category": "personalization|experience|goals|preferences",
  "order": 22,
  "title": "Titre de la question",
  "title_fr": "Titre en français",
  "title_en": "Title in English",
  "subtitle": "Description optionnelle",
  "subtitle_fr": "Description en français",
  "subtitle_en": "Description in English",
  "type": {
    "kind": "multiple_choice|text_input|toggle_list|grid_selection|information_screen",
    "allow_multiple": false,
    "display_mode": "list|grid"
  },
  "options": [
    {
      "id": "option1",
      "label": "Option 1",
      "label_fr": "Option 1 FR",
      "label_en": "Option 1 EN",
      "icon": "🎯",
      "order": 1
    }
  ],
  "required": true
}
```

3. **Ré-importer** la configuration:
```bash
node import_onboarding_to_firebase.js
```

### Modifier une Question Existante

1. **Ouvrir** Firebase Console → Firestore Database → `onboarding_configs`
2. **Trouver** le document avec `status: "active"`
3. **Modifier** directement dans l'éditeur Firestore
4. **Alternative:** Modifier `onboarding_personalization_config.json` et ré-importer

⚠️ **Important:** Modifier une config active affecte immédiatement tous les nouveaux utilisateurs.

### Archiver une Configuration

**Option 1: Via script (recommandé):**
Le script d'import archive automatiquement l'ancienne config active lors de l'import d'une nouvelle.

**Option 2: Manuellement:**
```javascript
await db.collection('onboarding_configs')
  .doc('config_id')
  .update({
    status: 'archived',
    archived_at: admin.firestore.FieldValue.serverTimestamp(),
    archived_by: 'admin'
  });
```

## 📱 Intégration avec OraWebApp (Admin Portal)

### Fonctionnalités à Développer

**1. Liste des Configurations**
- Vue tableau de toutes les configs (draft, active, archived)
- Filtres par status
- Actions: Voir, Éditer, Activer, Archiver, Dupliquer

**2. Éditeur de Configuration**
- Formulaire de création/édition
- Drag & drop pour réorganiser les questions
- Prévisualisation en temps réel
- Validation avant sauvegarde

**3. Éditeur de Question**
- Sélection du type de question
- Configuration des options
- Support multi-langue (FR/EN)
- Logique de skip (skip_logic)

**4. Prévisualisation**
- Simulation du flow mobile
- Navigation entre les questions
- Test des validations

**5. Analytics**
- Taux de complétion
- Temps moyen par question
- Questions abandonnées
- Réponses les plus fréquentes

## 🔐 Sécurité

### Firestore Security Rules

**Ajouter dans `firestore.rules`:**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Collection onboarding_configs
    match /onboarding_configs/{configId} {
      // Tout le monde peut lire les configs actives
      allow read: if resource.data.status == 'active';

      // Seuls les admins peuvent créer/modifier/archiver
      allow write: if request.auth != null &&
        exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }

    // Collection users - onboarding field
    match /users/{userId} {
      // L'utilisateur peut lire et écrire son propre onboarding
      allow read, write: if request.auth != null &&
        request.auth.uid == userId;

      // Admin peut lire tous les onboardings
      allow read: if request.auth != null &&
        exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
  }
}
```

**Déployer les rules:**
```bash
firebase deploy --only firestore:rules
```

## 📚 Ressources

### Documentation Existante

- [CLAUDE.md](CLAUDE.md) - Architecture globale du projet
- [ONBOARDING_INSCRIPTION_PERSISTENCE.md](docs/ONBOARDING_INSCRIPTION_PERSISTENCE.md) - Onboarding d'inscription
- [ONBOARDING_REGISTRATION_CODE_REFERENCE.md](docs/ONBOARDING_REGISTRATION_CODE_REFERENCE.md) - Référence code

### Références Firestore

- [FIRESTORE_SETUP_GUIDE.md](docs/FIRESTORE_SETUP_GUIDE.md)
- [FIRESTORE_KOTLIN_MAPPING_GUIDE.md](docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md)
- [FIRESTORE_TROUBLESHOOTING.md](docs/FIRESTORE_TROUBLESHOOTING.md)

## 🐛 Troubleshooting

### Erreur: "No active onboarding configuration found"

**Cause:** Aucune configuration avec `status: "active"` dans Firestore

**Solution:**
1. Vérifier dans Firebase Console → Firestore → `onboarding_configs`
2. S'assurer qu'un document a `status: "active"`
3. Ré-importer la config si nécessaire: `node import_onboarding_to_firebase.js`

### Erreur: "Failed to parse onboarding config"

**Cause:** Mauvais format JSON ou champs manquants

**Solution:**
1. Valider `onboarding_personalization_config.json` avec un validateur JSON
2. Vérifier que tous les champs requis sont présents
3. Vérifier les types de données (snake_case pour Firestore)

### Erreur: "Cannot deserialize field X"

**Cause:** Mismatch entre le modèle Kotlin et le document Firestore

**Solution:**
1. Vérifier que les noms de champs correspondent (snake_case dans Firestore)
2. Vérifier les annotations `@PropertyName` dans les modèles Kotlin
3. Consulter [FIRESTORE_KOTLIN_MAPPING_GUIDE.md](docs/FIRESTORE_KOTLIN_MAPPING_GUIDE.md)

### Question type "information_screen" ne s'affiche pas

**Cause:** Type non reconnu dans l'app Android

**Solution:**
1. Vérifier que `QuestionTypeKind.INFORMATION_SCREEN` existe dans `QuestionType.kt`
2. Vérifier que le case est ajouté dans `OnboardingScreen.kt`
3. Rebuild l'app: `./gradlew.bat clean assembleDebug installDebug`

## ✅ Checklist de Déploiement

### Avant le Déploiement

- [ ] Configuration JSON validée et complète (21 questions)
- [ ] Support `INFORMATION_SCREEN` ajouté dans l'app Android
- [ ] Tests unitaires écrits et passants
- [ ] Tests manuels effectués sur émulateur/device
- [ ] Service Account Key configuré
- [ ] Firestore Security Rules déployées

### Déploiement

- [ ] Import de la configuration dans Firestore
- [ ] Vérification dans Firebase Console
- [ ] Test avec un nouveau compte utilisateur
- [ ] Vérification des réponses sauvegardées

### Post-Déploiement

- [ ] Monitoring des erreurs (Timber logs)
- [ ] Vérification du taux de complétion
- [ ] Collecte de feedback utilisateurs
- [ ] Ajustements basés sur les analytics

---

**Créé le:** 2025-11-26
**Version:** 1.0
**Statut:** Documentation complète prête pour déploiement
