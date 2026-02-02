         1 +  # Ora - Guide des Dimensions pour Ressources Graphiques
         2 +  
         3 +  **Version**: 1.0
         4 +  **Date**: 2025-11-16
         5 +  **Usage**: Ce guide liste toutes les dimensions utilisées dans l'application Ora pour créer des ressources graphiques (images, icônes, previews) avec des outils comme Canva, Figma, ou Photoshop.
         6 +  
         7 +  ---
         8 +  
         9 +  ## 📐 Dimensions d'Écran de Référence
        10 +  
        11 +  ### Appareils Android Cibles
        12 +  - **Compact Phone** (Pixel 4a, Galaxy S21): 360 x 800 dp
        13 +  - **Medium Phone** (Pixel 6, Galaxy S22): 412 x 915 dp
        14 +  - **Large Phone** (Pixel 7 Pro, Galaxy S23 Ultra): 480 x 1050 dp
        15 +  - **Tablet 7"**: 600 x 960 dp
        16 +  - **Tablet 10"**: 800 x 1280 dp
        17 +  
        18 +  ### Paddings Standards par Écran
        19 +  - **Horizontal padding réduit**: 12dp (HomeScreen, LibraryScreen, ProgramsScreen)
        20 +  - **Horizontal padding standard**: 16dp (ProfileScreen, JournalScreen)
        21 +  - **Horizontal padding formulaires**: 24dp (AuthScreen, OnboardingScreen)
        22 +  
        23 +  ---
        24 +  
        25 +  ## 🖼️ Dimensions des Images de Contenu
        26 +  
        27 +  ### Images de Couverture de Programmes (Cover Images)
        28 +  
        29 +  **Cartes Horizontales** (liste déroulante):
        30 +  - **Largeur**: 280dp
        31 +  - **Hauteur recommandée**: 157dp (ratio 16:9)
        32 +  - **Pixels @ 3x**: 840 x 471 px
        33 +  - **Usage**: `ProgramsScreen.kt` - cartes de programmes en scroll horizontal
        34 +  - **Format recommandé**: JPEG ou WebP
        35 +  - **Poids max**: 150 KB
        36 +  
        37 +  **Cartes Grid** (2 colonnes):
        38 +  - **Largeur**: ~176dp (=(screen_width - 12dp padding x2 - 12dp spacing) / 2)
        39 +  - **Hauteur recommandée**: 99dp (ratio 16:9)
        40 +  - **Pixels @ 3x**: 528 x 297 px
        41 +  - **Usage**: `ProgramsScreen.kt` - grille de programmes
        42 +  - **Format recommandé**: JPEG ou WebP
        43 +  - **Poids max**: 100 KB
        44 +  
        45 +  ### Images de Leçons (Lessons/ContentItems)
        46 +  
        47 +  **Thumbnails Horizontaux**:
        48 +  - **Largeur**: 180dp
        49 +  - **Hauteur recommandée**: 101dp (ratio 16:9)
        50 +  - **Pixels @ 3x**: 540 x 303 px
        51 +  - **Usage**: `LibraryScreen.kt` - liste horizontale de contenus
        52 +  - **Format recommandé**: JPEG ou WebP
        53 +  - **Poids max**: 80 KB
        54 +  
        55 +  **Thumbnails Grid**:
        56 +  - **Largeur**: ~174dp (=(screen_width - 12dp x2 - 12dp spacing) / 2)
        57 +  - **Hauteur recommandée**: 98dp (ratio 16:9)
        58 +  - **Pixels @ 3x**: 522 x 294 px
        59 +  - **Usage**: `LibraryScreen.kt` - grille 2 colonnes
        60 +  - **Format recommandé**: JPEG ou WebP
        61 +  - **Poids max**: 80 KB
        62 +  
        63 +  **Previews Plein Écran** (lecteur vidéo):
        64 +  - **Largeur**: fillMaxWidth (~360-480dp selon appareil)
        65 +  - **Hauteur**: aspectRatio 16:9
        66 +  - **Pixels @ 3x**: 1440 x 810 px minimum
        67 +  - **Usage**: Écran de lecture de contenu (ExoPlayer)
        68 +  - **Format recommandé**: JPEG, WebP ou vidéo (MP4)
        69 +  - **Poids max**: 300 KB (image), vidéo selon rendition
        70 +  
        71 +  ### Images de Recommandations (HomeScreen)
        72 +  
        73 +  **Cartes de Recommandations**:
        74 +  - **Largeur**: 200dp
        75 +  - **Hauteur recommandée**: 112dp (ratio 16:9)
        76 +  - **Pixels @ 3x**: 600 x 337 px
        77 +  - **Usage**: `HomeScreen.kt:235` - sessions recommandées
        78 +  - **Format recommandé**: JPEG ou WebP
        79 +  - **Poids max**: 100 KB
        80 +  
        81 +  ### Images pour Onboarding (Question Types)
        82 +  
        83 +  **Image Cards** (grille de sélection):
        84 +  - **Largeur**: fillMaxWidth (avec padding 24dp x2)
        85 +  - **Zone image**: aspectRatio 16:9
        86 +  - **Pixels @ 3x**: ~936 x 527 px (pour 360dp screen)
        87 +  - **Usage**: `OnboardingScreen.kt:969` - sélection avec images
        88 +  - **Format recommandé**: JPEG ou WebP
        89 +  - **Poids max**: 150 KB
        90 +  
        91 +  **Grid Selection Cards**:
        92 +  - **Dimensions**: aspectRatio 1:1 (carré)
        93 +  - **Largeur estimée**: ~156dp (2 colonnes avec gridColumns = 2)
        94 +  - **Pixels @ 3x**: 468 x 468 px
        95 +  - **Usage**: `OnboardingScreen.kt:656` - grille de sélection
        96 +  - **Format recommandé**: PNG ou WebP (pour transparence possible)
        97 +  - **Poids max**: 80 KB
        98 +  
        99 +  ---
       100 +  
       101 +  ## 🎨 Dimensions des Icônes
       102 +  
       103 +  ### Icônes Material (Référence pour Icônes Personnalisées)
       104 +  
       105 +  **Tailles Standards**:
       106 +  - **16dp** (48px @ 3x): Petites icônes inline (stats, schedule)
       107 +  - **18dp** (54px @ 3x): Icônes de boutons moyens
       108 +  - **20dp** (60px @ 3x): Icônes de navigation
       109 +  - **24dp** (72px @ 3x): Taille standard (la plus courante)
       110 +  - **28dp** (84px @ 3x): Grandes icônes de header
       111 +  - **32dp** (96px @ 3x): Icônes extra-large (quick sessions, challenges)
       112 +  
       113 +  ### Icônes de Catégories (si personnalisées)
       114 +  
       115 +  **Taille recommandée**: 24dp (72px @ 3x)
       116 +  - **Format**: SVG ou PNG avec transparence
       117 +  - **Couleurs**: Utiliser les couleurs de catégorie définies dans OraTheme
       118 +    - Yoga: `#8E7CC3` (Violet)
       119 +    - Pilates: `#E57373` (Rouge corail)
       120 +    - Méditation: `#64B5F6` (Bleu)
       121 +    - Respiration: `#81C784` (Vert)
       122 +  
       123 +  ### Icônes de Pratiques (Quick Sessions)
       124 +  
       125 +  **Taille**: 32dp (96px @ 3x)
       126 +  - **Usage**: `HomeScreen.kt:179` - cartes de quick sessions
       127 +  - **Format**: SVG ou PNG
       128 +  - **Style**: Ligne simple, style Material Design
       129 +  - **Couleurs**: Primary Orange `#F4845F` ou blanc sur fond coloré
       130 +  
       131 +  ---
       132 +  
       133 +  ## 👤 Photos de Profil (Avatars)
       134 +  
       135 +  ### Avatar Utilisateur
       136 +  
       137 +  **Taille par défaut**: 140dp (420px @ 3x)
       138 +  - **Format**: Circulaire (CircleShape)
       139 +  - **Usage**: `UserAvatar.kt:37` - composant avatar
       140 +  - **Pixels @ 3x**: 420 x 420 px
       141 +  - **Format recommandé**: JPEG ou WebP
       142 +  - **Poids max**: 50 KB
       143 +  
       144 +  **Avatar Header de Profil**: 80dp (240px @ 3x)
       145 +  - **Usage**: `ProfileScreen.kt:196-198` - en-tête de profil
       146 +  - **Pixels @ 3x**: 240 x 240 px
       147 +  - **Format recommandé**: JPEG ou WebP
       148 +  - **Poids max**: 30 KB
       149 +  
       150 +  ### Photos de Profil Recommandées
       151 +  - **Résolution minimum**: 420 x 420 px
       152 +  - **Résolution optimale**: 600 x 600 px
       153 +  - **Ratio**: 1:1 (carré)
       154 +  - **Format**: JPEG ou WebP
       155 +  - **Poids max**: 100 KB
       156 +  
       157 +  ---
       158 +  
       159 +  ## 🃏 Dimensions des Cartes (Cards)
       160 +  
       161 +  ### Cartes de Programme (ProgramsScreen)
       162 +  
       163 +  **Cartes Actives** (pleine largeur):
       164 +  - **Largeur**: fillMaxWidth (avec padding 16dp)
       165 +  - **Hauteur**: Auto (content-based)
       166 +  - **Padding interne**: 16dp
       167 +  - **Corner radius**: 16dp
       168 +  - **Elevation**: 2dp
       169 +  
       170 +  **Cartes Challenges**:
       171 +  - **Largeur**: 200dp
       172 +  - **Hauteur**: Auto
       173 +  - **Pixels @ 3x**: 600px largeur
       174 +  - **Corner radius**: 16dp
       175 +  
       176 +  ### Cartes de Bibliothèque (LibraryScreen)
       177 +  
       178 +  **Cartes Grid** (2 colonnes):
       179 +  - **Largeur**: weight(1f) ~174dp
       180 +  - **Espacement**: 12dp entre cartes
       181 +  - **Corner radius**: 16dp
       182 +  - **Elevation**: 2dp
       183 +  
       184 +  ### Cartes de Favoris (ProfileScreen)
       185 +  
       186 +  **Dimensions**:
       187 +  - **Largeur**: fillMaxWidth
       188 +  - **Hauteur**: 120dp
       189 +  - **Pixels @ 3x**: hauteur fixe 360px
       190 +  - **Corner radius**: 20dp
       191 +  - **Elevation**: 0dp (flat design)
       192 +  
       193 +  ---
       194 +  
       195 +  ## 📊 Éléments d'Interface Spéciaux
       196 +  
       197 +  ### Boutons de Sélection d'Humeur (Mood)
       198 +  
       199 +  **Taille**: 56dp circulaire (ou 48dp/64dp selon nombre d'options)
       200 +  - **Pixels @ 3x**: 168 x 168 px (ou 144/192)
       201 +  - **Format**: Emoji ou icône
       202 +  - **Usage**: `DailyJournalEntryScreen.kt:278`, `OnboardingScreen.kt:453`
       203 +  
       204 +  ### Boutons Principaux (Primary Buttons)
       205 +  
       206 +  **Hauteur**: 56dp
       207 +  - **Largeur**: fillMaxWidth
       208 +  - **Corner radius**: RoundedCornerShape (défaut Material 3)
       209 +  - **Usage**: `AuthScreen.kt:223, 291` - boutons de connexion
       210 +  
       211 +  ### Pill-Shaped Cards (PracticeCard)
       212 +  
       213 +  **Dimensions**:
       214 +  - **Padding**: 20dp horizontal, 16dp vertical
       215 +  - **Corner radius**: 50dp (full pill)
       216 +  - **Icon size**: 24dp
       217 +  - **Usage**: `PracticeCard.kt` - cartes de pratiques rapides
       218 +  
       219 +  ---
       220 +  
       221 +  ## 📏 Espacements Standards (pour layout dans Canva)
       222 +  
       223 +  ### Padding Horizontal
       224 +  - **Compact**: 12dp (36px @ 3x)
       225 +  - **Standard**: 16dp (48px @ 3x)
       226 +  - **Large**: 20dp (60px @ 3x)
       227 +  - **Extra-large**: 24dp (72px @ 3x)
       228 +  
       229 +  ### Espacement Vertical
       230 +  - **Tight**: 4dp (12px @ 3x)
       231 +  - **Small**: 8dp (24px @ 3x)
       232 +  - **Medium**: 12dp (36px @ 3x)
       233 +  - **Large**: 16dp (48px @ 3x)
       234 +  - **Extra-large**: 20dp (60px @ 3x)
       235 +  - **Section**: 24dp (72px @ 3x)
       236 +  
       237 +  ### Corner Radius (Arrondis)
       238 +  - **Small**: 12dp (36px @ 3x)
       239 +  - **Medium**: 14dp (42px @ 3x)
       240 +  - **Standard**: 16dp (48px @ 3x) - **le plus courant**
       241 +  - **Large**: 20dp (60px @ 3x)
       242 +  - **Extra-large**: 24dp (72px @ 3x)
       243 +  - **Pill**: 28-50dp ou CircleShape
       244 +  
       245 +  ---
       246 +  
       247 +  ## 🎨 Palette de Couleurs Ora
       248 +  
       249 +  ### Couleurs Principales
       250 +  - **Primary Orange**: `#F4845F` (Ora brand color)
       251 +  - **Secondary Peach**: `#F5C9A9`
       252 +  - **Background Beige**: `#F5EFE6`
       253 +  - **Surface**: `#FFFBF8` (cartes blanches chaudes)
       254 +  - **Light Background**: `#FFF5F0`
       255 +  
       256 +  ### Couleurs de Catégories
       257 +  - **Yoga**: `#8E7CC3` (Violet)
       258 +  - **Pilates**: `#E57373` (Rouge corail)
       259 +  - **Méditation**: `#64B5F6` (Bleu)
       260 +  - **Respiration**: `#81C784` (Vert)
       261 +  - **Podcast**: `#FFB74D` (Orange)
       262 +  - **Documentaire**: `#4DB6AC` (Teal)
       263 +  - **Musique**: `#F06292` (Rose)
       264 +  - **Lecture**: `#9575CD` (Violet clair)
       265 +  
       266 +  ### Couleurs de Gratitude
       267 +  - **Gratitude Pink**: `#FFCCD4CC`
       268 +  - **Gratitude Peach**: `#FFCCE5`
       269 +  - **Gratitude Mint**: `#D4E8D9`
       270 +  
       271 +  ### Couleurs de Difficulté
       272 +  - **Beginner** (Débutant): `#81C784` (Vert)
       273 +  - **Intermediate** (Intermédiaire): `#FFB74D` (Orange)
       274 +  - **Advanced** (Avancé): `#E57373` (Rouge)
       275 +  
       276 +  ---
       277 +  
       278 +  ## 📱 Templates Canva Recommandés
       279 +  
       280 +  ### Template 1: Image de Couverture de Programme (Horizontal)
       281 +  - **Dimensions**: 840 x 471 px (ratio 16:9)
       282 +  - **Zone de sécurité**: Laisser 40px de marge sur les côtés
       283 +  - **Texte**: Titre du programme (max 2 lignes, 48-60pt)
       284 +  - **Overlay**: Gradient semi-transparent (#000000 0-40% opacité) si texte blanc
       285 +  
       286 +  ### Template 2: Image de Couverture de Programme (Grid)
       287 +  - **Dimensions**: 528 x 297 px (ratio 16:9)
       288 +  - **Zone de sécurité**: Laisser 20px de marge
       289 +  - **Texte**: Titre court (max 1-2 lignes, 36-48pt)
       290 +  
       291 +  ### Template 3: Thumbnail de Leçon (Horizontal)
       292 +  - **Dimensions**: 540 x 303 px (ratio 16:9)
       293 +  - **Zone de sécurité**: 20px de marge
       294 +  - **Icône de catégorie**: 72px (en haut à gauche avec padding 16px)
       295 +  - **Durée**: Badge en bas à droite (ex: "15 min")
       296 +  
       297 +  ### Template 4: Thumbnail de Leçon (Grid)
       298 +  - **Dimensions**: 522 x 294 px (ratio 16:9)
       299 +  - **Zone de sécurité**: 16px de marge
       300 +  - **Icône**: 60px
       301 +  - **Texte**: Minimal (titre overlay si nécessaire)
       302 +  
       303 +  ### Template 5: Avatar Utilisateur
       304 +  - **Dimensions**: 600 x 600 px (carré)
       305 +  - **Format**: Circulaire (utiliser masque circulaire dans Canva)
       306 +  - **Zone centrale**: Le visage doit occuper 60-80% du cadre
       307 +  - **Background**: Uni ou dégradé léger (éviter les arrière-plans complexes)
       308 +  
       309 +  ### Template 6: Icône de Catégorie
       310 +  - **Dimensions**: 96 x 96 px (24dp @ 3x)
       311 +  - **Format**: PNG transparent ou SVG
       312 +  - **Style**: Ligne simple, 3px stroke
       313 +  - **Couleur**: Utiliser la couleur de catégorie correspondante
       314 +  - **Zone de dessin**: 72 x 72 px (laisser 12px de padding)
       315 +  
       316 +  ### Template 7: Image Card Onboarding (16:9)
       317 +  - **Dimensions**: 936 x 527 px
       318 +  - **Overlay**: Optionnel, léger si texte superposé
       319 +  - **Style**: Illustration moderne, couleurs Ora
       320 +  
       321 +  ### Template 8: Grid Selection Card (Carré)
       322 +  - **Dimensions**: 468 x 468 px (1:1)
       323 +  - **Style**: Icône + texte court
       324 +  - **Background**: Surface color `#FFFBF8` ou couleur catégorie
       325 +  - **Corner radius**: Arrondir à 48px dans Canva
       326 +  
       327 +  ---
       328 +  
       329 +  ## 🔄 Facteurs de Densité Android
       330 +  
       331 +  **Important**: Les dimensions en dp sont indépendantes de la densité. Voici les conversions pour export:
       332 +  
       333 +  | Densité | Facteur | Exemple 100dp |
       334 +  |---------|---------|---------------|
       335 +  | mdpi    | 1x      | 100 px        |
       336 +  | hdpi    | 1.5x    | 150 px        |
       337 +  | xhdpi   | 2x      | 200 px        |
       338 +  | xxhdpi  | 3x      | 300 px        |
       339 +  | xxxhdpi | 4x      | 400 px        |
       340 +  
       341 +  **Recommandation**: Créer vos assets en **3x (xxhdpi)** qui est le standard pour la plupart des appareils Android modernes.
       342 +  
       343 +  ---
       344 +  
       345 +  ## 📋 Checklist pour Création d'Assets
       346 +  
       347 +  ### Images de Programmes/Leçons
       348 +  - [ ] Ratio 16:9 respecté
       349 +  - [ ] Résolution minimale atteinte (voir tableaux ci-dessus)
       350 +  - [ ] Poids de fichier optimisé (WebP ou JPEG compressé)
       351 +  - [ ] Zone de sécurité respectée (pas de texte important près des bords)
       352 +  - [ ] Contraste suffisant pour le texte overlay
       353 +  - [ ] Couleurs alignées avec la palette Ora
       354 +  
       355 +  ### Icônes
       356 +  - [ ] Taille correcte (24dp = 72px @ 3x minimum)
       357 +  - [ ] Format PNG transparent ou SVG
       358 +  - [ ] Style cohérent avec Material Design
       359 +  - [ ] Couleur de catégorie appropriée
       360 +  - [ ] Testée sur fond clair et foncé
       359 +  - [ ] Couleur de catégorie appropriée
       360 +  - [ ] Testée sur fond clair et foncé
       361 +  
       362 +  ### Photos de Profil
       363 +  - [ ] Format carré (1:1)
       364 +  - [ ] Résolution minimum 420 x 420 px
       365 +  - [ ] Visage centré et bien cadré
       366 +  - [ ] Bonne luminosité et contraste
       367 +  - [ ] Fond simple (éviter distraction)
       368 +  - [ ] Poids < 100 KB
       369 +  
       370 +  ---
       371 +  
       372 +  ## 🚀 Export et Intégration
       373 +  
       374 +  ### Formats Recommandés
       375 +  1. **Photos/Images de contenu**: WebP (meilleure compression) ou JPEG (90% qualité)
       376 +  2. **Icônes avec transparence**: PNG-24 ou SVG
       377 +  3. **Avatars**: JPEG (85-90% qualité) ou WebP
       378 +  
       379 +  ### Naming Convention
       380 +  ```
       381 +  [type]_[category]_[size]_[variant].ext
       382 +  
       383 +  Exemples:
       384 +  - program_cover_meditation_280x157.webp
       385 +  - lesson_thumb_yoga_540x303.jpg
       386 +  - icon_category_pilates_72.png
       387 +  - avatar_user_default_420.jpg
       388 +  ```
       389 +  
       390 +  ### Hébergement
       391 +  - **Firebase Storage**: Pour toutes les images uploadées par admin/users
       392 +  - **Assets Android**: Pour icônes statiques et images par défaut
       393 +  - **CDN**: Pour assets fréquemment accédés
       394 +  
       395 +  ---
       396 +  
       397 +  ## 📞 Support
       398 +  
       399 +  Pour toute question sur les dimensions ou la création d'assets graphiques:
       400 +  1. Consulter ce guide en premier
       401 +  2. Vérifier les fichiers source dans `presentation/screens/` et `presentation/theme/`
       402 +  3. Référencer la documentation design: `docs/DESIGN_SYSTEM_SUMMARY.md`
       403 +  
       404 +  **Dernière mise à jour**: 2025-11-16
       405 +  **Version**: 1.0