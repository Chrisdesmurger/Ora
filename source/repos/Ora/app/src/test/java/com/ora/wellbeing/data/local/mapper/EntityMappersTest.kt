package com.ora.wellbeing.data.local.mapper

import com.ora.wellbeing.data.local.entities.User
import com.ora.wellbeing.data.local.entities.UserStats as UserStatsEntity
import com.ora.wellbeing.data.model.UserProfile
import com.ora.wellbeing.data.model.UserStats
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.util.Date

/**
 * Unit tests for Entity Mappers
 * Validates bidirectional conversion between Room entities and Firestore models
 */
class EntityMappersTest {

    @Test
    fun `UserProfile to User entity mapping should work correctly`() {
        // Given
        val userProfile = UserProfile().apply {
            uid = "test_uid_123"
            firstName = "John"
            lastName = "Doe"
            email = "john.doe@example.com"
            createdAt = Date()
            updatedAt = Date()
        }

        // When
        val userEntity = userProfile.toEntity()

        // Then
        assertThat(userEntity.id).isEqualTo("test_uid_123")
        assertThat(userEntity.name).isEqualTo("John Doe")
        assertThat(userEntity.email).isEqualTo("john.doe@example.com")
        assertThat(userEntity.isOnboardingCompleted).isTrue()
    }

    @Test
    fun `UserProfile with null names should map to Guest`() {
        // Given
        val userProfile = UserProfile().apply {
            uid = "test_uid_123"
            firstName = null
            lastName = null
            email = "test@example.com"
        }

        // When
        val userEntity = userProfile.toEntity()

        // Then
        assertThat(userEntity.name).isEqualTo("Guest")
    }

    @Test
    fun `UserProfile with only firstName should map correctly`() {
        // Given
        val userProfile = UserProfile().apply {
            uid = "test_uid_123"
            firstName = "Alice"
            lastName = null
            email = "alice@example.com"
        }

        // When
        val userEntity = userProfile.toEntity()

        // Then
        assertThat(userEntity.name).isEqualTo("Alice")
    }

    @Test
    fun `User entity to UserProfile mapping should work correctly`() {
        // Given
        val userEntity = User(
            id = "test_uid_456",
            name = "Jane Smith",
            email = "jane.smith@example.com",
            createdAt = LocalDateTime.now(),
            lastActiveAt = LocalDateTime.now()
        )

        // When
        val userProfile = userEntity.toFirestoreProfile()

        // Then
        assertThat(userProfile.uid).isEqualTo("test_uid_456")
        assertThat(userProfile.firstName).isEqualTo("Jane")
        assertThat(userProfile.lastName).isEqualTo("Smith")
        assertThat(userProfile.email).isEqualTo("jane.smith@example.com")
    }

    @Test
    fun `User entity with single word name should map to firstName only`() {
        // Given
        val userEntity = User(
            id = "test_uid_789",
            name = "Madonna",
            email = "madonna@example.com"
        )

        // When
        val userProfile = userEntity.toFirestoreProfile()

        // Then
        assertThat(userProfile.firstName).isEqualTo("Madonna")
        assertThat(userProfile.lastName).isNull()
    }

    @Test
    fun `UserStats to UserStatsEntity mapping should preserve all fields`() {
        // Given
        val userStats = UserStats().apply {
            totalSessions = 42
            totalMinutes = 1260
            currentStreak = 7
            longestStreak = 14
            lastSessionDate = Date()
            favoritesCount = 5
            completedProgramsCount = 3
            level = 5
            xp = 1500
            badges = listOf("early_bird", "week_warrior", "zen_master")
        }

        // When
        val statsEntity = userStats.toEntity("user_123")

        // Then
        assertThat(statsEntity.userId).isEqualTo("user_123")
        assertThat(statsEntity.totalSessions).isEqualTo(42)
        assertThat(statsEntity.totalMinutes).isEqualTo(1260)
        assertThat(statsEntity.currentStreak).isEqualTo(7)
        assertThat(statsEntity.longestStreak).isEqualTo(14)
        assertThat(statsEntity.favoritesCount).isEqualTo(5)
        assertThat(statsEntity.completedProgramsCount).isEqualTo(3)
        assertThat(statsEntity.level).isEqualTo(5)
        assertThat(statsEntity.xp).isEqualTo(1500)
        assertThat(statsEntity.badges).containsExactly("early_bird", "week_warrior", "zen_master")
    }

    @Test
    fun `UserStatsEntity to UserStats mapping should preserve all fields`() {
        // Given
        val statsEntity = UserStatsEntity(
            userId = "user_456",
            totalSessions = 100,
            totalMinutes = 3000,
            currentStreak = 10,
            longestStreak = 20,
            lastSessionDate = LocalDateTime.now(),
            favoritesCount = 8,
            completedProgramsCount = 6,
            level = 10,
            xp = 5000,
            badges = listOf("meditation_master", "yogi"),
            updatedAt = LocalDateTime.now()
        )

        // When
        val userStats = statsEntity.toFirestoreStats()

        // Then
        assertThat(userStats.totalSessions).isEqualTo(100)
        assertThat(userStats.totalMinutes).isEqualTo(3000)
        assertThat(userStats.currentStreak).isEqualTo(10)
        assertThat(userStats.longestStreak).isEqualTo(20)
        assertThat(userStats.favoritesCount).isEqualTo(8)
        assertThat(userStats.completedProgramsCount).isEqualTo(6)
        assertThat(userStats.level).isEqualTo(10)
        assertThat(userStats.xp).isEqualTo(5000)
        assertThat(userStats.badges).containsExactly("meditation_master", "yogi")
    }

    @Test
    fun `Bidirectional UserProfile mapping should be consistent`() {
        // Given
        val originalProfile = UserProfile().apply {
            uid = "bidirectional_test"
            firstName = "Test"
            lastName = "User"
            email = "test@example.com"
            createdAt = Date()
            updatedAt = Date()
        }

        // When: Profile -> Entity -> Profile
        val entity = originalProfile.toEntity()
        val convertedProfile = entity.toFirestoreProfile()

        // Then
        assertThat(convertedProfile.uid).isEqualTo(originalProfile.uid)
        assertThat(convertedProfile.firstName).isEqualTo(originalProfile.firstName)
        assertThat(convertedProfile.lastName).isEqualTo(originalProfile.lastName)
        assertThat(convertedProfile.email).isEqualTo(originalProfile.email)
    }

    @Test
    fun `Bidirectional UserStats mapping should be consistent`() {
        // Given
        val originalStats = UserStats().apply {
            totalSessions = 50
            totalMinutes = 1500
            currentStreak = 5
            longestStreak = 10
            level = 7
            xp = 2500
        }

        // When: Stats -> Entity -> Stats
        val entity = originalStats.toEntity("test_user")
        val convertedStats = entity.toFirestoreStats()

        // Then
        assertThat(convertedStats.totalSessions).isEqualTo(originalStats.totalSessions)
        assertThat(convertedStats.totalMinutes).isEqualTo(originalStats.totalMinutes)
        assertThat(convertedStats.currentStreak).isEqualTo(originalStats.currentStreak)
        assertThat(convertedStats.longestStreak).isEqualTo(originalStats.longestStreak)
        assertThat(convertedStats.level).isEqualTo(originalStats.level)
        assertThat(convertedStats.xp).isEqualTo(originalStats.xp)
    }
}
