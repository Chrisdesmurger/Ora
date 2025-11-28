# Bibliothèque de Composants Ora

Cette bibliothèque contient tous les composants Compose réutilisables pour l'application Ora, organisés par catégorie et suivant les principes Material 3.

## Structure des Composants

### 1. Foundation Components
```
component_library/foundation/
├── OraButton.kt
├── OraCard.kt
├── OraChip.kt
├── OraTextField.kt
└── OraSurface.kt
```

### 2. Navigation Components
```
component_library/navigation/
├── OraBottomNavigation.kt
├── OraTopAppBar.kt
├── OraNavigationRail.kt
└── OraTabRow.kt
```

### 3. Content Components
```
component_library/content/
├── ContentCard.kt
├── ContentGrid.kt
├── VideoPlayer.kt
├── AudioPlayer.kt
├── ProgressIndicator.kt
└── RatingDisplay.kt
```

### 4. Input Components
```
component_library/input/
├── GratitudeInput.kt
├── MoodSelector.kt
├── FilterBottomSheet.kt
├── SearchBar.kt
└── DatePicker.kt
```

### 5. Display Components
```
component_library/display/
├── StatCard.kt
├── ProgressRing.kt
├── CalendarView.kt
├── BadgeDisplay.kt
├── TimelineView.kt
└── EmptyState.kt
```

### 6. Feedback Components
```
component_library/feedback/
├── LoadingState.kt
├── ErrorState.kt
├── SuccessAnimation.kt
├── ToastMessage.kt
└── ConfirmationDialog.kt
```

## Conventions de Design

### Nomenclature
- **Préfixe** : Tous les composants commencent par "Ora"
- **Suffixe** : Type de composant (Button, Card, etc.)
- **Modificateurs** : État ou variante (Primary, Secondary, Large, etc.)

### Structure des Composants
```kotlin
@Composable
fun OraComponentName(
    // Required parameters first
    text: String,
    onClick: () -> Unit,

    // Optional parameters with defaults
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ComponentVariant = ComponentVariant.Primary,

    // Styling parameters last
    colors: ComponentColors = ComponentDefaults.colors(),
    typography: TextStyle = MaterialTheme.typography.labelLarge
) {
    // Implementation
}
```

### États Standard
Chaque composant interactif doit supporter :
- **Default** : État normal
- **Pressed** : État appuyé (ripple effect)
- **Disabled** : État désactivé (opacity 38%)
- **Loading** : État de chargement (skeleton ou spinner)
- **Error** : État d'erreur (couleur error + feedback)

### Accessibilité
- **Content Description** : Obligatoire pour tous les éléments interactifs
- **Semantic Roles** : Utilisation appropriée des roles Material
- **State Description** : Description des états pour screen readers
- **Touch Targets** : Minimum 48dp pour tous les éléments cliquables

## Guidelines d'Utilisation

### Hiérarchie Visuelle
1. **Primary Actions** : OraButton variant Primary
2. **Secondary Actions** : OraButton variant Secondary ou Text
3. **Destructive Actions** : OraButton variant Error
4. **Navigation** : OraBottomNavigation, OraTopAppBar

### Espacement Cohérent
- **Padding interne** : 16dp standard pour cartes et conteneurs
- **Margins externes** : 16dp horizontal, 8dp vertical entre éléments
- **Groupes d'éléments** : 24dp de séparation

### Feedback Utilisateur
- **Immediate** : Ripple effects, state changes
- **Loading** : Skeleton screens, progress indicators
- **Success** : Animations de célébration, couleurs success
- **Error** : Messages clairs avec actions de récupération

## Personnalisation Avancée

### Thématique Contextuelle
Les composants s'adaptent automatiquement au contexte :
```kotlin
// Dans un écran Yoga
ContentCard(
    contentType = ContentType.Yoga,
    // Couleur automatiquement adaptée au yoga
)

// Dans un écran Méditation
ContentCard(
    contentType = ContentType.Meditation,
    // Couleur automatiquement adaptée à la méditation
)
```

### Responsive Design
Les composants s'adaptent aux différentes tailles d'écran :
```kotlin
@Composable
fun ResponsiveGrid(
    content: LazyGridScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val columns = when {
        configuration.screenWidthDp >= 840 -> 4
        configuration.screenWidthDp >= 600 -> 3
        else -> 2
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        content = content
    )
}
```

### Animations Cohérentes
Toutes les animations suivent les durées et easing standards :
```kotlin
val animationSpec = tween<Float>(
    durationMillis = 300,
    easing = EaseInOutCubic
)
```

## Testing des Composants

### Tests Unitaires
Chaque composant inclut des tests pour :
- Rendu correct avec différents paramètres
- Gestion des états (loading, error, success)
- Interactions utilisateur (click, long press)
- Accessibilité (content description, semantics)

### Tests Visuels
- **Screenshot Testing** : Validation du rendu visuel
- **Accessibility Testing** : Validation TalkBack et Switch Access
- **Responsive Testing** : Validation sur différentes tailles d'écran

Cette bibliothèque garantit une expérience utilisateur cohérente et accessible à travers toute l'application Ora.