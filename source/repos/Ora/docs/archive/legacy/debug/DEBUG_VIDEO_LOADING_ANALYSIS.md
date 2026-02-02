# 🔍 Analyse du Système de Chargement Vidéo - Lecteur Yoga

## 📋 Résumé du Problème

**Symptôme** : La dernière leçon créée et uploadée dans l'app ne se charge pas dans le lecteur Yoga.

**Date d'Analyse** : 2026-01-14

---

## 🏗️ Architecture du Système de Chargement

### 1. **Flux de Chargement (Data Flow)**

```
User clicks video
       ↓
YogaPlayerScreen (UI)
       ↓
YogaPlayerViewModel.loadPractice(practiceId)
       ↓
PracticeRepository.getById(practiceId)
       ↓
ContentDao.getContentById(contentId) [Room Database - Offline First]
       ↓
   ┌─────────────┴─────────────┐
   │                           │
   ✓ Found in Room             ✗ Not Found
   │                           │
   ↓                           ↓
getSignedDownloadUrl()    Fallback to mockPractices
   │                      or createFallbackPractice()
   ↓
Firebase Storage
.reference.child(storagePath)
.downloadUrl.await()
   ↓
Return signed URL
   ↓
PracticePlayerEnhanced.prepare(mediaUrl)
   ↓
ExoPlayer loads and plays video
```

---

## 🔑 Points Critiques Identifiés

### 1. **Room Database (ContentDao) - Premier Point de Vérification** ⚠️

**Fichier** : [ContentDao.kt](app/src/main/java/com/ora/wellbeing/data/local/dao/ContentDao.kt:46)

```kotlin
@Query("SELECT * FROM content WHERE id = :contentId")
suspend fun getContentById(contentId: String): Content?
```

**Problème Potentiel #1** : La nouvelle leçon n'est peut-être **pas synchronisée dans Room**.

#### Causes Possibles :
- ❌ Aucun WorkManager ou service de sync Firestore → Room trouvé
- ❌ Pas de listener en temps réel sur Firestore pour auto-sync
- ❌ La leçon existe dans Firestore mais **jamais insérée dans Room**
- ❌ L'app utilise une architecture offline-first mais le sync initial n'a pas été fait

**Vérification Nécessaire** :
```sql
-- Vérifier si la leçon existe dans Room
SELECT * FROM content WHERE id = 'NEW_LESSON_ID';
```

---

### 2. **Firebase Storage - Génération d'URL Signée** 🔐

**Fichier** : [PracticeRepository.kt:148-178](app/src/main/java/com/ora/wellbeing/core/data/practice/PracticeRepository.kt:148-178)

```kotlin
private suspend fun getSignedDownloadUrl(audioUrl: String?, videoUrl: String?): String {
    val storagePath = audioUrl ?: videoUrl

    if (storagePath.isNullOrBlank()) {
        return "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3" // Fallback
    }

    // Si déjà une URL HTTP, retourne tel quel
    if (storagePath.startsWith("http://") || storagePath.startsWith("https://")) {
        return storagePath
    }

    // Sinon, récupère l'URL signée depuis Firebase Storage
    val storageRef = firebaseStorage.reference.child(storagePath)
    val downloadUrl = storageRef.downloadUrl.await()
    return downloadUrl.toString()
}
```

**Problème Potentiel #2** : L'URL de stockage dans Room est **incorrecte ou mal formée**.

#### Causes Possibles :
- ❌ Le champ `audioUrl` ou `videoUrl` dans Room pointe vers un chemin invalide
- ❌ Le chemin Firebase Storage est incorrect : `media/lessons/ABC/audio/high.m4a` vs `lessons/ABC/audio/high.m4a`
- ❌ Le fichier n'existe pas réellement dans Firebase Storage (upload incomplet)
- ❌ Permissions Firebase Storage : l'utilisateur n'a pas accès au fichier
- ❌ Erreur réseau lors de la récupération de l'URL signée (timeout)

**Vérification Nécessaire** :
```kotlin
// Logs à ajouter dans PracticeRepository
Timber.d("Storage path from Room: audioUrl=$audioUrl, videoUrl=$videoUrl")
Timber.d("Attempting to get signed URL for: $storagePath")
Timber.d("Firebase Storage ref: ${storageRef.path}")
```

---

### 3. **Fallback System - Masquage des Erreurs** ⚠️

**Fichier** : [PracticeRepository.kt:104-135](app/src/main/java/com/ora/wellbeing/core/data/practice/PracticeRepository.kt:104-135)

```kotlin
suspend fun getById(id: String): Result<Practice> {
    val content = contentDao.getContentById(id)

    if (content != null) {
        // Trouvé dans Room
        val mediaUrl = getSignedDownloadUrl(content.audioUrl, content.videoUrl)
        return Result.success(content.toPractice(mediaUrl))
    } else {
        // FALLBACK : Mock data ou création fallback
        val mockPractice = mockPractices.find { it.id == id }
        if (mockPractice != null) {
            return Result.success(mockPractice)
        } else {
            return Result.success(createFallbackPractice(id))
        }
    }
}
```

**Problème Potentiel #3** : Le système de **fallback masque les vraies erreurs**.

#### Conséquences :
- ✅ L'app ne crash jamais (bon)
- ❌ L'utilisateur voit une vidéo **BigBuckBunny** au lieu de sa vraie leçon (mauvais)
- ❌ Aucune erreur n'est remontée à l'UI, donc impossible de savoir qu'il y a un problème
- ❌ Les logs disent "Content not found in Room for ID: XXX, using fallback"

**Symptôme Visible** :
```
User: "Ma vidéo de yoga ne charge pas"
Reality: Une vidéo de lapin (BigBuckBunny) se charge à la place ¯\_(ツ)_/¯
```

---

### 4. **ExoPlayer & Cache - Problème de Chargement Vidéo** 🎬

**Fichier** : [PracticePlayerEnhanced.kt:355-377](app/src/main/java/com/ora/wellbeing/feature/practice/player/PracticePlayerEnhanced.kt:355-377)

```kotlin
fun prepare(url: String, startPosition: Long = 0L) {
    try {
        val mediaItem = MediaItem.fromUri(url)
        player?.setMediaItem(mediaItem)
        player?.prepare()

        Timber.d("Media prepared: $url (start: $startPosition)")
    } catch (e: Exception) {
        Timber.e(e, "Error preparing media")
        _state.value = _state.value.copy(error = "Erreur de préparation: ${e.message}")
    }
}
```

**Problème Potentiel #4** : ExoPlayer reçoit une **URL invalide ou expirée**.

#### Causes Possibles :
- ❌ URL signée Firebase expirée (durée de vie limitée, généralement 1h)
- ❌ URL mal formée
- ❌ Problème de cache ExoPlayer (ancien fichier corrompu)
- ❌ Problème réseau (pas de connexion pour télécharger)
- ❌ Format vidéo non supporté

**Erreurs ExoPlayer à Surveiller** :
- `ERROR_CODE_IO_NETWORK_CONNECTION_FAILED` : Pas de réseau
- `ERROR_CODE_IO_FILE_NOT_FOUND` : Fichier introuvable (404)
- `ERROR_CODE_IO_BAD_HTTP_STATUS` : Erreur HTTP (403, 401, etc.)

---

## 🔍 Logs à Vérifier

### Logs Critiques à Chercher dans Logcat

```bash
# 1. Vérifier si la leçon est trouvée dans Room
D/PracticeRepository: Loading practice from Room: id=<LESSON_ID>
D/PracticeRepository: Found content in Room: title=<TITLE>

# 2. Vérifier l'URL de stockage
D/PracticeRepository: Storage path from Room: audioUrl=..., videoUrl=...
D/PracticeRepository: Getting signed download URL for path: ...

# 3. Vérifier l'URL signée
D/PracticeRepository: Got signed download URL: https://firebasestorage...

# 4. Vérifier la préparation ExoPlayer
D/PracticePlayerEnhanced: Media prepared: https://... (start: 0)

# 5. Vérifier les erreurs
W/PracticeRepository: Content not found in Room for ID: ..., trying mock data
E/PracticePlayerEnhanced: Playback error occurred
E/PracticeRepository: Failed to get signed download URL, using fallback
```

---

## 🐛 Scénarios d'Échec Identifiés

### **Scénario 1 : Leçon Jamais Synchronisée dans Room** (Plus Probable)

```
1. Admin crée leçon dans Firestore
2. Admin upload vidéo dans Firebase Storage
3. L'app de l'utilisateur NE synchronise PAS automatiquement
4. L'utilisateur clique sur la vidéo
5. Room ne trouve rien → Fallback → Vidéo BigBuckBunny
```

**Solution** :
- ✅ Implémenter un système de sync Firestore → Room au démarrage de l'app
- ✅ Utiliser `addSnapshotListener` sur la collection Firestore `content`
- ✅ Créer un WorkManager pour sync périodique

---

### **Scénario 2 : URL de Stockage Incorrecte**

```
1. Leçon existe dans Room
2. audioUrl/videoUrl pointe vers un mauvais chemin
3. Firebase Storage.reference.child(wrongPath) → 404
4. Catch exception → Fallback URL
5. ExoPlayer charge BigBuckBunny au lieu de la vraie vidéo
```

**Solution** :
- ✅ Vérifier le format du chemin dans Room : `media/lessons/<ID>/video/1080p.mp4`
- ✅ Logger le chemin exact utilisé pour Firebase Storage
- ✅ Vérifier manuellement dans Firebase Console si le fichier existe

---

### **Scénario 3 : Permissions Firebase Storage**

```
1. Leçon existe, URL correcte
2. Firebase Storage rules bloquent l'accès
3. downloadUrl.await() → Exception (403 Forbidden)
4. Catch → Fallback
5. Vidéo fallback chargée
```

**Solution** :
- ✅ Vérifier les règles Firebase Storage :
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /media/lessons/{lessonId}/{allPaths=**} {
      allow read: if request.auth != null; // ← Vérifier cette règle
    }
  }
}
```

---

### **Scénario 4 : URL Signée Expirée**

```
1. URL récupérée et mise en cache
2. 1 heure passe
3. URL signée expire
4. ExoPlayer essaie de charger → 403
5. Erreur de lecture
```

**Solution** :
- ✅ Ne PAS mettre en cache les URLs signées
- ✅ Régénérer l'URL à chaque chargement
- ✅ Le code actuel le fait déjà : `downloadUrl.await()` est appelé à chaque fois

---

## 🛠️ Solutions Recommandées

### **Solution Immédiate : Ajouter des Logs Détaillés**

**Fichier à modifier** : [PracticeRepository.kt](app/src/main/java/com/ora/wellbeing/core/data/practice/PracticeRepository.kt)

```kotlin
suspend fun getById(id: String): Result<Practice> {
    return try {
        Timber.d("🔍 Loading practice from Room: id=$id")

        val content = contentDao.getContentById(id)

        if (content != null) {
            Timber.d("✅ Found content in Room: title=${content.title}")
            Timber.d("📁 Storage paths - audioUrl=${content.audioUrl}, videoUrl=${content.videoUrl}")

            val mediaUrl = getSignedDownloadUrl(content.audioUrl, content.videoUrl)
            Timber.d("🔗 Signed media URL obtained: ${mediaUrl.take(100)}...")

            val practice = content.toPractice(mediaUrl)
            Result.success(practice)
        } else {
            Timber.w("⚠️ Content NOT found in Room for ID: $id")
            Timber.w("📦 Checking mock data...")

            val mockPractice = mockPractices.find { it.id == id }
            if (mockPractice != null) {
                Timber.i("✅ Using mock practice for ID: $id")
                Result.success(mockPractice)
            } else {
                Timber.w("🆘 No mock data, creating fallback practice for ID: $id")
                val fallbackPractice = createFallbackPractice(id)
                Result.success(fallbackPractice)
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "❌ ERROR loading practice $id")
        Result.failure(e)
    }
}

private suspend fun getSignedDownloadUrl(audioUrl: String?, videoUrl: String?): String {
    try {
        val storagePath = audioUrl ?: videoUrl

        Timber.d("🔍 getSignedDownloadUrl - audioUrl=$audioUrl, videoUrl=$videoUrl")
        Timber.d("📂 Selected storage path: $storagePath")

        if (storagePath.isNullOrBlank()) {
            Timber.w("⚠️ Storage path is NULL or BLANK, using fallback")
            return "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        }

        if (storagePath.startsWith("http://") || storagePath.startsWith("https://")) {
            Timber.d("✅ URL already resolved: $storagePath")
            return storagePath
        }

        Timber.d("🔥 Getting signed download URL from Firebase Storage...")
        Timber.d("📁 Firebase path: $storagePath")

        val storageRef = firebaseStorage.reference.child(storagePath)
        Timber.d("📍 Firebase Storage reference: ${storageRef.path}")

        val downloadUrl = storageRef.downloadUrl.await()
        val signedUrl = downloadUrl.toString()

        Timber.d("✅ Got signed download URL: ${signedUrl.take(150)}...")
        return signedUrl

    } catch (e: Exception) {
        Timber.e(e, "❌ FAILED to get signed download URL")
        Timber.e("🆘 Using fallback URL due to error")
        return "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
    }
}
```

---

### **Solution À Moyen Terme : Implémenter le Sync Firestore → Room**

**Créer un nouveau fichier** : `ContentSyncRepository.kt`

```kotlin
@Singleton
class ContentSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val contentDao: ContentDao
) {

    suspend fun syncAllContent(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("🔄 Starting Firestore → Room sync...")

            val snapshot = firestore.collection("content")
                .get()
                .await()

            val contentList = snapshot.documents.mapNotNull { doc ->
                doc.toObject<FirestoreContent>()?.toRoomEntity()
            }

            Timber.d("✅ Fetched ${contentList.size} items from Firestore")

            contentDao.insertAllContent(contentList)

            Timber.d("✅ Sync complete: ${contentList.size} items saved to Room")
            Result.success(Unit)

        } catch (e: Exception) {
            Timber.e(e, "❌ Firestore sync failed")
            Result.failure(e)
        }
    }

    fun observeContentChanges() {
        firestore.collection("content")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Firestore listener error")
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    when (change.type) {
                        DocumentChange.Type.ADDED,
                        DocumentChange.Type.MODIFIED -> {
                            // Insert/Update in Room
                            val content = change.document.toObject<FirestoreContent>()
                            viewModelScope.launch {
                                contentDao.insertContent(content.toRoomEntity())
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            // Delete from Room
                            viewModelScope.launch {
                                contentDao.deleteContentById(change.document.id)
                            }
                        }
                    }
                }
            }
    }
}
```

**Appeler au démarrage de l'app** :

```kotlin
@HiltViewModel
class AppViewModel @Inject constructor(
    private val contentSyncRepository: ContentSyncRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            contentSyncRepository.syncAllContent()
            contentSyncRepository.observeContentChanges() // Real-time sync
        }
    }
}
```

---

### **Solution À Long Terme : Améliorer la Gestion d'Erreurs**

**Modifier le ViewModel pour exposer les vraies erreurs** :

```kotlin
data class YogaPlayerState(
    val practice: Practice? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val errorType: ErrorType? = null, // ← NOUVEAU
    // ... autres champs
)

enum class ErrorType {
    CONTENT_NOT_FOUND_IN_ROOM,
    FIREBASE_STORAGE_ERROR,
    NETWORK_ERROR,
    PLAYBACK_ERROR,
    UNKNOWN
}
```

**UI affichant l'erreur détaillée** :

```kotlin
when (errorType) {
    ErrorType.CONTENT_NOT_FOUND_IN_ROOM -> {
        Text("Cette leçon n'est pas encore disponible. Synchronisez votre contenu.")
        Button(onClick = { viewModel.syncContent() }) { Text("Synchroniser") }
    }
    ErrorType.FIREBASE_STORAGE_ERROR -> {
        Text("Erreur de téléchargement. Vérifiez votre connexion.")
    }
    // ...
}
```

---

## 📊 Checklist de Débugage

### Étape 1 : Vérifier Room Database
```bash
# Via Android Studio Database Inspector
1. Ouvrir Database Inspector
2. Sélectionner "content" table
3. Chercher l'ID de la nouvelle leçon
4. Vérifier les colonnes audioUrl et videoUrl
```

**Questions** :
- [ ] La leçon existe-t-elle dans Room ?
- [ ] Les URLs audioUrl/videoUrl sont-elles renseignées ?
- [ ] Les URLs commencent-elles par `media/lessons/` ?

---

### Étape 2 : Vérifier Firebase Storage
```bash
# Via Firebase Console
1. Aller dans Storage
2. Naviguer vers media/lessons/<LESSON_ID>/
3. Vérifier que le fichier vidéo existe
```

**Questions** :
- [ ] Le fichier vidéo existe-t-il dans Storage ?
- [ ] Le chemin correspond-il à celui dans Room ?
- [ ] Les permissions permettent-elles la lecture ?

---

### Étape 3 : Analyser les Logs
```bash
# Dans Logcat, filtrer par :
tag:PracticeRepository OR tag:PracticePlayerEnhanced
```

**Chercher** :
- [ ] "Content NOT found in Room for ID: XXX"
- [ ] "FAILED to get signed download URL"
- [ ] "Playback error occurred"
- [ ] "Using fallback URL"

---

### Étape 4 : Tester Manuellement
```kotlin
// Dans un test ou debug screen
viewModelScope.launch {
    val result = practiceRepository.getById("NEW_LESSON_ID")
    result.onSuccess { practice ->
        Log.d("TEST", "Practice loaded: ${practice.title}")
        Log.d("TEST", "Media URL: ${practice.mediaUrl}")
    }.onFailure { error ->
        Log.e("TEST", "Failed to load practice", error)
    }
}
```

---

## 🎯 Conclusion

### Cause la Plus Probable
**La nouvelle leçon n'a jamais été synchronisée de Firestore vers Room.**

### Prochaines Actions
1. ✅ Ajouter les logs détaillés dans `PracticeRepository`
2. ✅ Relancer l'app et reproduire le problème
3. ✅ Analyser les logs Logcat pour identifier le point de défaillance
4. ✅ Implémenter le système de sync Firestore → Room
5. ✅ Tester à nouveau le chargement de la nouvelle leçon

---

**Date** : 2026-01-14
**Analysé par** : Claude Sonnet 4.5
**Fichiers Clés** :
- [YogaPlayerViewModel.kt:52-83](app/src/main/java/com/ora/wellbeing/feature/practice/player/specialized/yoga/YogaPlayerViewModel.kt:52-83)
- [PracticeRepository.kt:104-178](app/src/main/java/com/ora/wellbeing/core/data/practice/PracticeRepository.kt:104-178)
- [PracticePlayerEnhanced.kt:355-377](app/src/main/java/com/ora/wellbeing/feature/practice/player/PracticePlayerEnhanced.kt:355-377)
- [ContentDao.kt:46](app/src/main/java/com/ora/wellbeing/data/local/dao/ContentDao.kt:46)
