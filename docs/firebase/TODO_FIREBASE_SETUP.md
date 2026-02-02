# Firebase Setup TODO Checklist

Copy this checklist and check off each item as you complete it.

## 🔥 Firebase Console Setup

### Create Project
- [ ] Go to https://console.firebase.google.com/
- [ ] Click "Add project"
- [ ] Name: "Ora Wellbeing"
- [ ] Google Analytics: Enabled (optional)
- [ ] Project created successfully

### Add Android App
- [ ] Click Android icon in Firebase Console
- [ ] Package name: `com.ora.wellbeing`
- [ ] App nickname: "Ora Android"
- [ ] Register app clicked
- [ ] Download google-services.json completed

### Get SHA Fingerprints
- [ ] Run keytool command (see SETUP_INSTRUCTIONS.md)
- [ ] Copy SHA-1 hash
- [ ] Copy SHA-256 hash

### Add Fingerprints to Firebase
- [ ] Go to Project Settings (gear icon)
- [ ] Find "SHA certificate fingerprints" section
- [ ] Add SHA-1 debug fingerprint
- [ ] Add SHA-256 debug fingerprint
- [ ] (Optional) Add SHA-1 release fingerprint
- [ ] (Optional) Add SHA-256 release fingerprint

### Enable Email/Password Auth
- [ ] Go to Authentication section
- [ ] Click "Sign-in method" tab
- [ ] Click "Email/Password"
- [ ] Toggle "Enable" to ON
- [ ] Click "Save"
- [ ] Provider shows "Enabled" status

### Enable Google Sign-In
- [ ] Still in "Sign-in method" tab
- [ ] Click "Google"
- [ ] Toggle "Enable" to ON
- [ ] Select support email from dropdown
- [ ] Click "Save"
- [ ] Provider shows "Enabled" status
- [ ] Expand Google provider section
- [ ] **COPY Web client ID** (very important!)

## 📱 Android Project Setup

### Add google-services.json
- [ ] Locate downloaded `google-services.json` file
- [ ] Place in `Ora/app/` directory (same level as build.gradle.kts)
- [ ] Verify path: `app/google-services.json`
- [ ] File appears in Project view in Android Studio

### Configure Web Client ID
- [ ] Open `app/src/main/java/com/ora/wellbeing/presentation/screens/auth/AuthViewModel.kt`
- [ ] Find line ~135 in `signInWithGoogle()` method
- [ ] Replace `"YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"`
- [ ] Paste actual Web Client ID from Firebase Console
- [ ] Save file

### Gradle Sync
- [ ] File > Sync Project with Gradle Files
- [ ] Wait for sync to complete successfully
- [ ] No errors in Build output

### Clean and Build
- [ ] Build > Clean Project
- [ ] Wait for clean to complete
- [ ] Build > Rebuild Project
- [ ] Wait for build to complete successfully
- [ ] No compilation errors

## 🧪 Testing

### Email/Password Sign Up
- [ ] Run app on device/emulator
- [ ] Auth screen appears
- [ ] Enter test email: test@example.com
- [ ] Enter password: test123456 (6+ chars)
- [ ] Click "Créer mon compte"
- [ ] No errors appear
- [ ] Redirected to Home screen
- [ ] Check Firebase Console > Authentication > Users
- [ ] New user appears in list

### Email/Password Sign In
- [ ] Sign out (if needed)
- [ ] Navigate to Auth screen
- [ ] Enter same test email
- [ ] Enter same password
- [ ] Click "Se connecter"
- [ ] No errors appear
- [ ] Redirected to Home screen

### Google Sign-In
- [ ] Sign out (if needed)
- [ ] Navigate to Auth screen
- [ ] Click "Continuer avec Google"
- [ ] Google account picker appears
- [ ] Select a Google account
- [ ] No errors appear
- [ ] Redirected to Home screen
- [ ] Check Firebase Console > Authentication > Users
- [ ] Google user appears in list

### Error Handling
- [ ] Try sign up with invalid email (shows error)
- [ ] Try sign up with short password < 6 chars (shows error)
- [ ] Try sign up with existing email (shows error)
- [ ] Try sign in with wrong password (shows error)
- [ ] Try sign in with non-existent email (shows error)
- [ ] All errors display in Snackbar
- [ ] No app crashes

### Navigation
- [ ] After successful auth, Home screen shows
- [ ] Bottom navigation bar appears
- [ ] Press Back button - doesn't go to Auth screen
- [ ] Close and reopen app - still logged in
- [ ] Navigate between tabs - works correctly

## 🔒 Security Verification

### File Security
- [ ] `google-services.json` is NOT committed to git
- [ ] Check `.gitignore` includes `google-services.json`
- [ ] Run `git status` - google-services.json not listed

### Firebase Console Security
- [ ] Review Authentication > Settings
- [ ] Check authorized domains list
- [ ] (Production) Enable Firebase App Check
- [ ] (Production) Set up password policy

## 📚 Documentation Review

### Read Documentation
- [ ] Read `SETUP_INSTRUCTIONS.md`
- [ ] Read `FIREBASE_AUTH_INTEGRATION.md`
- [ ] Read `docs/auth_setup.md`
- [ ] Read `docs/README.md`

### Understand Architecture
- [ ] Understand AuthRepository role
- [ ] Understand AuthViewModel flow
- [ ] Understand navigation auth checking
- [ ] Know where to add new auth features

## ✅ Final Verification

### Code Review
- [ ] All files compile without errors
- [ ] No TODO comments remain in critical code
- [ ] Web Client ID is configured (not placeholder)
- [ ] Timber logs work in Logcat

### Functionality
- [ ] Email sign up works ✓
- [ ] Email sign in works ✓
- [ ] Google Sign-In works ✓
- [ ] Auth state persists ✓
- [ ] Navigation works correctly ✓
- [ ] Errors display properly ✓

### Firebase Console
- [ ] Users appear after sign up/sign in ✓
- [ ] Email provider shows users ✓
- [ ] Google provider shows users ✓
- [ ] No errors in Firebase Console logs ✓

## 🚀 Optional Enhancements

### Additional Features (Future)
- [ ] Add email verification requirement
- [ ] Implement password reset UI
- [ ] Add password strength indicator
- [ ] Add biometric authentication
- [ ] Implement multi-factor auth (MFA)
- [ ] Add Apple Sign-In
- [ ] Add phone number authentication

### Production Setup (When Ready)
- [ ] Create separate Firebase project for production
- [ ] Get release keystore SHA-1 and SHA-256
- [ ] Add release fingerprints to Firebase
- [ ] Download production google-services.json
- [ ] Set up environment-based configuration
- [ ] Enable Firebase App Check
- [ ] Configure Firebase Security Rules
- [ ] Set up monitoring and alerts

## 📝 Notes

**Issues Encountered:**
```
(Write any issues you encountered here)


```

**Solutions Applied:**
```
(Write solutions here)


```

**Time Taken:**
- Firebase Console setup: ___ minutes
- Android project setup: ___ minutes
- Testing: ___ minutes
- Total: ___ minutes

**Completion Date:** _______________

---

## Status: 🟡 IN PROGRESS

Once all items are checked:
- Change status to: ✅ COMPLETED
- Delete this TODO file or move to `docs/completed/`
- Celebrate! 🎉

## Need Help?

- **Quick Start**: `SETUP_INSTRUCTIONS.md`
- **Detailed Guide**: `docs/auth_setup.md`
- **Troubleshooting**: `docs/auth_setup.md#troubleshooting`
- **Firebase Support**: https://firebase.google.com/support
