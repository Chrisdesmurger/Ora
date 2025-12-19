#!/bin/bash
# Hook: pre-commit-i18n-check
# Vérifie automatiquement que tout le code est internationalisé (FR/EN/ES)
# Bloque le commit si des strings hardcodées sont détectées

echo "🌍 [i18n CHECK] Vérification de l'internationalisation..."

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Compteurs
VIOLATIONS=0
WARNINGS=0

# Fonction pour détecter les strings hardcodées
detect_hardcoded_strings() {
    local pattern="$1"
    local description="$2"
    local files=$(find app/src/main/java -name "*.kt" -type f)

    for file in $files; do
        # Ignorer les fichiers de test et les logs Timber
        if [[ $file == *"/test/"* ]] || [[ $file == *"Timber"* ]]; then
            continue
        fi

        # Rechercher le pattern
        matches=$(grep -n "$pattern" "$file" 2>/dev/null || true)

        if [ ! -z "$matches" ]; then
            echo -e "${RED}❌ VIOLATION:${NC} $description dans $file"
            echo "$matches"
            ((VIOLATIONS++))
        fi
    done
}

# Fonction pour vérifier les ressources manquantes
check_missing_translations() {
    local strings_fr="app/src/main/res/values/strings.xml"
    local strings_en="app/src/main/res/values-en/strings.xml"
    local strings_es="app/src/main/res/values-es/strings.xml"

    if [ ! -f "$strings_fr" ] || [ ! -f "$strings_en" ] || [ ! -f "$strings_es" ]; then
        echo -e "${RED}❌ ERREUR: Fichiers de ressources manquants${NC}"
        ((VIOLATIONS++))
        return
    fi

    # Extraire toutes les clés de chaque fichier
    keys_fr=$(grep -oP 'name="\K[^"]+' "$strings_fr" | sort)
    keys_en=$(grep -oP 'name="\K[^"]+' "$strings_en" | sort)
    keys_es=$(grep -oP 'name="\K[^"]+' "$strings_es" | sort)

    # Compter les clés
    count_fr=$(echo "$keys_fr" | wc -l)
    count_en=$(echo "$keys_en" | wc -l)
    count_es=$(echo "$keys_es" | wc -l)

    echo "📊 Nombre de clés par langue:"
    echo "   FR: $count_fr"
    echo "   EN: $count_en"
    echo "   ES: $count_es"

    # Vérifier les clés manquantes
    missing_en=$(comm -23 <(echo "$keys_fr") <(echo "$keys_en"))
    missing_es=$(comm -23 <(echo "$keys_fr") <(echo "$keys_es"))

    if [ ! -z "$missing_en" ]; then
        echo -e "${RED}❌ Traductions EN manquantes:${NC}"
        echo "$missing_en"
        ((VIOLATIONS++))
    fi

    if [ ! -z "$missing_es" ]; then
        echo -e "${RED}❌ Traductions ES manquantes:${NC}"
        echo "$missing_es"
        ((VIOLATIONS++))
    fi
}

# Pattern 1: Text("...") avec strings hardcodées
echo "🔍 Recherche de Text(\"...\") hardcodés..."
detect_hardcoded_strings 'Text\s*\(\s*"[^"]*[A-ZÀ-ÿ][a-zà-ÿéèêëàâäôö]+' "Text composable avec string hardcodée"

# Pattern 2: error = "..."
echo "🔍 Recherche de messages d'erreur hardcodés..."
detect_hardcoded_strings 'error\s*=\s*"[^"]*[A-ZÀ-ÿ]' "Message d'erreur hardcodé"

# Pattern 3: title = "..."
echo "🔍 Recherche de titres hardcodés..."
detect_hardcoded_strings 'title\s*=\s*"[^"]*[A-ZÀ-ÿ]' "Titre hardcodé"

# Pattern 4: label = "..."
echo "🔍 Recherche de labels hardcodés..."
detect_hardcoded_strings 'label\s*=\s*"[^"]*[A-ZÀ-ÿ]' "Label hardcodé"

# Pattern 5: message = "..."
echo "🔍 Recherche de messages hardcodés..."
detect_hardcoded_strings 'message\s*=\s*"[^"]*[A-ZÀ-ÿ]' "Message hardcodé"

# Pattern 6: Enum avec val name: String
echo "🔍 Recherche d'enums avec strings hardcodés..."
detect_hardcoded_strings 'enum\s+class\s+\w+\s*\([^)]*val\s+\w+:\s*String' "Enum avec String au lieu de @StringRes"

# Vérifier les traductions manquantes
echo "🔍 Vérification des traductions FR/EN/ES..."
check_missing_translations

# Vérifier les imports manquants
echo "🔍 Vérification des imports stringResource()..."
kt_files_with_text=$(find app/src/main/java -name "*.kt" -type f -exec grep -l "Text(" {} \; 2>/dev/null || true)

for file in $kt_files_with_text; do
    if ! grep -q "import androidx.compose.ui.res.stringResource" "$file"; then
        # Vérifier s'il y a des Text() qui utilisent des strings
        if grep -q 'Text\s*(\s*"' "$file"; then
            echo -e "${YELLOW}⚠️  WARNING:${NC} Fichier utilise Text() mais n'importe pas stringResource: $file"
            ((WARNINGS++))
        fi
    fi
done

# Résumé
echo ""
echo "========================================="
if [ $VIOLATIONS -eq 0 ]; then
    echo -e "${GREEN}✅ i18n CHECK PASSED${NC}"
    echo "   Aucune violation détectée"
    if [ $WARNINGS -gt 0 ]; then
        echo -e "${YELLOW}   $WARNINGS avertissement(s)${NC}"
    fi
    exit 0
else
    echo -e "${RED}❌ i18n CHECK FAILED${NC}"
    echo "   $VIOLATIONS violation(s) détectée(s)"
    echo ""
    echo "🔧 ACTIONS REQUISES:"
    echo "   1. Extraire toutes les strings hardcodées vers res/values/strings.xml"
    echo "   2. Ajouter les traductions EN et ES"
    echo "   3. Utiliser stringResource() dans les composables"
    echo "   4. Utiliser getString() dans les ViewModels"
    echo ""
    echo "💡 AIDE:"
    echo "   Consultez CLAUDE.md section 'Internationalization (i18n)'"
    echo "   Ou lancez: claude-code --agent i18n-l10n-android"
    echo ""

    # Proposer de lancer l'agent i18n automatiquement
    read -p "Voulez-vous lancer l'agent i18n pour corriger automatiquement? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🤖 Lancement de l'agent i18n-l10n-android..."
        echo "   (Cette fonctionnalité nécessite Claude Code CLI)"
        # TODO: Intégrer avec Claude Code CLI quand disponible
        exit 1
    else
        exit 1
    fi
fi
