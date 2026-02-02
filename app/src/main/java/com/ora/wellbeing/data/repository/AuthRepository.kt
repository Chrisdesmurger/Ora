package com.ora.wellbeing.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.ora.wellbeing.data.local.entities.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FIX(auth): Repository pour gérer l'authentification Firebase
 * Gère Email/Password et Google Sign-In
 */
@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) {

    /**
     * Crée un nouveau compte avec email/password
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<User> {
        return try {
            Timber.d("AuthRepository: Attempting sign up with email: $email")

            // Créer le compte Firebase
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("User creation failed"))

            Timber.d("AuthRepository: Firebase user created: ${firebaseUser.uid}")

            // Créer l'utilisateur local dans Room
            val localUser = firebaseUser.toLocalUser()
            userRepository.createUser(localUser)

            Timber.d("AuthRepository: Local user created successfully")
            Result.success(localUser)
        } catch (e: Exception) {
            Timber.e(e, "AuthRepository: Sign up failed")
            Result.failure(e)
        }
    }

    /**
     * Se connecte avec email/password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            Timber.d("AuthRepository: Attempting sign in with email: $email")

            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Sign in failed"))

            Timber.d("AuthRepository: Firebase sign in successful: ${firebaseUser.uid}")

            // Récupérer ou créer l'utilisateur local
            val localUser = userRepository.getUserById(firebaseUser.uid)
                ?: firebaseUser.toLocalUser().also { userRepository.createUser(it) }

            // Mettre à jour le temps de dernière activité
            userRepository.updateLastActiveTime(localUser.id)

            Timber.d("AuthRepository: Sign in completed successfully")
            Result.success(localUser)
        } catch (e: Exception) {
            Timber.e(e, "AuthRepository: Sign in failed")
            Result.failure(e)
        }
    }

    /**
     * Se connecte avec Google en utilisant un ID Token
     */
    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            Timber.d("AuthRepository: Attempting Google sign in")

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Google sign in failed"))

            Timber.d("AuthRepository: Google sign in successful: ${firebaseUser.uid}")

            // Récupérer ou créer l'utilisateur local
            val localUser = userRepository.getUserById(firebaseUser.uid)
                ?: firebaseUser.toLocalUser().also { userRepository.createUser(it) }

            // Mettre à jour le temps de dernière activité
            userRepository.updateLastActiveTime(localUser.id)

            Timber.d("AuthRepository: Google sign in completed successfully")
            Result.success(localUser)
        } catch (e: Exception) {
            Timber.e(e, "AuthRepository: Google sign in failed")
            Result.failure(e)
        }
    }

    /**
     * Déconnecte l'utilisateur
     */
    fun signOut(): Result<Unit> {
        return try {
            Timber.d("AuthRepository: Signing out")
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "AuthRepository: Sign out failed")
            Result.failure(e)
        }
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    fun isUserSignedIn(): Boolean {
        val isSignedIn = firebaseAuth.currentUser != null
        Timber.d("AuthRepository: User signed in: $isSignedIn")
        return isSignedIn
    }

    /**
     * Récupère l'utilisateur Firebase actuel
     */
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Flow qui émet l'utilisateur actuel et ses changements d'état
     */
    fun currentUserFlow(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            Timber.d("AuthRepository: Auth state changed, user: ${firebaseUser?.uid}")
            trySend(firebaseUser?.toLocalUser())
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        // Émettre l'état initial
        trySend(firebaseAuth.currentUser?.toLocalUser())

        awaitClose {
            Timber.d("AuthRepository: Removing auth state listener")
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    /**
     * Envoie un email de réinitialisation de mot de passe
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            Timber.d("AuthRepository: Sending password reset email to: $email")
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "AuthRepository: Password reset email failed")
            Result.failure(e)
        }
    }

    /**
     * Convertit un FirebaseUser en User local
     */
    private fun FirebaseUser.toLocalUser(): User {
        return User(
            id = this.uid,
            name = this.displayName ?: "Utilisateur Ora",
            email = this.email
        )
    }
}
