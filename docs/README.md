# Ora Wellbeing App - Documentation

This directory contains technical documentation for the Ora Android wellbeing application.

## Available Documentation

### [Authentication Setup Guide](auth_setup.md)
Complete guide for setting up Firebase Authentication with Email/Password and Google Sign-In providers.

**Topics covered**:
- Firebase project creation and configuration
- Enabling authentication providers
- SHA fingerprint setup
- Google Sign-In with Credential Manager API
- Troubleshooting common issues
- Security best practices

## Quick Start

1. **Firebase Setup**: Follow [auth_setup.md](auth_setup.md) to configure Firebase Authentication
2. **Download google-services.json**: Place it in `app/` directory
3. **Configure Web Client ID**: Update `AuthViewModel.kt` with your Web Client ID
4. **Add SHA Fingerprints**: Add debug/release SHA-1 and SHA-256 to Firebase Console
5. **Sync and Build**: Sync Gradle and build the app

## Project Structure

```
Ora/
├── app/
│   ├── src/main/java/com/ora/wellbeing/
│   │   ├── data/
│   │   │   └── repository/
│   │   │       ├── AuthRepository.kt         # Firebase auth operations
│   │   │       └── UserRepository.kt         # Local user data
│   │   ├── di/
│   │   │   └── AuthModule.kt                 # Hilt DI for auth
│   │   └── presentation/
│   │       ├── navigation/
│   │       │   ├── OraAuthViewModel.kt       # Auth state for navigation
│   │       │   ├── OraDestinations.kt        # Route definitions
│   │       │   └── OraNavigation.kt          # Nav graph with auth check
│   │       └── screens/
│   │           └── auth/
│   │               ├── AuthScreen.kt         # UI for sign in/up
│   │               ├── AuthViewModel.kt      # Auth screen logic
│   │               └── AuthUiState.kt        # UI state definitions
│   └── google-services.json                  # Firebase config (not in repo)
└── docs/
    ├── README.md                              # This file
    └── auth_setup.md                          # Auth setup guide
```

## Authentication Flow

1. **App Launch**: `OraAuthViewModel` checks if user is signed in via `AuthRepository`
2. **Not Authenticated**: User is redirected to `AuthScreen`
3. **Sign In/Sign Up**:
   - Email/Password: Direct Firebase auth
   - Google: Uses Credential Manager API to get ID token, then Firebase auth
4. **Success**: User data stored in Room database, navigated to Home screen
5. **Auth State**: Monitored via Flow, triggers navigation changes automatically

## Architecture

### Clean Architecture Layers

**Presentation Layer** (UI + ViewModels)
- `AuthScreen.kt`: Compose UI with Material 3
- `AuthViewModel.kt`: Handles UI events and auth operations
- `OraAuthViewModel.kt`: Exposes auth state for navigation

**Data Layer** (Repositories)
- `AuthRepository.kt`: Firebase auth operations, maps to local User entity
- `UserRepository.kt`: Room database operations for user data

**Dependency Injection** (Hilt)
- `AuthModule.kt`: Provides FirebaseAuth instance
- Other modules provide repositories and use cases

## Key Technologies

- **Firebase Authentication**: Email/Password and Google Sign-In
- **Credential Manager API**: Modern Google Sign-In (replaces deprecated Smart Lock)
- **Jetpack Compose**: Modern declarative UI
- **Hilt**: Dependency injection
- **Room**: Local database
- **Kotlin Coroutines + Flow**: Asynchronous programming
- **MVVM**: Architecture pattern with Clean Architecture principles

## Testing

### Manual Testing Checklist

- [ ] Email/Password sign up with valid credentials
- [ ] Email/Password sign in with existing account
- [ ] Google Sign-In flow
- [ ] Auth state persists after app restart
- [ ] Error messages display correctly
- [ ] Loading states work properly
- [ ] Navigation redirects to Home after successful auth
- [ ] Navigation redirects to Auth after sign out

### Common Test Scenarios

**Valid Sign Up**:
- Email: newuser@test.com
- Password: test123456 (6+ characters)

**Invalid Cases** (should show errors):
- Empty email or password
- Invalid email format
- Password < 6 characters
- Email already in use (for sign up)

## Contributing

When adding new authentication features:

1. Update `AuthRepository.kt` for new auth methods
2. Add UI in `AuthScreen.kt` if needed
3. Update `AuthViewModel.kt` for new events/state
4. Document changes in this directory
5. Update tests

## Security Notes

- **Never commit** `google-services.json` to version control (already in `.gitignore`)
- **Use environment variables** for sensitive config in CI/CD
- **Enable App Check** in Firebase for production
- **Implement email verification** for new accounts
- **Add rate limiting** for auth endpoints
- **Review Firebase Security Rules** regularly

## Troubleshooting

See [auth_setup.md - Troubleshooting](auth_setup.md#troubleshooting) section for common issues and solutions.

Quick debug checklist:
1. Check Logcat for "Auth" tagged logs
2. Verify `google-services.json` is in `app/` directory
3. Confirm SHA-1 fingerprints are added to Firebase Console
4. Ensure Web Client ID is configured in `AuthViewModel.kt`
5. Check Firebase Console > Authentication > Users for user records

## Future Enhancements

Planned authentication improvements:
- [ ] Email verification requirement
- [ ] Password reset UI in AuthScreen
- [ ] Phone number authentication
- [ ] Apple Sign-In (for iOS)
- [ ] Biometric authentication option
- [ ] Multi-factor authentication (MFA)
- [ ] Social providers (Facebook, Twitter)

## Resources

- [Firebase Auth Documentation](https://firebase.google.com/docs/auth)
- [Credential Manager Guide](https://developer.android.com/training/sign-in/credential-manager)
- [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)

---

**Last Updated**: 2025-10-01
**App Version**: 1.0.0
**Minimum SDK**: 26 (Android 8.0)
**Target SDK**: 34 (Android 14)
