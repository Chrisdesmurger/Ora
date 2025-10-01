# Firebase Authentication Setup Guide

This guide explains how to configure Firebase Authentication for the Ora Android app with Email/Password and Google Sign-In providers.

## Table of Contents
1. [Firebase Project Setup](#firebase-project-setup)
2. [Enable Authentication Providers](#enable-authentication-providers)
3. [Configure Google Sign-In](#configure-google-sign-in)
4. [Add SHA Fingerprints](#add-sha-fingerprints)
5. [Download google-services.json](#download-google-servicesjson)
6. [Update Code Configuration](#update-code-configuration)
7. [Testing](#testing)
8. [Troubleshooting](#troubleshooting)

## Firebase Project Setup

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name: "Ora Wellbeing" (or your choice)
4. Follow the setup wizard
5. Choose a Google Analytics account (optional but recommended)

### 2. Add Android App
1. In Firebase Console, click the Android icon to add an Android app
2. Register your app with these details:
   - **Android package name**: `com.ora.wellbeing`
   - **App nickname**: "Ora Android" (optional)
   - **Debug signing certificate SHA-1**: (see [Add SHA Fingerprints](#add-sha-fingerprints) section)

## Enable Authentication Providers

### Email/Password Authentication

1. In Firebase Console, go to **Authentication** > **Sign-in method**
2. Click on **Email/Password**
3. Enable the provider:
   - Toggle **Enable** to ON
   - You can optionally enable **Email link (passwordless sign-in)** for passwordless auth
4. Click **Save**

### Google Sign-In Authentication

1. In Firebase Console, go to **Authentication** > **Sign-in method**
2. Click on **Google**
3. Enable the provider:
   - Toggle **Enable** to ON
   - Enter **Project support email**: your email address
   - **Project public-facing name**: "Ora Wellbeing"
4. Click **Save**
5. **Important**: Expand the Google provider and copy the **Web client ID** - you'll need this later

## Configure Google Sign-In

### Get Web Client ID

1. In Firebase Console, go to **Authentication** > **Sign-in method** > **Google**
2. Expand the Google provider section
3. Copy the **Web client ID** (format: `xxxxx.apps.googleusercontent.com`)
4. Update `AuthViewModel.kt`:
   ```kotlin
   // In signInWithGoogle() method, replace:
   val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
   // With your actual Web Client ID:
   val webClientId = "123456789-abc123def456.apps.googleusercontent.com"
   ```

### Configure OAuth Consent Screen

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your Firebase project
3. Navigate to **APIs & Services** > **OAuth consent screen**
4. Choose **External** user type (or Internal if using Google Workspace)
5. Fill required fields:
   - **App name**: Ora Wellbeing
   - **User support email**: your email
   - **Developer contact**: your email
6. Add scopes (optional): `email`, `profile`
7. Save and continue

## Add SHA Fingerprints

SHA fingerprints are required for Google Sign-In and other Firebase services. You need to add both debug and release SHA fingerprints.

### Get Debug SHA-1 and SHA-256

#### Windows (PowerShell):
```powershell
cd "C:\Users\YOUR_USERNAME\.android"
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### macOS/Linux:
```bash
cd ~/.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### Get Release SHA-1 and SHA-256

If you have a release keystore:
```bash
keytool -list -v -keystore /path/to/your/release.keystore -alias your-alias
```

### Add to Firebase Console

1. Go to Firebase Console > **Project Settings** (gear icon)
2. Scroll to **Your apps** section
3. Click on your Android app
4. Scroll to **SHA certificate fingerprints**
5. Click **Add fingerprint**
6. Paste the SHA-1 hash
7. Click **Add fingerprint** again for SHA-256
8. Repeat for both debug and release keystores

**Important**: Every device/machine used for debugging needs its SHA-1 added!

## Download google-services.json

1. In Firebase Console > **Project Settings**
2. Scroll to **Your apps** section
3. Click on your Android app
4. Click **Download google-services.json**
5. Place the file in: `app/google-services.json`

**Security Note**: The `google-services.json` file is already added to `.gitignore` and should NOT be committed to version control.

### Verify File Placement

Your project structure should look like:
```
Ora/
├── app/
│   ├── google-services.json    <-- Place file here
│   ├── build.gradle.kts
│   └── src/
├── build.gradle.kts
└── settings.gradle.kts
```

## Update Code Configuration

### 1. Verify Gradle Sync

After placing `google-services.json`, sync your project:
- In Android Studio: **File** > **Sync Project with Gradle Files**

### 2. Update Web Client ID

In `AuthViewModel.kt` (line ~135):
```kotlin
val webClientId = "YOUR_ACTUAL_WEB_CLIENT_ID.apps.googleusercontent.com"
```

Replace with the Web Client ID from Firebase Console (Authentication > Sign-in method > Google).

## Testing

### Test Email/Password Authentication

1. Run the app
2. On Auth screen, enter:
   - Email: test@example.com
   - Password: test123 (minimum 6 characters)
3. Click "Créer mon compte" to sign up
4. Verify user appears in Firebase Console > **Authentication** > **Users**

### Test Google Sign-In

1. Run the app
2. Click "Continuer avec Google"
3. Select a Google account
4. Verify user appears in Firebase Console > **Authentication** > **Users**

**Note**: Google Sign-In requires:
- Valid SHA-1 fingerprint added
- Correct Web Client ID configured
- Physical device or emulator with Google Play Services

## Troubleshooting

### Common Issues

#### 1. "Please configure Web Client ID in AuthViewModel.kt"

**Solution**: Update the `webClientId` in `AuthViewModel.kt` with your actual Web Client ID from Firebase Console.

#### 2. Google Sign-In shows "12500: DEVELOPER_ERROR"

**Causes**:
- SHA-1 fingerprint not added to Firebase Console
- Wrong package name in Firebase Console
- Wrong Web Client ID in code

**Solution**:
1. Verify package name matches: `com.ora.wellbeing`
2. Add debug SHA-1 fingerprint (see [Add SHA Fingerprints](#add-sha-fingerprints))
3. Re-download `google-services.json` after adding SHA-1
4. Clean and rebuild project

#### 3. "Default FirebaseApp is not initialized"

**Solution**:
- Verify `google-services.json` is in `app/` directory
- Verify Google Services plugin is applied in `app/build.gradle.kts`
- Sync Gradle and rebuild

#### 4. Email/Password sign-in fails

**Causes**:
- Email/Password provider not enabled in Firebase Console
- Password too short (< 6 characters)
- Email already exists (for sign-up)

**Solution**:
- Enable Email/Password in Firebase Console
- Use valid email and password (6+ characters)
- Check Firebase Console > Authentication > Users for existing accounts

#### 5. Credential Manager not working

**Ensure**:
- `androidx.credentials` version 1.3.0 or higher
- Google Play Services updated on device/emulator
- Physical device or emulator with Play Store (not AOSP emulator)

### Debug Logs

The app uses Timber for logging. Check Logcat for auth-related logs:
- `AuthRepository:` - Repository operations
- `AuthViewModel:` - ViewModel state changes
- `OraAuthViewModel:` - Navigation auth state

Filter Logcat: `tag:Auth`

## Security Best Practices

1. **Never commit** `google-services.json` to version control
2. **Use different** Firebase projects for dev/staging/production
3. **Enable** Firebase App Check for additional security
4. **Implement** email verification for new accounts
5. **Set up** password reset functionality (already implemented in `AuthRepository`)
6. **Configure** authorized domains in Firebase Console > Authentication > Settings
7. **Review** security rules for Firebase services

## Next Steps

After authentication is set up:

1. **Add email verification**:
   ```kotlin
   firebaseUser.sendEmailVerification()
   ```

2. **Implement password reset**:
   - Already available: `AuthRepository.sendPasswordResetEmail(email)`
   - Add UI in AuthScreen

3. **Add profile completion**:
   - After first sign-in, navigate to onboarding/profile setup

4. **Sync user data**:
   - Update local Room database after authentication
   - Use `UserRepository` to manage user data

5. **Handle auth state changes**:
   - Already implemented in `OraAuthViewModel`
   - Add sign-out functionality in Profile screen

## Useful Resources

- [Firebase Authentication Docs](https://firebase.google.com/docs/auth)
- [Credential Manager Guide](https://developer.android.com/training/sign-in/credential-manager)
- [Google Sign-In Android](https://developers.google.com/identity/sign-in/android/start)
- [Firebase Console](https://console.firebase.google.com/)

## Support

For issues specific to the Ora app, check:
- Firebase Console > Authentication > Users (verify user creation)
- Android Studio Logcat (filter by "Auth")
- Firebase Console > Authentication > Templates (email templates)

---

**Last Updated**: 2025-10-01
**Firebase SDK Version**: 33.7.0
**Credentials Version**: 1.3.0
