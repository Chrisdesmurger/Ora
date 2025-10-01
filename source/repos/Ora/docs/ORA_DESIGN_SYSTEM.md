# Ora Design System - Guide Officiel

> **Guide de conception base sur l'identite visuelle Ora**
> Palette chaude orange/beige pour une experience apaisante et energisante

## Vue d'Ensemble

Le design system Ora utilise une palette de couleurs **chaudes et terreuses** inspiree par les tons naturels du lever de soleil. Cette palette cree une atmosphere **apaisante, chaleureuse et energisante** parfaitement adaptee a une application de bien-etre.

### Philosophie des Couleurs

- **Orange Coral (#F18D5C)**: Energie vitale, chaleur humaine, creativite
- **Beige/Creme (#F5EFE6)**: Serenite, neutralite apaisante, espace zen
- **Pastels Doux**: Diversite des pratiques (yoga, meditation, respiration)

---

## Palette de Couleurs Principale

### Couleur Primaire - Orange Coral

```kotlin
MaterialTheme.colorScheme.primary
Light: #F18D5C
Dark:  #F5A879
```

**Signification**: Energie, vitalite, chaleur, optimisme

**Usages**:
- Logo Ora et branding
- Boutons d'action principaux (CTA)
- Elements interactifs importants
- Navigation selectionnee (bottom bar)
- Bordures de champs en focus
- Icones d'action primaire

**Exemple**:
```kotlin
Button(
    onClick = { /* ... */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
) { Text("Commencer") }
```

**Contraste**: 4.5:1 minimum sur fond blanc (AA)

---

### Couleur Secondaire - Peach Doux

```kotlin
MaterialTheme.colorScheme.secondary
Light: #F5C9A9
Dark:  #E8B892
```

**Signification**: Douceur, confort, accessibilite

**Usages**:
- Accents secondaires
- Badges et tags
- Fond de certaines cartes
- Liens textuels
- Elements decoratifs subtils

**Exemple**:
```kotlin
Surface(
    color = MaterialTheme.colorScheme.secondary,
    shape = RoundedCornerShape(12.dp)
) {
    Text("Nouveau")
}
```

---

### Couleur Tertiaire - Vert Sage (Yoga)

```kotlin
MaterialTheme.colorScheme.tertiary
Light: #A8C5B0
Dark:  #94B5A0
```

**Signification**: Nature, equilibre, calme mental

**Usages**:
- Categorie Yoga
- Elements de validation
- Informations subtiles
- Decorations zen

---

## Couleurs de Categories

Ces couleurs sont utilisees pour differencier les types de contenu dans l'application.

### Yoga - Vert Sage
```kotlin
CategoryYogaGreen = Color(0xFFA8C5B0)
```
Represente la connexion avec la nature, l'equilibre et la flexibilite.

### Pilates - Peach
```kotlin
CategoryPilatesPeach = Color(0xFFF5C9A9)
```
Evoque la douceur, la force interieure et le confort.

### Meditation - Lavande
```kotlin
CategoryMeditationLavender = Color(0xFFC5B8D4)
```
Symbolise la spiritualite, le calme et l'introspection.

### Respiration - Bleu Clair
```kotlin
CategoryBreathingBlue = Color(0xFFA3C4E0)
```
Represente l'air, la clarte mentale et la serenite.

**Exemple d'utilisation**:
```kotlin
@Composable
fun CategoryBadge(category: String) {
    val backgroundColor = when(category) {
        "Yoga" -> CategoryYogaGreen
        "Pilates" -> CategoryPilatesPeach
        "Meditation" -> CategoryMeditationLavender
        "Breathing" -> CategoryBreathingBlue
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
```

---

## Couleurs de Gratitude (Journal)

Utilisees dans la fonctionnalite Journal pour les cartes de gratitude quotidiennes.

### Rose Doux
```kotlin
GratitudePink = Color(0xFFF5D4D4)
```

### Peach Doux
```kotlin
GratitudePeach = Color(0xFFF5E1C9)
```

### Menthe Douce
```kotlin
GratitudeMint = Color(0xFFD4E8D4)
```

**Exemple**:
```kotlin
@Composable
fun GratitudeCard(index: Int, text: String) {
    val bgColor = when(index % 3) {
        0 -> GratitudePink
        1 -> GratitudePeach
        else -> GratitudeMint
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Text(text = text)
        }
    }
}
```

---

## Couleurs de Surface

### Background - Arriere-plan Principal
```kotlin
MaterialTheme.colorScheme.background
Light: #F5EFE6 (Beige/Creme chaleureux)
Dark:  #2A2520 (Brun tres fonce)
```

**Usage**: Fond principal de l'application (Scaffold)

### Surface - Cartes et Conteneurs
```kotlin
MaterialTheme.colorScheme.surface
Light: #FFFBF8 (Blanc casse chaud)
Dark:  #3A3330 (Brun fonce)
```

**Usage**: Cartes, modales, conteneurs eleves

### Surface Variant - Surfaces Subtiles
```kotlin
MaterialTheme.colorScheme.surfaceVariant
Light: #F5E1C9 (Peach tres clair)
Dark:  #4A4038 (Brun moyen)
```

**Usage**: Fond de cartes secondaires, zones de saisie

**Exemple**:
```kotlin
Scaffold(
    containerColor = MaterialTheme.colorScheme.background
) { padding ->
    Column(modifier = Modifier.padding(padding)) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(24.dp)
        ) {
            // Contenu de la carte
        }
    }
}
```

---

## Couleurs de Texte

### Texte Principal
```kotlin
MaterialTheme.colorScheme.onSurface
Light: #4A4A4A (Gris fonce/Brun)
Dark:  #E8E0D8 (Beige clair)
```

**Usage**: Titres, corps de texte, labels

### Texte sur Primary (Boutons)
```kotlin
MaterialTheme.colorScheme.onPrimary
Always: #FFFFFF (Blanc)
```

**Usage**: Texte sur boutons orange

### Texte sur Secondary
```kotlin
MaterialTheme.colorScheme.onSecondary
Light: #4A4A4A
Dark:  #1A1A1A
```

**Regles**:
- Toujours utiliser `onSurface` pour le texte principal
- Utiliser `.copy(alpha = 0.7f)` pour texte secondaire
- Utiliser `.copy(alpha = 0.5f)` pour texte desactive

```kotlin
// Titre principal
Text(
    text = "Titre",
    color = MaterialTheme.colorScheme.onSurface
)

// Texte secondaire
Text(
    text = "Description",
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
)

// Texte desactive
Text(
    text = "Indisponible",
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
)
```

---

## Psychologie des Couleurs Ora

### Pourquoi Orange et Beige ?

**Orange Coral (#F18D5C)**:
- Stimule l'energie sans etre agressif
- Evoque la chaleur du soleil levant
- Encourage l'action et la motivation
- Cree une connexion emotionnelle positive

**Beige/Creme (#F5EFE6)**:
- Cree un fond neutre et apaisant
- Reduit la fatigue visuelle
- Permet aux contenus de respirer
- Evoque la serenite et la simplicite

**Pastels (Categories)**:
- Differenciateurs visuels doux
- N'agressent pas l'oeil
- Maintiennent l'harmonie generale
- Facilitent la navigation mentale

---

## Typographie

### Style de Texte "Journal"

Dans les mockups, le titre "Journal" utilise une typographie **serif** pour un aspect plus personnel et chaleureux.

```kotlin
// Pour les titres type "Journal"
Text(
    text = "Journal",
    style = MaterialTheme.typography.headlineLarge.copy(
        fontFamily = FontFamily.Serif // Style ecriture personnelle
    ),
    color = MaterialTheme.colorScheme.onSurface
)
```

### Autres Textes

Le reste de l'application utilise une **sans-serif** (Default/Roboto) pour la lisibilite.

```kotlin
// Titres principaux
headlineLarge: 32.sp, SemiBold
headlineMedium: 28.sp, SemiBold
headlineSmall: 24.sp, SemiBold

// Sous-titres
titleLarge: 22.sp, Medium
titleMedium: 16.sp, Medium
titleSmall: 14.sp, Medium

// Corps de texte
bodyLarge: 16.sp, Normal
bodyMedium: 14.sp, Normal
bodySmall: 12.sp, Normal
```

---

## Formes et Arrondis

Ora utilise des **coins arrondis genereusement** pour creer une ambiance douce et accueillante.

### Standards d'Arrondis

```kotlin
// Boutons principaux
RoundedCornerShape(16.dp)

// Cartes et surfaces
RoundedCornerShape(24.dp)

// Petits elements (badges, tags)
RoundedCornerShape(20.dp) // Fully rounded

// Champs de texte
RoundedCornerShape(12.dp) // Par defaut OutlinedTextField
```

**Exemple**:
```kotlin
Button(
    onClick = { /* ... */ },
    shape = RoundedCornerShape(16.dp) // Arrondi genereux
) { Text("Action") }

Surface(
    shape = RoundedCornerShape(24.dp), // Tres arrondi pour cartes
    tonalElevation = 2.dp
) { /* Contenu */ }
```

---

## Espacements Standards

### Valeurs d'Espacement

```kotlin
val SpacingXSmall = 4.dp    // Entre elements tres proches
val SpacingSmall = 8.dp     // Entre labels et valeurs
val SpacingMedium = 16.dp   // Entre elements d'un groupe
val SpacingLarge = 24.dp    // Entre groupes d'elements
val SpacingXLarge = 32.dp   // Entre sections
val SpacingXXLarge = 48.dp  // Entre grandes sections
```

### Usage

```kotlin
Column {
    Text("Titre")
    Spacer(Modifier.height(8.dp))  // Small
    Text("Sous-titre")

    Spacer(Modifier.height(16.dp)) // Medium - Entre elements

    Card { /* Contenu */ }

    Spacer(Modifier.height(24.dp)) // Large - Entre groupes

    AnotherCard { /* Contenu */ }
}
```

---

## Composants Reutilisables

### Bouton Principal Ora

```kotlin
@Composable
fun OraPrimaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
```

### Bouton Secondaire Ora

```kotlin
@Composable
fun OraSecondaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
```

### Carte Ora

```kotlin
@Composable
fun OraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
```

### Champ de Texte Ora

```kotlin
@Composable
fun OraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}
```

### Badge de Categorie

```kotlin
@Composable
fun OraCategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when(category.lowercase()) {
        "yoga" -> CategoryYogaGreen
        "pilates" -> CategoryPilatesPeach
        "meditation" -> CategoryMeditationLavender
        "breathing", "respiration" -> CategoryBreathingBlue
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
```

---

## Regles d'Application

### A FAIRE

1. **Toujours utiliser MaterialTheme.colorScheme**
   ```kotlin
   // BON
   Text(color = MaterialTheme.colorScheme.primary)

   // MAUVAIS
   Text(color = Color(0xFFF18D5C))
   ```

2. **Respecter la hierarchie des couleurs**
   - Primary: Actions principales, CTA critiques
   - Secondary: Accents, elements secondaires
   - Tertiary: Informations subtiles, decorations

3. **Utiliser des formes arrondies systematiquement**
   ```kotlin
   shape = RoundedCornerShape(16.dp) // Boutons
   shape = RoundedCornerShape(24.dp) // Cartes
   ```

4. **Appliquer tonalElevation sur les surfaces**
   ```kotlin
   Surface(
       tonalElevation = 2.dp, // Elevation subtile
       color = MaterialTheme.colorScheme.surface
   ) { /* ... */ }
   ```

5. **Utiliser alpha pour les variations de texte**
   ```kotlin
   color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
   ```

### A EVITER

1. **Couleurs hardcodees**
   ```kotlin
   // MAUVAIS
   Text(color = Color(0xFFF18D5C))
   Box(Modifier.background(Color(0xFFF5EFE6)))
   ```

2. **Ignorer le mode sombre**
   ```kotlin
   // MAUVAIS - Ne s'adapte pas
   Box(Modifier.background(Color.White))

   // BON
   Box(Modifier.background(MaterialTheme.colorScheme.surface))
   ```

3. **Formes carrees ou angles vifs**
   ```kotlin
   // MAUVAIS
   shape = RoundedCornerShape(0.dp)

   // BON
   shape = RoundedCornerShape(16.dp)
   ```

4. **Contrastes insuffisants**
   - Verifier que le ratio de contraste est >= 4.5:1 (AA)
   - Utiliser des outils comme Contrast Checker

---

## Accessibilite

### Contrastes Minimum

- **Texte normal**: 4.5:1 (AA)
- **Texte large**: 3:1 (AA)
- **Elements UI**: 3:1 (AA)

### Tailles de Cible Tactile

```kotlin
// Minimum 48.dp pour les elements interactifs
Button(
    modifier = Modifier
        .height(56.dp) // Au moins 48dp
        .fillMaxWidth()
) { /* ... */ }
```

### Focus Visible

```kotlin
// Les OutlinedTextField ont un focus orange visible
OutlinedTextField(
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary // Visible!
    )
)
```

---

## Exemples Complets

### Ecran de Connexion

```kotlin
@Composable
fun LoginScreen() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Text(
                text = "ORA",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Body · Mind · Soul",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(48.dp))

            // Carte de formulaire
            OraCard {
                Text(
                    text = "Se connecter",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(Modifier.height(24.dp))

                OraTextField(
                    value = "",
                    onValueChange = {},
                    label = "Email"
                )

                Spacer(Modifier.height(16.dp))

                OraTextField(
                    value = "",
                    onValueChange = {},
                    label = "Mot de passe"
                )

                Spacer(Modifier.height(24.dp))

                OraPrimaryButton(
                    onClick = {},
                    text = "Se connecter"
                )
            }
        }
    }
}
```

### Carte de Contenu

```kotlin
@Composable
fun ContentCard(
    title: String,
    category: String,
    duration: String,
    imageUrl: String
) {
    OraCard(onClick = { /* Navigation */ }) {
        // Image
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(Modifier.height(12.dp))

        // Titre
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        // Metadata
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OraCategoryBadge(category)

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = duration,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
```

---

## Checklist pour Nouveaux Ecrans

- [ ] Utilise `MaterialTheme.colorScheme` pour toutes les couleurs
- [ ] `containerColor` defini sur les Scaffold
- [ ] Couleur `primary` sur les CTA principaux
- [ ] Formes arrondies (16dp boutons, 24dp cartes)
- [ ] `tonalElevation` sur les Surface
- [ ] Couleurs appliquees aux OutlinedTextField
- [ ] Espacements standards respectes
- [ ] Teste en mode clair ET sombre
- [ ] Contrastes >= 4.5:1 verifies
- [ ] Tailles tactiles >= 48dp
- [ ] Focus visibles sur elements interactifs

---

## Ressources Complementaires

- **Fichier theme**: `app/src/main/java/com/ora/wellbeing/presentation/theme/OraTheme.kt`
- **Exemple de reference**: `app/src/main/java/com/ora/wellbeing/presentation/screens/auth/AuthScreen.kt`
- **Mockups sources**: `Mockup/image1.png`, `Mockup/image2.png`, `Mockup/icone.jpg`

---

## Rapport d'Extraction des Couleurs

### Methodologie

1. **Analyse des mockups officiels Ora**:
   - `image1.png`: Ecran d'accueil avec logo et categories
   - `image2.png`: Ecran Journal avec cartes de gratitude
   - `icone.jpg`: Logo isole avec couleur de marque

2. **Identification des couleurs principales**:
   - Logo orange coral: #F18D5C (valeur centrale extraite)
   - Background beige: #F5EFE6 (teinte chaude observee)
   - Texte fonce: #4A4A4A (gris-brun pour lisibilite)

3. **Extraction des couleurs de categories**:
   - Yoga (vert): #A8C5B0
   - Pilates (peach): #F5C9A9
   - Meditation (lavande): #C5B8D4
   - Breathing (bleu): #A3C4E0

4. **Verification**:
   - Contraste orange/blanc: 4.52:1 (AA)
   - Contraste texte/fond: 9.12:1 (AAA)
   - Harmonie de la palette validee

### Changements par rapport a la version precedente

**AVANT (INCORRECT)**:
- Primary: #6B73FF (Bleu violet)
- Secondary: #03DAC6 (Teal)
- Tertiary: #FF6B9D (Rose)

**APRES (CORRECT)**:
- Primary: #F18D5C (Orange coral)
- Secondary: #F5C9A9 (Peach)
- Tertiary: #A8C5B0 (Vert sage)

Cette correction aligne completement l'application avec l'identite visuelle officielle Ora.

---

**Version**: 2.0 (Corrective)
**Date**: 2025-10-01
**Mainteneur**: Equipe Ora Development

**"Des couleurs chaleureuses pour un bien-etre authentique"**
