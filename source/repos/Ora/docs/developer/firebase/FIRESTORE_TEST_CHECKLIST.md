# ✅ Checklist de Test Firestore - Ora Android

## 🎯 Corrections Appliquées

### Problème Identifié
❌ **Collections mal nommées** : Le code utilisait `user_profiles` et `user_stats` mais les règles Firestore attendent `users` et `stats`.

### Solution Appliquée
✅ Changé `COLLECTION_USER_PROFILES = "user_profiles"` → `"users"`
✅ Changé `COLLECTION_USER_STATS = "user_stats"` → `"stats"`

---

## 📱 Test 1 : Installer l'App Mise à Jour

### Étape 1 : Connecter un device

**Option A : Émulateur Android Studio**
```bash
# Démarrer l'émulateur depuis Android Studio
# Outils → Device Manager → Play button sur un émulateur
```

**Option B : Téléphone physique**
```bash
# Activer le mode développeur sur votre téléphone
# Paramètres → À propos → Taper 7x sur "Numéro de build"
# Activer "Débogage USB"
# Connecter via USB
```

### Étape 2 : Installer l'APK

```bash
cd C:\Users\chris\source\repos\Ora
./gradlew installDebug
```

**Sortie attendue :**
```
> Task :app:installDebug
Installing APK 'app-debug.apk' on 'Device_Name'
Installed on 1 device.
BUILD SUCCESSFUL
```

---

## 🔍 Test 2 : Vérifier la Création Automatique des Collections

### Scénario A : Nouveau Compte Email/Password

1. **Lancez l'app** sur le device
2. **Créez un nouveau compte** :
   - Email : `test-firestore@ora.com`
   - Password : `Test123456`
3. **Cliquez "Créer mon compte"**
4. **Attendez 2-3 secondes** (création en arrière-plan)

### Vérification dans Firebase Console

1. **Ouvrez** : https://console.firebase.google.com/project/ora-wellbeing/firestore/databases/-default-/data

2. **Vous devriez voir 2 collections** :

#### Collection `users`
```
users/
  └── {uid-long-string}/
        ├── uid: "abc123xyz..."
        ├── email: "test-firestore@ora.com"
        ├── firstName: null
        ├── lastName: null
        ├── photoUrl: null
        ├── motto: "Je prends soin de moi chaque jour"
        ├── planTier: "free"
        ├── createdAt: [Timestamp]
        └── updatedAt: [Timestamp]
```

#### Collection `stats`
```
stats/
  └── {même-uid}/
        ├── uid: "abc123xyz..."
        ├── totalMinutes: 0
        ├── totalSessions: 0
        ├── yogaMinutes: 0
        ├── pilatesMinutes: 0
        ├── meditationMinutes: 0
        ├── breathingMinutes: 0
        ├── currentStreak: 0
        ├── longestStreak: 0
        ├── hasGratitudeToday: false
        ├── totalGratitudes: 0
        ├── activeGoals: []
        ├── completedGoals: []
        ├── badges: []
        └── updatedAt: [Timestamp]
```

### ✅ Critères de Succès

- [ ] Collection `users` existe
- [ ] Collection `stats` existe
- [ ] Les 2 documents ont le même `uid`
- [ ] `email` est rempli avec votre email de test
- [ ] `planTier` = "free"
- [ ] `totalSessions` = 0
- [ ] Timestamps `createdAt` et `updatedAt` sont présents

---

## 🧪 Test 3 : Vérifier le ProfileScreen

1. **Dans l'app**, allez sur l'onglet **"Profil"** (icône utilisateur)
2. **Vérifications** :

### Affichage Attendu
```
┌─────────────────────────────┐
│    👤  Invité               │ ← Si firstName = null
│    Gratuit                  │ ← Plan tier
├─────────────────────────────┤
│ Cette semaine               │
│ 🧘 Yoga        0 min        │
│ 🏋️ Pilates     0 min        │
│ 🧘‍♀️ Méditation 0 min        │
│ 🌬️ Respiration 0 min        │
├─────────────────────────────┤
│ 📊 Statistiques             │
│ Total: 0 séances            │
│ Série: 0 jours              │
└─────────────────────────────┘
```

### ✅ Critères
- [ ] Nom affiché : "Invité" (car firstName = null)
- [ ] Plan : "Gratuit"
- [ ] Stats : Toutes à 0
- [ ] Pas de message d'erreur
- [ ] Pas de crash

---

## 🧪 Test 4 : Connexion Google

1. **Déconnectez-vous** de l'app
2. **Cliquez "Continuer avec Google"**
3. **Sélectionnez un compte Google**

### Vérification dans Firebase Console

Après login Google réussi :

1. **Nouvelle ligne dans `users`** avec un UID différent
2. **`firstName`** devrait contenir votre prénom Google
3. **`email`** = votre email Google
4. **`photoUrl`** = URL de votre photo Google (si disponible)

### Dans ProfileScreen
```
┌─────────────────────────────┐
│    👤  John                 │ ← Votre prénom Google
│    🌟 GRATUIT               │
└─────────────────────────────┘
```

---

## 🐛 Test 5 : Vérifier les Logs

### Dans Android Studio Logcat

Filtrez par `SyncManager` :

```
Timber.d: SyncManager: Starting sync for uid=abc123...
Timber.d: UserProfileRepository: Setting up listener for uid=abc123...
Timber.d: UserProfileRepository: Profile doesn't exist yet
Timber.d: SyncManager: Profile doesn't exist, creating default profile
Timber.d: UserProfileRepository: Setting profile for uid=abc123...
Timber.d: UserProfileRepository: Profile set successfully
Timber.d: SyncManager: Default profile created successfully
Timber.d: UserProfileRepository: Profile updated: null
Timber.d: UserStatsRepository: Setting up listener for uid=abc123...
Timber.d: SyncManager: Stats don't exist, creating default stats
Timber.d: UserStatsRepository: Stats set successfully
Timber.d: SyncManager: Default stats created successfully
```

### ✅ Logs Attendus
- [ ] "Starting sync for uid=..."
- [ ] "Profile doesn't exist, creating default profile"
- [ ] "Profile set successfully"
- [ ] "Stats set successfully"
- [ ] "Default stats created successfully"

### ❌ Logs d'Erreur à Surveiller
- ❌ "PERMISSION_DENIED" → Règles Firestore mal configurées
- ❌ "Error setting profile" → Vérifier la structure du modèle
- ❌ "Network error" → Vérifier connexion Internet

---

## 🧪 Test 6 : Mode Offline

1. **Déconnectez le Wi-Fi/Data** sur le device
2. **Redémarrez l'app**
3. **Vérifications** :
   - [ ] ProfileScreen affiche "Invité" (données en cache)
   - [ ] Stats affichent 0 (cache)
   - [ ] Pas de crash
4. **Reconnectez Internet**
5. **Attendez 2-3 secondes**
6. **Vérifications** :
   - [ ] Données Firestore synchronisées
   - [ ] ProfileScreen se met à jour si modifications

---

## 🎯 Checklist Finale

### Infrastructure Firebase
- [ ] Firestore activé dans Firebase Console
- [ ] Région sélectionnée : `eur3 (europe-west)`
- [ ] Règles déployées (collection `users` et `stats`)
- [ ] Index créés (optionnel pour démarrage)

### Code Android
- [ ] Collection `users` (pas `user_profiles`) ✅
- [ ] Collection `stats` (pas `user_stats`) ✅
- [ ] SyncManager appelé dans OraAuthViewModel ✅
- [ ] Dépendance `firebase-firestore-ktx` présente ✅

### Tests Fonctionnels
- [ ] Nouveau compte → 2 collections créées
- [ ] ProfileScreen affiche "Invité"
- [ ] Stats à 0
- [ ] Google login → profil avec prénom
- [ ] Mode offline → pas de crash
- [ ] Logs Timber sans erreur PERMISSION_DENIED

---

## 📸 Captures d'Écran Attendues

### Firebase Console - Collection users
![users collection](https://via.placeholder.com/800x300?text=Collection+users+avec+documents)

### Firebase Console - Collection stats
![stats collection](https://via.placeholder.com/800x300?text=Collection+stats+avec+documents)

### App - ProfileScreen
![profile screen](https://via.placeholder.com/400x800?text=ProfileScreen+avec+Invit%C3%A9)

---

## 🆘 En Cas de Problème

### Problème 1 : Collections toujours vides

**Diagnostic :**
```bash
# Vérifier les logs
adb logcat | Select-String "SyncManager|UserProfile|UserStats|PERMISSION"
```

**Solutions :**
1. Vérifier que les règles Firestore sont déployées
2. Vérifier que l'utilisateur est bien authentifié
3. Vérifier la connexion Internet
4. Forcer la suppression du cache : `adb shell pm clear com.ora.wellbeing.debug`

### Problème 2 : PERMISSION_DENIED

**Cause** : Règles Firestore pas déployées ou incorrectes

**Solution :**
1. Retournez dans Firebase Console → Firestore → Rules
2. Vérifiez que vous avez bien :
   ```javascript
   match /users/{uid} {
     allow read, write: if request.auth != null && request.auth.uid == uid;
   }
   match /stats/{uid} {
     allow read, write: if request.auth != null && request.auth.uid == uid;
   }
   ```
3. Cliquez **"Publish"**
4. Attendez 1-2 minutes
5. Réessayez

### Problème 3 : App crash au login

**Diagnostic :**
```bash
# Voir le stacktrace complet
adb logcat | Select-String "FATAL|AndroidRuntime"
```

**Solutions courantes :**
- Vérifier que `google-services.json` est bien présent dans `app/`
- Rebuild : `./gradlew clean assembleDebug`
- Vérifier Hilt : Tous les modules sont bien annotés `@Module` ?

---

## ✅ Validation Finale

Une fois tous les tests passés :

- [ ] ✅ Firestore est opérationnel
- [ ] ✅ Profils créés automatiquement au login
- [ ] ✅ Stats initialisées à 0
- [ ] ✅ ProfileScreen affiche les données
- [ ] ✅ Mode offline fonctionne
- [ ] ✅ Aucune erreur dans les logs

🎉 **Firestore est maintenant lié à Firebase Authentication et prêt pour la production !**

---

**Date de création** : 2025-10-04
**Version App** : Debug
**Auteur** : Claude Code
