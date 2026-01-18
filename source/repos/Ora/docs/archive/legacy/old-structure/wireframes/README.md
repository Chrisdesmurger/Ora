# Wireframes Ora Android

Ce dossier contient les wireframes détaillés de l'application Ora Android, organisés par flux utilisateur principal.

## Structure des wireframes

### 1. Onboarding Flow
```
wireframes/onboarding/
├── 01_welcome_screen.md
├── 02_intro_carousel.md
├── 03_preferences_setup.md
└── 04_permissions_request.md
```

### 2. Main Navigation
```
wireframes/main/
├── bottom_navigation_layout.md
└── tab_transitions.md
```

### 3. Home Dashboard
```
wireframes/home/
├── 01_home_dashboard.md
├── 02_daily_recommendation.md
└── 03_quick_actions.md
```

### 4. Content Library
```
wireframes/library/
├── 01_content_grid.md
├── 02_filters_bottom_sheet.md
├── 03_search_interface.md
└── 04_content_detail.md
```

### 5. Journal
```
wireframes/journal/
├── 01_journal_entry_form.md
├── 02_history_calendar.md
└── 03_entry_detail_view.md
```

### 6. Video Player
```
wireframes/player/
├── 01_video_player_controls.md
├── 02_audio_player_interface.md
└── 03_session_completion.md
```

### 7. Profile & Settings
```
wireframes/profile/
├── 01_profile_overview.md
├── 02_statistics_detail.md
├── 03_settings_menu.md
└── 04_achievements_badges.md
```

## Conventions des wireframes

### Notation et symboles
- `[Button]` : Bouton interactif
- `{Dynamic Content}` : Contenu variable/personnalisé
- `...` : Contenu scroll/extension
- `→` : Navigation/transition
- `↓` : Flux vertical
- `(optional)` : Élément conditionnel

### Responsive breakpoints
- **Phone Portrait** : 360dp x 800dp (référence)
- **Phone Landscape** : 800dp x 360dp
- **Tablet** : 768dp x 1024dp (considerations futures)

### États d'interface
- **Loading** : Skeleton screens et spinners
- **Empty** : États vides avec CTAs
- **Error** : Messages d'erreur et actions de récupération
- **Offline** : Adaptations mode hors ligne

## Guidelines Material 3

### Layout et spacing
- **Margin standard** : 16dp
- **Padding cards** : 16dp
- **Spacing vertical** : 8dp, 16dp, 24dp
- **Touch targets** : 48dp minimum

### Typography scale
- **Display Large** : Titres principaux
- **Headline Medium** : Titres de sections
- **Title Large** : Titres de cartes
- **Body Large** : Texte principal
- **Label Medium** : Labels et boutons

### Composants clés
- **Cards** : Surface elevation 1dp
- **Bottom Navigation** : 5 onglets max
- **FAB** : Action principale par écran
- **Bottom Sheets** : Modals et filtres

Chaque wireframe suit ces conventions pour assurer la cohérence de l'expérience utilisateur à travers l'application.