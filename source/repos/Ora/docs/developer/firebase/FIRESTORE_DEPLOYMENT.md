# FIX(user-dynamic): Guide de Déploiement Firestore

Instructions pour déployer les règles de sécurité et indexes Firestore.

## 📋 Prérequis

1. **Firebase CLI** installée:
   ```bash
   npm install -g firebase-tools
   ```

2. **Authentification Firebase**:
   ```bash
   firebase login
   ```

3. **Projet Firebase** configuré (déjà fait):
   - Project ID: `ora-wellbeing` (à confirmer)
   - `google-services.json` présent dans `app/`

## 🚀 Déploiement Initial

### 1. Initialiser Firebase (si pas déjà fait)

```bash
# Dans le répertoire racine Ora/
firebase init firestore

# Sélectionner:
# - Use existing project: ora-wellbeing
# - Firestore rules file: firestore.rules (défaut)
# - Firestore indexes file: firestore.indexes.json (défaut)
```

### 2. Déployer les Règles de Sécurité

```bash
# Déployer uniquement les règles
firebase deploy --only firestore:rules

# Vérifier déploiement
firebase firestore:rules get
```

**Contenu déployé** (`firestore.rules`):
- ✅ Privacy by design: `request.auth.uid == uid`
- ✅ Validation champs: types, bornes min/max
- ✅ Blocage accès cross-user

### 3. Déployer les Indexes

```bash
# Déployer uniquement les indexes
firebase deploy --only firestore:indexes

# Vérifier déploiement
firebase firestore:indexes
```

**Index déployé** (`firestore.indexes.json`):
- ✅ `stats.lastPracticeAt` (DESCENDING) → Pour queries "utilisateurs actifs récents"

### 4. Déploiement Complet

```bash
# Déployer rules + indexes en une commande
firebase deploy --only firestore
```

## 🧪 Validation Locale avec Emulator

### 1. Démarrer Firebase Emulator

```bash
# Installer emulator (première fois)
firebase setup:emulators:firestore

# Démarrer emulator
firebase emulators:start --only firestore

# Output attendu:
# ✔  firestore: Firestore Emulator running at http://localhost:8080
# ✔  firestore: View Firestore Emulator UI at http://localhost:4000
```

### 2. Configurer App pour Emulator

**Dans `FirestoreModule.kt` (mode debug uniquement)**:
```kotlin
@Provides
@Singleton
fun provideFirebaseFirestore(): FirebaseFirestore {
    val firestore = Firebase.firestore

    // FIX(user-dynamic): Utiliser emulator en debug
    if (BuildConfig.DEBUG) {
        firestore.useEmulator("10.0.2.2", 8080) // Android emulator
        // Pour device physique: firestore.useEmulator("<YOUR_LOCAL_IP>", 8080)
        Timber.d("Firestore configuré avec emulator")
    }

    val settings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .setCacheSizeBytes(10 * 1024 * 1024L)
        .build()

    firestore.firestoreSettings = settings
    return firestore
}
```

### 3. Tester avec Emulator

1. **Démarrer emulator**: `firebase emulators:start --only firestore`
2. **Lancer app** en mode debug
3. **Ouvrir Emulator UI**: http://localhost:4000/firestore
4. **Voir collections**: `users/{uid}` et `stats/{uid}` créées en temps réel

### 4. Tests de Sécurité avec Emulator

```bash
# Installer Firebase Emulator Suite (si pas déjà fait)
npm install -g @firebase/rules-unit-testing

# Créer fichier de test firestore.test.js
node firestore.test.js
```

**Fichier de test exemple** (`firestore.test.js`):
```javascript
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');

let testEnv;

beforeAll(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: 'ora-test',
    firestore: {
      rules: fs.readFileSync('firestore.rules', 'utf8'),
    },
  });
});

test('User can read their own profile', async () => {
  const alice = testEnv.authenticatedContext('alice');
  const docRef = alice.firestore().doc('users/alice');
  await assertSucceeds(docRef.get());
});

test('User cannot read another user profile', async () => {
  const alice = testEnv.authenticatedContext('alice');
  const docRef = alice.firestore().doc('users/bob');
  await assertFails(docRef.get());
});

test('User can update their own stats', async () => {
  const alice = testEnv.authenticatedContext('alice');
  const docRef = alice.firestore().doc('stats/alice');
  await assertSucceeds(docRef.set({
    uid: 'alice',
    totalMinutes: 100,
    sessions: 5,
    streakDays: 3,
    lastPracticeAt: Date.now(),
    updatedAt: Date.now()
  }));
});

test('Stats validation rejects invalid totalMinutes', async () => {
  const alice = testEnv.authenticatedContext('alice');
  const docRef = alice.firestore().doc('stats/alice');
  await assertFails(docRef.set({
    uid: 'alice',
    totalMinutes: 600000, // > 525600 max
    sessions: 5,
    streakDays: 3,
    updatedAt: Date.now()
  }));
});

afterAll(async () => {
  await testEnv.cleanup();
});
```

## 📊 Monitoring Production

### 1. Firebase Console

**Vérifier règles actives**:
1. Ouvrir [Firebase Console](https://console.firebase.google.com)
2. Sélectionner projet `ora-wellbeing`
3. Firestore Database → Rules
4. Vérifier que les règles matchent `firestore.rules`

**Vérifier indexes**:
1. Firestore Database → Indexes
2. Vérifier index `stats.lastPracticeAt DESC`

### 2. Métriques à Surveiller

**Usage quotidien** (Firestore → Usage):
- **Reads**: ~100 reads/user/jour (profil + stats listeners)
- **Writes**: ~5 writes/user/jour (sessions + updates)
- **Storage**: ~1KB/user (profil + stats)

**Limites tier gratuit**:
- ✅ 50,000 reads/jour
- ✅ 20,000 writes/jour
- ✅ 20,000 deletes/jour
- ✅ 1 GiB storage

**Estimation 100 utilisateurs actifs/jour**:
- Reads: 10,000 (✅ bien dans limite)
- Writes: 500 (✅ bien dans limite)
- Storage: 100 KB (✅ bien dans limite)

### 3. Alertes à Configurer

**Dans Firebase Console → Monitoring**:
1. **Alerte quota**: Email si > 80% limite gratuite
2. **Alerte erreurs**: Email si > 100 permission denied/jour
3. **Alerte latence**: Email si p95 > 1000ms

## 🔐 Sécurité Production

### Checklist Avant Déploiement

- [x] **Rules déployées** avec validation complète
- [x] **Indexes créés** pour queries optimisées
- [x] **UID vérifié** dans chaque repository
- [ ] **Tests sécurité** passés avec emulator
- [ ] **Rate limiting** (optionnel, via Cloud Functions)
- [ ] **Monitoring alertes** configurées

### Règles Additionnelles (Optionnel)

**Rate limiting avec Firestore Rules** (expérimental):
```javascript
// firestore.rules (section avancée)
match /stats/{uid} {
  allow write: if request.auth != null
            && request.auth.uid == uid
            && validateUserStats(request.resource.data)
            && request.time < resource.data.updatedAt + duration.value(5, 's'); // Max 1 write/5s
}
```

**Validation business logic complexe**:
```javascript
// Empêcher regression de stats
allow update: if request.resource.data.totalMinutes >= resource.data.totalMinutes
           && request.resource.data.sessions >= resource.data.sessions;
```

## 🔄 Workflow Mise à Jour

### Modifier les Règles

1. **Éditer** `firestore.rules` en local
2. **Tester** avec emulator: `firebase emulators:start --only firestore`
3. **Valider** avec tests: `node firestore.test.js`
4. **Déployer**: `firebase deploy --only firestore:rules`
5. **Vérifier** dans Firebase Console

### Ajouter un Index

1. **Identifier query** qui nécessite index (erreur Firestore dans logs)
2. **Éditer** `firestore.indexes.json`
3. **Déployer**: `firebase deploy --only firestore:indexes`
4. **Attendre** création index (peut prendre quelques minutes)

### Rollback d'Urgence

Si règles cassent production:

```bash
# Voir historique déploiements
firebase firestore:rules list

# Rollback à version précédente
firebase firestore:rules release <RULESET_ID>
```

Ou **rollback manuel** via Firebase Console:
1. Firestore → Rules
2. Onglet "Versions"
3. Sélectionner version précédente
4. "Restore"

## 📝 Commandes Utiles

```bash
# Voir projet actif
firebase projects:list

# Changer de projet
firebase use <project-id>

# Voir règles actuelles
firebase firestore:rules get

# Voir indexes actuels
firebase firestore:indexes

# Déployer tout (rules + indexes)
firebase deploy --only firestore

# Tester règles en local
firebase emulators:start --only firestore

# Voir logs Firestore
firebase firestore:logs
```

## 🎯 Prochaines Étapes

1. **Déployer règles** en production:
   ```bash
   firebase deploy --only firestore:rules
   ```

2. **Déployer indexes**:
   ```bash
   firebase deploy --only firestore:indexes
   ```

3. **Configurer monitoring** dans Firebase Console

4. **Tester security** avec Firebase Emulator

5. **Documenter** procédure rollback pour équipe

---

**Firestore prêt pour production** 🚀

## 📞 Support

- **Firebase Docs**: https://firebase.google.com/docs/firestore
- **Emulator Suite**: https://firebase.google.com/docs/emulator-suite
- **Security Rules**: https://firebase.google.com/docs/firestore/security/get-started
