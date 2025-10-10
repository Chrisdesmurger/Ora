# 🔥 Ora Firebase Data Import

Scripts pour importer les données seed dans Firestore.

## 📋 Prérequis

1. **Node.js** installé (v16+)
2. **Service Account Key** de Firebase

### Obtenir la Service Account Key

1. Allez sur [Firebase Console](https://console.firebase.google.com/)
2. Sélectionnez votre projet Ora
3. **Project Settings** (⚙️) → **Service Accounts**
4. Cliquez sur **Generate New Private Key**
5. Téléchargez le fichier JSON
6. Renommez-le en `serviceAccountKey.json`
7. Placez-le dans le dossier `firebase/`

⚠️ **Important**: Ne committez JAMAIS ce fichier ! Il est dans `.gitignore`.

## 🚀 Installation

```bash
cd firebase
npm install
```

## 📦 Import des données

### Import complet (tout)

```bash
npm run import
```

Importe:
- ✅ 10 programmes (Méditation, Yoga, Sommeil, Défis, Bien-être, Pilates)
- ✅ 20 contenus (méditations, vidéos yoga, exercices respiration)
- ✅ 1 utilisateur test avec données complètes

### Import partiel

```bash
# Programmes uniquement
npm run import:programs

# Contenus uniquement
npm run import:content
```

## 🗑️ Nettoyer les données

```bash
npm run clean
```

⚠️ **Attention**: Supprime TOUTES les données des collections !

## 📊 Données importées

### Collections Firestore

| Collection | Documents | Description |
|------------|-----------|-------------|
| `programs` | 10 | Programmes par catégorie |
| `content` | 20 | Méditations, vidéos yoga, etc. |
| `users` | 1 | Profil utilisateur test |
| `stats` | 1 | Statistiques utilisateur test |
| `user_programs/{uid}/enrolled` | 2 | Inscriptions aux programmes |
| `gratitudes/{uid}/entries` | 3 | Entrées de journal |

### Utilisateur test

- **UID**: `test-user-123`
- **Nom**: Demo User
- **Plan**: Premium
- **Statistiques**: 245 min, 18 sessions, 5 jours de streak
- **Programmes inscrits**: 2 (Méditation débutant 42%, Gratitude 24%)
- **Gratitudes**: 3 entrées (aujourd'hui, hier, avant-hier)

## 🎯 Catégories de programmes

Les programmes sont répartis dans ces catégories:

- **Méditation** (3 programmes)
  - Méditation pour Débutants (7j, gratuit)
  - Méditation Avancée (30j, premium)
  - Défi Méditation Quotidienne (30j, gratuit)

- **Yoga** (2 programmes)
  - Yoga Matinal Énergisant (14j, gratuit)
  - Yoga pour la Souplesse (21j, premium)

- **Sommeil** (1 programme)
  - Sommeil Réparateur (10j, premium)

- **Défis** (2 programmes)
  - Défi Gratitude 21 Jours (21j, gratuit)
  - Défi Méditation Quotidienne (30j, gratuit)

- **Bien-être** (2 programmes)
  - Bien-être au Travail (7j, gratuit)
  - Respiration & Énergie (14j, gratuit)

- **Pilates** (1 programme)
  - Pilates Renforcement Profond (28j, premium)

## 🧪 Tester avec l'app

1. Importez les données: `npm run import`
2. Lancez l'app Android
3. Connectez-vous avec Firebase Auth
4. L'app devrait afficher les programmes et contenus

**Note**: L'utilisateur test (`test-user-123`) est différent de votre compte Firebase Auth. Pour voir ses données, vous devrez:
- Soit créer un compte avec cet UID (via Firebase Auth)
- Soit modifier le script pour utiliser votre propre UID

## 🔧 Personnalisation

### Ajouter vos propres programmes

Éditez `seed-data/programs.json`:

```json
{
  "id": "mon-programme",
  "title": "Mon Programme",
  "description": "...",
  "category": "Méditation",
  "duration": 7,
  "level": "Débutant",
  "is_premium_only": false,
  "sessions": [...]
}
```

### Ajouter vos propres contenus

Éditez `seed-data/content.json`:

```json
{
  "id": "mon-contenu",
  "title": "Mon Contenu",
  "category": "Méditation",
  "type": "video",
  "duration_minutes": 10,
  ...
}
```

## 🐛 Dépannage

### Erreur: Service account key not found

```bash
# Vérifiez que le fichier existe
ls -la serviceAccountKey.json

# Définissez la variable d'environnement
export GOOGLE_APPLICATION_CREDENTIALS="./serviceAccountKey.json"
```

### Erreur: Permission denied

Vérifiez que votre Service Account a les permissions Firestore:
- Firebase Console → IAM & Admin
- Votre service account doit avoir le rôle **Cloud Datastore User**

### Les données n'apparaissent pas dans l'app

1. Vérifiez que l'import s'est bien passé: `npm run import`
2. Vérifiez Firestore dans Firebase Console
3. Vérifiez que les règles de sécurité permettent la lecture
4. Videz le cache de l'app

## 📚 Ressources

- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [Firestore Data Model](https://firebase.google.com/docs/firestore/data-model)
- [Security Rules](https://firebase.google.com/docs/firestore/security/get-started)

## 📝 Licence

MIT - Ora Wellbeing © 2025
