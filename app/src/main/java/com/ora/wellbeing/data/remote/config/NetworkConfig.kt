package com.ora.wellbeing.data.remote.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ora.wellbeing.data.remote.api.OraApiService
import com.ora.wellbeing.data.remote.client.ApiException
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkConfig @Inject constructor() {

    companion object {
        private const val BASE_URL_PRODUCTION = "https://api.ora-wellbeing.com/"
        private const val BASE_URL_STAGING = "https://staging-api.ora-wellbeing.com/"
        private const val BASE_URL_DEVELOPMENT = "https://dev-api.ora-wellbeing.com/"
        private const val BASE_URL_LOCAL = "http://localhost:8080/"

        private const val TIMEOUT_CONNECT = 30L
        private const val TIMEOUT_READ = 30L
        private const val TIMEOUT_WRITE = 30L

        // For MVP, we'll use local development server
        // In production, this would be determined by build variant
        private const val CURRENT_BASE_URL = BASE_URL_LOCAL
    }

    fun getBaseUrl(): String = CURRENT_BASE_URL

    fun createApiService(): OraApiService {
        return createRetrofit().create(OraApiService::class.java)
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(createOkHttpClient())
            .addConverterFactory(createGsonConverterFactory())
            .build()
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
            .addInterceptor(createLoggingInterceptor())
            .addInterceptor(createAuthInterceptor())
            .addInterceptor(createErrorHandlingInterceptor())
            .addInterceptor(createUserAgentInterceptor())
            .build()
    }

    private fun createGsonConverterFactory(): GsonConverterFactory {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
            .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
            .setLenient()
            .create()

        return GsonConverterFactory.create(gson)
    }

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (isDebugMode()) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()

            // For MVP, we don't have authentication yet
            // In production, this would add Bearer token to requests
            val requestBuilder = originalRequest.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")

            // Add auth token when available
            getAuthToken()?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }
    }

    private fun createErrorHandlingInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            // Handle specific HTTP error codes
            when (response.code) {
                401 -> {
                    // Unauthorized - trigger token refresh or logout
                    throw ApiException.AuthenticationException("Authentication required")
                }
                500, 502, 503, 504 -> {
                    // Server errors
                    throw ApiException.ServerException("Server error: ${response.code}")
                }
            }

            response
        }
    }

    private fun createUserAgentInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val requestWithUserAgent = originalRequest.newBuilder()
                .header("User-Agent", getUserAgent())
                .build()

            chain.proceed(requestWithUserAgent)
        }
    }

    private fun isDebugMode(): Boolean {
        // In production, this would check BuildConfig.DEBUG
        return true // For MVP development
    }

    private fun getAuthToken(): String? {
        // For MVP, return null (no authentication)
        // In production, this would retrieve token from secure storage
        return null
    }

    private fun getUserAgent(): String {
        // In production, this would include app version, OS version, etc.
        return "OraAndroid/1.0.0 (Android)"
    }

    // Retry configuration
    fun shouldRetry(exception: Exception, attemptCount: Int): Boolean {
        if (attemptCount >= 3) return false

        return when (exception) {
            is ApiException.NetworkException -> true
            is ApiException.ServerException -> true
            else -> false
        }
    }

    fun getRetryDelay(attemptCount: Int): Long {
        // Exponential backoff: 1s, 2s, 4s
        return (1000L * Math.pow(2.0, attemptCount.toDouble())).toLong()
    }
}

// Custom type adapters for Gson
class LocalDateTimeTypeAdapter : com.google.gson.TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun write(out: com.google.gson.stream.JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.format(formatter))
        }
    }

    override fun read(reader: com.google.gson.stream.JsonReader): LocalDateTime? {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        return LocalDateTime.parse(reader.nextString(), formatter)
    }
}

class LocalDateTypeAdapter : com.google.gson.TypeAdapter<LocalDate>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun write(out: com.google.gson.stream.JsonWriter, value: LocalDate?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.format(formatter))
        }
    }

    override fun read(reader: com.google.gson.stream.JsonReader): LocalDate? {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        return LocalDate.parse(reader.nextString(), formatter)
    }
}

// Network state monitoring
interface NetworkStateListener {
    fun onNetworkAvailable()
    fun onNetworkLost()
}

class NetworkMonitor @Inject constructor() {
    private val listeners = mutableListOf<NetworkStateListener>()

    fun addListener(listener: NetworkStateListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: NetworkStateListener) {
        listeners.remove(listener)
    }

    fun notifyNetworkAvailable() {
        listeners.forEach { it.onNetworkAvailable() }
    }

    fun notifyNetworkLost() {
        listeners.forEach { it.onNetworkLost() }
    }
}