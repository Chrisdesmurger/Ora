package com.ora.wellbeing.data.remote.interceptor

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Interceptor that automatically adds Firebase Authentication token
 * to all outgoing requests to OraWebApp API.
 *
 * This interceptor:
 * 1. Retrieves the current Firebase Auth user
 * 2. Gets a fresh ID token (with force refresh if needed)
 * 3. Adds the token as a Bearer Authorization header
 *
 * If no user is authenticated, requests proceed without the Authorization header.
 * The server will return 401 Unauthorized for protected endpoints.
 *
 * Usage: This interceptor is automatically added to the OraWebApp OkHttpClient
 * via Hilt dependency injection in OraWebAppModule.
 *
 * @see OraWebAppModule
 */
@Singleton
class FirebaseAuthInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {

    companion object {
        private const val TAG = "FirebaseAuthInterceptor"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get current Firebase user
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            Timber.tag(TAG).d("No authenticated user, proceeding without auth token")
            return chain.proceed(originalRequest)
        }

        // Get fresh ID token
        val token = try {
            runBlocking {
                currentUser.getIdToken(false).await().token
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to get Firebase ID token")
            // Proceed without token - server will return 401 if authentication required
            return chain.proceed(originalRequest)
        }

        if (token.isNullOrEmpty()) {
            Timber.tag(TAG).w("Firebase ID token is null or empty")
            return chain.proceed(originalRequest)
        }

        // Build new request with Authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$token")
            .build()

        Timber.tag(TAG).d("Added Firebase auth token to request: ${originalRequest.url}")
        return chain.proceed(authenticatedRequest)
    }
}
