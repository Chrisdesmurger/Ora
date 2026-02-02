# Guide Complet - Configuration Firestore pour Ora Android

## 🎯 Objectif
Activer et configurer Firestore pour stocker les profils et statistiques des utilisateurs authentifiés via Firebase Auth.

---

## 📋 Prérequis

✅ Firebase Authentication activé et fonctionnel
✅ Projet Firebase "ora-wellbeing" créé
✅ Application Android connectée au projet
✅ google-services.json téléchargé et placé dans `app/`

---

## 🚀 Étape 1 : Activer Firestore dans Firebase Console

### 1.1 Accéder à Firestore

1. Ouvrez **Firebase Console** : https://console.firebase.google.com/
2. Sélectionnez votre projet **"ora-wellbeing"**
3. Dans le menu de gauche, cliquez sur **"Firestore Database"** (icône base de données)
4. Cliquez sur **"Create database"**

### 1.2 Choisir le mode de démarrage

Vous verrez 2 options :

#### Option 1 : **Mode Production (Recommandé)**
- Sélectionnez **"Start in production mode"**
- Cliquez **"Next"**
- **Pourquoi ?** Nous avons déjà des règles de sécurité strictes prêtes à déployer

#### Option 2 : Mode Test (Ne PAS utiliser)
- ⚠️ **Ne sélectionnez PAS "Start in test mode"**
- Les règles test permettent l'accès public pendant 30 jours (dangereux)

### 1.3 Choisir la localisation

1. Sélectionnez la région la plus proche de vos utilisateurs :
   - **Europe** : `eur3 (europe-west)` (Recommandé pour France/Europe)
   - **USA** : `nam5 (us-central)`

2. ⚠️ **IMPORTANT** : La localisation ne peut PAS être changée après création

3. Cliquez **"Enable"**

4. ⏳ Attendez 1-2 minutes que Firestore soit provisionné

---

## 🔒 Étape 2 : Déployer les Règles de Sécurité

### 2.1 Vérifier le fichier de règles

Le fichier `firebase/rules/firestore.rules` contient déjà nos règles de sécurité strictes :

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Profils utilisateurs - isolation stricte
    match /users/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }

    // Stats utilisateurs - isolation stricte
    match /stats/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

### 2.2 Option A : Déployer via Firebase Console (Méthode Simple)

1. Dans Firebase Console, allez dans **Firestore Database**
2. Cliquez sur l'onglet **"Rules"** en haut
3. **Supprimez tout** le contenu actuel
4. **Copiez-collez** le contenu complet de `firebase/rules/firestore.rules`
5. Cliquez **"Publish"**
6. ✅ Attendez la confirmation "Rules published successfully"

### 2.3 Option B : Déployer via Firebase CLI (Méthode Pro)

#### 2.3.1 Installer Firebase CLI

**Windows (PowerShell en tant qu'Admin) :**
```bash
npm install -g firebase-tools
```

Vérifiez l'installation :
```bash
firebase --version
# Devrait afficher : 13.x.x ou supérieur
```

#### 2.3.2 Se connecter à Firebase

```bash
firebase login
```
- Une fenêtre de navigateur s'ouvrira
- Connectez-vous avec votre compte Google (celui qui a accès au projet Firebase)
- Autorisez Firebase CLI

#### 2.3.3 Initialiser Firebase dans le projet

**Depuis le dossier racine du projet Ora :**

```bash
cd C:\Users\chris\source\repos\Ora
firebase init firestore
```

**Répondez aux questions :**
```
? What file should be used for Firestore Rules?
  → firebase/rules/firestore.rules

? What file should be used for Firestore indexes?
  → firebase/indexes/firestore.indexes.json

? File firebase/rules/firestore.rules already exists. Do you want to overwrite it?
  → No (n)
```

#### 2.3.4 Déployer les règles

```bash
firebase deploy --only firestore:rules
```

**Sortie attendue :**
```
✔ Deploy complete!

Project Console: https://console.firebase.google.com/project/ora-wellbeing/firestore
```

#### 2.3.5 Vérifier le déploiement

```bash
firebase firestore:rules get
```

---

## 📊 Étape 3 : Créer les Index Firestore

### 3.1 Pourquoi des index ?

Firestore nécessite des index pour certaines requêtes (ex: tri + filtre). Nous avons préparé les index nécessaires.

### 3.2 Fichier d'index

Le fichier `firebase/indexes/firestore.indexes.json` définit :

```json
{
  "indexes": [
    {
      "collectionGroup": "stats",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "uid", "order": "ASCENDING" },
        { "fieldPath": "lastPracticeAt", "order": "DESCENDING" }
      ]
    }
  ]
}
```

### 3.3 Déployer les index

#### Option A : Via Firebase CLI
```bash
firebase deploy --only firestore:indexes
```

#### Option B : Via Firebase Console
1. Allez dans **Firestore Database** → **Indexes**
2. Cliquez **"Add index"**
3. Configurez :
   - Collection ID: `stats`
   - Fields to index:
     - `uid` : Ascending
     - `lastPracticeAt` : Descending
   - Query scope: Collection
4. Cliquez **"Create"**
5. ⏳ Attendez que le status passe à "Enabled" (1-5 minutes)

---

## 🔗 Étape 4 : Lier Auth et Firestore dans l'App

### 4.1 Vérifier la dépendance Firestore

Dans `app/build.gradle.kts`, vérifiez que cette ligne est présente :

```kotlin
implementation("com.google.firebase:firebase-firestore-ktx")
```

### 4.2 Code de liaison (Déjà implémenté)

Le `SyncManager` créé automatiquement le profil Firestore au premier login :

**Fichier : `data/sync/SyncManager.kt`**
```kotlin
fun startSync(uid: String) {
    // Observe le profil utilisateur
    userProfileRepository.getUserProfile(uid)
        .onEach { profile ->
            if (profile == null) {
                // Pas de profil → Créer automatiquement
                createUserProfile(uid)
            }
        }
}
```

### 4.3 Déclencher la création au login

**Fichier : `presentation/navigation/OraAuthViewModel.kt`** (déjà configuré)

```kotlin
@HiltViewModel
class OraAuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    // Utilisateur connecté → Démarrer sync Firestore
                    syncManager.startSync(user.uid)
                } else {
                    // Déconnecté → Arrêter sync
                    syncManager.stopSync()
                }
            }
        }
    }
}
```

---

## 🧪 Étape 5 : Tester la Configuration

### 5.1 Build et installer l'app

```bash
./gradlew installDebug
```

### 5.2 Scénario de test complet

#### Test 1 : Nouveau compte Email/Password

1. **Lancez l'app** sur l'émulateur
2. **Créez un nouveau compte** :
   - Email : `test@ora.com`
   - Password : `Test1234`
3. **Connectez-vous**
4. **Vérifiez dans Firebase Console** :
   - Allez dans **Firestore Database** → **Data**
   - Vous devriez voir 2 collections :

     **Collection `users` :**
     ```
     users/
       └── {uid-du-user}/
             ├── uid: "abc123..."
             ├── firstName: null
             ├── photoUrl: null
             ├── planTier: "free"
             ├── createdAt: 1728054321000
             ├── locale: null
     ```

     **Collection `stats` :**
     ```
     stats/
       └── {uid-du-user}/
             ├── uid: "abc123..."
             ├── totalMinutes: 0
             ├── sessions: 0
             ├── streakDays: 0
             ├── lastPracticeAt: null
             ├── updatedAt: 1728054321000
     ```

5. ✅ **Si les 2 documents apparaissent, Firestore est opérationnel !**

#### Test 2 : Connexion Google

1. **Déconnectez-vous** de l'app
2. **Reconnectez-vous avec Google**
3. **Vérifiez Firestore** :
   - Un nouveau document dans `users/{uid-google}` devrait apparaître
   - `firstName` devrait contenir votre prénom Google

#### Test 3 : Vérifier l'isolation des données

1. **Créez un 2e compte** (autre email)
2. **Dans Firestore Console**, vérifiez que vous voyez 2 UID différents
3. **Essayez d'accéder aux données cross-user** (impossible grâce aux rules)

---

## 🐛 Dépannage

### Problème 1 : "Permission denied" lors de l'écriture

**Symptôme :**
```
FirebaseFirestoreException: PERMISSION_DENIED: Missing or insufficient permissions
```

**Solutions :**
1. ✅ Vérifier que l'utilisateur est bien authentifié (`request.auth != null`)
2. ✅ Vérifier que le UID du document correspond au UID de l'utilisateur connecté
3. ✅ Vérifier que les règles Firestore sont bien déployées (étape 2)
4. ✅ Attendre 1-2 minutes après le déploiement des règles (propagation)

**Test rapide :**
```bash
# Dans Firebase Console → Firestore → Rules → "Rules Playground"
# Simuler une lecture sur users/{votre-uid}
# Devrait retourner "Allowed" si les règles sont correctes
```

### Problème 2 : Documents non créés automatiquement

**Symptôme :** Collections `users/` et `stats/` vides après login

**Solutions :**
1. ✅ Vérifier les logs Timber dans Android Studio Logcat :
   ```
   Filtre : "SyncManager"
   Rechercher : "Creating default profile for user"
   ```

2. ✅ Vérifier que `AUTO_CREATE_PROFILE` flag est activé :
   ```kotlin
   // config/flags.json
   "AUTO_CREATE_PROFILE": { "enabled": true }
   ```

3. ✅ Forcer la création manuelle via debug :
   ```kotlin
   // Dans ProfileViewModel ou debug screen
   viewModelScope.launch {
       val uid = Firebase.auth.currentUser?.uid ?: return@launch
       userProfileRepository.createUserProfile(
           UserProfile.createDefault(uid, "Test User")
       )
   }
   ```

### Problème 3 : "Index required" error

**Symptôme :**
```
The query requires an index. You can create it here: https://...
```

**Solutions :**
1. ✅ Cliquez sur le lien dans l'erreur (ouvre Firebase Console)
2. ✅ Cliquez "Create index"
3. ✅ Attendez que l'index soit "Enabled" (quelques minutes)
4. ✅ Réessayez la requête

**Ou déployez tous les index d'un coup :**
```bash
firebase deploy --only firestore:indexes
```

### Problème 4 : Firestore en mode offline (grisé)

**Symptôme :** Données non synchronisées, icône "offline" dans Firebase Console

**Solutions :**
1. ✅ Vérifier la connexion Internet de l'émulateur
2. ✅ Vérifier que le cache Firestore est activé :
   ```kotlin
   // di/FirestoreModule.kt
   .setPersistenceEnabled(true)  // Doit être true
   ```

3. ✅ Vider le cache de l'app :
   ```bash
   adb shell pm clear com.ora.wellbeing.debug
   ```

---

## 📊 Vérification Finale

### Checklist de validation

- [ ] ✅ Firestore activé dans Firebase Console
- [ ] ✅ Règles de sécurité déployées et testées
- [ ] ✅ Index créés et "Enabled"
- [ ] ✅ Collection `users/` contient des documents après login
- [ ] ✅ Collection `stats/` contient des documents après login
- [ ] ✅ Chaque utilisateur voit uniquement ses propres données
- [ ] ✅ ProfileScreen affiche le nom d'utilisateur (ou "Invité")
- [ ] ✅ HomeScreen affiche "Bonjour {firstName}"
- [ ] ✅ Stats s'affichent (0 sessions, 0 minutes au début)

---

## 🎉 Prochaines Étapes

Une fois Firestore opérationnel :

1. **Tester la modification de profil** :
   - Changer le prénom dans ProfileScreen
   - Vérifier que Firestore se met à jour en temps réel

2. **Tester l'enregistrement d'une séance** :
   - Terminer une vidéo/audio de 10 minutes
   - Vérifier que `totalMinutes` passe à 10 dans Firestore

3. **Tester le mode offline** :
   - Désactiver le Wi-Fi sur l'émulateur
   - Vérifier que l'app affiche les données en cache
   - Réactiver le Wi-Fi
   - Vérifier la synchronisation automatique

4. **Configurer la sauvegarde** :
   - Activer les backups automatiques dans Firebase Console
   - Firestore → Settings → Backups

5. **Monitorer les coûts** :
   - Firebase Console → Usage and billing
   - Firestore → Usage
   - Vérifier que vous restez dans les limites gratuites

---

## 📞 Support

- **Documentation Firebase Firestore** : https://firebase.google.com/docs/firestore
- **Documentation Ora** : Voir `docs/FIRESTORE_IMPLEMENTATION.md`
- **Règles de sécurité** : Voir `docs/firestore_rules_notes.md`

---

**Bonne configuration ! 🚀**
