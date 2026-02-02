package com.ora.wellbeing.data.remote.api

import com.google.gson.annotations.SerializedName
import com.ora.wellbeing.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface OraApiService {

    // User endpoints
    @GET("api/v1/users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<UserDto>

    @POST("api/v1/users")
    suspend fun createUser(@Body user: UserDto): Response<UserDto>

    @PUT("api/v1/users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Body user: UserDto
    ): Response<UserDto>

    @DELETE("api/v1/users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String): Response<Unit>

    // Content endpoints
    @GET("api/v1/content")
    suspend fun getContent(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("level") level: String? = null,
        @Query("max_duration") maxDuration: Int? = null,
        @Query("is_flash_session") isFlashSession: Boolean? = null
    ): Response<ContentListResponse>

    @GET("api/v1/content/{contentId}")
    suspend fun getContentById(@Path("contentId") contentId: String): Response<ContentDto>

    @POST("api/v1/content/sync")
    suspend fun syncContent(@Body request: ContentSyncRequest): Response<ContentSyncResponse>

    @GET("api/v1/content/search")
    suspend fun searchContent(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<ContentListResponse>

    @GET("api/v1/users/{userId}/content/recommendations")
    suspend fun getRecommendations(
        @Path("userId") userId: String,
        @Query("time_slot") timeSlot: String? = null,
        @Query("limit") limit: Int = 5
    ): Response<List<ContentDto>>

    // Journal endpoints
    @GET("api/v1/users/{userId}/journal")
    suspend fun getJournalEntries(
        @Path("userId") userId: String,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<JournalEntryDto>>

    @POST("api/v1/users/{userId}/journal")
    suspend fun createJournalEntry(
        @Path("userId") userId: String,
        @Body entry: JournalEntryDto
    ): Response<JournalEntryDto>

    @PUT("api/v1/users/{userId}/journal/{entryId}")
    suspend fun updateJournalEntry(
        @Path("userId") userId: String,
        @Path("entryId") entryId: String,
        @Body entry: JournalEntryDto
    ): Response<JournalEntryDto>

    @DELETE("api/v1/users/{userId}/journal/{entryId}")
    suspend fun deleteJournalEntry(
        @Path("userId") userId: String,
        @Path("entryId") entryId: String
    ): Response<Unit>

    @POST("api/v1/users/{userId}/journal/sync")
    suspend fun syncJournal(
        @Path("userId") userId: String,
        @Body request: JournalSyncRequest
    ): Response<JournalSyncResponse>

    // User Activity endpoints
    @GET("api/v1/users/{userId}/activities")
    suspend fun getUserActivities(
        @Path("userId") userId: String,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<UserActivityDto>>

    @POST("api/v1/users/{userId}/activities")
    suspend fun createActivity(
        @Path("userId") userId: String,
        @Body activity: UserActivityDto
    ): Response<UserActivityDto>

    @PUT("api/v1/users/{userId}/activities/{activityId}")
    suspend fun updateActivity(
        @Path("userId") userId: String,
        @Path("activityId") activityId: String,
        @Body activity: UserActivityDto
    ): Response<UserActivityDto>

    @POST("api/v1/users/{userId}/activities/sync")
    suspend fun syncActivities(
        @Path("userId") userId: String,
        @Body request: ActivitySyncRequest
    ): Response<ActivitySyncResponse>

    // User Stats endpoints
    @GET("api/v1/users/{userId}/stats")
    suspend fun getUserStats(@Path("userId") userId: String): Response<UserStatsDto>

    // Favorites endpoints
    @GET("api/v1/users/{userId}/favorites")
    suspend fun getUserFavorites(@Path("userId") userId: String): Response<List<String>>

    @POST("api/v1/users/{userId}/favorites/{contentId}")
    suspend fun addToFavorites(
        @Path("userId") userId: String,
        @Path("contentId") contentId: String
    ): Response<Unit>

    @DELETE("api/v1/users/{userId}/favorites/{contentId}")
    suspend fun removeFromFavorites(
        @Path("userId") userId: String,
        @Path("contentId") contentId: String
    ): Response<Unit>

    // Authentication endpoints (for future implementation)
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>

    // Health check
    @GET("api/v1/health")
    suspend fun healthCheck(): Response<HealthResponse>
}

// Authentication DTOs
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("device_id")
    val deviceId: String? = null
)

data class RegisterRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("device_id")
    val deviceId: String? = null
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("expires_in")
    val expiresIn: Long,

    @SerializedName("user")
    val user: UserDto
)

data class HealthResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("version")
    val version: String,

    @SerializedName("timestamp")
    val timestamp: String
)