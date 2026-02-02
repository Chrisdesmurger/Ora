# Flux Utilisateurs - Ora Android

## Vue d'ensemble

Cette documentation définit les parcours utilisateurs principaux de l'application Ora Android, optimisés pour une expérience mobile intuitive et engageante.

## Personas Cibles

### Persona Principale : Sarah, 32 ans
- **Profil** : Maman active, travaille à temps plein
- **Objectifs** : Réduire stress, améliorer sommeil, moments de détente
- **Contraintes** : Temps limité (5-20min max), utilisation irrégulière
- **Motivations** : Bien-être personnel, équilibre vie pro/perso

### Persona Secondaire : Emma, 28 ans
- **Profil** : Étudiante/jeune professionnelle, débutante en yoga
- **Objectifs** : Découvrir yoga et méditation, créer routine matinale
- **Contraintes** : Budget limité, besoin de guidance
- **Motivations** : Développement personnel, gestion anxiété

## Flux Principaux

### 1. Premier Lancement & Onboarding

#### 1.1 Parcours Découverte (Nouvelle Utilisatrice)
```
[Écran Splash]
    ↓
[Écran Bienvenue]
    → "Commencer"
    ↓
[Carousel Introduction] (3 écrans)
    → Écran 1: "Bienvenue dans votre espace bien-être"
    → Écran 2: "Yoga, méditation, respiration en 5-20min"
    → Écran 3: "Votre journal personnel de gratitude"
    ↓
[Configuration Préférences]
    → Sélection créneaux préférés:
      • Matin (6h-10h)
      • Journée (10h-18h)
      • Soir (18h-23h)
    → Question: "Quel est votre niveau en yoga ?"
      • Débutante complète
      • Quelques bases
      • Pratique occasionnelle
    ↓
[Permissions & Confidentialité]
    → Explication notifications intelligentes
    → Politique de confidentialité (résumé)
    → Boutons: "Autoriser notifications" / "Plus tard"
    ↓
[Dashboard Principal] ✓
```

**Points clés UX :**
- **Durée cible** : 2-3 minutes maximum
- **Skip possible** : À partir du 2ème écran carousel
- **Progression visible** : Dots indicator en bas
- **Personnalisation immédiate** : Adaptations basées sur les réponses

#### 1.2 États d'Erreur Onboarding
```
[Erreur Réseau]
    → Message: "Pas de souci, Ora fonctionne hors ligne !"
    → Action: "Continuer" (skip synchronisation)

[Permission Refusée]
    → Message: "Vous pourrez activer les rappels plus tard dans Profil"
    → Action: "D'accord" (continue sans notifications)
```

### 2. Navigation Quotidienne

#### 2.1 Ouverture Application (Utilisatrice Régulière)
```
[Écran Splash] (< 1 seconde)
    ↓
[Dashboard Principal]
    → Salutation contextuelle: "Bonjour Sarah !" / "Bonne soirée !"
    → Recommandation du jour:
      • Carte avec thumbnail, titre, durée
      • CTA: "Commencer maintenant"
    → Actions rapides:
      • "Flash Yoga 5min"
      • "Respiration Calme"
      • "Mini Méditation"
    → Aperçu progrès:
      • "3 sessions cette semaine"
      • "Série de 7 jours !" (avec célébration)
```

#### 2.2 Bottom Navigation (5 Onglets)
```
[Accueil] 🏠
    → Dashboard principal
    → Recommandations personnalisées
    → Actions rapides

[Bibliothèque] 📚
    → Catalogue complet des contenus
    → Filtres et recherche
    → Catégories : Yoga, Méditation, Respiration

[Journal] ✍️
    → Écriture quotidienne
    → Gratitudes + humeur + récit
    → Historique personnel

[Programmes] 📅
    → Programmes 21 jours (V1.1)
    → Défis hebdomadaires
    → Parcours structurés

[Profil] 👤
    → Statistiques personnelles
    → Paramètres application
    → Badges et accomplissements
```

**Règles Navigation :**
- **Badge notifications** : Affichage sur Journal si pas rempli aujourd'hui
- **États actifs** : Onglet courant highlighté Material 3
- **Deep links** : Chaque onglet accessible directement via URL

### 3. Flux Consommation de Contenu

#### 3.1 Découverte & Sélection dans Bibliothèque
```
[Bibliothèque]
    ↓
[Navigation par Onglets]
    → "Tous" (vue par défaut)
    → "Yoga" | "Méditation" | "Respiration" | "Pilates"
    ↓
[Liste/Grille Contenu] (Lazy Loading)
    → Affichage: Card avec thumbnail, titre, durée, niveau
    → Action: Tap sur card
    ↓
[Détail Contenu]
    → Informations: Description, instructeur, bienfaits
    → Preview video (10 secondes)
    → Boutons: "Commencer" | "♡ Favoris" | "Partager"
    ↓
[Player Vidéo/Audio] ✓
```

#### 3.2 Système de Filtres
```
[Bibliothèque]
    → Tap "Filtres" (icône en haut à droite)
    ↓
[Bottom Sheet Filtres]
    → Durée: [5min] [10min] [15min] [20min+]
    → Niveau: [Débutant] [Intermédiaire] [Avancé]
    → Objectif: [Réveil] [Énergie] [Détente] [Sommeil]
    → Type: [Yoga] [Pilates] [Méditation] [Respiration]
    → Actions: "Réinitialiser" | "Appliquer (X résultats)"
    ↓
[Résultats Filtrés]
    → Indication filtres actifs en haut
    → Possibilité de retirer filtres individuellement
```

#### 3.3 Recherche Intelligente
```
[Barre Recherche] (dans Bibliothèque)
    → Saisie utilisateur
    ↓
[Suggestions Temps Réel]
    → Recherches récentes
    → Termes populaires: "matin énergisant", "détente soir"
    → Autocomplete intelligent
    ↓
[Résultats Recherche]
    → Groupés par type de contenu
    → Highlight des termes recherchés
    → "Aucun résultat" → suggestions alternatives
```

### 4. Expérience Player Vidéo/Audio

#### 4.1 Lancement Session
```
[Détail Contenu]
    → Tap "Commencer"
    ↓
[Écran Préparation] (2-3 secondes)
    → "Préparez votre tapis..."
    → Countdown 3, 2, 1
    ↓
[Player Full Screen]
    → Vidéo en plein écran
    → Contrôles masqués par défaut
    → Tap pour afficher contrôles (auto-hide 3s)
```

#### 4.2 Contrôles Player
```
[Interface Player]
    → Contrôles principaux:
      • Play/Pause (centre, large)
      • Seek bar avec preview
      • Timer: "3:45 / 12:00"
      • Volume (slider)
    → Contrôles secondaires:
      • Vitesse: 0.75x, 1x, 1.25x, 1.5x
      • Sous-titres (si disponibles)
      • Picture-in-Picture
    → Action sortie:
      • Back button → Confirmation "Quitter la session ?"
      • Home gesture → Continuer en PiP (optionnel)
```

#### 4.3 Fin de Session
```
[Completion Session]
    → Animation célébration (2s)
    → Message: "Bravo ! Session terminée 🌟"
    → Statistiques:
      • Durée pratiquée: "12 minutes"
      • Calories brûlées (estimation)
    → Actions:
      • "Écrire dans mon journal" (CTA principal)
      • "Sessions similaires"
      • "Retour à l'accueil"
    → Auto-save progression utilisateur
```

### 5. Flux Journal Personnel

#### 5.1 Écriture Quotidienne
```
[Onglet Journal]
    ↓
[État Vide/Nouveau Jour]
    → Message motivant: "Comment s'est passée votre journée ?"
    → Card "Nouvelle entrée" prominente
    ↓
[Formulaire Journal]
    → Section 1: "3 gratitudes d'aujourd'hui"
      • 3 champs texte avec placeholders inspirants
      • Compteur caractères (optionnel)
    → Section 2: "Comment vous sentez-vous ?"
      • Sélecteur humeur visuel (7 émojis colorés)
      • Animation feedback au tap
    → Section 3: "Votre histoire du jour"
      • Éditeur texte libre
      • Placeholder: "Racontez votre journée..."
    → Auto-save toutes les 10 secondes
    ↓
[Sauvegarde]
    → Bouton "Sauvegarder" toujours visible
    → Confirmation: "Votre journal est sauvé ✓"
    → Option: "Voir mon journal" ou "Retour accueil"
```

#### 5.2 Consultation Historique
```
[Journal - Vue Historique]
    → Toggle view: [Calendrier] / [Liste]
    ↓
[Vue Calendrier]
    → Calendrier mensuel
    → Dots colorés selon humeur du jour
    → Tap sur date → Entrée complète
    ↓
[Vue Liste]
    → Cards chronologiques
    → Aperçu: date, humeur, première gratitude
    → Recherche par mots-clés
    ↓
[Détail Entrée]
    → Lecture complète
    → Options: "Modifier" | "Supprimer" | "Partager insights"
```

#### 5.3 Fonctionnalités Privacy
```
[Protection Journal]
    → Premier accès après installation:
      • "Votre journal est privé et sécurisé"
      • Option biométrie: "Verrouiller avec empreinte ?"
    ↓
[Mode Privé Activé]
    → Accès journal nécessite biométrie
    → Fallback: code PIN 4 chiffres
    → Timeout automatique après 5min inactivité
    ↓
[Échec Authentification]
    → 3 tentatives maximum
    → Message: "Journal temporairement verrouillé"
    → Bouton: "Mot de passe oublié ?" → Reset complet
```

### 6. Flux Profil & Statistiques

#### 6.1 Vue d'ensemble Profil
```
[Onglet Profil]
    ↓
[Header Utilisateur]
    → Avatar (initiales ou photo)
    → "Bonjour Sarah !"
    → Niveau: "Yogi en herbe" (gamification)
    ↓
[Cartes Statistiques] (2x2 grid)
    → "Sessions totales": 47 (avec trend ↗️)
    → "Minutes pratiquées": 542min
    → "Série actuelle": 7 jours 🔥
    → "Type préféré": Méditation
    ↓
[Calendrier Activité]
    → Vue similaire GitHub (365 jours)
    → Intensité par couleur
    → Tap pour détails jour spécifique
    ↓
[Menu Actions]
    → "Paramètres"
    → "Mes badges"
    → "Exporter mes données"
    → "À propos"
```

#### 6.2 Paramètres Application
```
[Paramètres]
    ↓
[Sections Organisées]
    → Préférences:
      • Notifications (ON/OFF + horaires)
      • Thème: Auto, Clair, Sombre
      • Langue: Français, English
    → Contenu:
      • Téléchargements hors ligne
      • Qualité vidéo: Auto, HD, SD
    → Confidentialité:
      • Verrouillage journal
      • Données analytics (opt-in)
      • Supprimer mon compte
    → Support:
      • Aide & FAQ
      • Nous contacter
      • Évaluer l'app
```

### 7. Flux Programmes 21 Jours (V1.1)

#### 7.1 Découverte Programmes
```
[Onglet Programmes]
    ↓
[Catalogue Programmes]
    → Cards programmes disponibles:
      • "21 jours Réveil Énergisant"
      • "Détox Stress 21 jours"
      • "Sommeil Profond 3 semaines"
    → Infos: Durée, niveau, nombre sessions
    ↓
[Détail Programme]
    → Description complète
    → Planning aperçu (21 sessions)
    → Témoignages utilisatrices
    → CTA: "Commencer le programme"
```

#### 7.2 Suivi Programme
```
[Programme Actif]
    → Progress bar: "Jour 8 / 21"
    → Session du jour mise en avant
    → Calendrier progression avec états:
      • ✅ Complété
      • 🔵 Aujourd'hui
      • ⚪ À venir
    → Statistiques: "12 sessions complétées"
    ↓
[Completion Programme]
    → Célébration spéciale
    → Badge "Programme terminé"
    → Recommendations programmes suivants
```

## Cas d'Erreur & Edge Cases

### Gestion Hors Ligne
```
[Pas de Réseau - Premier Lancement]
    → Message: "Conexion requise pour la première utilisation"
    → Action: "Réessayer" (check connectivité)

[Pas de Réseau - Utilisation Normale]
    → Bandeau discret: "Mode hors ligne"
    → Fonctionnalités limitées clairement indiquées
    → Synchronisation auto au retour réseau
```

### Gestion d'État Loading
```
[Chargement Contenu]
    → Skeleton loading pour cards
    → Spinners pour actions utilisateur
    → Timeout après 30s → Message d'erreur

[Échec Chargement]
    → Message contextuel: "Impossible de charger ce contenu"
    → Actions: "Réessayer" | "Signaler le problème"
```

### Interruptions Externes
```
[Appel Entrant pendant Session]
    → Player automatiquement mis en pause
    → Notification sticky: "Session en pause - Reprendre ?"
    → Reprise automatique après appel

[Batterie Faible]
    → Warning à 15% : "Branchez votre appareil pour continuer"
    → Sauvegarde automatique état session
```

## Métriques UX Cibles

### Performance Perçue
- **Time to First Content** : < 2 secondes
- **Navigation Fluidity** : 60 FPS constant
- **Video Start Time** : < 3 secondes

### Engagement
- **Onboarding Completion** : > 80%
- **Daily Return Rate** : > 25%
- **Session Completion** : > 70%

### Satisfaction
- **Task Success Rate** : > 95% (scénarios critiques)
- **Error Recovery** : < 10 secondes
- **Accessibility Score** : 100% AA compliance

Cette conception UX priorise la simplicité, la personnalisation et l'engagement quotidien tout en respectant le temps limité des utilisatrices cibles.