package com.ora.wellbeing.di

import com.google.firebase.auth.FirebaseAuth
import com.ora.wellbeing.BuildConfig
import com.ora.wellbeing.data.remote.api.OraWebAppApi
import com.ora.wellbeing.data.remote.interceptor.FirebaseAuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module providing dependencies for OraWebApp API communication.
 *
 * This module configures a separate Retrofit instance specifically for
 * communicating with the OraWebApp Next.js backend (admin portal).
 *
 * Key features:
 * - Separate OkHttpClient with Firebase Auth interceptor
 * - Configurable base URL via BuildConfig.ORA_WEBAPP_BASE_URL
 * - Automatic token injection for all requests
 * - Request/Response logging in debug builds
 *
 * The base URL is configured per build type:
 * - Debug: Local development or staging URL
 * - Release: Production Vercel URL
 *
 * @see OraWebAppApi for available endpoints
 * @see FirebaseAuthInterceptor for authentication handling
 */
@Module
@InstallIn(SingletonComponent::class)
object OraWebAppModule {

    /**
     * Provides the Firebase Auth interceptor for adding authentication tokens.
     */
    @Provides
    @Singleton
    fun provideFirebaseAuthInterceptor(
        firebaseAuth: FirebaseAuth
    ): FirebaseAuthInterceptor {
        return FirebaseAuthInterceptor(firebaseAuth)
    }

    /**
     * Provides a dedicated OkHttpClient for OraWebApp API.
     *
     * This client is separate from the main app's OkHttpClient to allow
     * different configuration (auth interceptor, timeouts, etc.).
     *
     * Features:
     * - Firebase Auth token injection via interceptor
     * - HTTP logging in debug builds
     * - 30 second timeouts for all operations
     */
    @Provides
    @Singleton
    @Named("OraWebAppClient")
    fun provideOraWebAppOkHttpClient(
        firebaseAuthInterceptor: FirebaseAuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(firebaseAuthInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides a dedicated Retrofit instance for OraWebApp API.
     *
     * Uses the OraWebApp-specific OkHttpClient and base URL.
     * The base URL is configured via BuildConfig.ORA_WEBAPP_BASE_URL.
     */
    @Provides
    @Singleton
    @Named("OraWebAppRetrofit")
    fun provideOraWebAppRetrofit(
        @Named("OraWebAppClient") okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.ORA_WEBAPP_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides the OraWebApp API interface implementation.
     *
     * This is the main entry point for making API calls to OraWebApp.
     * Inject this interface into repositories or use cases that need
     * to communicate with the admin portal backend.
     */
    @Provides
    @Singleton
    fun provideOraWebAppApi(
        @Named("OraWebAppRetrofit") retrofit: Retrofit
    ): OraWebAppApi {
        return retrofit.create(OraWebAppApi::class.java)
    }
}
