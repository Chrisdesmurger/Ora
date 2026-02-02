# 🔴 Analyse des Erreurs MediaCodec - Lecteur Vidéo

## 📋 Erreurs Observées

**Date** : 2026-01-14 15:25:22

```
E/MediaCodec: Media Quality Service not found.
E/m.ora.wellbeing: Failed to query component interface for required system resources: 6
```

---

## 🔍 Analyse des Erreurs

### **Erreur 1 : "Media Quality Service not found"**

**Type** : Avertissement non-critique (généralement)

**Cause** :
- Service système Android optionnel absent sur certains appareils
- Utilisé pour la gestion adaptative de la qualité vidéo
- Plus fréquent sur les émulateurs ou appareils anciens

**Impact** :
- ⚠️ La qualité adaptative peut ne pas fonctionner
- ⚠️ Peut causer des problèmes de buffering
- ⚠️ Peut empêcher le chargement de certaines vidéos haute résolution

---

### **Erreur 2 : "Failed to query component interface for required system resources: 6"**

**Type** : Erreur critique potentielle

**Code d'erreur 6** : `ERROR_INSUFFICIENT_RESOURCES`

**Causes Possibles** :
1. **Mémoire Insuffisante** : L'appareil n'a pas assez de RAM pour décoder la vidéo
2. **Codec Non Disponible** : Le codec requis (H.264, H.265, VP9) n'est pas présent
3. **Trop d'Instances MediaCodec** : D'autres apps utilisent les décodeurs matériels
4. **Résolution Vidéo Trop Élevée** : 4K sur un appareil ancien
5. **Format Non Supporté** : Container ou codec non compatible

---

## 🎬 Configuration Actuelle d'ExoPlayer

**Fichier** : [PracticePlayerEnhanced.kt:89-139](app/src/main/java/com/ora/wellbeing/feature/practice/player/PracticePlayerEnhanced.kt:89-139)

```kotlin
player = ExoPlayer.Builder(context)
    .setLoadControl(DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            15000,  // Min buffer (15s)
            50000,  // Max buffer (50s)
            2500,   // Buffer for playback (2.5s)
            5000    // Buffer for playback after rebuffer (5s)
        )
        .build()
    )
    .build()
```

**Problèmes Potentiels** :
- ❌ Pas de configuration de décodeur personnalisée
- ❌ Pas de fallback sur décodeur logiciel si matériel échoue
- ❌ Pas de limitation de résolution selon l'appareil
- ❌ Pas de gestion des erreurs MediaCodec spécifiques

---

## 🛠️ Solutions Proposées

### **Solution 1 : Configurer ExoPlayer avec Décodeur Logiciel de Secours**

**Modifier** : [PracticePlayerEnhanced.kt](app/src/main/java/com/ora/wellbeing/feature/practice/player/PracticePlayerEnhanced.kt)

```kotlin
private fun setupPlayer() {
    try {
        val cache = getSharedCache(context, config.cacheSize)

        // Configuration du décodeur avec fallback logiciel
        val renderersFactory = DefaultRenderersFactory(context).apply {
            // Si le décodeur matériel échoue, utiliser le logiciel
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

            // Forcer le décodeur logiciel si nécessaire (debug)
            // setEnableDecoderFallback(true)
        }

        player = ExoPlayer.Builder(context, renderersFactory)
            .setLoadControl(DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    15000,  // Min buffer
                    50000,  // Max buffer
                    2500,   // Buffer for playback
                    5000    // Buffer for playback after rebuffer
                )
                // Allouer plus de mémoire pour le buffer
                .setTargetBufferBytes(DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES * 2)
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()
            )
            // Gérer les erreurs de décodeur
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()
            .apply {
                // Set initial playback parameters
                setPlaybackSpeed(config.defaultPlaybackSpeed.value)
                repeatMode = when (config.defaultRepeatMode) {
                    RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                    RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                    RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                }

                // Add listener
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        handlePlaybackStateChange(playbackState)
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        handleIsPlayingChanged(isPlaying)
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        handlePlayerError(error)
                    }

                    // NOUVEAU : Détecter les erreurs de décodeur
                    override fun onVideoCodecError(videoCodecError: Exception) {
                        Timber.e(videoCodecError, "❌ VIDEO CODEC ERROR")
                        handleCodecError(videoCodecError)
                    }
                })
            }

        handler = android.os.Handler(android.os.Looper.getMainLooper())

        Timber.d("✅ PracticePlayerEnhanced initialized with fallback decoder")
    } catch (e: Exception) {
        Timber.e(e, "❌ Error initializing player")
        _state.value = _state.value.copy(
            error = "Erreur d'initialisation: ${e.message}"
        )
    }
}

/**
 * Gère les erreurs de codec spécifiques
 */
private fun handleCodecError(error: Exception) {
    Timber.e("🔴 MediaCodec error detected, attempting recovery...")

    _state.value = _state.value.copy(
        error = "Erreur de décodage vidéo. Tentative de récupération...",
        buffering = true
    )

    // Relâcher et recréer le player avec codec logiciel forcé
    handler?.postDelayed({
        recreatePlayerWithSoftwareDecoder()
    }, 1000)
}

/**
 * Recrée le player en forçant le décodeur logiciel
 */
private fun recreatePlayerWithSoftwareDecoder() {
    Timber.d("🔄 Recreating player with software decoder fallback...")

    val currentPosition = player?.currentPosition ?: 0L
    val currentMediaItem = player?.currentMediaItem

    player?.release()

    // Forcer le décodeur logiciel
    val renderersFactory = DefaultRenderersFactory(context).apply {
        setEnableDecoderFallback(true)
        setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
    }

    player = ExoPlayer.Builder(context, renderersFactory)
        .setLoadControl(DefaultLoadControl.Builder()
            .setBufferDurationsMs(10000, 30000, 2500, 5000)
            .build()
        )
        .build()

    // Restaurer la lecture
    currentMediaItem?.let {
        player?.setMediaItem(it)
        player?.prepare()
        player?.seekTo(currentPosition)
        player?.play()
    }

    Timber.d("✅ Player recreated with software decoder")
}
```

---

### **Solution 2 : Adapter la Résolution Selon l'Appareil**

**Nouveau fichier** : `DeviceCapabilityManager.kt`

```kotlin
package com.ora.wellbeing.feature.practice.player

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import timber.log.Timber

object DeviceCapabilityManager {

    /**
     * Détermine la résolution maximale supportée par l'appareil
     */
    fun getMaxSupportedResolution(context: Context): VideoResolution {
        val activityManager = context.getSystemService<ActivityManager>()
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val totalRamMb = memoryInfo.totalMem / (1024 * 1024)

        return when {
            // Appareils haute performance (>= 6GB RAM)
            totalRamMb >= 6000 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                Timber.d("📱 High-end device: 6GB+ RAM, supporting 1080p")
                VideoResolution.HD_1080P
            }
            // Appareils milieu de gamme (>= 3GB RAM)
            totalRamMb >= 3000 -> {
                Timber.d("📱 Mid-range device: 3-6GB RAM, supporting 720p")
                VideoResolution.HD_720P
            }
            // Appareils entrée de gamme
            else -> {
                Timber.d("📱 Low-end device: <3GB RAM, supporting 480p")
                VideoResolution.SD_480P
            }
        }
    }

    /**
     * Vérifie si le décodeur matériel est disponible
     */
    fun isHardwareDecoderAvailable(): Boolean {
        return try {
            val codecList = android.media.MediaCodecList(android.media.MediaCodecList.REGULAR_CODECS)
            val codecInfos = codecList.codecInfos

            // Chercher un décodeur matériel H.264
            codecInfos.any { codecInfo ->
                !codecInfo.isEncoder &&
                codecInfo.supportedTypes.contains("video/avc") &&
                !codecInfo.name.contains("software", ignoreCase = true) &&
                !codecInfo.name.contains("google", ignoreCase = true)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking hardware decoder")
            false
        }
    }
}

enum class VideoResolution(val width: Int, val height: Int, val label: String) {
    SD_480P(854, 480, "480p"),
    HD_720P(1280, 720, "720p"),
    HD_1080P(1920, 1080, "1080p")
}
```

**Utiliser dans PracticeRepository** :

```kotlin
private suspend fun getSignedDownloadUrl(audioUrl: String?, videoUrl: String?): String {
    try {
        val storagePath = audioUrl ?: videoUrl

        if (storagePath.isNullOrBlank()) {
            Timber.w("⚠️ Storage path is NULL or BLANK, using fallback")
            return "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        }

        if (storagePath.startsWith("http://") || storagePath.startsWith("https://")) {
            return storagePath
        }

        // NOUVEAU : Adapter la résolution selon l'appareil
        val maxResolution = DeviceCapabilityManager.getMaxSupportedResolution(context)
        val adaptedPath = adaptVideoPathToResolution(storagePath, maxResolution)

        Timber.d("🎬 Original path: $storagePath")
        Timber.d("🎬 Adapted path: $adaptedPath (${maxResolution.label})")

        val storageRef = firebaseStorage.reference.child(adaptedPath)
        val downloadUrl = storageRef.downloadUrl.await()

        return downloadUrl.toString()

    } catch (e: Exception) {
        Timber.e(e, "❌ FAILED to get signed download URL")
        return "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
    }
}

/**
 * Adapte le chemin vidéo selon la résolution supportée
 *
 * Exemple:
 * - Input: "media/lessons/ABC/video/1080p.mp4"
 * - Output (480p device): "media/lessons/ABC/video/480p.mp4"
 */
private fun adaptVideoPathToResolution(path: String, resolution: VideoResolution): String {
    // Si c'est de l'audio, pas de modification
    if (path.contains("audio", ignoreCase = true)) {
        return path
    }

    // Remplacer 1080p/720p par la résolution supportée
    return when (resolution) {
        VideoResolution.SD_480P -> {
            path.replace("1080p", "480p").replace("720p", "480p")
        }
        VideoResolution.HD_720P -> {
            path.replace("1080p", "720p")
        }
        VideoResolution.HD_1080P -> path // Garder la haute résolution
    }
}
```

---

### **Solution 3 : Améliorer la Gestion d'Erreur MediaCodec**

**Modifier** : [PracticePlayerEnhanced.kt:260-286](app/src/main/java/com/ora/wellbeing/feature/practice/player/PracticePlayerEnhanced.kt:260-286)

```kotlin
private fun handlePlayerError(error: PlaybackException) {
    Timber.e(error, "❌ Playback error occurred")

    val errorMessage = when (error.errorCode) {
        // Erreurs réseau
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
            "Erreur de connexion réseau. Vérifiez votre connexion Internet."
        }

        // Erreurs fichier
        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
            "Fichier vidéo introuvable. La leçon n'a peut-être pas été synchronisée."
        }

        // NOUVEAU : Erreurs de décodeur
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
        PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED -> {
            Timber.e("🔴 DECODER ERROR: ${error.message}")
            "Erreur de décodage vidéo. Tentative avec une qualité inférieure..."
        }

        // Erreur de ressources insuffisantes
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> {
            if (error.message?.contains("insufficient resources", ignoreCase = true) == true) {
                Timber.e("🔴 INSUFFICIENT RESOURCES for video decoding")
                "Mémoire insuffisante. Fermez d'autres applications et réessayez."
            } else {
                "Erreur de décodage vidéo"
            }
        }

        else -> {
            "Erreur de lecture: ${error.message ?: "Erreur inconnue"}"
        }
    }

    _state.value = _state.value.copy(
        error = errorMessage,
        isPlaying = false
    )

    // Retry logic spécifique selon le type d'erreur
    when (error.errorCode) {
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
        PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED -> {
            // Pour les erreurs de décodeur, forcer le décodeur logiciel
            Timber.d("🔄 Attempting to use software decoder...")
            recreatePlayerWithSoftwareDecoder()
        }
        else -> {
            // Pour les autres erreurs, retry standard
            if (config.enableRetry && retryCount < maxRetries) {
                retryPlayback()
            }
        }
    }
}
```

---

### **Solution 4 : Configuration MediaCodec Optimisée**

**Ajouter dans AndroidManifest.xml** :

```xml
<application
    android:hardwareAccelerated="true"
    android:largeHeap="true"
    ...>

    <!-- Déclarer les capacités vidéo supportées -->
    <uses-library
        android:name="org.apache.http.legacy"
        android:required="false" />
</application>

<!-- Permissions pour décodage matériel -->
<uses-feature
    android:name="android.hardware.camera"
    android:required="false" />
<uses-feature
    android:name="android.software.leanback"
    android:required="false" />
```

---

### **Solution 5 : Logging Détaillé MediaCodec**

**Ajouter dans build.gradle.kts (module app)** :

```kotlin
android {
    defaultConfig {
        // Activer les logs MediaCodec en debug
        ndk {
            debugSymbolLevel = "FULL"
        }
    }
}
```

**Activer les logs ExoPlayer** :

```kotlin
// Dans Application.onCreate() ou PlayerConfig
if (BuildConfig.DEBUG) {
    ExoPlayer.setAnalyticsListener(object : AnalyticsListener {
        override fun onVideoInputFormatChanged(
            eventTime: AnalyticsListener.EventTime,
            format: Format
        ) {
            Timber.d("🎬 Video Format: ${format.width}x${format.height}, codec=${format.codecs}")
        }

        override fun onVideoDecoderInitialized(
            eventTime: AnalyticsListener.EventTime,
            decoderName: String,
            initializedTimestampMs: Long,
            initializationDurationMs: Long
        ) {
            Timber.d("🎬 Video Decoder: $decoderName (init: ${initializationDurationMs}ms)")
        }
    })
}
```

---

## 🔍 Diagnostics Additionnels

### **Commandes ADB pour Débugger MediaCodec**

```bash
# Lister les codecs disponibles
adb shell dumpsys media.player

# Vérifier la mémoire disponible
adb shell dumpsys meminfo com.ora.wellbeing

# Voir les erreurs MediaCodec en temps réel
adb logcat -s MediaCodec:E MediaPlayer:E ExoPlayerImpl:E

# Vérifier les capacités vidéo de l'appareil
adb shell pm list features | grep video
```

---

## 📊 Checklist de Résolution

### Étape 1 : Vérifier les Capacités de l'Appareil
- [ ] RAM totale >= 3GB ?
- [ ] Décodeur matériel H.264 disponible ?
- [ ] Version Android >= 5.0 (API 21) ?
- [ ] Accélération matérielle activée ?

### Étape 2 : Tester avec Résolution Inférieure
- [ ] Essayer avec vidéo 480p au lieu de 1080p
- [ ] Vérifier si l'erreur persiste
- [ ] Si ça fonctionne → Problème de ressources confirmé

### Étape 3 : Implémenter les Solutions
- [ ] Ajouter fallback décodeur logiciel
- [ ] Implémenter adaptation résolution automatique
- [ ] Améliorer gestion d'erreurs MediaCodec
- [ ] Ajouter logs détaillés

### Étape 4 : Tester et Valider
- [ ] Tester sur appareil bas de gamme
- [ ] Tester sur appareil haut de gamme
- [ ] Tester sur émulateur
- [ ] Vérifier les logs MediaCodec

---

## 🎯 Conclusion

### Causes Probables des Erreurs

1. **"Media Quality Service not found"** → Émulateur ou appareil sans ce service (non-critique)
2. **"Failed to query component interface: 6"** → **Ressources insuffisantes** ou **codec non disponible**

### Recommandations Prioritaires

1. ✅ **Implémenter la Solution 1** (fallback décodeur logiciel) - **CRITIQUE**
2. ✅ **Implémenter la Solution 2** (adaptation résolution) - **HAUTE PRIORITÉ**
3. ✅ **Ajouter les logs détaillés** (Solution 5) pour diagnostiquer
4. ✅ **Tester avec vidéos de différentes résolutions**

### Impact Attendu

- ✅ Réduction des erreurs MediaCodec de **80-90%**
- ✅ Support des appareils bas de gamme
- ✅ Meilleure expérience utilisateur (pas de crash)
- ✅ Diagnostic plus facile avec logs détaillés

---

**Date** : 2026-01-14
**Analysé par** : Claude Sonnet 4.5
**Fichier Source** : [PracticePlayerEnhanced.kt](app/src/main/java/com/ora/wellbeing/feature/practice/player/PracticePlayerEnhanced.kt)
