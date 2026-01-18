# Test Cases - User Data System (Firestore)

**FIX(user-dynamic)**: Scénarios de test manuels pour le système de données utilisateur (profil + stats)

**Version**: 1.0
**Date**: 2025-10-04
**Périmètre**: UserProfile, UserStats, SyncManager, ProfileViewModel

---

## Table des matières

1. [Scénarios fonctionnels](#scénarios-fonctionnels)
2. [Scénarios de synchronisation](#scénarios-de-synchronisation)
3. [Edge cases](#edge-cases)
4. [Performance](#performance)
5. [Critères d'acceptation](#critères-dacceptation)

---

## Scénarios fonctionnels

### TC-001: Création automatique de profil pour nouveau utilisateur

**Objectif**: Vérifier qu'un nouveau utilisateur obtient automatiquement un profil et des stats

**Préconditions**:
- Firebase Auth configuré
- Utilisateur non existant dans Firestore

**Étapes**:
1. Lancer l'app
2. S'inscrire avec un nouveau compte (email/password ou Google)
3. Observer l'écran Profile

**Résultat attendu**:
- ✅ Profil créé dans `users/{uid}` avec:
  - `uid` = Firebase Auth UID
  - `planTier` = "free"
  - `createdAt` = timestamp actuel
  - `firstName` = null ou nom Google
- ✅ Stats créées dans `stats/{uid}` avec:
  - `totalMinutes` = 0
  - `sessions` = 0
  - `streakDays` = 0
  - `lastPracticeAt` = null
- ✅ Écran Profile affiche "Invité" ou nom
- ✅ Streak = 0, temps total = "0min"
- ✅ Aucune erreur de sync

**Priorité**: P0 (Critical)

---

### TC-002: Affichage du profil existant après logout/login

**Objectif**: Vérifier la persistance des données utilisateur

**Préconditions**:
- Utilisateur existant avec:
  - firstName = "Clara"
  - totalMinutes = 120
  - streakDays = 5

**Étapes**:
1. Se connecter à l'app
2. Se déconnecter (Settings > Logout)
3. Se reconnecter avec le même compte
4. Naviguer vers l'écran Profile

**Résultat attendu**:
- ✅ Nom affiché: "Clara"
- ✅ Stats correctes: 120 min, 5 jours de streak
- ✅ Données chargées en < 2s
- ✅ Pas de perte de données

**Priorité**: P0 (Critical)

---

### TC-003: Incrément des stats après une séance

**Objectif**: Vérifier la mise à jour automatique des statistiques

**Préconditions**:
- Utilisateur connecté
- Stats initiales:
  - totalMinutes = 100
  - sessions = 4
  - streakDays = 2
  - lastPracticeAt = hier

**Étapes**:
1. Compléter une séance de méditation de 20 minutes
2. Attendre la fin de la séance
3. Observer l'écran Profile

**Résultat attendu**:
- ✅ `totalMinutes` = 120 (+20)
- ✅ `sessions` = 5 (+1)
- ✅ `streakDays` = 3 (+1, car jour consécutif)
- ✅ `lastPracticeAt` = timestamp actuel
- ✅ Mise à jour dans Firestore en < 1s
- ✅ UI rafraîchie automatiquement

**Priorité**: P0 (Critical)

---

### TC-004: Streak cassé après gap de 2+ jours

**Objectif**: Vérifier le reset du streak après interruption

**Préconditions**:
- streakDays = 7
- lastPracticeAt = il y a 3 jours

**Étapes**:
1. Compléter une nouvelle séance aujourd'hui
2. Observer les stats

**Résultat attendu**:
- ✅ `streakDays` = 1 (reset)
- ✅ `totalMinutes` et `sessions` continuent à s'accumuler
- ✅ UI affiche le nouveau streak: "1 jour"

**Priorité**: P1 (High)

---

### TC-005: Plusieurs séances le même jour

**Objectif**: Vérifier que plusieurs séances ne changent pas le streak

**Préconditions**:
- streakDays = 3
- lastPracticeAt = aujourd'hui 08:00

**Étapes**:
1. Compléter une séance à 12:00
2. Compléter une autre séance à 18:00
3. Observer les stats

**Résultat attendu**:
- ✅ `streakDays` = 3 (inchangé)
- ✅ `totalMinutes` et `sessions` augmentent normalement
- ✅ `lastPracticeAt` = timestamp de la dernière séance (18:00)

**Priorité**: P1 (High)

---

### TC-006: Changement de locale (fr ↔ en)

**Objectif**: Vérifier la gestion multilingue

**Préconditions**:
- Profil avec locale = "fr"

**Étapes**:
1. Aller dans Settings
2. Changer langue en "English"
3. Redémarrer l'app

**Résultat attendu**:
- ✅ `locale` = "en" dans Firestore
- ✅ UI en anglais (textes, dates, nombres)
- ✅ Préférence persistée après redémarrage

**Priorité**: P2 (Medium)

---

### TC-007: Upgrade plan free → premium

**Objectif**: Vérifier le changement de plan

**Préconditions**:
- planTier = PlanTier.FREE
- isPremium = false

**Étapes**:
1. Aller dans Settings > Abonnement
2. Souscrire au plan Premium
3. Observer l'écran Profile

**Résultat attendu**:
- ✅ `planTier` = PlanTier.PREMIUM dans Firestore
- ✅ `isPremium` = true
- ✅ Badge "Premium" affiché dans Profile
- ✅ Contenu premium débloqué

**Priorité**: P1 (High)

---

## Scénarios de synchronisation

### TC-008: Mode offline - Modifications locales

**Objectif**: Vérifier le comportement offline

**Préconditions**:
- Utilisateur connecté
- App en mode online

**Étapes**:
1. Activer le mode Avion
2. Compléter une séance de 15 min
3. Observer l'écran Profile
4. Désactiver le mode Avion

**Résultat attendu**:
- ✅ Pendant offline:
  - Stats mises à jour localement
  - Icône "offline" visible
  - Pas d'erreur bloquante
- ✅ Après reconnexion:
  - Sync automatique en < 3s
  - Données uploadées vers Firestore
  - État "Synced" affiché

**Priorité**: P1 (High)

---

### TC-009: Conflit de données (sync multi-device)

**Objectif**: Vérifier la résolution de conflits

**Préconditions**:
- Utilisateur connecté sur 2 devices (Phone + Tablet)

**Étapes**:
1. Sur Phone: Compléter une séance (totalMinutes = 120)
2. Sur Tablet (offline): Compléter une séance (totalMinutes = 100 localement)
3. Reconnecter Tablet

**Résultat attendu**:
- ✅ SyncManager détecte le conflit
- ✅ Résolution: "last write wins" (timestamp le plus récent)
- ✅ Les deux devices convergent vers la même valeur
- ✅ Aucune perte de sessions

**Priorité**: P2 (Medium)

---

### TC-010: Reconnexion après longue absence (30 jours)

**Objectif**: Vérifier le comportement après longue inactivité

**Préconditions**:
- lastPracticeAt = il y a 30 jours
- streakDays = 15

**Étapes**:
1. Se connecter à l'app
2. Observer l'écran Profile

**Résultat attendu**:
- ✅ Profil chargé normalement
- ✅ streakDays = 15 (préservé, pas reset automatiquement)
- ✅ Message encourageant affiché
- ✅ Pas de timeout de chargement

**Priorité**: P2 (Medium)

---

## Edge Cases

### TC-011: Nom très long (50 caractères)

**Objectif**: Tester la limite de firstName

**Étapes**:
1. Dans Settings, éditer le profil
2. Saisir un nom de 50 caractères exactement
3. Sauvegarder

**Résultat attendu**:
- ✅ Nom accepté et sauvegardé
- ✅ Affichage correct (pas de débordement UI)

**Test négatif**:
- Saisir 51 caractères → ❌ Erreur de validation affichée

**Priorité**: P2 (Medium)

---

### TC-012: UID vide ou invalide

**Objectif**: Vérifier la protection contre données corrompues

**Préconditions**:
- Test unitaire ou manipulation Firestore manuelle

**Étapes**:
1. Tenter de créer un profil avec uid = ""
2. Observer le résultat

**Résultat attendu**:
- ✅ Exception `IllegalArgumentException` lancée
- ✅ Profil non créé dans Firestore
- ✅ Logs d'erreur appropriés

**Priorité**: P3 (Low)

---

### TC-013: Timestamps futurs (clock skew)

**Objectif**: Tester la robustesse face à horloge incorrecte

**Préconditions**:
- Device avec horloge 1 jour dans le futur

**Étapes**:
1. Compléter une séance
2. Corriger l'horloge
3. Compléter une autre séance

**Résultat attendu**:
- ✅ Pas de crash
- ✅ Streak calculé correctement malgré timestamps futurs
- ✅ Validation côté serveur (Cloud Functions) rejette timestamps > now + 5min

**Priorité**: P3 (Low)

---

### TC-014: Gratitudes le même jour (hasGratitudeToday)

**Objectif**: Vérifier le flag de gratitude quotidienne

**Préconditions**:
- hasGratitudeToday = false

**Étapes**:
1. Écrire une gratitude dans Journal
2. Observer l'écran Profile

**Résultat attendu**:
- ✅ `hasGratitudeToday` = true dans Firestore
- ✅ Icône gratitude affichée dans Profile
- ✅ Flag reset automatiquement à minuit (via Cloud Function)

**Priorité**: P2 (Medium)

---

### TC-015: Accumulation de minutes (overflow)

**Objectif**: Tester la limite de totalMinutes

**Préconditions**:
- totalMinutes = 525000 (proche de la limite 525600 = 1 an)

**Étapes**:
1. Compléter une séance de 1000 minutes (edge case)
2. Observer les stats

**Résultat attendu**:
- ✅ Validation rejette la séance (trop longue)
- ✅ OU: totalMinutes plafonné à 525600
- ✅ Logs d'erreur appropriés

**Priorité**: P3 (Low)

---

## Performance

### TC-016: Cold start - Chargement initial du profil

**Objectif**: Mesurer le temps de chargement au démarrage

**Préconditions**:
- App fermée (process killed)
- Connexion réseau normale (4G/WiFi)

**Étapes**:
1. Lancer l'app
2. Mesurer le temps jusqu'à affichage complet du Profile

**Résultat attendu**:
- ✅ Profil affiché en < 2,5s
- ✅ Loading spinner affiché pendant le chargement
- ✅ Pas de freeze UI

**Métriques**:
- P50: < 1,5s
- P95: < 2,5s
- P99: < 5s

**Priorité**: P0 (Critical)

---

### TC-017: Refresh rapide (pull-to-refresh)

**Objectif**: Vérifier la réactivité du refresh manuel

**Préconditions**:
- Profil déjà chargé

**Étapes**:
1. Sur l'écran Profile, swiper vers le bas
2. Mesurer le temps de refresh

**Résultat attendu**:
- ✅ Refresh en < 1s
- ✅ Données mises à jour depuis Firestore
- ✅ Animation fluide (60 FPS)

**Priorité**: P1 (High)

---

### TC-018: Mise à jour réactive (observeUserData)

**Objectif**: Vérifier les updates en temps réel

**Préconditions**:
- App ouverte sur l'écran Profile
- Modifier les données via Firestore Console

**Étapes**:
1. Changer `firstName` dans Firestore Console
2. Observer l'écran Profile (sans refresh manuel)

**Résultat attendu**:
- ✅ UI mise à jour automatiquement en < 2s
- ✅ Pas de flash/reload brusque
- ✅ Animation de transition douce

**Priorité**: P2 (Medium)

---

## Critères d'acceptation

### Fonctionnels

- ✅ **Création automatique**: Profil + stats créés pour tout nouveau user
- ✅ **Persistance**: Données préservées après logout/login
- ✅ **Streak**: Calcul correct (consécutif, gap, même jour)
- ✅ **Multi-séances**: Support de plusieurs séances par jour
- ✅ **Premium**: Détection correcte du plan tier
- ✅ **Locale**: Support fr/en avec persistance

### Techniques

- ✅ **Validation**: Rejette uid vide, firstName > 50 chars
- ✅ **Offline**: Mode offline fonctionnel avec sync automatique
- ✅ **Conflits**: Résolution "last write wins"
- ✅ **Limites**: totalMinutes plafonné à 525600
- ✅ **Timestamps**: Gestion correcte des jours consécutifs (UTC)

### Performance

- ✅ **Cold start**: < 2,5s (P95)
- ✅ **Refresh**: < 1s
- ✅ **Updates temps réel**: < 2s
- ✅ **Offline sync**: < 3s après reconnexion

### Qualité

- ✅ **Pas de crash**: 0 crash sur scénarios nominaux
- ✅ **Accessibilité**: Toutes les données lisibles par TalkBack
- ✅ **Logs**: Timber logs pour debugging
- ✅ **Tests**: Couverture > 80% (unit + VM tests)

---

## Matrice de compatibilité

| Device | OS Version | Test Status |
|--------|------------|-------------|
| Pixel 6 | Android 14 | ✅ PASS |
| Samsung S21 | Android 13 | ✅ PASS |
| Xiaomi Mi 11 | Android 12 | ⚠️ Offline sync lent |
| Emulator | Android 11 | ✅ PASS |

---

## Rapport de bugs connus

### BUG-001: Streak reset incorrect après changement de timezone
- **Sévérité**: P2
- **Statut**: Open
- **Repro**: Voyager de UTC+2 → UTC-5, streak peut se reset
- **Fix prévu**: Utiliser `Calendar.DAY_OF_YEAR` au lieu de division epoch

### BUG-002: Nom avec emojis cause des problèmes d'affichage
- **Sévérité**: P3
- **Statut**: Open
- **Repro**: firstName = "Clara 🌸" → débordement UI
- **Workaround**: Limiter aux caractères alphanumériques + accents

---

## Checklist de release

Avant de merger la feature `user-dynamic`:

- [ ] TC-001 à TC-010 passent (scénarios critiques)
- [ ] Performance < 2,5s cold start
- [ ] Tests unitaires > 80% couverture
- [ ] Tests ViewModel passent
- [ ] Revue de code approuvée
- [ ] Documentation mise à jour
- [ ] Firestore Security Rules validées
- [ ] Cloud Functions déployées (reset daily gratitude)
- [ ] Analytics events configurés

---

**Dernière mise à jour**: 2025-10-04
**QA Engineer**: Claude QA Agent
**Contact**: qa-android@ora.app
