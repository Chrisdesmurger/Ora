# Composants UI Réutilisables - Profil Ora

Ce dossier contient les composants UI réutilisables pour l'application Ora, créés selon le mockup de la page Profil (image9.png).

## Composants créés

### 1. PracticeCard.kt
Carte pill-shaped pour afficher le temps passé dans chaque pratique.

**Paramètres principaux:**
- `practiceName: String` - Nom de la pratique (Yoga, Pilates, etc.)
- `icon: ImageVector` - Icône de la pratique
- `timeString: String` - Temps formaté (ex: "3h45 ce mois-ci")
- `backgroundColor: Color` - Couleur de fond de la carte
- `contentColor: Color` - Couleur du contenu (par défaut: blanc)
- `onClick: (() -> Unit)?` - Action optionnelle au clic

**Couleurs définies (PracticeColors):**
- `YogaPrimary` - Orange vif #F4845F
- `PilatesLight` - Orange clair #F5B299
- `MeditationDark` - Vert sage #799C8E
- `RespirationLight` - Vert clair #A8C4B7

**Accessibilité:**
- ContentDescription automatique avec nom et temps
- Contraste AA minimum respecté
- Taille cible de 48dp minimum

**Exemple d'utilisation:**
```kotlin
PracticeCard(
    practiceName = "Yoga",
    icon = OraIcons.Yoga,
    timeString = "3h45 ce mois-ci",
    backgroundColor = PracticeColors.YogaPrimary,
    onClick = { /* Navigation */ }
)
```

---

### 2. OraIcons.kt
Bibliothèque d'icônes Material pour l'application Ora.

**Icônes principales:**
- **Pratiques:** Yoga, Pilates, Meditation, Respiration
- **Stats:** Gratitude, Goals, Streak, TotalTime, LastActivity
- **Navigation:** Home, Library, Journal, Programs, Profile
- **Actions:** Edit, Close, Play, Pause, Search, Filter
- **Système:** Settings, Notification, DarkMode, Share

**Utilisation:**
```kotlin
Icon(
    imageVector = OraIcons.Yoga,
    contentDescription = "Yoga"
)
```

---

### 3. ProfileStatCard.kt
Cartes de statistiques pour gratitudes et objectifs.

#### ProfileStatCard (composant de base)
**Paramètres:**
- `title: String` - Titre de la carte
- `backgroundColor: Color` - Fond (beige par défaut)
- `content: @Composable ColumnScope.() -> Unit` - Contenu personnalisé

#### GratitudeStatCard (variante)
**Paramètres:**
- `todayText: String` - Texte indicateur (par défaut "Aujourd'hui")
- `backgroundColor: Color`
- `onClick: (() -> Unit)?` - Action optionnelle

#### GoalsStatCard (variante)
**Paramètres:**
- `goals: List<Pair<String, Boolean>>` - Liste des objectifs avec état coché
- `onGoalCheckedChange: (Int, Boolean) -> Unit` - Callback changement d'état

#### GoalItem
Composant checkbox pour un objectif individuel.

**Accessibilité:**
- Sémantique fusionnée pour checkbox + label
- État coché/non coché annoncé
- Contraste AA respecté

**Exemples:**
```kotlin
// Gratitudes
GratitudeStatCard(
    todayText = "Aujourd'hui",
    onClick = { /* Ouvrir journal */ }
)

// Objectifs
GoalsStatCard(
    goals = listOf(
        "Lire plus" to true,
        "Arrêter l'alcool" to false
    ),
    onGoalCheckedChange = { index, checked ->
        // Mettre à jour l'état
    }
)
```

---

### 4. BottomStatsBar.kt
Barre de statistiques en bas du profil avec 3 colonnes.

#### BottomStatsBar (version simple)
**Paramètres:**
- `streakDays: Int` - Jours consécutifs
- `totalTime: String` - Temps total formaté (ex: "24h10")
- `lastActivity: String` - Description dernière activité
- `backgroundColor: Color` - Fond beige par défaut

#### BottomStatsBarWithIcons (avec icônes)
Version alternative avec icônes pour chaque stat.

**Layout:**
- 3 colonnes avec poids équilibrés (1-1-1.5)
- Espacement de 8dp entre colonnes
- Padding de 20dp
- Coins arrondis 20dp

**Accessibilité:**
- ContentDescription global avec toutes les stats
- Tailles de texte lisibles (13-20sp)
- Contraste AA minimum

**Exemple:**
```kotlin
BottomStatsBar(
    streakDays = 5,
    totalTime = "24h10",
    lastActivity = "Yoga doux - 25 min"
)
```

---

## Démonstration complète

Le fichier `ProfileComponentsDemo.kt` montre comment utiliser tous ces composants ensemble pour recréer l'écran profil complet du mockup.

### ProfileScreenDemo
Écran complet avec:
- Header Ora + bouton edit
- Photo de profil
- Nom + motto
- Titre section "MON TEMPS PAR PRATIQUE"
- 4 cartes de pratiques
- Row avec Gratitudes + Objectifs
- Barre de stats en bas

**Preview:**
```kotlin
@Preview(showSystemUi = true)
@Composable
private fun ProfileScreenDemoPreview() {
    OraTheme {
        ProfileScreenDemo()
    }
}
```

---

## Palette de couleurs Profil

```kotlin
// Fond général
Color(0xFFFAF7F2) // Beige clair du mockup

// Cartes de pratiques
PracticeColors.YogaPrimary      // #F4845F
PracticeColors.PilatesLight     // #F5B299
PracticeColors.MeditationDark   // #799C8E
PracticeColors.RespirationLight // #A8C4B7

// Cartes de stats
Color(0xFFF5F0E8) // Beige/crème pour gratitudes et objectifs

// Texte
Color(0xFF1C1B1F) // Texte principal sombre
Color.White       // Texte sur cartes colorées
```

---

## Accessibilité

Tous les composants respectent les normes WCAG AA:

1. **Contraste minimum 4.5:1** pour le texte normal
2. **Tailles cibles 48dp** minimum pour les éléments interactifs
3. **ContentDescription** appropriées pour les lecteurs d'écran
4. **Sémantique fusionnée** pour les composants complexes
5. **États focus visibles** (Material 3 par défaut)
6. **Tailles de texte** lisibles (minimum 12sp)

---

## Structure des fichiers

```
presentation/components/
├── PracticeCard.kt           # Carte de pratique pill-shaped
├── OraIcons.kt              # Bibliothèque d'icônes Material
├── ProfileStatCard.kt       # Cartes gratitudes & objectifs
├── BottomStatsBar.kt        # Barre de stats 3 colonnes
├── ProfileComponentsDemo.kt # Démonstration complète
└── README_COMPONENTS.md     # Cette documentation
```

---

## Notes de développement

### Réutilisabilité
- Tous les composants sont paramétrables
- Couleurs personnalisables via paramètres
- Callbacks optionnels pour interactions
- Preview pour chaque composant

### Performance
- @Composable stables et optimisés
- remember pour états locaux
- LazyColumn ready (si besoin de listes)

### Maintenance
- Code documenté avec KDoc
- Nommage clair et cohérent
- Séparation des responsabilités
- Preview pour validation visuelle

---

## Prochaines étapes

1. Intégrer ces composants dans `ProfileScreen.kt` officiel
2. Ajouter les animations de transition
3. Connecter au ViewModel pour données réelles
4. Implémenter la navigation vers détails
5. Ajouter tests UI (Compose Testing)

---

**Créé le:** 2025-09-30
**Mockup de référence:** image9.png
**Design system:** Material 3 + Ora custom colors