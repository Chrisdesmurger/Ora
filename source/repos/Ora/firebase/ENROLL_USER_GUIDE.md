# Guide d'Inscription Utilisateur aux Programmes

## Script: enroll-user-programs.js

Ce script permet d'inscrire un utilisateur existant à des programmes sample et de simuler une utilisation active de l'application.

### Fonctionnalités

Le script effectue les opérations suivantes:

1. **Vérifie l'utilisateur existant** dans Firestore
2. **Inscrit l'utilisateur à 3 programmes**:
   - **Méditation Débutant 7j**: En cours (jour 4/7)
   - **Défi Gratitude 21j**: En cours (jour 8/21)
   - **Yoga Matinal 14j**: Terminé (100%)

3. **Met à jour les statistiques**:
   - 24 sessions complétées
   - 180 minutes de pratique (3h)
   - Streak actuel de 8 jours
   - Record de 12 jours

4. **Ajoute 5 entrées de gratitude récentes** (5 derniers jours)

### Prérequis

1. Node.js installé
2. Firebase Admin SDK configuré
3. Fichier `ora-wellbeing-firebase-adminsdk.json` à la racine du projet
4. Dépendances installées: `npm install`
5. **Utilisateur déjà créé** dans Firestore (collection `users`)

### Usage

#### 1. Obtenir l'ID utilisateur

**Option A**: Depuis l'app Android (logs):
```
D/AuthViewModel: User logged in: <USER_ID>
```

**Option B**: Depuis Firebase Console:
- Aller dans **Authentication**
- Copier l'UID de l'utilisateur
- Exemple: `rgzkId7TdvXHoyzDZD7feFFOxAR2`

**Option C**: Depuis Firestore Console:
- Aller dans **Firestore Database**
- Collection `users`
- Copier l'ID du document

#### 2. Exécuter le script

```bash
node firebase/enroll-user-programs.js <USER_ID>
```

**Exemple avec un utilisateur réel**:
```bash
node firebase/enroll-user-programs.js rgzkId7TdvXHoyzDZD7feFFOxAR2
```

### Sortie Attendue

```
📋 Inscription de l'utilisateur rgzkId7TdvXHoyzDZD7feFFOxAR2 aux programmes...

✅ Utilisateur trouvé: Christophe
   Plan: premium

📚 10 programmes trouvés

✅ Programme en cours - jour 4/7
   Programme: Méditation pour Débutants
   Progression: 4/7 jours
   Sessions: 3 complétées

✅ Programme actif - jour 8/21
   Programme: Défi Gratitude 21 Jours
   Progression: 8/21 jours
   Sessions: 7 complétées

✅ Programme terminé - 100%
   Programme: Yoga Matinal Énergisant
   Progression: 14/14 jours
   Sessions: 14 complétées

📊 Statistiques mises à jour:
   • 180 minutes de pratique
   • 24 sessions complétées
   • 8 jours de streak actuel
   • Catégorie préférée: Méditation

📝 5 entrées de gratitude ajoutées
   Dates: 2025-10-05, 2025-10-04, 2025-10-03, 2025-10-02, 2025-10-01

✅ ✅ ✅ Inscription terminée avec succès! ✅ ✅ ✅

Résumé pour l'utilisateur rgzkId7TdvXHoyzDZD7feFFOxAR2:
  • 3 programmes (2 en cours, 1 terminé)
  • 24 sessions complétées
  • 180 minutes de pratique
  • 8 jours de streak
  • 5 jours de gratitude

✅ Script terminé
```

### Vérification dans l'App

Après exécution du script, **redémarrez l'application Android** et vérifiez:

#### 📱 Écran Accueil
- Nom affiché: "Christophe"
- Statistiques:
  - **24 sessions** complétées
  - **180 min** de pratique
  - **8 jours** de streak
- Programmes actifs affichés (2)

#### 📅 Écran Programmes
- 3 programmes inscrits visibles
- **En cours**: Méditation Débutant (4/7), Gratitude (8/21)
- **Terminé**: Yoga Matinal (14/14) ✓

#### 📖 Écran Journal
- 5 entrées de gratitude affichées
- Dates: 5 derniers jours
- Humeurs variées (joyful, peaceful, grateful, calm, energized)
- Streak de 8 jours

#### 👤 Écran Profil
- Nom: Christophe
- Plan: Premium
- Stats: 24 sessions, 180 min, 8 jours

### Collections Firestore Créées

Le script crée/met à jour les documents suivants:

```
user_programs/{userId}/enrolled/{programId}
├── meditation-debutant-7j
├── defi-gratitude-21j
└── yoga-matinal-14j

stats/{userId}
└── Statistiques agrégées

gratitudes/{userId}/entries/{date}
├── 2025-10-05
├── 2025-10-04
├── 2025-10-03
├── 2025-10-02
└── 2025-10-01
```

### Personnalisation

Vous pouvez modifier le script pour:

#### Changer les programmes inscrits
```javascript
const selectedPrograms = [
  {
    programId: 'sommeil-reparateur-10j', // Programme différent
    status: 'in_progress',
    currentDay: 3,
    sessionsCompleted: 2,
    description: 'Sommeil - jour 3/10'
  },
  // ... autres programmes
];
```

#### Ajuster les statistiques
```javascript
const statsData = {
  totalMinutes: 300,  // 5h au lieu de 3h
  totalSessions: 40,  // 40 sessions au lieu de 24
  currentStreak: 15,  // 15 jours de streak
  // ...
};
```

#### Modifier les gratitudes
```javascript
const gratitudeData = {
  gratitudes: [
    "Votre gratitude personnalisée 1",
    "Votre gratitude personnalisée 2",
    "Votre gratitude personnalisée 3"
  ],
  mood: 'joyful',
  notes: "Votre note personnalisée"
};
```

### Erreurs Courantes

#### Erreur: "Utilisateur introuvable"
```
❌ Utilisateur rgzkId7TdvXHoyzDZD7feFFOxAR2 introuvable dans Firestore
```
**Solution**: Créez d'abord l'utilisateur avec `import-seed-data.js` ou vérifiez l'UID

#### Erreur: "Programme introuvable"
```
⚠️ Programme meditation-debutant-7j introuvable, ignoré
```
**Solution**: Importez d'abord les programmes avec `import-seed-data.js`

#### Erreur: Permission denied
```
Error: Missing or insufficient permissions
```
**Solution**: Vérifiez que `firestore.rules` autorise l'écriture sur `user_programs/{uid}/enrolled/`

### Commandes Utiles

```bash
# Lister les utilisateurs existants
firebase firestore:get users

# Vérifier les inscriptions d'un utilisateur
firebase firestore:get user_programs/<USER_ID>/enrolled

# Voir les statistiques d'un utilisateur
firebase firestore:get stats/<USER_ID>

# Voir les gratitudes d'un utilisateur
firebase firestore:get gratitudes/<USER_ID>/entries
```

### Nettoyage

Pour supprimer les inscriptions d'un utilisateur:

```bash
# Supprimer toutes les inscriptions
firebase firestore:delete user_programs/<USER_ID>/enrolled --recursive

# Supprimer les gratitudes
firebase firestore:delete gratitudes/<USER_ID>/entries --recursive

# Réinitialiser les stats
firebase firestore:delete stats/<USER_ID>
```

### Prochaines Étapes

Après avoir inscrit un utilisateur:

1. **Tester l'interface utilisateur**: Vérifier que toutes les données s'affichent correctement
2. **Tester la progression**: Marquer une session comme complétée dans l'app
3. **Tester la gratitude**: Ajouter une nouvelle entrée de gratitude
4. **Vérifier la synchro**: Observer la mise à jour en temps réel des données
5. **Tester offline**: Désactiver le réseau et vérifier le cache Firestore

---

## Fichiers Associés

- **Script**: [firebase/enroll-user-programs.js](./enroll-user-programs.js)
- **Import Initial**: [firebase/import-seed-data.js](./import-seed-data.js)
- **Programmes**: [firebase/seed-data/programs.json](./seed-data/programs.json)
- **Contenu**: [firebase/seed-data/content.json](./seed-data/content.json)
