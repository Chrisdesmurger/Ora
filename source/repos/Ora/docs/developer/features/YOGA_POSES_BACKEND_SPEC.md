# Tech Spec: Yoga Poses Backend Implementation

**Version:** 1.0
**Date:** 2026-01-19
**Status:** Draft
**Author:** Claude Code

---

## 1. Overview

### 1.1 Objective

Implémenter le backend Firestore pour supporter la fonctionnalité de **liste des poses de yoga** dans le lecteur Android. Cette fonctionnalité permet d'afficher les poses/chapitres d'une séance de yoga avec leurs descriptions, instructions et zones ciblées.

### 1.2 Current State (Android)

Le lecteur yoga Android (`YogaPlayerScreen`) fonctionne déjà avec :
- Navigation par chapitres (barre de chapitres cliquable)
- Affichage des descriptions de pose (cards swipeables)
- Mode miroir et suivi des côtés (Gauche/Droit)
- Aperçu de la prochaine pose

**Limitation actuelle :** Les chapitres sont **auto-générés** côté Android (5 chapitres par défaut divisés équitablement sur la durée). Les données de poses ne viennent pas du backend.

### 1.3 Goal

Permettre aux créateurs de contenu de définir les poses/chapitres précis d'une séance via l'admin backend (OraWebApp), avec synchronisation vers l'app Android.

---

## 2. Data Model

### 2.1 Firestore Collection Structure

```
firestore/
├── lessons/{lessonId}              # Collection existante
│   ├── ... (champs existants)
│   └── yoga_poses: [...]           # NOUVEAU: Array de poses
│
└── yoga_poses/{poseId}             # NOUVELLE collection (référentiel)
    └── ... (pose template)
```

### 2.2 Nouveau champ dans `lessons` collection

Ajouter un champ `yoga_poses` dans les documents de type yoga :

```typescript
// Dans lessons/{lessonId}
{
  // ... champs existants ...

  // NOUVEAU: Liste des poses de la séance (optionnel)
  yoga_poses: YogaPoseEntry[]
}
```

### 2.3 Schema: YogaPoseEntry (embedded in lesson)

```typescript
interface YogaPoseEntry {
  // Timing
  start_time_ms: number;        // Début de la pose en millisecondes
  end_time_ms: number;          // Fin de la pose en millisecondes

  // Identité
  pose_id?: string;             // Référence optionnelle vers yoga_poses collection

  // Contenu (inline ou override de pose_id)
  title: string;                // Ex: "Chien tête en bas"
  title_fr?: string;
  title_en?: string;
  title_es?: string;

  // Description / Stimulus
  stimulus: string;             // Ex: "Étirement profond du dos et des jambes"
  stimulus_fr?: string;
  stimulus_en?: string;
  stimulus_es?: string;

  // Instructions étape par étape
  instructions: string[];       // Ex: ["Placez les mains...", "Poussez les hanches..."]
  instructions_fr?: string[];
  instructions_en?: string[];
  instructions_es?: string[];

  // Métadonnées
  target_zones: string[];       // Ex: ["dos", "jambes", "épaules"]
  benefits?: string[];          // Ex: ["Renforce le dos", "Améliore la flexibilité"]

  // Asymétrie (poses bilatérales)
  side?: 'none' | 'left' | 'right' | 'both';  // default: 'none'

  // Visuel
  thumbnail_url?: string;       // Image de la pose

  // Difficulté (1-3)
  difficulty?: number;          // 1=débutant, 2=intermédiaire, 3=avancé
}
```

### 2.4 Schema: YogaPose (référentiel global - optionnel)

Pour éviter la duplication, un référentiel de poses peut être créé :

```typescript
// Collection: yoga_poses/{poseId}
interface YogaPose {
  id: string;

  // Nom canonique
  name: string;                 // Ex: "Adho Mukha Svanasana"
  common_name: string;          // Ex: "Chien tête en bas"
  common_name_fr?: string;
  common_name_en?: string;      // "Downward-Facing Dog"
  common_name_es?: string;      // "Perro boca abajo"

  // Description
  description: string;
  description_fr?: string;
  description_en?: string;
  description_es?: string;

  // Instructions par défaut
  default_instructions: string[];
  default_instructions_fr?: string[];
  default_instructions_en?: string[];
  default_instructions_es?: string[];

  // Métadonnées
  target_zones: string[];
  benefits: string[];
  contraindications?: string[]; // Ex: ["hypertension", "grossesse"]

  // Catégorisation
  category: 'standing' | 'seated' | 'supine' | 'prone' | 'inversion' | 'balance' | 'twist';
  difficulty: number;           // 1-3
  is_asymmetric: boolean;       // true si pose bilatérale

  // Visuels
  image_url?: string;
  video_demo_url?: string;

  // Timestamps
  created_at: Timestamp;
  updated_at: Timestamp;
}
```

---

## 3. Firestore Rules

Ajouter les règles pour la nouvelle collection et le nouveau champ :

```javascript
// firestore.rules

rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Existing lessons rules (UPDATE)
    match /lessons/{lessonId} {
      // Read: authenticated users only
      allow read: if request.auth != null;

      // Write: admin only
      allow write: if request.auth != null
        && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';

      // Validate yoga_poses array if present
      allow update: if request.auth != null
        && (
          !request.resource.data.keys().hasAny(['yoga_poses'])
          || (
            request.resource.data.yoga_poses is list
            && request.resource.data.yoga_poses.size() <= 50
          )
        );
    }

    // NEW: Yoga poses reference collection
    match /yoga_poses/{poseId} {
      // Read: all authenticated users
      allow read: if request.auth != null;

      // Write: admin only
      allow write: if request.auth != null
        && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
  }
}
```

---

## 4. Android Integration

### 4.1 Update LessonDocument.kt

```kotlin
// Ajouter dans LessonDocument.kt

/**
 * Liste des poses de yoga pour cette séance (optionnel)
 * Utilisé uniquement pour les leçons de type yoga/pilates
 */
var yoga_poses: List<Map<String, Any>>? = null
```

### 4.2 New Data Model: YogaPoseDocument.kt

```kotlin
package com.ora.wellbeing.data.model.firestore

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * YogaPoseDocument - Firestore model for yoga pose entries in lessons
 * Embedded in lessons collection as yoga_poses array
 */
@IgnoreExtraProperties
data class YogaPoseDocument(
    // Timing
    @get:PropertyName("start_time_ms")
    @set:PropertyName("start_time_ms")
    var startTimeMs: Long = 0,

    @get:PropertyName("end_time_ms")
    @set:PropertyName("end_time_ms")
    var endTimeMs: Long = 0,

    // Identity
    @get:PropertyName("pose_id")
    @set:PropertyName("pose_id")
    var poseId: String? = null,

    // Content
    var title: String = "",

    @get:PropertyName("title_fr")
    @set:PropertyName("title_fr")
    var titleFr: String? = null,

    @get:PropertyName("title_en")
    @set:PropertyName("title_en")
    var titleEn: String? = null,

    @get:PropertyName("title_es")
    @set:PropertyName("title_es")
    var titleEs: String? = null,

    // Stimulus/Description
    var stimulus: String = "",

    @get:PropertyName("stimulus_fr")
    @set:PropertyName("stimulus_fr")
    var stimulusFr: String? = null,

    @get:PropertyName("stimulus_en")
    @set:PropertyName("stimulus_en")
    var stimulusEn: String? = null,

    @get:PropertyName("stimulus_es")
    @set:PropertyName("stimulus_es")
    var stimulusEs: String? = null,

    // Instructions
    var instructions: List<String> = emptyList(),

    @get:PropertyName("instructions_fr")
    @set:PropertyName("instructions_fr")
    var instructionsFr: List<String>? = null,

    @get:PropertyName("instructions_en")
    @set:PropertyName("instructions_en")
    var instructionsEn: List<String>? = null,

    @get:PropertyName("instructions_es")
    @set:PropertyName("instructions_es")
    var instructionsEs: List<String>? = null,

    // Metadata
    @get:PropertyName("target_zones")
    @set:PropertyName("target_zones")
    var targetZones: List<String> = emptyList(),

    var benefits: List<String>? = null,

    // Side tracking
    var side: String? = null, // "none", "left", "right", "both"

    // Visuals
    @get:PropertyName("thumbnail_url")
    @set:PropertyName("thumbnail_url")
    var thumbnailUrl: String? = null,

    // Difficulty
    var difficulty: Int? = null
)
```

### 4.3 Update YogaPlayerViewModel

Le ViewModel doit charger les poses depuis Firestore au lieu de les auto-générer :

```kotlin
// Dans YogaPlayerViewModel.kt - loadPractice()

private fun loadPractice(practiceId: String) {
    viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }

        practiceRepository.getById(practiceId)
            .onSuccess { practice ->
                // Charger les poses depuis Firestore
                val poses = practiceRepository.getYogaPoses(practiceId)

                val chapters = if (poses.isNotEmpty()) {
                    // Utiliser les poses du backend
                    poses.map { pose ->
                        Chapter(
                            title = pose.getLocalizedTitle(),
                            startTime = pose.startTimeMs
                        )
                    }
                } else {
                    // Fallback: auto-générer les chapitres
                    generateChaptersForPractice(practice.durationMin)
                }

                _state.update {
                    it.copy(
                        practice = practice,
                        chapters = chapters,
                        yogaPoses = poses, // Nouveau champ
                        isLoading = false
                    )
                }
            }
            .onFailure { error ->
                _state.update { it.copy(error = error.message, isLoading = false) }
            }
    }
}
```

### 4.4 Mapping Flow

```
Firestore                    Android
────────────────────────────────────────────
lessons/{id}
  └─ yoga_poses: [...]  ───► YogaPoseDocument[]
                         │
                         ▼ (LessonMapper)
                        YogaPose (domain)
                         │
                         ▼ (YogaPlayerViewModel)
                        Chapter + PoseDescription
                         │
                         ▼
                        YogaPlayerScreen UI
```

---

## 5. Backend Implementation (OraWebApp)

### 5.1 Admin UI Requirements

L'interface admin doit permettre de :

1. **Créer une leçon yoga** avec poses
2. **Ajouter des poses** avec timeline visuelle
3. **Éditer les poses** (drag & drop pour réorganiser)
4. **Prévisualiser** le découpage temporel

### 5.2 API Endpoints (si API REST)

Si OraWebApp expose une API REST :

```typescript
// POST /api/lessons/{lessonId}/yoga-poses
// Body: YogaPoseEntry[]

// GET /api/lessons/{lessonId}/yoga-poses
// Response: YogaPoseEntry[]

// PUT /api/lessons/{lessonId}/yoga-poses/{index}
// Body: YogaPoseEntry

// DELETE /api/lessons/{lessonId}/yoga-poses/{index}
```

### 5.3 Firebase Admin SDK (Node.js)

```typescript
// services/lessonService.ts

import { firestore } from '../config/firebase';

interface YogaPoseEntry {
  start_time_ms: number;
  end_time_ms: number;
  title: string;
  title_fr?: string;
  title_en?: string;
  title_es?: string;
  stimulus: string;
  stimulus_fr?: string;
  stimulus_en?: string;
  stimulus_es?: string;
  instructions: string[];
  instructions_fr?: string[];
  instructions_en?: string[];
  instructions_es?: string[];
  target_zones: string[];
  benefits?: string[];
  side?: 'none' | 'left' | 'right' | 'both';
  thumbnail_url?: string;
  difficulty?: number;
}

export async function setYogaPoses(
  lessonId: string,
  poses: YogaPoseEntry[]
): Promise<void> {
  // Valider les poses
  validatePoses(poses);

  // Mettre à jour le document
  await firestore
    .collection('lessons')
    .doc(lessonId)
    .update({
      yoga_poses: poses,
      updated_at: new Date().toISOString()
    });
}

export async function getYogaPoses(
  lessonId: string
): Promise<YogaPoseEntry[]> {
  const doc = await firestore
    .collection('lessons')
    .doc(lessonId)
    .get();

  return doc.data()?.yoga_poses || [];
}

function validatePoses(poses: YogaPoseEntry[]): void {
  if (poses.length > 50) {
    throw new Error('Maximum 50 poses per lesson');
  }

  for (let i = 0; i < poses.length; i++) {
    const pose = poses[i];

    if (pose.start_time_ms < 0) {
      throw new Error(`Pose ${i}: start_time_ms must be >= 0`);
    }

    if (pose.end_time_ms <= pose.start_time_ms) {
      throw new Error(`Pose ${i}: end_time_ms must be > start_time_ms`);
    }

    if (!pose.title || pose.title.length === 0) {
      throw new Error(`Pose ${i}: title is required`);
    }

    // Vérifier le chevauchement avec la pose précédente
    if (i > 0 && pose.start_time_ms < poses[i-1].end_time_ms) {
      throw new Error(`Pose ${i}: overlaps with previous pose`);
    }
  }
}
```

---

## 6. Example Data

### 6.1 Sample Yoga Lesson with Poses

```json
{
  "id": "lesson-yoga-morning-flow",
  "title": "Yoga Flow Matinal",
  "title_fr": "Yoga Flow Matinal",
  "title_en": "Morning Yoga Flow",
  "type": "video",
  "duration_sec": 900,
  "tags": ["yoga", "morning", "beginner"],
  "status": "ready",

  "yoga_poses": [
    {
      "start_time_ms": 0,
      "end_time_ms": 60000,
      "title": "Position de l'enfant",
      "title_en": "Child's Pose",
      "stimulus": "Détente et centrage pour commencer la pratique",
      "stimulus_en": "Relaxation and centering to begin practice",
      "instructions": [
        "Agenouillez-vous sur le tapis",
        "Asseyez-vous sur vos talons",
        "Penchez-vous vers l'avant, bras étendus"
      ],
      "instructions_en": [
        "Kneel on the mat",
        "Sit back on your heels",
        "Fold forward with arms extended"
      ],
      "target_zones": ["dos", "hanches", "épaules"],
      "benefits": ["Calme le système nerveux", "Étire le bas du dos"],
      "side": "none",
      "difficulty": 1
    },
    {
      "start_time_ms": 60000,
      "end_time_ms": 180000,
      "title": "Chat-Vache",
      "title_en": "Cat-Cow",
      "stimulus": "Mobilisation de la colonne vertébrale",
      "instructions": [
        "À quatre pattes, mains sous les épaules",
        "Inspirez: creusez le dos (vache)",
        "Expirez: arrondissez le dos (chat)"
      ],
      "target_zones": ["colonne", "abdominaux"],
      "side": "none",
      "difficulty": 1
    },
    {
      "start_time_ms": 180000,
      "end_time_ms": 300000,
      "title": "Chien tête en bas",
      "title_en": "Downward-Facing Dog",
      "stimulus": "Étirement complet du corps",
      "instructions": [
        "Depuis quatre pattes, levez les hanches",
        "Formez un V inversé avec le corps",
        "Pédalez doucement les pieds"
      ],
      "target_zones": ["ischio-jambiers", "mollets", "épaules", "dos"],
      "benefits": ["Renforce les bras", "Étire l'arrière des jambes"],
      "side": "none",
      "difficulty": 2
    },
    {
      "start_time_ms": 300000,
      "end_time_ms": 420000,
      "title": "Fente basse - Côté Gauche",
      "title_en": "Low Lunge - Left Side",
      "stimulus": "Ouverture des hanches",
      "instructions": [
        "Avancez le pied gauche entre les mains",
        "Genou arrière au sol",
        "Levez les bras vers le ciel"
      ],
      "target_zones": ["psoas", "quadriceps", "hanches"],
      "side": "left",
      "difficulty": 2
    },
    {
      "start_time_ms": 420000,
      "end_time_ms": 540000,
      "title": "Fente basse - Côté Droit",
      "title_en": "Low Lunge - Right Side",
      "stimulus": "Ouverture des hanches",
      "instructions": [
        "Avancez le pied droit entre les mains",
        "Genou arrière au sol",
        "Levez les bras vers le ciel"
      ],
      "target_zones": ["psoas", "quadriceps", "hanches"],
      "side": "right",
      "difficulty": 2
    },
    {
      "start_time_ms": 540000,
      "end_time_ms": 720000,
      "title": "Guerrier II - Gauche",
      "title_en": "Warrior II - Left",
      "stimulus": "Force et stabilité",
      "instructions": [
        "Pied gauche vers l'avant, genou à 90°",
        "Bras étendus parallèles au sol",
        "Regard vers la main avant"
      ],
      "target_zones": ["cuisses", "hanches", "bras"],
      "side": "left",
      "difficulty": 2
    },
    {
      "start_time_ms": 720000,
      "end_time_ms": 840000,
      "title": "Guerrier II - Droit",
      "title_en": "Warrior II - Right",
      "stimulus": "Force et stabilité",
      "instructions": [
        "Pied droit vers l'avant, genou à 90°",
        "Bras étendus parallèles au sol",
        "Regard vers la main avant"
      ],
      "target_zones": ["cuisses", "hanches", "bras"],
      "side": "right",
      "difficulty": 2
    },
    {
      "start_time_ms": 840000,
      "end_time_ms": 900000,
      "title": "Savasana",
      "title_en": "Corpse Pose",
      "stimulus": "Relaxation finale",
      "instructions": [
        "Allongez-vous sur le dos",
        "Bras le long du corps, paumes vers le ciel",
        "Fermez les yeux et respirez naturellement"
      ],
      "target_zones": ["corps entier"],
      "benefits": ["Intègre les bienfaits de la pratique", "Calme le mental"],
      "side": "none",
      "difficulty": 1
    }
  ]
}
```

---

## 7. Migration Strategy

### 7.1 Phase 1: Backend Ready (Week 1)

1. Ajouter le champ `yoga_poses` au schema Firestore
2. Mettre à jour les règles de sécurité
3. Créer l'interface admin pour saisir les poses
4. Tester avec 2-3 leçons pilotes

### 7.2 Phase 2: Android Integration (Week 2)

1. Mettre à jour `LessonDocument.kt` avec le nouveau champ
2. Créer `YogaPoseDocument.kt`
3. Mettre à jour le mapper
4. Modifier `YogaPlayerViewModel` pour charger les poses
5. Garder le fallback auto-génération si pas de poses

### 7.3 Phase 3: Content Population (Week 3+)

1. Former les créateurs de contenu
2. Saisir les poses pour les leçons existantes
3. Valider avec les utilisateurs beta

---

## 8. Testing Checklist

### 8.1 Backend Tests

- [ ] Création de leçon avec poses valides
- [ ] Validation : poses ne se chevauchent pas
- [ ] Validation : start_time < end_time
- [ ] Validation : max 50 poses
- [ ] Mise à jour des poses existantes
- [ ] Suppression de poses
- [ ] Lecture par utilisateur authentifié
- [ ] Écriture refusée pour non-admin

### 8.2 Android Tests

- [ ] Chargement des poses depuis Firestore
- [ ] Fallback si pas de poses (auto-génération)
- [ ] Affichage correct des chapitres
- [ ] Navigation entre chapitres
- [ ] Affichage des descriptions localisées (FR/EN/ES)
- [ ] Indicateur de côté (Gauche/Droit)
- [ ] Synchronisation offline (Room cache)

### 8.3 Integration Tests

- [ ] Créer pose dans admin → visible dans app
- [ ] Modifier pose dans admin → mis à jour dans app
- [ ] Supprimer pose dans admin → disparaît de l'app

---

## 9. Future Enhancements

### 9.1 Short Term

- Audio cues automatiques avant changement de pose
- Vibration/notification au changement de pose
- Mode "pose par pose" avec pause automatique

### 9.2 Medium Term

- Référentiel global de poses (yoga_poses collection)
- Recherche de poses par nom sanskrit
- Poses favorites de l'utilisateur

### 9.3 Long Term

- Détection de pose via caméra (ML Kit)
- Feedback en temps réel sur la posture
- Séances personnalisées basées sur les poses maîtrisées

---

## 10. References

- [LessonDocument.kt](../../../app/src/main/java/com/ora/wellbeing/data/model/firestore/LessonDocument.kt)
- [YogaPlayerState.kt](../../../app/src/main/java/com/ora/wellbeing/feature/practice/player/specialized/yoga/YogaPlayerState.kt)
- [PoseDescription.kt](../../../app/src/main/java/com/ora/wellbeing/feature/practice/player/specialized/yoga/PoseDescription.kt)
- [Firestore Rules](../../../firestore.rules)
- [CLAUDE.md](../../../CLAUDE.md) - Project context

---

**Changelog:**
- 2026-01-19: Initial draft
