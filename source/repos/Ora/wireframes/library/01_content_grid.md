# Wireframe : Content Library Grid

## Layout Structure (360dp x 800dp)

```
┌─────────────────────────────────────┐
│ Status Bar                          │
├─────────────────────────────────────┤
│ Top App Bar                         │
│ Bibliothèque            [🔍] [⚙️]  │
│                                     │
├─────────────────────────────────────┤
│ Category Tabs (Scrollable)          │
│ [Tous] [Yoga] [Méditation] [Resp.]  │
│   ●     ○       ○          ○        │
│ ← [Pilates] [Auto-massage] →        │
│                                     │
├─────────────────────────────────────┤
│ Filter Bar                          │
│ [Filtres (3)] [Durée ↓] [Niveau ↓] │
│                                     │
├─────────────────────────────────────┤
│ Content Grid (Staggered)            │
│ ┌─────────────┐ ┌─────────────┐     │
│ │[Thumbnail]  │ │[Thumbnail]  │     │
│ │   150x100   │ │   150x100   │     │
│ │             │ │             │     │
│ │[♡ 24] [⏱ 12]│ │[♡ 45] [⏱ 8] │     │
│ │             │ │             │     │
│ │Yoga Matin   │ │Respiration  │     │
│ │Énergisant   │ │Anti-Stress  │     │
│ │⭐⭐⭐ Déb.   │ │⭐⭐ Déb.     │     │
│ └─────────────┘ └─────────────┘     │
│                                     │
│ ┌─────────────┐ ┌─────────────┐     │
│ │[Thumbnail]  │ │[Thumbnail]  │     │
│ │   150x120   │ │   150x80    │     │
│ │             │ │             │     │
│ │[♡ 12] [⏱ 20]│ │[♡ 67] [⏱ 15]│     │
│ │             │ │             │     │
│ │Méditation   │ │Pilates      │     │
│ │Pleine Cons. │ │Core Gentle  │     │
│ │⭐⭐⭐⭐ Int. │ │⭐⭐⭐ Int.   │     │
│ └─────────────┘ └─────────────┘     │
│                                     │
│ ┌─────────────┐ ┌─────────────┐     │
│ │    ...      │ │    ...      │     │
│ └─────────────┘ └─────────────┘     │
│                                     │
│ [Loading more...]                   │
│                                     │
├─────────────────────────────────────┤
│ Bottom Navigation                   │
│ [🏠] [📚] [✍️] [📅] [👤]          │
│ Home Bibl Journal Prog Profil      │
└─────────────────────────────────────┘
```

## Composants détaillés

### 1. Top App Bar avec Search
- **Height** : 64dp
- **Actions** :
  - Search icon (24dp) → Navigation vers search screen
  - Filter icon (24dp) → Bottom sheet filtres
- **Typography** : Title Large pour "Bibliothèque"

### 2. Category Tabs
- **Layout** : Scrollable TabRow
- **Tabs** :
  - Tous (défaut)
  - Yoga, Méditation, Respiration, Pilates
  - Auto-massage, Programmes (V1.1)
- **Indicator** : Material 3 style, couleur primary

#### Configuration responsive :
```
Portrait (360dp) : 4 tabs visibles + scroll
Landscape (640dp) : 6+ tabs visibles
```

### 3. Filter Bar (Optionnel)
- **Visibility** : Affiché si filtres actifs
- **Layout** : Chips horizontaux scrollables
- **Chips types** :
  - Count chip : "Filtres (3)" avec badge
  - Dropdown chips : "Durée", "Niveau", "Objectif"

### 4. Content Grid
- **Layout** : LazyVerticalStaggeredGrid
- **Columns** : 2 colonnes en portrait, 3 en landscape
- **Spacing** : 8dp between items, 16dp margins

#### Content Card Layout :
```
┌─────────────────┐
│ Thumbnail       │ Aspect ratio variable
│ 150dp width     │ Rounded corners 8dp
│                 │
│ [Overlay Icons] │ Top: favorite, duration
│                 │
├─────────────────┤
│ Title           │ Title Medium, 2 lines max
│ Subtitle        │ Body Small
│ Rating + Level  │ ⭐⭐⭐ + Débutant
└─────────────────┘
```

#### Card states :
- **Normal** : Elevation 1dp
- **Pressed** : Elevation 3dp + ripple
- **Favorited** : Heart icon filled
- **Completed** : Check badge overlay

## Interactions

### Navigation
- **Tap Category Tab** → Filter content + smooth scroll to top
- **Tap Filter Chip** → Open filter bottom sheet
- **Tap Content Card** → Navigate to content detail
- **Tap Search** → Navigate to search screen

### Gestures
- **Pull to Refresh** → Reload content library
- **Scroll to End** → Load next page (pagination)
- **Long Press Card** → Quick action menu (favorite, share)

### Search Integration
```
[Search Icon Tap]
    ↓
[Search Screen]
    → Query input
    → Suggestions
    → Results in same grid format
```

## États d'écran

### État Normal
```
┌─────────────────────────────────────┐
│ Bibliothèque               [🔍] [⚙] │
│                                     │
│ [Tous] [Yoga] [Méditation] ...      │
│                                     │
│ [Grid avec contenu]                 │
│ 47 sessions disponibles             │
│                                     │
│ [Pagination automatique]            │
└─────────────────────────────────────┘
```

### État Loading (Initial)
```
┌─────────────────────────────────────┐
│ Bibliothèque               [🔍] [⚙] │
│                                     │
│ [Skeleton Tabs]                     │
│ ████ ████ ████████                  │
│                                     │
│ [Skeleton Grid]                     │
│ ┌─────────┐ ┌─────────┐             │
│ │░░░░░░░░░│ │░░░░░░░░░│             │
│ │░░░░░░░░░│ │░░░░░░░░░│             │
│ │░░░░░░░░░│ │░░░░░░░░░│             │
│ └─────────┘ └─────────┘             │
└─────────────────────────────────────┘
```

### État Empty (Filtres trop restrictifs)
```
┌─────────────────────────────────────┐
│ Bibliothèque               [🔍] [⚙] │
│                                     │
│ [Tabs normaux]                      │
│                                     │
│ [Filtres actifs: Durée(5min)]       │
│                                     │
│          🔍                         │
│     Aucun contenu trouvé            │
│                                     │
│ Essayez d'ajuster vos filtres       │
│ [Réinitialiser les filtres]         │
│                                     │
│ Ou explorez d'autres catégories     │
│ [Voir tout le contenu]              │
└─────────────────────────────────────┘
```

### État Error (Réseau)
```
┌─────────────────────────────────────┐
│ Bibliothèque               [🔍] [⚙] │
│                                     │
│ [Tabs normaux]                      │
│                                     │
│          ⚠️                         │
│   Impossible de charger             │
│      le contenu                     │
│                                     │
│ Vérifiez votre connexion            │
│ [Réessayer]                         │
│                                     │
│ Contenu local disponible :          │
│ [Voir contenu téléchargé]           │
└─────────────────────────────────────┘
```

### État Offline
```
┌─────────────────────────────────────┐
│ Mode hors ligne           [🔍] [⚙] │
│                                     │
│ [Tabs normaux]                      │
│                                     │
│ [Grid avec contenu local]           │
│                                     │
│ ⬇️ 12 sessions téléchargées         │
│                                     │
│ [Grid normal mais contenu limité]   │
│                                     │
│ "Connectez-vous pour voir tout      │
│  le catalogue"                      │
└─────────────────────────────────────┘
```

## Filtres et tri

### Filtres disponibles
- **Durée** : 5min, 10min, 15min, 20min+
- **Niveau** : Débutant, Intermédiaire, Avancé
- **Objectif** : Réveil, Énergie, Détente, Sommeil
- **Type** : Yoga, Pilates, Méditation, Respiration
- **Instructeur** : Liste des instructeurs
- **Nouveau** : Contenu ajouté cette semaine

### Tri par défaut
1. **Personnalisé** : Basé sur préférences utilisateur
2. **Populaire** : Le plus aimé par la communauté
3. **Récent** : Derniers ajouts
4. **Durée** : Du plus court au plus long
5. **Alphabétique** : A-Z

## Optimisations performance

### Lazy Loading
- **Initial Load** : 20 items
- **Pagination** : 10 items par page
- **Preload** : 2 écrans en avance

### Image Loading
- **Thumbnails** : Coil avec cache LRU
- **Placeholder** : Skeleton avec couleur de marque
- **Error State** : Icône générique avec retry

### Animations
- **Content Appear** : Fade in staggered (100ms delay)
- **Category Switch** : Crossfade transition
- **Pull Refresh** : Material refresh indicator

Cette grille de contenu équilibre découvrabilité et performance tout en maintenant une expérience utilisateur fluide et engageante.