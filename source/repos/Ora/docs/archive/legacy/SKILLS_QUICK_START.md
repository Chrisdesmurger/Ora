# 🚀 Skills Quick Start - Test en 5 Minutes

Guide de démarrage rapide pour tester les 4 skills Claude Code haute priorité.

## ✅ Prérequis (2 min)

**Vérifier que tout est prêt**:

```bash
# 1. Git configuré
git --version
# Doit afficher: git version 2.x.x

# 2. GitHub CLI authentifié
gh auth status
# Doit afficher: Logged in to github.com as <username>
# Si non: gh auth login

# 3. Gradle wrapper présent
ls gradlew.bat
# Doit afficher: gradlew.bat

# 4. Dans le bon répertoire
pwd
# Doit afficher: /c/Users/chris/source/repos/Ora
```

**Si tous les checks passent**: ✅ Prêt à tester!

---

## 🧪 Test 1: ora-pre-commit-check (3 min)

**Objectif**: Valider la qualité du code avant commit

### Étape 1: Faire un petit changement

```bash
# Ouvrir un fichier et ajouter un commentaire
echo "// Test comment" >> app/src/main/java/com/ora/wellbeing/MainActivity.kt
```

### Étape 2: Stager le fichier

```bash
git add app/src/main/java/com/ora/wellbeing/MainActivity.kt
```

### Étape 3: Invoquer le skill

**Dans Claude Code, dire**:
```
Run ora-pre-commit-check
```

**Ou**:
```
Use the ora-pre-commit-check skill to validate my changes before committing
```

### Résultat Attendu

```
🔍 Ora Pre-Commit Check Report

✅ Git Status (2s) - 1 file staged
✅ Build Compilation (42s) - PASSED
✅ Unit Tests (89s) - 127/127 passed
✅ Lint Analysis (38s) - 0 errors, 3 warnings
✅ Pattern Compliance (15s) - All compliant
✅ Security Check (2s) - No issues

Total: 3m 8s
Status: ✅ SAFE TO COMMIT
```

### Annuler le changement

```bash
git reset HEAD app/src/main/java/com/ora/wellbeing/MainActivity.kt
git checkout -- app/src/main/java/com/ora/wellbeing/MainActivity.kt
```

---

## 🧪 Test 2: ora-firestore-mapper-generator (2 min)

**Objectif**: Générer un mapper Firestore complet en 2 minutes

### Invoquer le skill

**Dans Claude Code, dire**:
```
Use ora-firestore-mapper-generator to create a mapper for:

Collection: test_items
Fields:
- item_id: String (required)
- title: String (required)
- description: String (optional)
- created_at: Timestamp (required)
Android model: TestItem
```

### Résultat Attendu

Le skill génère 3 fichiers:

**1. TestItemDocument.kt**
```kotlin
package com.ora.wellbeing.data.model.firestore

@IgnoreExtraProperties
class TestItemDocument {
    @get:PropertyName("item_id")
    @set:PropertyName("item_id")
    var itemId: String = ""

    var title: String = ""
    var description: String? = null

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Timestamp? = null

    // ... constructors
}
```

**2. TestItemMapper.kt**
```kotlin
object TestItemMapper {
    fun fromFirestore(id: String, doc: TestItemDocument): TestItem? {
        return try {
            require(doc.itemId.isNotBlank())
            TestItem(/* ... */)
        } catch (e: Exception) {
            Timber.e(e, "Failed to map")
            null
        }
    }
}
```

**3. TestItemMapperTest.kt**
```kotlin
class TestItemMapperTest {
    @Test
    fun `fromFirestore maps all fields correctly`() { /* ... */ }

    @Test
    fun `fromFirestore returns null when required field is empty`() { /* ... */ }

    // ... 15+ tests
}
```

### Vérifier la compilation

```bash
./gradlew.bat compileDebugKotlin
# Doit afficher: BUILD SUCCESSFUL
```

### Nettoyer (optionnel)

```bash
# Supprimer les fichiers de test
rm app/src/main/java/com/ora/wellbeing/data/model/firestore/TestItemDocument.kt
rm app/src/main/java/com/ora/wellbeing/data/mapper/TestItemMapper.kt
rm app/src/test/java/com/ora/wellbeing/data/mapper/TestItemMapperTest.kt
```

---

## 🧪 Test 3: ora-android-build-validator (3 min)

**Objectif**: Valider le build complet avec rapport détaillé

### Invoquer le skill

**Dans Claude Code, dire**:
```
Run ora-android-build-validator to validate the current state of the project
```

### Résultat Attendu

```
🔍 Ora Android Build Validation Report

Date: 2025-11-19 14:35:22
Branch: main
Commit: 7cbd66e

---

## 1. Gradle Compilation ✅

Status: PASSED
Duration: 42s

BUILD SUCCESSFUL in 42s
16 actionable tasks: 16 executed

---

## 2. Unit Tests ✅

Status: PASSED
Total Tests: 127
Passed: 127
Failed: 0
Skipped: 0
Duration: 89s

---

## 3. Lint Analysis ✅

Status: PASSED
Errors: 0
Warnings: 3
Duration: 38s

Warnings:
- Unused import in HomeViewModel.kt:12
- Missing contentDescription in ProfileScreen.kt:45
- TODO comment in JournalRepository.kt:89

---

## 4. Pattern Compliance ✅

Status: PASSED

Firestore Models: ✅ 8/8 compliant
Repository Pattern: ✅ 5/5 compliant
ViewModel Pattern: ✅ 6/6 compliant
Mapper Pattern: ✅ 4/4 compliant

---

## 📊 Summary

Overall Status: ✅ SAFE TO COMMIT

All quality checks passed. Your code is ready to commit and push.

Next Steps:
git commit -m "your message"
git push
```

---

## 🧪 Test 4: ora-pr-workflow (1 min)

**Objectif**: Créer une PR complète en 30 secondes

### ⚠️ Prérequis pour ce test

**NE PAS EXÉCUTER sur la branche main!**

Créer une branche de test d'abord:

```bash
git checkout -b test/skills-demo
echo "// Test PR workflow" >> README.md
git add README.md
```

### Invoquer le skill

**Dans Claude Code, dire**:
```
Use ora-pr-workflow to create a PR for testing skills
```

### Le skill va:

1. **Détecter la branche**: `test/skills-demo`
2. **Analyser les changements**: 1 file modified (README.md)
3. **Générer le commit message**:
   ```
   test(skills): Test PR workflow skill

   Added test comment to README to demonstrate PR workflow automation.

   🤖 Generated with Claude Code
   Co-Authored-By: Claude <noreply@anthropic.com>
   ```
4. **Valider le build**: ✅ PASSED
5. **Pusher**: `origin/test/skills-demo`
6. **Créer la PR**: #XX

### Résultat Attendu

```
✅ Pull Request Created Successfully!

Branch: test/skills-demo
Commits: 1 commit
Files Changed: 1 file (+1, -0)
Build: ✅ Passed

PR URL: https://github.com/Chrisdesmurger/Ora/pull/XX

Next Steps:
- Review changes on GitHub
- Close this test PR (it was just for testing)
```

### Nettoyer

**Sur GitHub**:
1. Aller sur le PR créé
2. Cliquer "Close pull request" (sans merger)
3. Optionnel: Supprimer la branche `test/skills-demo`

**En local**:
```bash
git checkout main
git branch -D test/skills-demo
```

---

## 📊 Résumé des Tests

| Skill | Test Duration | Status |
|-------|---------------|--------|
| ora-pre-commit-check | 3 min | ✅ Testé |
| ora-firestore-mapper-generator | 2 min | ✅ Testé |
| ora-android-build-validator | 3 min | ✅ Testé |
| ora-pr-workflow | 1 min | ✅ Testé |

**Total**: 9 minutes pour tester les 4 skills

---

## 🎯 Prochaines Étapes

Maintenant que tu as testé les skills:

### 1. Utilise-les dans ton workflow quotidien

**Avant chaque commit**:
```
Run ora-pre-commit-check
```

**Quand tu crées un nouveau mapper Firestore**:
```
Use ora-firestore-mapper-generator for <collection>
```

**Avant de créer une PR**:
```
Use ora-pr-workflow to create a PR for <feature>
```

### 2. Configure le git hook (optionnel)

**Créer `.git/hooks/pre-commit`**:
```bash
#!/bin/bash
echo "🔍 Running Ora pre-commit check..."
# Invoke ora-pre-commit-check via Claude Code
# (Implementation depends on Claude Code CLI availability)
exit 0
```

**Rendre exécutable**:
```bash
chmod +x .git/hooks/pre-commit
```

### 3. Adapte les skills à tes besoins

Si tu veux modifier un skill:
1. Éditer le fichier `.claude/skills/ora-{skill-name}.md`
2. Ajuster les règles de validation
3. Tester avec un cas réel

---

## 🐛 Dépannage

### Skill ne fonctionne pas

**Vérifier**:
```bash
# 1. Fichier existe
ls .claude/skills/ora-pre-commit-check.md

# 2. Contenu du fichier est valide
cat .claude/skills/ora-pre-commit-check.md | head -20

# 3. Relancer Claude Code
```

### Build validation échoue

**C'est normal si**:
- Tu as des changements non committés qui cassent le build
- Des tests échouent

**Action**:
1. Lire le rapport d'erreur
2. Corriger les erreurs listées
3. Re-exécuter le skill

### PR workflow échoue

**Vérifier**:
```bash
# 1. GitHub CLI authentifié
gh auth status

# 2. Remote configuré
git remote -v
# Doit afficher: origin https://github.com/Chrisdesmurger/Ora.git

# 3. Branche trackée
git branch -vv
```

---

## 💡 Conseils

1. **Commence petit**: Teste d'abord `ora-pre-commit-check` sur un petit changement
2. **Lis les rapports**: Les skills génèrent des rapports détaillés avec file:line
3. **Combine les skills**: `ora-pre-commit-check` + `ora-pr-workflow` = workflow complet
4. **N'aie pas peur d'expérimenter**: Tous les tests sont non-destructifs (sauf `ora-pr-workflow` qui crée une vraie PR)

---

## ✅ Check Final

Après avoir testé les 4 skills, tu devrais avoir:

- [ ] Compris comment invoquer un skill dans Claude Code
- [ ] Vu un rapport de validation complet
- [ ] Généré un mapper Firestore automatiquement
- [ ] Créé une PR de test (puis fermée)
- [ ] Confiance pour utiliser les skills quotidiennement

**Si tous cochés**: 🎉 Tu es prêt à utiliser les skills en production!

---

## 📚 Documentation Complète

- **Guide complet**: [docs/CLAUDE_CODE_SKILLS_GUIDE.md](docs/CLAUDE_CODE_SKILLS_GUIDE.md)
- **README skills**: [.claude/skills/README.md](.claude/skills/README.md)
- **Skills individuels**: [.claude/skills/](. claude/skills/)

---

**Dernière mise à jour**: 2025-11-19
**Temps total de test**: ~10 minutes
**Statut**: ✅ Prêt à tester
