# Plan de Test - Onboarding Responses Collection

**Issue**: #15 - Dedicated Firestore collection for onboarding responses
**Date**: 2025-11-16
**Phase**: Phase 2 - Testing & Validation

## ✅ Phase 1: Completed

- [x] Firestore rules deployed
- [x] Android dual write implemented
- [x] Web Admin APIs updated
- [x] Data models verified

## 🧪 Phase 2: Tests à Exécuter

### Test 1: Dual Write Android

**Objectif**: Vérifier que les réponses sont sauvegardées dans les deux collections

**Étapes**:
1. Compiler et installer l'app Android:
   ```bash
   cd /c/Users/chris/source/repos/Ora
   ./gradlew.bat clean assembleDebug installDebug
   ```

2. Lancer l'app sur émulateur/appareil

3. Se connecter avec un compte de test (noter l'UID)

4. Compléter le questionnaire d'onboarding:
   - Répondre à toutes les questions
   - Cliquer sur "Terminer"

5. Vérifier dans Firebase Console:
   - Ouvrir: https://console.firebase.google.com/project/ora-wellbeing/firestore
   - **Collection 1**: `users/{uid}`
     - Champ `onboarding` doit exister
     - Contenir: `uid`, `config_version`, `completed`, `started_at`, `completed_at`, `answers`, `metadata`
   - **Collection 2**: `user_onboarding_responses/{uid}/responses/{configVersion}`
     - Document avec ID = version de config
     - Contenir les mêmes données avec snake_case: `config_version`, `started_at`, etc.

**Résultat attendu**: ✅ Les deux collections contiennent les mêmes données

---

### Test 2: API Responses

**Objectif**: Vérifier que l'API récupère les réponses depuis la nouvelle collection

**Étapes**:
1. Récupérer l'ID de configuration active:
   ```bash
   # Ouvrir Firebase Console > onboarding_configs
   # Noter l'ID du config avec status="active"
   ```

2. Tester l'endpoint dans le Web Admin:
   - Se connecter à OraWebApp en tant qu'admin
   - Naviguer vers: `/admin/onboarding/[CONFIG_ID]/analytics`
   - Vérifier que la page charge sans erreur

3. Vérifier les données dans l'onglet Network:
   - Ouvrir DevTools > Network
   - Recharger la page
   - Trouver la requête: `GET /api/admin/onboarding/[id]/responses`
   - Vérifier la réponse JSON contient:
     ```json
     {
       "data": {
         "responses": [
           {
             "uid": "...",
             "configVersion": "...",
             "completed": true,
             "startedAt": {...},
             "completedAt": {...},
             "answers": [...]
           }
         ],
         "pagination": {...}
       }
     }
     ```

**Résultat attendu**: ✅ API retourne les réponses avec structure correcte

---

### Test 3: Analytics

**Objectif**: Vérifier les calculs d'analytics (taux de completion, drop-offs)

**Étapes**:
1. Naviguer vers: `/admin/onboarding/[CONFIG_ID]/analytics`

2. Vérifier l'endpoint analytics:
   - DevTools > Network > `GET /api/admin/onboarding/[id]/analytics`
   - Vérifier la structure de réponse:
     ```json
     {
       "data": {
         "versionId": "...",
         "totalStarts": 5,
         "totalCompletions": 3,
         "completionRate": 60.0,
         "averageTimeSeconds": 120,
         "questionMetrics": {
           "q1": {
             "questionId": "q1",
             "questionTitle": "...",
             "views": 5,
             "answers": 5,
             "dropOffs": 0,
             "dropOffRate": 0,
             "answerDistribution": {...}
           }
         }
       }
     }
     ```

3. Vérifier les calculs:
   - `completionRate = (totalCompletions / totalStarts) * 100`
   - `dropOffRate` par question
   - `answerDistribution` contient les counts par option

**Résultat attendu**: ✅ Analytics calculées correctement

---

### Test 4: Export CSV

**Objectif**: Vérifier l'export CSV des réponses

**Étapes**:
1. Dans Web Admin, naviguer vers l'onboarding analytics

2. Cliquer sur le bouton "Export CSV"

3. Vérifier le fichier téléchargé:
   - Nom: `onboarding-responses-[CONFIG_ID]-[TIMESTAMP].csv`
   - Headers contiennent: `UID, Email, First Name, Last Name, Completed, Started At, Completed At, Total Time (seconds), Device Type, App Version, Locale`
   - Puis une colonne par question: `Q: [Question Title]`
   - Les réponses sont correctement échappées (virgules, guillemets)

4. Ouvrir dans Excel/Google Sheets et vérifier la lisibilité

**Résultat attendu**: ✅ CSV bien formé et lisible

---

### Test 5: Export JSON

**Objectif**: Vérifier l'export JSON des réponses

**Étapes**:
1. Modifier l'URL pour forcer format JSON:
   ```
   /api/admin/onboarding/[CONFIG_ID]/export?format=json
   ```

2. Vérifier le fichier téléchargé:
   - Nom: `onboarding-responses-[CONFIG_ID]-[TIMESTAMP].json`
   - Structure JSON valide
   - Contient tous les champs: `uid`, `configVersion`, `completed`, `answers`, `metadata`, `goals`, etc.

3. Valider le JSON avec un parser:
   ```bash
   cat onboarding-responses-*.json | jq .
   ```

**Résultat attendu**: ✅ JSON valide avec tous les champs

---

### Test 6: Pagination

**Objectif**: Vérifier la pagination des réponses

**Étapes**:
1. Si possible, créer 60+ réponses de test (ou utiliser données existantes)

2. Tester la pagination:
   ```
   GET /api/admin/onboarding/[id]/responses?page=1&limit=20
   GET /api/admin/onboarding/[id]/responses?page=2&limit=20
   GET /api/admin/onboarding/[id]/responses?page=3&limit=20
   ```

3. Vérifier:
   - Chaque page retourne max 20 résultats
   - Pas de doublons entre pages
   - `pagination.hasNext` = true si plus de pages
   - `pagination.hasPrev` = true si page > 1
   - `pagination.total` = nombre total de réponses

**Résultat attendu**: ✅ Pagination fonctionne correctement

---

### Test 7: Filtres

**Objectif**: Vérifier les filtres par statut de completion

**Étapes**:
1. Tester le filtre "completed":
   ```
   GET /api/admin/onboarding/[id]/responses?completed=true
   ```
   - Vérifier que toutes les réponses ont `completed = true`

2. Tester le filtre "non completed":
   ```
   GET /api/admin/onboarding/[id]/responses?completed=false
   ```
   - Vérifier que toutes les réponses ont `completed = false`

3. Tester sans filtre:
   ```
   GET /api/admin/onboarding/[id]/responses
   ```
   - Vérifier que les deux types sont retournés

**Résultat attendu**: ✅ Filtres fonctionnent correctement

---

### Test 8: Security Rules

**Objectif**: Vérifier que les règles Firestore empêchent les accès non autorisés

**Étapes**:
1. Dans Firebase Console > Firestore > Rules Playground

2. Tester lecture avec UID correct:
   ```
   Location: /user_onboarding_responses/USER_UID/responses/CONFIG_VERSION
   Auth: {uid: "USER_UID"}
   Operation: Read
   ```
   - **Résultat attendu**: ✅ Allowed

3. Tester lecture avec UID différent:
   ```
   Location: /user_onboarding_responses/USER_UID/responses/CONFIG_VERSION
   Auth: {uid: "OTHER_UID"}
   Operation: Read
   ```
   - **Résultat attendu**: ❌ Denied

4. Tester écriture sans auth:
   ```
   Location: /user_onboarding_responses/USER_UID/responses/CONFIG_VERSION
   Auth: null
   Operation: Write
   ```
   - **Résultat attendu**: ❌ Denied

5. Tester écriture avec données invalides (missing required fields):
   ```
   Location: /user_onboarding_responses/USER_UID/responses/CONFIG_VERSION
   Auth: {uid: "USER_UID"}
   Operation: Write
   Data: {uid: "USER_UID"} // Missing config_version
   ```
   - **Résultat attendu**: ❌ Denied

**Résultat attendu**: ✅ Rules protègent correctement les données

---

## 📊 Performance Tests

### Test 9: Query Performance

**Objectif**: Vérifier que collectionGroup est plus rapide que scan users

**Méthodologie**:
1. Noter le temps de réponse actuel avec collectionGroup:
   - Ouvrir DevTools > Network
   - GET `/api/admin/onboarding/[id]/responses`
   - Noter le temps dans "Time" column

2. Comparer avec l'ancien système (si logs disponibles)

**Résultat attendu**:
- ✅ < 500ms pour 100 réponses
- ✅ < 2s pour 1000 réponses
- ✅ 10-100x plus rapide que l'ancien scan

---

## 🐛 Edge Cases

### Test 10: Gestion des erreurs

**Scénarios à tester**:

1. **Config inexistant**:
   ```
   GET /api/admin/onboarding/INVALID_ID/responses
   ```
   - **Résultat attendu**: 404 "Onboarding configuration not found"

2. **Aucune réponse**:
   - Créer une nouvelle config sans réponses
   - GET `/api/admin/onboarding/[NEW_CONFIG_ID]/responses`
   - **Résultat attendu**: 200 avec `responses: []`

3. **Export sans réponses**:
   ```
   GET /api/admin/onboarding/[NEW_CONFIG_ID]/export?format=csv
   ```
   - **Résultat attendu**: 404 "No responses found for this configuration"

4. **Format export invalide**:
   ```
   GET /api/admin/onboarding/[id]/export?format=xml
   ```
   - **Résultat attendu**: 400 "Invalid format. Must be csv or json"

---

## ✅ Checklist Final

Avant de merger les PRs:

- [ ] Test 1: Dual write fonctionne (données dans les 2 collections)
- [ ] Test 2: API responses retourne les données
- [ ] Test 3: Analytics calcule correctement
- [ ] Test 4: Export CSV fonctionne
- [ ] Test 5: Export JSON fonctionne
- [ ] Test 6: Pagination fonctionne
- [ ] Test 7: Filtres fonctionnent
- [ ] Test 8: Security rules protègent les données
- [ ] Test 9: Performance acceptable (< 500ms)
- [ ] Test 10: Edge cases gérés correctement

---

## 🚀 Après Validation

1. Merger PR #52 (OraWebApp)
2. Merger le commit Android dans feat/user-onboarding
3. (Optionnel) Créer Cloud Function pour migration de données existantes

---

## 📝 Notes de Test

**Tester avec**:
- Utilisateur: [UID à noter]
- Config Version: [ID à noter]
- Date: [Date du test]

**Résultats**:
- [ ] Tous les tests passent
- [ ] Problèmes identifiés: [Liste]
- [ ] Actions correctives: [Liste]
