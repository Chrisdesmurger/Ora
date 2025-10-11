#!/bin/bash

################################################################################
# deploy-firestore-rules.sh
#
# Script to deploy Firestore security rules to Firebase
#
# Usage:
#   ./scripts/deploy-firestore-rules.sh [environment]
#
# Arguments:
#   environment - (optional) dev|staging|prod (default: dev)
#
# Examples:
#   ./scripts/deploy-firestore-rules.sh           # Deploy to dev
#   ./scripts/deploy-firestore-rules.sh staging   # Deploy to staging
#   ./scripts/deploy-firestore-rules.sh prod      # Deploy to production
#
# Prerequisites:
#   - Firebase CLI installed (npm install -g firebase-tools)
#   - Authenticated with Firebase (firebase login)
#   - firebase.json configured in project root
#
################################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
RULES_FILE="$PROJECT_ROOT/firebase/rules/firestore.rules"

# Environment configuration
ENVIRONMENT="${1:-dev}"
case "$ENVIRONMENT" in
    dev)
        FIREBASE_PROJECT="ora-wellbeing-dev"
        ;;
    staging)
        FIREBASE_PROJECT="ora-wellbeing-staging"
        ;;
    prod)
        FIREBASE_PROJECT="ora-wellbeing-prod"
        ;;
    *)
        echo -e "${RED}Error: Invalid environment '$ENVIRONMENT'${NC}"
        echo "Valid options: dev, staging, prod"
        exit 1
        ;;
esac

################################################################################
# Functions
################################################################################

print_header() {
    echo -e "${BLUE}"
    echo "========================================================================"
    echo "  Firestore Security Rules Deployment"
    echo "========================================================================"
    echo -e "${NC}"
}

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    print_info "Checking prerequisites..."

    # Check if Firebase CLI is installed
    if ! command -v firebase &> /dev/null; then
        print_error "Firebase CLI not found. Install with: npm install -g firebase-tools"
        exit 1
    fi
    print_success "Firebase CLI found: $(firebase --version)"

    # Check if rules file exists
    if [ ! -f "$RULES_FILE" ]; then
        print_error "Rules file not found: $RULES_FILE"
        exit 1
    fi
    print_success "Rules file found: $RULES_FILE"

    # Check if firebase.json exists
    if [ ! -f "$PROJECT_ROOT/firebase.json" ]; then
        print_warning "firebase.json not found. Creating default configuration..."
        create_firebase_json
    fi
    print_success "firebase.json found"
}

create_firebase_json() {
    cat > "$PROJECT_ROOT/firebase.json" <<EOF
{
  "firestore": {
    "rules": "firebase/rules/firestore.rules"
  }
}
EOF
    print_success "Created firebase.json"
}

validate_rules() {
    print_info "Validating rules syntax..."

    # Basic syntax check - Firebase CLI will do full validation on deploy
    if grep -q "rules_version = '2'" "$RULES_FILE"; then
        print_success "Rules version correct"
    else
        print_error "Rules version not found or incorrect"
        exit 1
    fi

    # Check for common mistakes
    if grep -qE "allow\s+(read|write)\s*:\s*if\s+true" "$RULES_FILE"; then
        print_error "Found 'allow ... if true' - this is a security risk!"
        exit 1
    fi

    print_success "Rules validation passed"
}

show_deployment_summary() {
    echo ""
    echo -e "${BLUE}========================================================================"
    echo "  Deployment Summary"
    echo "========================================================================${NC}"
    echo -e "  Environment:      ${YELLOW}$ENVIRONMENT${NC}"
    echo -e "  Firebase Project: ${YELLOW}$FIREBASE_PROJECT${NC}"
    echo -e "  Rules File:       ${YELLOW}$RULES_FILE${NC}"
    echo -e "  File Size:        ${YELLOW}$(wc -l < "$RULES_FILE") lines${NC}"
    echo -e "${BLUE}========================================================================${NC}"
    echo ""
}

confirm_deployment() {
    if [ "$ENVIRONMENT" = "prod" ]; then
        echo -e "${RED}WARNING: You are about to deploy to PRODUCTION!${NC}"
        read -p "Type 'DEPLOY' to confirm: " confirmation
        if [ "$confirmation" != "DEPLOY" ]; then
            print_warning "Deployment cancelled"
            exit 0
        fi
    else
        read -p "Continue with deployment? (y/N): " confirmation
        if [[ ! "$confirmation" =~ ^[Yy]$ ]]; then
            print_warning "Deployment cancelled"
            exit 0
        fi
    fi
}

deploy_rules() {
    print_info "Deploying rules to $FIREBASE_PROJECT..."

    # Deploy using Firebase CLI
    if firebase deploy \
        --only firestore:rules \
        --project "$FIREBASE_PROJECT" \
        --force; then
        print_success "Rules deployed successfully!"
        return 0
    else
        print_error "Deployment failed!"
        return 1
    fi
}

verify_deployment() {
    print_info "Verifying deployment..."

    # Get current rules from Firebase
    if firebase firestore:rules get \
        --project "$FIREBASE_PROJECT" \
        > /tmp/deployed_rules.txt 2>&1; then
        print_success "Rules verified on Firebase"

        # Show rule count
        local deployed_lines=$(wc -l < /tmp/deployed_rules.txt)
        local local_lines=$(wc -l < "$RULES_FILE")
        print_info "Deployed rules: $deployed_lines lines (local: $local_lines lines)"

        rm -f /tmp/deployed_rules.txt
    else
        print_warning "Could not verify deployment (may still be successful)"
    fi
}

run_tests() {
    print_info "Checking for rules tests..."

    local test_file="$PROJECT_ROOT/firebase/rules/firestore.rules.test.js"
    if [ -f "$test_file" ]; then
        print_info "Running rules tests..."
        if npm test; then
            print_success "All tests passed"
        else
            print_error "Tests failed!"
            read -p "Continue deployment anyway? (y/N): " continue_anyway
            if [[ ! "$continue_anyway" =~ ^[Yy]$ ]]; then
                exit 1
            fi
        fi
    else
        print_warning "No test file found at $test_file"
        print_warning "Consider adding tests: https://firebase.google.com/docs/rules/unit-tests"
    fi
}

show_next_steps() {
    echo ""
    echo -e "${GREEN}========================================================================"
    echo "  Deployment Complete!"
    echo "========================================================================${NC}"
    echo ""
    echo "Next steps:"
    echo ""
    echo "  1. Test the rules in your app:"
    echo "     - Try reading/writing data as an authenticated user"
    echo "     - Verify permission denials work correctly"
    echo ""
    echo "  2. Monitor Firebase Console:"
    echo "     - Check for permission errors in Firestore logs"
    echo "     - Review audit logs for suspicious activity"
    echo ""
    echo "  3. Use Rules Playground:"
    echo "     - https://console.firebase.google.com/project/$FIREBASE_PROJECT/firestore/rules"
    echo ""
    echo -e "${GREEN}========================================================================${NC}"
}

################################################################################
# Main Execution
################################################################################

main() {
    print_header

    # Pre-deployment checks
    check_prerequisites
    validate_rules

    # Optional: run tests
    # run_tests

    # Show summary and confirm
    show_deployment_summary
    confirm_deployment

    # Deploy
    if deploy_rules; then
        verify_deployment
        show_next_steps
        exit 0
    else
        print_error "Deployment failed. Check the error messages above."
        exit 1
    fi
}

# Run main function
main
