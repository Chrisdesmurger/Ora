# Ora Design System - Guide des Couleurs et Composants

> **Guide officiel pour tous les développements de l'application Ora**
> Respectez ces règles pour garantir une expérience utilisateur cohérente et zen.

## 📱 Palette de Couleurs Ora

### Couleurs Principales (Material 3)

#### 🟣 Primary - Bleu Violet Zen
```kotlin
MaterialTheme.colorScheme.primary
// Light: #6B73FF (Bleu violet zen)
// Dark:  #8B93FF (Bleu violet plus clair)
```
**Usage:**
- Boutons principaux d'action (CTA)
- Éléments interactifs importants
- Bordures de champs de saisie en focus
- Icônes principales
- Logo et branding

**Exemple:**
```kotlin
Button(
    onClick = { /* ... */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
) { Text("Action principale") }
```

---

#### 🟢 Secondary - Teal Apaisante
```kotlin
MaterialTheme.colorScheme.secondary
// Light: #03DAC6 (Teal apaisante)
// Dark:  #66FFF2 (Teal lumineuse)
```
**Usage:**
- Accents et éléments secondaires
- Boutons secondaires et bordures
- Tags et badges
- Liens textuels
- Subtitles et descriptions importantes

**Exemple:**
```kotlin
Text(
    text = "Description importante",
    color = MaterialTheme.colorScheme.secondary
)

OutlinedButton(
    onClick = { /* ... */ },
    colors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.secondary
    )
) { Text("Action secondaire") }
```

---

#### 🌸 Tertiary - Rose Doux
```kotlin
MaterialTheme.colorScheme.tertiary
// Light: #FF6B9D (Rose doux)
// Dark:  #FF94B3 (Rose plus clair)
```
**Usage:**
- Informations subtiles
- Messages de validation
- Éléments décoratifs doux
- Mentions légales
- Badges de nouveauté

**Exemple:**
```kotlin
Text(
    text = "Message d'information",
    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
)
```

---

### Couleurs de Surface

#### Background & Surface
```kotlin
// Background - Arrière-plan principal
MaterialTheme.colorScheme.background
// Light: #FFFBFE (Blanc cassé)
// Dark:  #1C1B1F (Noir doux)

// Surface - Cartes et conteneurs
MaterialTheme.colorScheme.surface
// Light: #F7F2FA (Violet très clair)
// Dark:  #2B2930 (Gris foncé)
```

**Usage:**
```kotlin
Scaffold(
    containerColor = MaterialTheme.colorScheme.background
) { /* ... */ }

Surface(
    color = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(24.dp),
    tonalElevation = 2.dp
) { /* Contenu de la carte */ }
```

---

### Couleurs de Texte

```kotlin
// Texte principal
MaterialTheme.colorScheme.onSurface
// Light: #1C1B1F
// Dark:  #E6E1E5

// Texte secondaire
MaterialTheme.colorScheme.onSurfaceVariant

// Texte sur primary
MaterialTheme.colorScheme.onPrimary

// Texte sur secondary
MaterialTheme.colorScheme.onSecondary
```

---

## 🎨 Règles d'Application des Couleurs

### ✅ À FAIRE

1. **Toujours utiliser `MaterialTheme.colorScheme`**
   ```kotlin
   // ✅ BON
   Text(color = MaterialTheme.colorScheme.primary)

   // ❌ MAUVAIS
   Text(color = Color(0xFF6B73FF))
   ```

2. **Respecter la hiérarchie des couleurs**
   - Primary: Actions principales, éléments critiques
   - Secondary: Actions secondaires, accents
   - Tertiary: Informations subtiles, décorations

3. **Utiliser des formes arrondies (Zen Design)**
   ```kotlin
   // Boutons
   shape = RoundedCornerShape(16.dp)

   // Cartes et surfaces
   shape = RoundedCornerShape(24.dp)

   // Champs de texte
   shape = RoundedCornerShape(12.dp) // par défaut OutlinedTextField
   ```

4. **Appliquer les couleurs aux champs de texte**
   ```kotlin
   OutlinedTextField(
       colors = OutlinedTextFieldDefaults.colors(
           focusedBorderColor = MaterialTheme.colorScheme.primary,
           focusedLabelColor = MaterialTheme.colorScheme.primary,
           cursorColor = MaterialTheme.colorScheme.primary
       )
   )
   ```

5. **Utiliser `tonalElevation` pour les surfaces**
   ```kotlin
   Surface(
       tonalElevation = 2.dp,  // Légère élévation pour cartes
       color = MaterialTheme.colorScheme.surface
   ) { /* ... */ }
   ```

---

### ❌ À ÉVITER

1. **NE PAS utiliser de couleurs hardcodées**
   ```kotlin
   // ❌ MAUVAIS
   Text(color = Color.Blue)
   Text(color = Color(0xFF6B73FF))
   ```

2. **NE PAS ignorer le mode sombre**
   ```kotlin
   // ❌ MAUVAIS - Ne s'adapte pas au dark mode
   Box(modifier = Modifier.background(Color.White))

   // ✅ BON
   Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface))
   ```

3. **NE PAS mélanger des styles différents**
   - Éviter les formes carrées (sharp corners) si le reste est arrondi
   - Garder une cohérence dans les espacements

---

## 🧩 Composants Réutilisables

### Bouton Principal (CTA)
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
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
```

### Bouton Secondaire
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
            contentColor = MaterialTheme.colorScheme.secondary
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
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
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
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
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier.fillMaxWidth()
    )
}
```

### Carte Zen
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

---

## 📐 Espacements Standards

```kotlin
// Espacements internes (padding)
val SpacingXSmall = 4.dp
val SpacingSmall = 8.dp
val SpacingMedium = 16.dp
val SpacingLarge = 24.dp
val SpacingXLarge = 32.dp
val SpacingXXLarge = 48.dp

// Espacements entre sections
Spacer(modifier = Modifier.height(16.dp)) // Entre éléments
Spacer(modifier = Modifier.height(24.dp)) // Entre groupes
Spacer(modifier = Modifier.height(48.dp)) // Entre sections
```

---

## 🎯 Exemples d'Utilisation Complète

### Écran avec Formulaire
```kotlin
@Composable
fun ExampleFormScreen() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre avec couleur primary
            Text(
                text = "Titre de l'écran",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sous-titre avec couleur secondary
            Text(
                text = "Description de l'écran",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Carte avec formulaire
            OraCard {
                OraTextField(
                    value = "",
                    onValueChange = {},
                    label = "Champ de saisie"
                )

                Spacer(modifier = Modifier.height(16.dp))

                OraPrimaryButton(
                    onClick = {},
                    text = "Valider"
                )
            }
        }
    }
}
```

---

## ✅ Checklist pour Nouveaux Écrans

- [ ] Utilise `MaterialTheme.colorScheme` pour toutes les couleurs
- [ ] Applique la couleur `primary` aux actions principales
- [ ] Applique la couleur `secondary` aux éléments secondaires
- [ ] Utilise `tertiary` pour les informations subtiles
- [ ] Utilise `RoundedCornerShape` pour les boutons et cartes
- [ ] Définit `containerColor` sur les Scaffold
- [ ] Applique les couleurs aux `OutlinedTextField`
- [ ] Respecte les espacements standards
- [ ] Teste en mode clair ET sombre
- [ ] Vérifie l'accessibilité des contrastes

---

## 🚨 Révision de Code

Lors des revues de code, vérifiez:

1. **Pas de `Color(0xFFxxxxxx)` hardcodé**
2. **Utilisation cohérente de `MaterialTheme.colorScheme`**
3. **Formes arrondies sur tous les composants**
4. **Surface avec `tonalElevation` pour les cartes**
5. **Respect de la hiérarchie primary > secondary > tertiary**

---

## 📚 Ressources

- [CLAUDE.md](../CLAUDE.md) - Vue d'ensemble du projet
- [OraTheme.kt](../app/src/main/java/com/ora/wellbeing/presentation/theme/OraTheme.kt) - Définition des couleurs
- [AuthScreen.kt](../app/src/main/java/com/ora/wellbeing/presentation/screens/auth/AuthScreen.kt) - Exemple de référence

---

**Version:** 1.0
**Date:** 2025-10-01
**Mainteneur:** Équipe Ora Development

✨ **"Cohérence visuelle = Sérénité utilisateur"** ✨
