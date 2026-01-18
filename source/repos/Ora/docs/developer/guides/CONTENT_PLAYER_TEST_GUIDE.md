# Guide de Test - Content Player

## 🎯 Vue d'ensemble

Le Content Player est maintenant **intégré et fonctionnel** dans la branche `feat/content-player`. Voici comment le tester.

---

## ✅ Statut

- ✅ Player implémenté (PlayerScreen, PlayerViewModel, PracticePlayerEnhanced)
- ✅ Navigation intégrée (route `practice/{id}`)
- ✅ Build successful
- ⚠️ Besoin de données de test dans Firestore

---

## 🚀 Méthode 1 : Test avec Données Réelles (Recommandé)

### 1. Ajouter du Contenu dans Firestore

Connecte-toi à [Firebase Console](https://console.firebase.google.com) et ajoute des documents dans la collection `content`:

```javascript
// Collection: content
// Document ID: meditation-001

{
  "id": "meditation-001",
  "title": "Méditation du Matin",
  "discipline": "MEDITATION",  // ou "YOGA", "PILATES", "BREATHING"
  "level": "BEGINNER",         // ou "INTERMEDIATE", "ADVANCED"
  "durationMin": 10,
  "description": "Commencez votre journée en pleine conscience avec cette méditation guidée de 10 minutes.",
  "mediaType": "AUDIO",        // ou "VIDEO"
  "mediaUrl": "https://example.com/audio/meditation-morning.mp3",
  "thumbnailUrl": "https://example.com/thumbnails/meditation.jpg",
  "tags": ["matin", "énergie", "éveil"],
  "downloadable": true,
  "instructor": "Marie Dupont",
  "benefits": [
    "Réduit le stress",
    "Améliore la concentration",
    "Augmente l'énergie"
  ],
  "createdAt": { "seconds": 1697000000, "nanoseconds": 0 },
  "updatedAt": { "seconds": 1697000000, "nanoseconds": 0 }
}
```

### 2. URLs de Test Gratuites

Utilise ces URLs publiques pour tester :

**Audio (MP3):**
```
https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3
```

**Vidéo (MP4):**
```
https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4
```

### 3. Exemple Complet Firestore

```javascript
// Document: meditation-test
{
  "id": "meditation-test",
  "title": "Test Méditation Audio",
  "discipline": "MEDITATION",
  "level": "BEGINNER",
  "durationMin": 5,
  "description": "Audio de test pour le player",
  "mediaType": "AUDIO",
  "mediaUrl": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
  "thumbnailUrl": "https://via.placeholder.com/400x300/F18D5C/FFFFFF?text=Meditation",
  "tags": ["test"],
  "downloadable": true,
  "instructor": "Test Instructor",
  "benefits": ["Test benefit 1", "Test benefit 2"]
}

// Document: yoga-test
{
  "id": "yoga-test",
  "title": "Test Yoga Vidéo",
  "discipline": "YOGA",
  "level": "BEGINNER",
  "durationMin": 10,
  "description": "Vidéo de test pour le player",
  "mediaType": "VIDEO",
  "mediaUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
  "thumbnailUrl": "https://via.placeholder.com/400x300/F18D5C/FFFFFF?text=Yoga",
  "tags": ["test", "video"],
  "downloadable": true,
  "instructor": "Test Instructor",
  "benefits": ["Flexibilité", "Force"]
}
```

---

## 🧪 Méthode 2 : Navigation Directe pour Test

### Option A : Depuis l'écran Home

1. Lance l'app
2. Va sur l'écran **Home** (🏠)
3. Clique sur une pratique recommandée
4. Le PlayerScreen devrait s'ouvrir

### Option B : Depuis l'écran Library

1. Lance l'app
2. Va sur l'écran **Library** (📚)
3. Clique sur un contenu (méditation, yoga, etc.)
4. Le PlayerScreen devrait s'ouvrir

### Option C : Test Direct avec DeepLink (Pour Développeurs)

Ajoute ce code temporaire dans `HomeScreen.kt` pour tester :

```kotlin
// À ajouter temporairement dans HomeScreen
Button(
    onClick = {
        navController.navigate("practice/meditation-test")
    }
) {
    Text("🎬 Test Player")
}
```

---

## 🎮 Fonctionnalités à Tester

Une fois le player ouvert, teste ces fonctionnalités :

### ✅ Contrôles de Base
- [ ] ▶️ Play / Pause
- [ ] ⏪ Reculer 10 secondes
- [ ] ⏩ Avancer 10 secondes
- [ ] 🔊 Volume (utilise les boutons physiques)
- [ ] 📊 Seek bar (glisser pour changer la position)

### ✅ Fonctionnalités Avancées
- [ ] 🔄 Vitesse de lecture (0.5x, 0.75x, 1x, 1.25x, 1.5x, 2x)
- [ ] 🔁 Mode répétition (Off, One, All)
- [ ] 🖼️ Plein écran / Minimiser
- [ ] ⬅️ Bouton retour
- [ ] 📱 Picture-in-Picture (sur Android 8+)

### ✅ UI & Informations
- [ ] Titre de la pratique affiché
- [ ] Instructeur affiché
- [ ] Durée actuelle / durée totale
- [ ] Bénéfices listés
- [ ] Bouton favori (❤️)
- [ ] Bouton télécharger (📥)

### ✅ États
- [ ] Loading state (spinner pendant le chargement)
- [ ] Error state (si URL invalide)
- [ ] Completed state (à la fin)

---

## 🐛 Problèmes Courants

### "Practice not found"
**Solution:** Vérifie que l'ID du document Firestore correspond exactement à celui utilisé dans la navigation.

### Player ne charge pas
**Solutions:**
1. Vérifie l'URL du media (doit être HTTPS)
2. Vérifie les permissions réseau dans AndroidManifest.xml
3. Vérifie les logs Logcat pour voir les erreurs ExoPlayer

### Pas de son
**Solutions:**
1. Vérifie le volume de l'appareil
2. Vérifie que `mediaType` est bien "AUDIO" ou "VIDEO"
3. Vérifie le format du fichier (MP3/MP4 supportés)

### Erreur de navigation
**Solution:** Vérifie que la route `practice/{id}` est bien définie dans `OraDestinations.kt`

---

## 📱 Commandes ADB pour Test

```bash
# Installer l'APK
./gradlew installDebug

# Voir les logs du player
adb logcat | grep -i "player\|exoplayer\|practice"

# Lancer l'app directement sur le player (avec deeplink)
adb shell am start -a android.intent.action.VIEW -d "ora://practice/meditation-test"
```

---

## 📊 Analytics à Vérifier

Le player log ces événements Firebase Analytics :

- `practice_started` - Quand le player démarre
- `practice_paused` - Quand l'utilisateur met en pause
- `practice_resumed` - Quand l'utilisateur reprend
- `practice_completed` - Quand la pratique est terminée
- `practice_speed_changed` - Quand la vitesse change
- `practice_repeat_mode_changed` - Quand le mode répétition change

Vérifie dans Firebase Console > Analytics > DebugView

---

## 🎬 Vidéo de Test Recommandée

Pour un test complet, utilise cette vidéo courte :

```
https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_1MB.mp4
```

C'est une vidéo de 10 secondes (1MB) - parfaite pour tester rapidement toutes les fonctionnalités.

---

## ✅ Checklist de Test Complète

- [ ] Build successful
- [ ] Données de test ajoutées dans Firestore
- [ ] App installée sur appareil/émulateur
- [ ] Navigation vers le player fonctionne
- [ ] Vidéo/Audio se charge correctement
- [ ] Tous les contrôles fonctionnent
- [ ] UI responsive et fluide
- [ ] Analytics events loggés
- [ ] Pas de crash
- [ ] Retour en arrière fonctionne

---

## 🚀 Prochaines Étapes

Après validation du player :

1. ✅ Merge `feat/content-player` vers `master`
2. 📝 Créer du contenu réel dans Firestore
3. 🎨 Personnaliser l'UI si besoin
4. 📊 Configurer Firebase Analytics
5. 🧪 Tests utilisateurs

---

**Besoin d'aide ?** Vérifie les logs avec :
```bash
adb logcat | grep -E "PlayerViewModel|PracticePlayer|ExoPlayer"
```
