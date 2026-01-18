# Firestore Security Rules - Documentation

**Project**: Ora Wellbeing Android App
**Last Updated**: 2025-10-04
**Rules Version**: 2
**File**: `firebase/rules/firestore.rules`

## Table of Contents

1. [Overview](#overview)
2. [Security Principles](#security-principles)
3. [Collection Structure](#collection-structure)
4. [Rule Explanations](#rule-explanations)
5. [Common Pitfalls](#common-pitfalls)
6. [Testing Rules](#testing-rules)
7. [Deployment Procedure](#deployment-procedure)
8. [Troubleshooting](#troubleshooting)

---

## Overview

The Firestore security rules for Ora implement **strict user data isolation**. Every user can only access their own data, with comprehensive validation to prevent data manipulation and ensure data integrity.

### Key Features
- ✅ UID-based access control for all user data
- ✅ Strict type and boundary validation
- ✅ Prevention of cross-user data access
- ✅ Read-only public content catalog
- ✅ Subcollection support for journal and programs
- ✅ Protection against timestamp manipulation
- ✅ Explicit deny for undefined paths

---

## Security Principles

### 1. **Authentication Required**
All operations require `request.auth != null` - unauthenticated users have NO access.

### 2. **UID Matching**
Users can only access documents where the document ID matches their UID:
```javascript
request.auth.uid == uid
```

### 3. **No Cross-User Access**
There is NO way for User A to read or write User B's data. Each user is completely isolated.

### 4. **Strict Validation**
All writes are validated for:
- Required fields presence
- Correct data types
- Reasonable boundaries (e.g., totalMinutes <= 525600)
- Preventing timestamp manipulation
- List size limits

### 5. **Immutable Fields**
Certain fields cannot be modified after creation:
- `createdAt` timestamps
- `enrolledAt` dates
- User email (use update with caution)

---

## Collection Structure

```
firestore/
├── users/{uid}                           # User profiles (read/write by owner)
│   ├── journal_entries/{entryId}         # Journal subcollection
│   └── programs/{programId}              # Enrolled programs subcollection
├── stats/{uid}                           # User statistics (read/write by owner)
├── content/{contentId}                   # Public content (read-only)
└── programs/{programId}                  # Public programs (read-only)
```

### User Document (`users/{uid}`)
```json
{
  "email": "user@example.com",
  "displayName": "John Doe",
  "photoUrl": "https://...",
  "createdAt": "2025-10-04T10:00:00Z"
}
```

### Stats Document (`stats/{uid}`)
```json
{
  "totalMinutes": 1250,
  "sessions": 45,
  "streakDays": 12,
  "lastSessionDate": "2025-10-04T10:00:00Z"
}
```

### Journal Entry (`users/{uid}/journal_entries/{entryId}`)
```json
{
  "date": "2025-10-04T10:00:00Z",
  "gratitudes": ["Family time", "Good health", "Beautiful weather"],
  "mood": "happy",
  "reflection": "Today was a great day...",
  "createdAt": "2025-10-04T10:00:00Z"
}
```

### Program Enrollment (`users/{uid}/programs/{programId}`)
```json
{
  "enrolledAt": "2025-10-01T10:00:00Z",
  "progress": 65,
  "completedAt": null
}
```

---

## Rule Explanations

### 1. Users Collection

```javascript
match /users/{uid} {
  allow read: if request.auth != null && request.auth.uid == uid;
  allow create: if request.auth != null
                && request.auth.uid == uid
                && validateUserCreate(request.resource.data);
  allow update: if request.auth != null
                && request.auth.uid == uid
                && validateUserUpdate(request.resource.data);
  allow delete: if false; // Prevent deletion
}
```

**Why separate create/update?**
- `create`: Enforces required fields (email, createdAt)
- `update`: Prevents modification of immutable fields (createdAt)
- `delete`: Disabled to prevent accidental profile deletion (use soft-delete flag instead)

### 2. Stats Collection

```javascript
allow update: if request.auth != null
              && request.auth.uid == uid
              && validateStatsUpdate(request.resource.data, resource.data);
```

**Key Validation Logic:**
```javascript
function validateStatsUpdate(newData, existingData) {
  return newData.totalMinutes >= existingData.totalMinutes
      || newData.totalMinutes == 0  // Allow reset
      && newData.sessions >= existingData.sessions
      || newData.sessions == 0;      // Allow reset
}
```

**Protection Against:**
- ❌ Decreasing stats (except explicit reset to 0)
- ❌ Negative values
- ❌ Unrealistic values (e.g., 1 million minutes)

### 3. Journal Entries (Subcollection)

```javascript
match /users/{uid}/journal_entries/{entryId} {
  allow read: if request.auth != null && request.auth.uid == uid;
  // ...
}
```

**Why Subcollection?**
- Automatic inheritance of parent UID security
- Logical organization under user document
- Easy to query all entries for a user

### 4. Content Catalog (Read-Only)

```javascript
match /content/{contentId} {
  allow read: if request.auth != null;
  allow write: if false; // Admin-only via Console/Functions
}
```

**Purpose:**
- Public content accessible to all authenticated users
- Prevents user modification
- Content managed by admins through Firebase Console or Cloud Functions

---

## Common Pitfalls

### ❌ Pitfall 1: Cross-User Writes
**Problem:**
```kotlin
// WRONG: Trying to update another user's stats
firestore.collection("stats")
    .document(otherUserId)  // Different from auth.currentUser.uid
    .update("totalMinutes", 100)
```

**Solution:**
```kotlin
// CORRECT: Always use current user's UID
val currentUserId = auth.currentUser?.uid ?: return
firestore.collection("stats")
    .document(currentUserId)
    .update("totalMinutes", 100)
```

### ❌ Pitfall 2: Missing Required Fields
**Problem:**
```kotlin
// WRONG: Missing 'createdAt' on user creation
firestore.collection("users")
    .document(uid)
    .set(mapOf("email" to email))  // Missing createdAt!
```

**Solution:**
```kotlin
// CORRECT: Include all required fields
firestore.collection("users")
    .document(uid)
    .set(mapOf(
        "email" to email,
        "createdAt" to FieldValue.serverTimestamp()
    ))
```

### ❌ Pitfall 3: Invalid Data Types
**Problem:**
```kotlin
// WRONG: totalMinutes as String
.update("totalMinutes", "150")  // String instead of Int
```

**Solution:**
```kotlin
// CORRECT: Use proper types
.update("totalMinutes", 150)  // Int
```

### ❌ Pitfall 4: Exceeding List Limits
**Problem:**
```kotlin
// WRONG: Too many gratitudes
val gratitudes = (1..15).map { "Gratitude $it" }  // Max is 10!
```

**Solution:**
```kotlin
// CORRECT: Enforce limits in app
val gratitudes = userInput.take(10)
```

### ❌ Pitfall 5: Modifying Immutable Fields
**Problem:**
```kotlin
// WRONG: Trying to change createdAt
.update("createdAt", newTimestamp)  // DENIED by rules!
```

**Solution:**
```kotlin
// CORRECT: Only update mutable fields
.update("displayName", newName)
```

---

## Testing Rules

### Local Testing with Firebase Emulator

#### 1. Setup Emulator
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login to Firebase
firebase login

# Initialize emulators (first time only)
firebase init emulators
# Select: Firestore, Authentication

# Start emulators
firebase emulators:start
```

#### 2. Configure Android App for Emulator
```kotlin
// In your Firebase initialization (OraApplication.kt or similar)
if (BuildConfig.DEBUG) {
    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.auth.useEmulator("10.0.2.2", 9099)
}
```

#### 3. Write Unit Tests for Rules
Create `firestore.rules.test.js`:
```javascript
const firebase = require('@firebase/rules-unit-testing');
const fs = require('fs');

describe('Firestore Security Rules', () => {
  let testEnv;

  beforeAll(async () => {
    testEnv = await firebase.initializeTestEnvironment({
      projectId: 'ora-wellbeing-test',
      firestore: {
        rules: fs.readFileSync('firebase/rules/firestore.rules', 'utf8'),
      },
    });
  });

  test('User can read own profile', async () => {
    const context = testEnv.authenticatedContext('user123');
    await firebase.assertSucceeds(
      context.firestore().collection('users').doc('user123').get()
    );
  });

  test('User cannot read other user profile', async () => {
    const context = testEnv.authenticatedContext('user123');
    await firebase.assertFails(
      context.firestore().collection('users').doc('user456').get()
    );
  });

  test('Stats validation prevents negative values', async () => {
    const context = testEnv.authenticatedContext('user123');
    await firebase.assertFails(
      context.firestore().collection('stats').doc('user123').set({
        totalMinutes: -100,  // Invalid!
        sessions: 5,
        streakDays: 2
      })
    );
  });

  afterAll(async () => {
    await testEnv.cleanup();
  });
});
```

Run tests:
```bash
npm test
```

### Testing Checklist

- [ ] User can read own data
- [ ] User CANNOT read other user's data
- [ ] User can create valid documents
- [ ] Invalid data types are rejected
- [ ] Boundary values are enforced (min/max)
- [ ] Immutable fields cannot be modified
- [ ] Unauthenticated access is denied
- [ ] Public content is readable by all authenticated users
- [ ] Public content cannot be written by users

---

## Deployment Procedure

### Manual Deployment (Firebase Console)

1. **Navigate to Firebase Console**
   - Go to: https://console.firebase.google.com
   - Select your project: "Ora Wellbeing"

2. **Open Firestore Rules**
   - Click "Firestore Database" in left menu
   - Click "Rules" tab

3. **Copy and Paste Rules**
   - Copy content from `firebase/rules/firestore.rules`
   - Paste into the editor
   - Click "Publish"

4. **Verify Deployment**
   - Check for syntax errors
   - Review "Rules Playground" for quick tests

### Automated Deployment (CLI)

#### 1. Setup `firebase.json`
Create in project root:
```json
{
  "firestore": {
    "rules": "firebase/rules/firestore.rules"
  }
}
```

#### 2. Deploy Command
```bash
# Deploy only Firestore rules
firebase deploy --only firestore:rules

# Deploy with confirmation
firebase deploy --only firestore:rules --project ora-wellbeing-prod
```

#### 3. CI/CD Integration (GitHub Actions)

Create `.github/workflows/deploy-firestore-rules.yml`:
```yaml
name: Deploy Firestore Rules

on:
  push:
    branches: [main]
    paths:
      - 'firebase/rules/firestore.rules'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Install Firebase CLI
        run: npm install -g firebase-tools

      - name: Deploy Rules
        run: firebase deploy --only firestore:rules --token ${{ secrets.FIREBASE_TOKEN }}
        env:
          FIREBASE_TOKEN: ${{ secrets.FIREBASE_TOKEN }}
```

Generate token:
```bash
firebase login:ci
# Copy token to GitHub Secrets as FIREBASE_TOKEN
```

---

## Troubleshooting

### Error: "Missing or insufficient permissions"

**Cause:** User trying to access data they don't own.

**Solution:**
1. Verify `auth.currentUser.uid` matches document ID
2. Check user is authenticated
3. Review Firestore rules for correct UID matching

**Debug:**
```kotlin
val currentUserId = auth.currentUser?.uid
Log.d("Firestore", "Current UID: $currentUserId")
Log.d("Firestore", "Accessing document: $documentId")
// Ensure they match!
```

### Error: "Invalid argument" or "Type mismatch"

**Cause:** Data type doesn't match rules validation.

**Solution:**
1. Ensure integers are Int, not String
2. Timestamps are FieldValue.serverTimestamp()
3. Lists have correct size limits

**Debug:**
```kotlin
val data = hashMapOf(
    "totalMinutes" to minutes.toInt(),  // Explicit type
    "sessions" to sessions.toInt(),
    "createdAt" to FieldValue.serverTimestamp()
)
Log.d("Firestore", "Writing data: $data")
```

### Error: "Value exceeds maximum"

**Cause:** Boundary validation failed (e.g., totalMinutes > 525600).

**Solution:**
1. Check maximum values in rules
2. Validate data client-side before write
3. Review validation functions in rules

### Rules Not Updating

**Cause:** Browser cache or deployment delay.

**Solution:**
1. Hard refresh Firebase Console (Ctrl+Shift+R)
2. Wait 1-2 minutes for propagation
3. Verify deployment with CLI: `firebase firestore:rules get`

### Emulator Connection Issues (Android)

**Cause:** Incorrect emulator host.

**Solution:**
```kotlin
// For Android Emulator: use 10.0.2.2
Firebase.firestore.useEmulator("10.0.2.2", 8080)

// For physical device: use computer's IP
Firebase.firestore.useEmulator("192.168.1.100", 8080)
```

---

## Best Practices

### 1. **Always Test Locally First**
- Use Firebase Emulator for development
- Test all CRUD operations before deploying

### 2. **Version Control Rules**
- Keep rules in Git (`firebase/rules/firestore.rules`)
- Document changes in commit messages
- Use PR reviews for rule changes

### 3. **Monitor Rule Violations**
- Enable Firestore audit logs in Firebase Console
- Set up alerts for repeated permission denials
- Review logs weekly

### 4. **Client-Side Validation**
- Validate data in app BEFORE writing to Firestore
- Show user-friendly error messages
- Don't rely solely on server-side rules for UX

### 5. **Graceful Error Handling**
```kotlin
try {
    firestore.collection("users")
        .document(uid)
        .set(userData)
        .await()
} catch (e: FirebaseFirestoreException) {
    when (e.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            Log.e("Firestore", "Permission denied: ${e.message}")
        FirebaseFirestoreException.Code.INVALID_ARGUMENT ->
            Log.e("Firestore", "Invalid data: ${e.message}")
        else ->
            Log.e("Firestore", "Error: ${e.message}")
    }
}
```

---

## Quick Reference

### Common Rule Patterns

**Read own document:**
```javascript
allow read: if request.auth != null && request.auth.uid == uid;
```

**Write with validation:**
```javascript
allow write: if request.auth != null
             && request.auth.uid == uid
             && validateData(request.resource.data);
```

**Read-only public data:**
```javascript
allow read: if request.auth != null;
allow write: if false;
```

**Subcollection inheritance:**
```javascript
match /users/{uid}/items/{itemId} {
  allow read: if request.auth != null && request.auth.uid == uid;
}
```

---

## Additional Resources

- **Firebase Documentation**: https://firebase.google.com/docs/firestore/security/get-started
- **Rules Playground**: https://console.firebase.google.com → Firestore → Rules → Playground
- **Emulator Suite**: https://firebase.google.com/docs/emulator-suite
- **Rules Unit Testing**: https://firebase.google.com/docs/rules/unit-tests

---

## Change Log

### 2025-10-04 - Initial Rules
- Implemented UID-based access control for users and stats
- Added subcollections for journal and programs
- Created validation functions for all data types
- Set up read-only content catalog
- Added comprehensive boundary checks
- Documented common pitfalls and testing procedures

---

**IMPORTANT SECURITY NOTES:**

1. ⚠️ **Never disable authentication checks** - All rules require `request.auth != null`
2. ⚠️ **Never use wildcard write permissions** - Always validate UID matching
3. ⚠️ **Test in emulator first** - Never deploy untested rules to production
4. ⚠️ **Monitor audit logs** - Set up alerts for suspicious activity
5. ⚠️ **Document all changes** - Update this file when modifying rules

---

*For questions or issues, contact the DevOps team or refer to the Firebase Security Rules documentation.*
