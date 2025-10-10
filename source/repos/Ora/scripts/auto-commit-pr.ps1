# Auto-commit and PR script for Ora project
# Usage: .\scripts\auto-commit-pr.ps1 -Type "feat|fix|refactor|..." -Scope "auth|profile|..." -Message "Description"

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("feat", "fix", "refactor", "test", "docs", "style", "perf", "chore")]
    [string]$Type,

    [Parameter(Mandatory=$false)]
    [string]$Scope = "",

    [Parameter(Mandatory=$true)]
    [string]$Message,

    [Parameter(Mandatory=$false)]
    [string]$Description = "",

    [Parameter(Mandatory=$false)]
    [string]$IssueNumber = "",

    [Parameter(Mandatory=$false)]
    [switch]$SkipPR = $false,

    [Parameter(Mandatory=$false)]
    [string]$BaseBranch = "main",

    [Parameter(Mandatory=$false)]
    [string]$Remote = "ora"
)

# Colors for output
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

Write-ColorOutput Green "🚀 Ora Git Automation Script"
Write-ColorOutput Green "================================"

# 1. Check git status
Write-ColorOutput Cyan "`n📋 Checking git status..."
$status = git status --porcelain
if ([string]::IsNullOrEmpty($status)) {
    Write-ColorOutput Red "❌ No changes to commit"
    exit 1
}

Write-ColorOutput Yellow "Files to commit:"
git status --short

# 2. Create branch name
$sanitizedMessage = $Message -replace '[^a-zA-Z0-9\s]', '' -replace '\s+', '-'
$sanitizedMessage = $sanitizedMessage.ToLower().Substring(0, [Math]::Min(30, $sanitizedMessage.Length))
$branchName = "$Type/$sanitizedMessage"

Write-ColorOutput Cyan "`n🌿 Creating/switching to branch: $branchName"

# Check if branch exists
$branchExists = git branch --list $branchName
if ([string]::IsNullOrEmpty($branchExists)) {
    git checkout -b $branchName
    Write-ColorOutput Green "✅ Created new branch: $branchName"
} else {
    git checkout $branchName
    Write-ColorOutput Green "✅ Switched to existing branch: $branchName"
}

# 3. Add all changes
Write-ColorOutput Cyan "`n📦 Adding changes..."
git add .

# 4. Build commit message
$commitTitle = if ([string]::IsNullOrEmpty($Scope)) {
    "$Type`: $Message"
} else {
    "$Type($Scope): $Message"
}

$commitBody = @"
$commitTitle

$Description
"@

if (![string]::IsNullOrEmpty($IssueNumber)) {
    $commitBody += "`n`nCloses #$IssueNumber"
}

$commitBody += @"


🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
"@

# 5. Commit
Write-ColorOutput Cyan "`n💾 Creating commit..."
git commit -m $commitBody

if ($LASTEXITCODE -eq 0) {
    Write-ColorOutput Green "✅ Commit created successfully"
} else {
    Write-ColorOutput Red "❌ Commit failed"
    exit 1
}

# 6. Push
Write-ColorOutput Cyan "`n📤 Pushing to remote '$Remote'..."
git push $Remote $branchName -u

if ($LASTEXITCODE -eq 0) {
    Write-ColorOutput Green "✅ Pushed successfully"
} else {
    Write-ColorOutput Red "❌ Push failed"
    exit 1
}

# 7. Create PR (if not skipped)
if (!$SkipPR) {
    Write-ColorOutput Cyan "`n📝 Creating Pull Request..."

    # Build PR title
    $prTitle = if ([string]::IsNullOrEmpty($Scope)) {
        "$Type`: $Message"
    } else {
        "$Type($Scope): $Message"
    }

    # Build PR body
    $prType = switch ($Type) {
        "feat" { "- [x] 🚀 Nouvelle fonctionnalité (feat)" }
        "fix" { "- [x] 🐛 Correction de bug (fix)" }
        "refactor" { "- [x] 🔧 Refactoring (refactor)" }
        "test" { "- [x] ✅ Tests (test)" }
        "docs" { "- [x] 📚 Documentation (docs)" }
        "style" { "- [x] 🎨 Style/UI (style)" }
        "perf" { "- [x] ⚡ Performance (perf)" }
        "chore" { "- [x] 🛠️ Maintenance (chore)" }
        default { "- [x] Changement" }
    }

    $prBody = @"
## 📝 Description
$Message

$Description

## 🎯 Type de changement
$prType

## 📋 Changements principaux
$(git diff --name-only $BaseBranch...$branchName | ForEach-Object { "- $_" } | Out-String)

## 🧪 Tests effectués
- [ ] Build réussi (``./gradlew build``)
- [ ] Tests unitaires passent
- [ ] Testé manuellement sur émulateur/appareil
- [ ] Testé avec Firebase (si applicable)

---
🤖 Generated with [Claude Code](https://claude.com/claude-code)
"@

    if (![string]::IsNullOrEmpty($IssueNumber)) {
        $prBody += "`n`n## 🔗 Issues liées`nCloses #$IssueNumber"
    }

    gh pr create --title $prTitle --body $prBody --base $BaseBranch

    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput Green "`n✅ Pull Request created successfully!"
        Write-ColorOutput Yellow "`nView PR: $(gh pr view --web)"
    } else {
        Write-ColorOutput Red "❌ PR creation failed"
        exit 1
    }
} else {
    Write-ColorOutput Yellow "`n⏭️ Skipped PR creation (use -SkipPR `$false to create PR)"
}

Write-ColorOutput Green "`n🎉 Git automation completed successfully!"
Write-ColorOutput Cyan "Branch: $branchName"
Write-ColorOutput Cyan "Remote: $Remote"
