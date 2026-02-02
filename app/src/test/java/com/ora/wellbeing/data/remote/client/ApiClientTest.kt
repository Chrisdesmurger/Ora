package com.ora.wellbeing.data.remote.client

import com.ora.wellbeing.data.remote.api.OraApiService
import com.ora.wellbeing.data.remote.config.NetworkConfig
import com.ora.wellbeing.data.remote.dto.UserDto
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class ApiClientTest {

    @Mock
    private lateinit var apiService: OraApiService

    @Mock
    private lateinit var networkConfig: NetworkConfig

    private lateinit var apiClient: ApiClient

    private val testUserDto = UserDto(
        id = "test_user_123",
        name = "Test User",
        email = "test@ora.com",
        preferredTimeSlot = "MORNING",
        experienceLevel = "BEGINNER",
        goals = listOf("relaxation", "flexibility"),
        createdAt = "2023-01-01T10:00:00",
        lastActiveAt = "2023-01-01T10:00:00",
        isOnboardingCompleted = false,
        notificationsEnabled = true,
        darkModeEnabled = false
    )

    @Before
    fun setup() {
        apiClient = ApiClient(apiService, networkConfig)
    }

    @Test
    fun `getUser returns success when api call succeeds`() = runTest {
        // Given
        val userId = "test_user_123"
        val response = Response.success(testUserDto)
        whenever(apiService.getUser(userId)).thenReturn(response)

        // When
        val result = apiClient.getUser(userId)

        // Then
        assertTrue(result is ApiResult.Success)
        assertEquals(testUserDto, result.data)
    }

    @Test
    fun `getUser returns error when api call fails with http error`() = runTest {
        // Given
        val userId = "test_user_123"
        val errorBody = "User not found".toResponseBody("application/json".toMediaType())
        val response = Response.error<UserDto>(404, errorBody)
        whenever(apiService.getUser(userId)).thenReturn(response)

        // When
        val result = apiClient.getUser(userId)

        // Then
        assertTrue(result is ApiResult.Error)
        assertTrue(result.exception is ApiException.HttpException)
        assertEquals(404, (result.exception as ApiException.HttpException).code)
    }

    @Test
    fun `getUser returns error when api call throws IOException`() = runTest {
        // Given
        val userId = "test_user_123"
        whenever(apiService.getUser(userId)).thenThrow(IOException("Network error"))

        // When
        val result = apiClient.getUser(userId)

        // Then
        assertTrue(result is ApiResult.Error)
        assertTrue(result.exception is ApiException.NetworkException)
        assertTrue(result.exception.message?.contains("Network error") == true)
    }

    @Test
    fun `getUser returns error when response body is null`() = runTest {
        // Given
        val userId = "test_user_123"
        val response = Response.success<UserDto>(null)
        whenever(apiService.getUser(userId)).thenReturn(response)

        // When
        val result = apiClient.getUser(userId)

        // Then
        assertTrue(result is ApiResult.Error)
        assertTrue(result.exception is ApiException.EmptyBodyException)
    }

    @Test
    fun `createUser returns success when api call succeeds`() = runTest {
        // Given
        val response = Response.success(testUserDto)
        whenever(apiService.createUser(testUserDto)).thenReturn(response)

        // When
        val result = apiClient.createUser(testUserDto)

        // Then
        assertTrue(result is ApiResult.Success)
        assertEquals(testUserDto, result.data)
    }

    @Test
    fun `updateUser returns success when api call succeeds`() = runTest {
        // Given
        val userId = "test_user_123"
        val updatedUser = testUserDto.copy(name = "Updated Name")
        val response = Response.success(updatedUser)
        whenever(apiService.updateUser(userId, updatedUser)).thenReturn(response)

        // When
        val result = apiClient.updateUser(userId, updatedUser)

        // Then
        assertTrue(result is ApiResult.Success)
        assertEquals(updatedUser, result.data)
    }

    @Test
    fun `deleteUser returns success when api call succeeds`() = runTest {
        // Given
        val userId = "test_user_123"
        val response = Response.success<Unit>(Unit)
        whenever(apiService.deleteUser(userId)).thenReturn(response)

        // When
        val result = apiClient.deleteUser(userId)

        // Then
        assertTrue(result is ApiResult.Success)
        assertEquals(Unit, result.data)
    }

    @Test
    fun `checkNetworkConnectivity returns true when health check succeeds`() = runTest {
        // Given
        val healthResponse = com.ora.wellbeing.data.remote.api.HealthResponse(
            status = "OK",
            version = "1.0.0",
            timestamp = "2023-01-01T10:00:00Z"
        )
        val response = Response.success(healthResponse)
        whenever(apiService.healthCheck()).thenReturn(response)

        // When
        val result = apiClient.checkNetworkConnectivity()

        // Then
        assertTrue(result)
    }

    @Test
    fun `checkNetworkConnectivity returns false when health check fails`() = runTest {
        // Given
        whenever(apiService.healthCheck()).thenThrow(IOException("Network error"))

        // When
        val result = apiClient.checkNetworkConnectivity()

        // Then
        assertTrue(!result)
    }

    @Test
    fun `onSuccess extension function executes action when result is success`() {
        // Given
        val result: ApiResult<String> = ApiResult.Success("test data")
        var actionExecuted = false
        var receivedData: String? = null

        // When
        result.onSuccess { data ->
            actionExecuted = true
            receivedData = data
        }

        // Then
        assertTrue(actionExecuted)
        assertEquals("test data", receivedData)
    }

    @Test
    fun `onSuccess extension function does not execute action when result is error`() {
        // Given
        val result: ApiResult<String> = ApiResult.Error(ApiException.NetworkException("Network error"))
        var actionExecuted = false

        // When
        result.onSuccess { actionExecuted = true }

        // Then
        assertTrue(!actionExecuted)
    }

    @Test
    fun `onError extension function executes action when result is error`() {
        // Given
        val exception = ApiException.NetworkException("Network error")
        val result: ApiResult<String> = ApiResult.Error(exception)
        var actionExecuted = false
        var receivedException: ApiException? = null

        // When
        result.onError { ex ->
            actionExecuted = true
            receivedException = ex
        }

        // Then
        assertTrue(actionExecuted)
        assertEquals(exception, receivedException)
    }

    @Test
    fun `onError extension function does not execute action when result is success`() {
        // Given
        val result: ApiResult<String> = ApiResult.Success("test data")
        var actionExecuted = false

        // When
        result.onError { actionExecuted = true }

        // Then
        assertTrue(!actionExecuted)
    }

    @Test
    fun `getDataOrNull returns data when result is success`() {
        // Given
        val result: ApiResult<String> = ApiResult.Success("test data")

        // When
        val data = result.getDataOrNull()

        // Then
        assertEquals("test data", data)
    }

    @Test
    fun `getDataOrNull returns null when result is error`() {
        // Given
        val result: ApiResult<String> = ApiResult.Error(ApiException.NetworkException("Network error"))

        // When
        val data = result.getDataOrNull()

        // Then
        assertEquals(null, data)
    }

    @Test
    fun `getErrorOrNull returns exception when result is error`() {
        // Given
        val exception = ApiException.NetworkException("Network error")
        val result: ApiResult<String> = ApiResult.Error(exception)

        // When
        val error = result.getErrorOrNull()

        // Then
        assertEquals(exception, error)
    }

    @Test
    fun `getErrorOrNull returns null when result is success`() {
        // Given
        val result: ApiResult<String> = ApiResult.Success("test data")

        // When
        val error = result.getErrorOrNull()

        // Then
        assertEquals(null, error)
    }
}
