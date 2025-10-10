# Firebase Authentication Setup - Quick Start

Follow these steps to complete the Firebase Authentication setup for Ora.

## Prerequisites

- Android Studio installed
- Firebase account (free)
- Physical Android device OR Android emulator with Google Play Services

## Step 1: Create Firebase Project (5 minutes)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Enter project name: `Ora Wellbeing` (or your choice)
4. Enable/disable Google Analytics (optional)
5. Click **"Create project"**

## Step 2: Add Android App to Firebase (3 minutes)

1. In Firebase Console, click the **Android icon**
2. Enter package name: **`com.ora.wellbeing`** (MUST match exactly)
3. App nickname: `Ora Android` (optional)
4. Click **"Register app"**
5. Click **"Download google-services.json"**
6. **IMPORTANT**: Place the downloaded file in:
   ```
   Ora/app/google-services.json
   ```
   (Same folder as `app/build.gradle.kts`)

## Step 3: Get SHA-1 Fingerprint (2 minutes)

### Windows (PowerShell):
```powershell
cd "C:\Users\YOUR_USERNAME\.android"
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### macOS/Linux:
```bash
cd ~/.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Copy both SHA-1 and SHA-256** from the output.

## Step 4: Add SHA Fingerprints to Firebase (2 minutes)

1. In Firebase Console, go to **Project Settings** (gear icon)
2. Scroll to **"Your apps"** section
3. Find your Android app
4. Scroll to **"SHA certificate fingerprints"**
5. Click **"Add fingerprint"**
6. Paste SHA-1, then click **"Add fingerprint"** again for SHA-256

## Step 5: Enable Authentication Providers (3 minutes)

### Enable Email/Password:
1. In Firebase Console, go to **Authentication** (left menu)
2. Click **"Get started"** if first time
3. Go to **"Sign-in method"** tab
4. Click **"Email/Password"**
5. Toggle **"Enable"** to ON
6. Click **"Save"**

### Enable Google Sign-In:
1. Still in **"Sign-in method"** tab
2. Click **"Google"**
3. Toggle **"Enable"** to ON
4. Select **"Project support email"** from dropdown
5. Click **"Save"**
6. **IMPORTANT**: Expand the Google provider section
7. **COPY the "Web client ID"** - you'll need this next!

## Step 6: Configure Web Client ID (2 minutes)

1. Open file: `app/src/main/java/com/ora/wellbeing/presentation/screens/auth/AuthViewModel.kt`
2. Find line **~135** (in `signInWithGoogle()` method):
   ```kotlin
   val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
   ```
3. Replace with your actual Web Client ID copied from Step 5:
   ```kotlin
   val webClientId = "123456789-abc123def456.apps.googleusercontent.com"
   ```
4. Save the file

## Step 7: Sync and Build (2 minutes)

1. In Android Studio, click **"File"** > **"Sync Project with Gradle Files"**
2. Wait for sync to complete
3. Click **"Build"** > **"Clean Project"**
4. Click **"Build"** > **"Rebuild Project"**

## Step 8: Test Authentication (5 minutes)

1. Run the app on a device or emulator
2. You should see the **Auth screen**

### Test Email/Password:
- Email: `test@example.com`
- Password: `test123` (minimum 6 characters)
- Click **"Créer mon compte"** (Create account)
- Check Firebase Console > Authentication > Users - you should see the user!

### Test Google Sign-In:
- Click **"Continuer avec Google"**
- Select a Google account
- You should be signed in and redirected to Home screen
- Check Firebase Console > Authentication > Users - you should see the Google user!

## Verification Checklist

- [ ] `google-services.json` is in `app/` directory
- [ ] SHA-1 and SHA-256 added to Firebase Console
- [ ] Email/Password provider enabled in Firebase
- [ ] Google provider enabled in Firebase
- [ ] Web Client ID configured in `AuthViewModel.kt`
- [ ] Gradle sync successful
- [ ] App builds without errors
- [ ] Email sign-up works
- [ ] Google Sign-In works
- [ ] Users appear in Firebase Console

## Troubleshooting

### "Default FirebaseApp is not initialized"
- **Fix**: Verify `google-services.json` is in `app/` directory, then sync Gradle

### "Please configure Web Client ID"
- **Fix**: Update `AuthViewModel.kt` line ~135 with your Web Client ID

### Google Sign-In Error 12500
- **Fix**: Add SHA-1 fingerprint to Firebase Console (Step 4)
- Also ensure package name is exactly `com.ora.wellbeing`

### "google-services.json not found"
- **Fix**: Download from Firebase Console and place in `app/` directory (not `app/src`)

### Google Sign-In doesn't work on emulator
- **Fix**: Use an emulator with **Google Play Services** (has Play Store icon)
- OR test on a physical device

## Next Steps

After successful setup:

1. **Read the documentation**: See `docs/auth_setup.md` for detailed guide
2. **Test thoroughly**: Use different email addresses and Google accounts
3. **Add features**: Implement password reset UI, email verification, etc.
4. **Deploy**: When ready for production, repeat Steps 4-5 with release keystore SHA

## Need Help?

- **Detailed guide**: `docs/auth_setup.md`
- **Architecture docs**: `docs/README.md`
- **Integration summary**: `FIREBASE_AUTH_INTEGRATION.md`
- **Firebase docs**: https://firebase.google.com/docs/auth
- **Credential Manager**: https://developer.android.com/training/sign-in/credential-manager

## Estimated Total Time: 20-25 minutes

Good luck! 🚀
