package com.ora.wellbeing.data.remote.client

import com.ora.wellbeing.data.remote.api.OraApiService
import com.ora.wellbeing.data.remote.config.NetworkConfig
import com.ora.wellbeing.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor(
    private val apiService: OraApiService,
    private val networkConfig: NetworkConfig
) {

    // User operations
    suspend fun getUser(userId: String): ApiResult<UserDto> {
        return safeApiCall { apiService.getUser(userId) }
    }

    suspend fun createUser(user: UserDto): ApiResult<UserDto> {
        return safeApiCall { apiService.createUser(user) }
    }

    suspend fun updateUser(userId: String, user: UserDto): ApiResult<UserDto> {
        return safeApiCall { apiService.updateUser(userId, user) }
    }

    suspend fun deleteUser(userId: String): ApiResult<Unit> {
        return safeApiCall { apiService.deleteUser(userId) }
    }

    // Content operations
    suspend fun getContent(
        page: Int = 1,
        pageSize: Int = 20,
        type: String? = null,
        category: String? = null,
        level: String? = null
    ): ApiResult<ContentListResponse> {
        return safeApiCall {
            apiService.getContent(page, pageSize, type, category, level)
        }
    }

    suspend fun getContentById(contentId: String): ApiResult<ContentDto> {
        return safeApiCall { apiService.getContentById(contentId) }
    }

    // Journal operations
    suspend fun getJournalEntries(userId: String): ApiResult<List<JournalEntryDto>> {
        return safeApiCall { apiService.getJournalEntries(userId) }
    }

    suspend fun createJournalEntry(userId: String, entry: JournalEntryDto): ApiResult<JournalEntryDto> {
        return safeApiCall { apiService.createJournalEntry(userId, entry) }
    }

    // User Activity operations
    suspend fun getUserActivities(
        userId: String,
        startDate: String? = null,
        endDate: String? = null,
        limit: Int? = null
    ): ApiResult<List<UserActivityDto>> {
        return safeApiCall { apiService.getUserActivities(userId, startDate, endDate, limit) }
    }

    suspend fun createActivity(userId: String, activity: UserActivityDto): ApiResult<UserActivityDto> {
        return safeApiCall { apiService.createActivity(userId, activity) }
    }

    /**
     * Fonction utilitaire pour les appels API sécurisés
     */
    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    ApiResult.Success(body)
                } ?: ApiResult.Error(ApiException.ClientException("Empty response body"))
            } else {
                val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                when (response.code()) {
                    401 -> ApiResult.Error(ApiException.AuthenticationException(errorMessage))
                    in 400..499 -> ApiResult.Error(ApiException.ClientException(errorMessage))
                    in 500..599 -> ApiResult.Error(ApiException.ServerException(errorMessage))
                    else -> ApiResult.Error(ApiException.UnknownException(errorMessage))
                }
            }
        } catch (e: IOException) {
            ApiResult.Error(ApiException.NetworkException("Network error: ${e.message}", e))
        } catch (e: Exception) {
            ApiResult.Error(ApiException.UnknownException("Unknown error: ${e.message}", e))
        }
    }
}