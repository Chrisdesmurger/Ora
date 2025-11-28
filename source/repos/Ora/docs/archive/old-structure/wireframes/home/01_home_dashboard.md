# Wireframe : Home Dashboard

## Layout Structure (360dp x 800dp)

```
┌─────────────────────────────────────┐
│ Status Bar                          │
├─────────────────────────────────────┤
│ Top App Bar                    [⚙] │
│ "Bonjour Sarah ! ☀️"               │
│                                     │
├─────────────────────────────────────┤
│ Daily Recommendation Card           │
│ ┌─────────────────────────────────┐ │
│ │ [Thumbnail Image 120x80]        │ │
│ │ Yoga Réveil Énergisant          │ │
│ │ 12 min • Débutant              │ │
│ │ "Parfait pour bien commencer"   │ │
│ │                                 │ │
│ │ [Commencer maintenant]   [♡ 24]│ │
│ └─────────────────────────────────┘ │
│                                     │
├─────────────────────────────────────┤
│ Quick Actions                       │
│ Flash Sessions (5 min)              │
│ ┌──────┐ ┌──────┐ ┌──────┐         │
│ │[🧘]  │ │[🫁]  │ │[🕯️]  │         │
│ │Yoga  │ │Resp. │ │Médit.│         │
│ │Flash │ │Calme │ │Mini  │         │
│ └──────┘ └──────┘ └──────┘         │
│                                     │
├─────────────────────────────────────┤
│ Progress Overview                   │
│ Cette semaine                       │
│ ┌─────────────────────────────────┐ │
│ │ ○○○●●●○  3 sessions            │ │
│ │ 🔥 7     Série de jours         │ │
│ │ 47 min   Temps pratiqué         │ │
│ └─────────────────────────────────┘ │
│                                     │
├─────────────────────────────────────┤
│ Recent Activity                     │
│ ┌─────────────────────────────────┐ │
│ │ Hier • Méditation Sommeil       │ │
│ │ [thumbnail] 15 min ⭐⭐⭐       │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ Mardi • Yoga Matin              │ │
│ │ [thumbnail] 20 min ⭐⭐⭐⭐     │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [Voir tout l'historique]            │
│                                     │
├─────────────────────────────────────┤
│ Bottom Navigation                   │
│ [🏠] [📚] [✍️] [📅] [👤]          │
│ Home Bibl Journal Prog Profil      │
└─────────────────────────────────────┘
```

## Composants détaillés

### 1. Top App Bar
- **Height** : 64dp
- **Background** : Surface color
- **Content** :
  - Salutation personnalisée avec emoji contextuel
  - Icône settings (24dp) alignée à droite
- **Typography** : Headline Medium pour salutation

### 2. Daily Recommendation Card
- **Margin** : 16dp horizontal, 8dp vertical
- **Padding** : 16dp
- **Elevation** : 2dp (Material 3 elevated card)
- **Corner radius** : 12dp

#### Layout interne :
```
┌───────────────────────────────────┐
│ Row Layout                        │
│ ┌─────────────┐ ┌──────────────┐  │
│ │ Thumbnail   │ │ Content      │  │
│ │ 120x80dp    │ │ Column       │  │
│ │ Rounded 8dp │ │              │  │
│ │             │ │ Title        │  │
│ │ [Play Icon] │ │ Duration     │  │
│ │ overlay     │ │ Description  │  │
│ └─────────────┘ │              │  │
│                 │ Action Row   │  │
│                 │ [Button][♡]  │  │
│                 └──────────────┘  │
└───────────────────────────────────┘
```

#### États :
- **Normal** : Contenu personnalisé basé sur heure/préférences
- **Loading** : Skeleton avec shimmer effect
- **Empty** : Message motivant + CTA découverte

### 3. Quick Actions Grid
- **Layout** : 3 colonnes égales
- **Spacing** : 8dp entre colonnes
- **Margin** : 16dp horizontal

#### Card Quick Action :
```
┌─────────────┐
│    [Icône]  │ 48dp emoji/icon
│             │
│   Titre     │ Label Medium
│  Sous-titre │ Body Small
└─────────────┘
```

#### États individuels :
- **Available** : Couleur normale, tap enabled
- **Completed** : Checkmark overlay, couleur success
- **Locked** : Opacity 50%, tap disabled

### 4. Progress Overview Card
- **Layout** : Information dense en format card
- **Visual elements** :
  - Week dots : 7 cercles avec état (vide/rempli)
  - Streak fire emoji avec compteur
  - Time badge avec minutes

#### Layout interne :
```
┌─────────────────────────────────┐
│ "Cette semaine"                 │
│                                 │
│ ○○○●●●○   3 sessions           │
│ Lun Mar Mer Jeu Ven Sam Dim     │
│                                 │
│ 🔥 7 jours    47 minutes       │
│ Série actuelle   Cette semaine  │
└─────────────────────────────────┘
```

### 5. Recent Activity List
- **Layout** : Liste verticale avec cards
- **Max items** : 2-3 sessions récentes
- **CTA** : Lien vers historique complet

#### Item layout :
```
┌─────────────────────────────────┐
│ Row Layout                      │
│ [thumb] Hier • Méditation       │
│  60x40   15 min ⭐⭐⭐⭐      │
└─────────────────────────────────┘
```

## Interactions

### Gestures
- **Tap Recommendation Card** → Navigation vers content detail
- **Tap Quick Action** → Lancement session flash
- **Tap Progress** → Navigation vers statistiques détaillées
- **Tap Recent Item** → Navigation vers session replay
- **Pull to Refresh** → Actualisation recommandations

### Feedback visuel
- **Tap States** : Ripple effect Material 3
- **Loading** : Shimmer skeleton pour cartes
- **Success** : Micro-animations pour actions complétées

## États d'écran

### État Normal
- Tous les composants visibles et interactifs
- Données personnalisées chargées
- Recommandation contextuelle affichée

### État Loading (Premier lancement)
```
┌─────────────────────────────────────┐
│ [Skeleton Top Bar]                  │
│                                     │
│ [Skeleton Recommendation Card]      │
│ ████████████████████████            │
│ ████████████████                    │
│                                     │
│ [Skeleton Quick Actions]            │
│ [○○○] [○○○] [○○○]                  │
│                                     │
│ [Skeleton Progress]                 │
│ ████████████████████                │
└─────────────────────────────────────┘
```

### État Empty (Nouvelle utilisatrice)
```
┌─────────────────────────────────────┐
│ "Bienvenue dans Ora ! 🌟"          │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ 🧘‍♀️                             │ │
│ │ "Votre première session vous    │ │
│ │  attend"                        │ │
│ │                                 │ │
│ │ [Découvrir le yoga]             │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [Quick Actions normales]            │
│                                     │
│ "Vos statistiques apparaîtront     │
│  après votre première session"     │
└─────────────────────────────────────┘
```

### État Offline
- Bandeau discret : "Mode hors ligne"
- Recommandations basées sur contenu local
- Désactivation des CTAs nécessitant réseau

## Personnalisation dynamique

### Salutation contextuelle
- **Matin (6h-12h)** : "Bonjour [prénom] ! ☀️"
- **Après-midi (12h-18h)** : "Bon après-midi [prénom] ! 🌤️"
- **Soir (18h-22h)** : "Bonsoir [prénom] ! 🌅"
- **Nuit (22h-6h)** : "Bonne nuit [prénom] ! 🌙"

### Recommandations intelligentes
- **Algorithme** : Heure + préférences + historique
- **Fallback** : Contenu populaire si pas d'historique
- **Rotation** : Changement quotidien garanti

### Quick Actions adaptatives
- **Sessions completées** : Checkmark + animation
- **Streak active** : Fire badge sur actions
- **Nouveautés** : Badge "Nouveau" discret

Cette page d'accueil priorise l'action immédiate tout en fournissant un aperçu motivant des progrès utilisateur.