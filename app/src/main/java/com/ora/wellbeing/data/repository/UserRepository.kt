package com.ora.wellbeing.data.repository

import com.ora.wellbeing.data.local.dao.UserDao
import com.ora.wellbeing.data.local.entities.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    fun getCurrentUserFlow(): Flow<User?> = userDao.getCurrentUserFlow()

    suspend fun getCurrentUser(): User? = userDao.getCurrentUser()

    suspend fun getUserById(userId: String): User? = userDao.getUserById(userId)

    fun getUserByIdFlow(userId: String): Flow<User?> = userDao.getUserByIdFlow(userId)

    suspend fun createUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun updateLastActiveTime(userId: String) {
        userDao.updateLastActiveTime(userId, LocalDateTime.now())
    }

    suspend fun completeOnboarding(userId: String) {
        userDao.updateOnboardingStatus(userId, true)
    }

    suspend fun updateNotificationSettings(userId: String, enabled: Boolean) {
        userDao.updateNotificationSettings(userId, enabled)
    }

    suspend fun updateDarkModeSettings(userId: String, enabled: Boolean) {
        userDao.updateDarkModeSettings(userId, enabled)
    }

    suspend fun deleteUser(userId: String) {
        userDao.deleteUserById(userId)
    }

    suspend fun isUserLoggedIn(): Boolean {
        return getCurrentUser() != null
    }

    suspend fun isOnboardingCompleted(): Boolean {
        return getCurrentUser()?.isOnboardingCompleted ?: false
    }
}