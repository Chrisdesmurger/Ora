# Git Automation pour Ora

Ce guide explique comment utiliser l'automatisation Git intégrée dans le projet Ora avec Claude Code.

## 🎯 Objectif

Automatiser le workflow Git complet après chaque implémentation de fonctionnalité ou correction de bug :
1. ✅ Création de commit avec message conventionnel
2. ✅ Création automatique de branche feature/fix
3. ✅ Push vers le dépôt distant
4. ✅ Création de Pull Request avec template complet

## 🤖 Agent Git Automation

Un agent Claude Code spécialisé (`git-automation`) a été créé pour gérer automatiquement le workflow Git.

### Quand l'agent est-il déclenché ?

L'agent s'active automatiquement après :
- ✅ Implémentation complète d'une fonctionnalité
- ✅ Correction d'un bug avec tests passants
- ✅ Refactoring significatif terminé
- ✅ Ajout de tests
- ✅ Mise à jour de documentation importante

### Comment l'utiliser avec Claude ?

Après avoir terminé une tâche, demandez simplement à Claude :

```
"Peux-tu créer un commit et une PR pour cette fonctionnalité ?"
"Automatise le commit et push pour ce fix"
"Crée une PR pour les changements que je viens de faire"
```

Claude utilisera automatiquement l'agent `git-automation` pour :
1. Analyser les changements
2. Détecter le type de changement (feat, fix, etc.)
3. Créer une branche appropriée
4. Générer un message de commit conventionnel
5. Pousser les changements
6. Créer une Pull Request avec description complète

## 📝 Convention de commits (Conventional Commits)

### Format

```
type(scope): description courte

Description détaillée optionnelle expliquant:
- Pourquoi le changement a été fait
- Quel était le problème
- Comment il a été résolu

Closes #issue-number (si applicable)

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

### Types disponibles

| Type | Emoji | Description | Exemple |
|------|-------|-------------|---------|
| `feat` | 🚀 | Nouvelle fonctionnalité | `feat(auth): add Google Sign-In` |
| `fix` | 🐛 | Correction de bug | `fix(profile): resolve avatar upload crash` |
| `refactor` | 🔧 | Refactoring sans changement de fonctionnalité | `refactor(home): simplify ViewModel logic` |
| `test` | ✅ | Ajout ou modification de tests | `test(auth): add unit tests for AuthViewModel` |
| `docs` | 📚 | Documentation seulement | `docs(readme): update installation steps` |
| `style` | 🎨 | Formatage, UI (pas de logique) | `style(theme): update primary color palette` |
| `perf` | ⚡ | Amélioration de performance | `perf(library): lazy load content images` |
| `chore` | 🛠️ | Maintenance (deps, config) | `chore(deps): update Firebase to 33.8.0` |

### Scopes pour Ora

| Scope | Description |
|-------|-------------|
| `auth` | Authentification (login, signup, Google Sign-In) |
| `profile` | Profil utilisateur |
| `home` | Écran d'accueil |
| `library` | Bibliothèque de contenu |
| `journal` | Journal de gratitude |
| `programs` | Programmes et challenges |
| `firestore` | Base de données Firestore |
| `ui` | Interface utilisateur générale |
| `theme` | Thème et design system |
| `di` | Injection de dépendances (Hilt) |
| `build` | Configuration de build (Gradle) |
| `navigation` | Navigation entre écrans |

## 🛠️ Script PowerShell Helper

Un script PowerShell est disponible pour automatisation manuelle :

### Utilisation basique

```powershell
# Nouvelle fonctionnalité
.\scripts\auto-commit-pr.ps1 `
  -Type "feat" `
  -Scope "profile" `
  -Message "Add profile editing functionality"

# Correction de bug
.\scripts\auto-commit-pr.ps1 `
  -Type "fix" `
  -Scope "auth" `
  -Message "Fix Google Sign-In crash on Android 14"

# Avec description détaillée
.\scripts\auto-commit-pr.ps1 `
  -Type "feat" `
  -Scope "journal" `
  -Message "Add daily gratitude reminders" `
  -Description "Implemented WorkManager for evening reminders at 8 PM. Users can customize time in settings."

# Avec référence à issue
.\scripts\auto-commit-pr.ps1 `
  -Type "fix" `
  -Scope "firestore" `
  -Message "Fix user stats sync issue" `
  -IssueNumber "42"

# Sans créer de PR (juste commit + push)
.\scripts\auto-commit-pr.ps1 `
  -Type "chore" `
  -Scope "deps" `
  -Message "Update dependencies" `
  -SkipPR
```

### Paramètres

| Paramètre | Type | Requis | Description |
|-----------|------|--------|-------------|
| `-Type` | String | ✅ | Type de changement (feat, fix, etc.) |
| `-Scope` | String | ❌ | Portée du changement (auth, profile, etc.) |
| `-Message` | String | ✅ | Description courte du changement |
| `-Description` | String | ❌ | Description détaillée |
| `-IssueNumber` | String | ❌ | Numéro d'issue GitHub à fermer |
| `-SkipPR` | Switch | ❌ | Ne pas créer de PR |
| `-BaseBranch` | String | ❌ | Branche de base (défaut: main) |
| `-Remote` | String | ❌ | Remote Git (défaut: ora) |

## 📋 Template de Pull Request

Chaque PR créée automatiquement inclut :

```markdown
## 📝 Description
[Description du changement]

## 🎯 Type de changement
- [x] 🚀 Nouvelle fonctionnalité (feat)
- [ ] 🐛 Correction de bug (fix)
- [ ] 🔧 Refactoring (refactor)
- [ ] 📚 Documentation (docs)
- [ ] ✅ Tests (test)
- [ ] 🎨 Style/UI (style)
- [ ] ⚡ Performance (perf)

## 📋 Changements principaux
- Fichier 1
- Fichier 2
- Fichier 3

## 🧪 Tests effectués
- [ ] Build réussi (`./gradlew build`)
- [ ] Tests unitaires passent
- [ ] Testé manuellement sur émulateur/appareil
- [ ] Testé avec Firebase (si applicable)

## 🔗 Issues liées
Closes #issue-number

---
🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

## 🔒 Règles de sécurité

L'automatisation respecte ces règles :

✅ **Vérifie .gitignore** avant chaque commit
✅ **Bloque** les commits contenant des secrets (google-services.json, .env, clés API)
✅ **Demande confirmation** si > 50 fichiers modifiés
✅ **Teste le build** avant de créer la PR (si configuré)

## 📚 Exemples de workflow complet

### Exemple 1 : Nouvelle fonctionnalité

```bash
# 1. Développer la fonctionnalité
# ... code, code, code ...

# 2. Tester
./gradlew test
./gradlew build

# 3. Demander à Claude
"Claude, crée un commit et une PR pour l'ajout du player audio"

# Claude va :
# - Analyser les changements
# - Créer la branche feat/audio-player
# - Commit avec message conventionnel
# - Push vers ora
# - Créer PR avec template complet
```

### Exemple 2 : Correction de bug

```bash
# 1. Reproduire le bug
# 2. Corriger le bug
# 3. Tester la correction

# 4. Utiliser le script
.\scripts\auto-commit-pr.ps1 `
  -Type "fix" `
  -Scope "library" `
  -Message "Fix content filtering crash" `
  -Description "Fixed NPE when filtering with empty category. Added null checks and unit tests." `
  -IssueNumber "23"

# Résultat :
# ✅ Branche: fix/content-filtering-crash
# ✅ Commit avec message détaillé
# ✅ Push vers ora
# ✅ PR créée avec référence à #23
```

### Exemple 3 : Refactoring

```bash
# Demander à Claude après refactoring
"Peux-tu créer un commit pour le refactoring du ViewModel ?"

# Claude analysera et créera :
# - Type: refactor
# - Scope: (détecté automatiquement selon les fichiers)
# - Message: basé sur l'analyse des changements
# - PR avec liste des changements
```

## 🎓 Best Practices

1. **Commits atomiques** : Un commit = Une fonctionnalité/fix
2. **Messages clairs** : Décrivez le "pourquoi", pas le "quoi"
3. **Tests avant commit** : Assurez-vous que le build passe
4. **Branches courtes** : Gardez les features branches petites
5. **PR descriptives** : Utilisez le template pour bien documenter
6. **Revue de code** : Demandez une review avant merge

## 🔧 Configuration

### Permissions Git (déjà configurées)

Les permissions suivantes sont activées dans `.claude/settings.local.json` :

```json
{
  "permissions": {
    "allow": [
      "Bash(git status:*)",
      "Bash(git diff:*)",
      "Bash(git log:*)",
      "Bash(git checkout:*)",
      "Bash(git branch:*)",
      "Bash(git commit:*)",
      "Bash(git push:*)",
      "Bash(gh pr:*)",
      "Bash(gh repo:*)"
    ]
  }
}
```

### Remote configuré

```bash
# Vérifier les remotes
git remote -v

# Devrait afficher :
# ora     https://github.com/Chrisdesmurger/Ora.git (fetch)
# ora     https://github.com/Chrisdesmurger/Ora.git (push)
```

## ❓ FAQ

**Q: L'automatisation fonctionne-t-elle sans ma confirmation ?**
R: Non, Claude demande toujours confirmation avant de pousser des changements ou créer une PR.

**Q: Puis-je personnaliser le message de commit ?**
R: Oui, en utilisant le script PowerShell avec des paramètres personnalisés, ou en donnant des instructions spécifiques à Claude.

**Q: Que se passe-t-il si j'ai des conflits ?**
R: L'automatisation détecte les conflits et vous demande de les résoudre manuellement avant de continuer.

**Q: Puis-je désactiver l'automatisation ?**
R: Oui, il suffit de ne pas demander à Claude de créer des commits/PR automatiquement. Vous pouvez toujours faire vos commits manuellement.

**Q: L'agent peut-il faire des commits partiels (staging partiel) ?**
R: Non, l'automatisation actuelle fait `git add .` (tous les fichiers). Pour un staging partiel, utilisez Git manuellement.

## 📞 Support

Pour toute question ou problème :
1. Consultez les logs de Claude Code
2. Vérifiez les permissions dans `.claude/settings.local.json`
3. Testez le script PowerShell manuellement
4. Consultez l'agent dans `.claude/agents/55_git-automation.md`

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
