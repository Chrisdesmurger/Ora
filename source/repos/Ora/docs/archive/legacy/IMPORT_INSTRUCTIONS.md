# Instructions d'Import - Onboarding de Personnalisation

Guide pas à pas pour importer la configuration d'onboarding dans Firebase Firestore.

## 📋 Prérequis

- ✅ Node.js installé (v18+)
- ✅ Firebase CLI installé
- ✅ Compte Firebase avec accès au projet `ora-wellbeing`
- ✅ firebase-admin installé (déjà fait)

## 🔑 Étape 1: Télécharger le Service Account Key

1. **Ouvrir Firebase Console:**
   - Aller sur [https://console.firebase.google.com/](https://console.firebase.google.com/)
   - Sélectionner le projet **Ora Wellbeing**

2. **Accéder aux Service Accounts:**
   - Cliquer sur l'icône ⚙️ (Settings) en haut à gauche
   - Sélectionner **Project settings**
   - Aller dans l'onglet **Service accounts**

3. **Générer une nouvelle clé:**
   - Cliquer sur **Generate new private key**
   - Une boîte de dialogue apparaît avec un avertissement
   - Cliquer sur **Generate key**
   - Un fichier JSON sera téléchargé

4. **Placer la clé dans le projet:**
   - Renommer le fichier téléchargé en `serviceAccountKey.json`
   - Le déplacer à la racine du projet Ora:
     ```
     C:\Users\chris\source\repos\Ora\serviceAccountKey.json
     ```

⚠️ **IMPORTANT:** Ne JAMAIS commit ce fichier dans Git! (Déjà dans .gitignore)

## 🚀 Étape 2: Exécuter l'Import

Une fois le service account key en place:

```bash
node import_onboarding_simple.js
```

**Sortie attendue:**

```
════════════════════════════════════════════════════════════
  ORA Onboarding Configuration Import
════════════════════════════════════════════════════════════

✅ Firebase initialized with project: ora-wellbeing
📖 Loaded configuration: Onboarding de Personnalisation Ora
   Version: 1.0
   Questions: 21

⚠️  Warning: Active config exists, archiving...
✅ Existing config archived: abc123xyz

✅ Successfully imported onboarding configuration!
   Document ID: xyz789abc
   Collection: onboarding_configs
   Status: active

📊 Configuration Summary:
   - Total questions: 21
   - By category:
     • personalization: 10
     • experience: 4
     • goals: 1
     • preferences: 6
   - By type:
     • information_screen: 5
     • text_input: 2
     • multiple_choice: 8
     • toggle_list: 4
     • grid_selection: 1
   - Required: 16
   - Optional: 5

🎉 Import completed successfully!
```

## ✅ Étape 3: Vérifier l'Import

### Dans Firebase Console:

1. Ouvrir [Firebase Console](https://console.firebase.google.com/)
2. Sélectionner **Ora Wellbeing**
3. Aller dans **Firestore Database**
4. Naviguer vers la collection `onboarding_configs`
5. Vérifier qu'un document existe avec:
   - ✅ `status: "active"`
   - ✅ `questions: [Array with 21 items]`
   - ✅ `version: "1.0"`
   - ✅ `title: "Onboarding de Personnalisation Ora"`

### Dans l'Application Android:

1. **Build et install l'app:**
   ```bash
   cd app
   ../gradlew.bat clean assembleDebug installDebug
   ```

2. **Tester l'onboarding:**
   - Créer un nouveau compte utilisateur
   - L'onboarding devrait se lancer automatiquement
   - Naviguer à travers les 21 écrans
   - Compléter le questionnaire

3. **Vérifier les réponses dans Firestore:**
   - Firebase Console → Firestore → `users/{uid}`
   - Vérifier le champ `onboarding`:
     ```json
     {
       "uid": "user_id_here",
       "config_version": "document_id",
       "completed": true,
       "completed_at": Timestamp,
       "started_at": Timestamp,
       "answers": [Array of answers],
       "metadata": {
         "device_type": "Android 14",
         "app_version": "1.0.0",
         "total_time_seconds": 180,
         "locale": "fr"
       }
     }
     ```

## 🔧 Alternative: Utiliser le Script Original

Si vous préférez utiliser une variable d'environnement:

```bash
# Windows (PowerShell)
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\Users\chris\source\repos\Ora\serviceAccountKey.json"
node import_onboarding_to_firebase.js

# Windows (CMD)
set GOOGLE_APPLICATION_CREDENTIALS=C:\Users\chris\source\repos\Ora\serviceAccountKey.json
node import_onboarding_to_firebase.js
```

## 🐛 Troubleshooting

### Erreur: "Could not load default credentials"

**Cause:** Service account key manquant ou mal placé

**Solution:**
1. Vérifier que `serviceAccountKey.json` existe à la racine du projet
2. Vérifier que le fichier n'est pas vide
3. Re-télécharger la clé depuis Firebase Console si nécessaire

### Erreur: "Permission denied"

**Cause:** Le service account n'a pas les droits Firestore

**Solution:**
1. Firebase Console → IAM & Admin
2. Trouver le service account
3. Ajouter le rôle **Cloud Datastore User** ou **Firebase Admin**

### Erreur: "Document already exists"

**Cause:** Une configuration active existe déjà

**Solution:**
Le script archive automatiquement l'ancienne config. Si l'erreur persiste:
1. Aller dans Firestore Console
2. Collection `onboarding_configs`
3. Changer manuellement le `status` de "active" à "archived"
4. Re-exécuter le script

### Erreur: "Failed to parse configuration"

**Cause:** Fichier JSON mal formé

**Solution:**
1. Vérifier `onboarding_personalization_config.json`
2. Valider le JSON sur [jsonlint.com](https://jsonlint.com/)
3. Vérifier qu'il y a exactement 21 questions

## 📊 Structure Attendue

Après l'import, votre Firestore devrait avoir:

```
onboarding_configs (collection)
└── xyz789abc (document - auto-generated ID)
    ├── id: "xyz789abc"
    ├── title: "Onboarding de Personnalisation Ora"
    ├── description: "..."
    ├── status: "active"
    ├── version: "1.0"
    ├── questions: [21 questions]
    ├── created_at: Timestamp
    ├── updated_at: Timestamp
    ├── created_by: "admin"
    ├── published_at: Timestamp
    └── published_by: "admin"
```

## 📝 Fichiers Créés

Les fichiers suivants ont été créés dans le projet:

- ✅ `onboarding_personalization_config.json` - Configuration complète (21 questions)
- ✅ `import_onboarding_simple.js` - Script d'import Node.js
- ✅ `import_onboarding_to_firebase.js` - Script d'import alternatif
- ✅ `import_with_rest_api.js` - Script avec API REST
- ✅ `ONBOARDING_PERSONALIZATION_GUIDE.md` - Documentation complète
- ✅ `IMPORT_INSTRUCTIONS.md` - Ce fichier
- ✅ `package.json` - Dépendances Node.js (firebase-admin)

## 🎯 Prochaines Étapes

Après l'import réussi:

1. **Tester dans l'app Android** (voir instructions ci-dessus)
2. **Vérifier les analytics** dans Firebase Console
3. **Collecter du feedback** des premiers utilisateurs
4. **Ajuster la configuration** si nécessaire
5. **Développer l'interface admin** dans OraWebApp pour gérer les onboardings

## 📚 Documentation Complète

Pour plus d'informations, consulter:

- [ONBOARDING_PERSONALIZATION_GUIDE.md](ONBOARDING_PERSONALIZATION_GUIDE.md) - Guide complet
- [CLAUDE.md](CLAUDE.md) - Architecture du projet
- [docs/FIRESTORE_SETUP_GUIDE.md](docs/FIRESTORE_SETUP_GUIDE.md) - Configuration Firestore

---

**Créé le:** 2025-11-26
**Dernière mise à jour:** 2025-11-26
**Version:** 1.0
