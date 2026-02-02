package com.ora.wellbeing.data.repository

import com.ora.wellbeing.data.local.dao.UserDao
import com.ora.wellbeing.data.local.entities.ExperienceLevel
import com.ora.wellbeing.data.local.entities.TimeSlot
import com.ora.wellbeing.data.local.entities.User
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class UserRepositoryTest {

    @Mock
    private lateinit var userDao: UserDao

    private lateinit var userRepository: UserRepository

    private val testUser = User(
        id = "test_user_123",
        name = "Test User",
        email = "test@ora.com",
        preferredTimeSlot = TimeSlot.MORNING,
        experienceLevel = ExperienceLevel.BEGINNER,
        goals = listOf("relaxation", "flexibility"),
        createdAt = LocalDateTime.now(),
        lastActiveAt = LocalDateTime.now(),
        isOnboardingCompleted = false,
        notificationsEnabled = true,
        darkModeEnabled = false
    )

    @Before
    fun setup() {
        userRepository = UserRepository(userDao)
    }

    @Test
    fun `getCurrentUser returns user when exists`() = runTest {
        // Given
        whenever(userDao.getCurrentUser()).thenReturn(testUser)

        // When
        val result = userRepository.getCurrentUser()

        // Then
        assertEquals(testUser, result)
        verify(userDao).getCurrentUser()
    }

    @Test
    fun `getCurrentUser returns null when no user exists`() = runTest {
        // Given
        whenever(userDao.getCurrentUser()).thenReturn(null)

        // When
        val result = userRepository.getCurrentUser()

        // Then
        assertNull(result)
        verify(userDao).getCurrentUser()
    }

    @Test
    fun `getCurrentUserFlow returns flow of user`() = runTest {
        // Given
        whenever(userDao.getCurrentUserFlow()).thenReturn(flowOf(testUser))

        // When
        val flow = userRepository.getCurrentUserFlow()

        // Then
        flow.collect { user ->
            assertEquals(testUser, user)
        }
        verify(userDao).getCurrentUserFlow()
    }

    @Test
    fun `createUser calls dao insertUser`() = runTest {
        // When
        userRepository.createUser(testUser)

        // Then
        verify(userDao).insertUser(testUser)
    }

    @Test
    fun `updateUser calls dao updateUser`() = runTest {
        // When
        userRepository.updateUser(testUser)

        // Then
        verify(userDao).updateUser(testUser)
    }

    @Test
    fun `updateLastActiveTime calls dao with current time`() = runTest {
        // When
        userRepository.updateLastActiveTime(testUser.id)

        // Then
        verify(userDao).updateLastActiveTime(any(), any())
    }

    @Test
    fun `completeOnboarding calls dao updateOnboardingStatus with true`() = runTest {
        // When
        userRepository.completeOnboarding(testUser.id)

        // Then
        verify(userDao).updateOnboardingStatus(testUser.id, true)
    }

    @Test
    fun `updateNotificationSettings calls dao updateNotificationSettings`() = runTest {
        // Given
        val enabled = true

        // When
        userRepository.updateNotificationSettings(testUser.id, enabled)

        // Then
        verify(userDao).updateNotificationSettings(testUser.id, enabled)
    }

    @Test
    fun `updateDarkModeSettings calls dao updateDarkModeSettings`() = runTest {
        // Given
        val enabled = true

        // When
        userRepository.updateDarkModeSettings(testUser.id, enabled)

        // Then
        verify(userDao).updateDarkModeSettings(testUser.id, enabled)
    }

    @Test
    fun `deleteUser calls dao deleteUserById`() = runTest {
        // When
        userRepository.deleteUser(testUser.id)

        // Then
        verify(userDao).deleteUserById(testUser.id)
    }

    @Test
    fun `isUserLoggedIn returns true when user exists`() = runTest {
        // Given
        whenever(userDao.getCurrentUser()).thenReturn(testUser)

        // When
        val result = userRepository.isUserLoggedIn()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isUserLoggedIn returns false when no user exists`() = runTest {
        // Given
        whenever(userDao.getCurrentUser()).thenReturn(null)

        // When
        val result = userRepository.isUserLoggedIn()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isOnboardingCompleted returns true when user completed onboarding`() = runTest {
        // Given
        val completedUser = testUser.copy(isOnboardingCompleted = true)
        whenever(userDao.getCurrentUser()).thenReturn(completedUser)

        // When
        val result = userRepository.isOnboardingCompleted()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isOnboardingCompleted returns false when user not completed onboarding`() = runTest {
        // Given
        whenever(userDao.getCurrentUser()).thenReturn(testUser)

        // When
        val result = userRepository.isOnboardingCompleted()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isOnboardingCompleted returns false when no user exists`() = runTest {
        // Given
        whenever(userDao.getCurrentUser()).thenReturn(null)

        // When
        val result = userRepository.isOnboardingCompleted()

        // Then
        assertFalse(result)
    }

    @Test
    fun `getUserById returns correct user`() = runTest {
        // Given
        whenever(userDao.getUserById(testUser.id)).thenReturn(testUser)

        // When
        val result = userRepository.getUserById(testUser.id)

        // Then
        assertEquals(testUser, result)
        verify(userDao).getUserById(testUser.id)
    }

    @Test
    fun `getUserByIdFlow returns flow of correct user`() = runTest {
        // Given
        whenever(userDao.getUserByIdFlow(testUser.id)).thenReturn(flowOf(testUser))

        // When
        val flow = userRepository.getUserByIdFlow(testUser.id)

        // Then
        flow.collect { user ->
            assertEquals(testUser, user)
        }
        verify(userDao).getUserByIdFlow(testUser.id)
    }
}