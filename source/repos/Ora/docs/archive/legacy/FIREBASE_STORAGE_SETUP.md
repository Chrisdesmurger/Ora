# Firebase Storage Setup - Photo Upload Fix

## Problem

Users encounter a **"Permission denied"** error when trying to upload profile photos:

```
StorageException: User does not have permission to access this object.
java.io.IOException: {  "error": {    "code": 403,    "message": "Permission denied."  }}
```

## Solution

Deploy Firebase Storage security rules to allow authenticated users to upload their profile photos.

---

## 📋 Step 1: Deploy Storage Rules

### Option A: Deploy via Firebase CLI (Recommended)

```bash
# Install Firebase CLI if not already installed
npm install -g firebase-tools

# Login to Firebase
firebase login

# Deploy only Storage rules
firebase deploy --only storage

# Or deploy all rules (Firestore + Storage)
firebase deploy --only firestore,storage
```

### Option B: Deploy via Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **Ora**
3. Navigate to **Storage** → **Rules**
4. Copy the contents of `storage.rules` and paste into the editor
5. Click **Publish**

---

## 🔒 Security Rules Explanation

The `storage.rules` file defines the following permissions:

### User Profile Photos (`/users/{userId}/profile.jpg`)

```javascript
match /users/{userId}/profile.jpg {
  // ✅ All authenticated users can READ
  allow read: if request.auth != null;

  // ✅ Only the owner can WRITE
  allow write: if request.auth != null
               && request.auth.uid == userId
               && request.resource.size < 5 * 1024 * 1024  // Max 5MB
               && request.resource.contentType.matches('image/.*');  // Images only
}
```

**Rules**:
- ✅ **Read**: Any authenticated user can view profile photos
- ✅ **Write**: Users can only upload/update their own photo
- ✅ **Size limit**: Maximum 5MB per photo
- ✅ **Type validation**: Only image files allowed

### User Files (`/users/{userId}/**`)

```javascript
match /users/{userId}/{allPaths=**} {
  allow read: if request.auth != null;
  allow write: if request.auth != null
               && request.auth.uid == userId
               && request.resource.size < 10 * 1024 * 1024;  // Max 10MB
}
```

**Rules**:
- ✅ Users can read/write their own files
- ✅ Maximum 10MB per file

---

## 🧪 Testing

After deploying the rules:

1. **Open the Ora app**
2. **Navigate to**: Profile → Settings icon
3. **Tap the profile photo** to upload a new image
4. **Select an image** from gallery
5. **Verify**: Image uploads successfully without "Permission denied" error

---

## 📂 Files Modified

- `storage.rules` - NEW file with security rules
- `firebase.json` - Updated to include storage rules configuration
- `FIREBASE_STORAGE_SETUP.md` - This documentation

---

## 🔗 Related Documentation

- [Firebase Storage Security Rules](https://firebase.google.com/docs/storage/security)
- [ProfileEditViewModel.kt](app/src/main/java/com/ora/wellbeing/presentation/screens/profile/ProfileEditViewModel.kt) - Photo upload implementation (lines 197-253)

---

## ⚠️ Important Notes

1. **Deploy these rules BEFORE testing photo upload** - Otherwise users will continue to get 403 errors
2. **Storage bucket must be enabled** - Make sure Firebase Storage is activated in your Firebase project
3. **Authentication required** - Users must be signed in to upload photos

---

**Last Updated**: 2025-11-08
