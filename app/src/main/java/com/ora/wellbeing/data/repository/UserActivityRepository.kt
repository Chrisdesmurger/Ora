package com.ora.wellbeing.data.repository

import com.ora.wellbeing.data.local.dao.UserActivityDao
import com.ora.wellbeing.data.local.entities.UserActivity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserActivityRepository @Inject constructor(
    private val userActivityDao: UserActivityDao
) {

    fun getUserActivitiesFlow(userId: String): Flow<List<UserActivity>> {
        return userActivityDao.getUserActivitiesFlow(userId)
    }

    suspend fun getRecentUserActivities(userId: String, limit: Int = 20): List<UserActivity> {
        return userActivityDao.getRecentUserActivities(userId, limit)
    }

    fun getCompletedActivitiesFlow(userId: String): Flow<List<UserActivity>> {
        return userActivityDao.getCompletedActivitiesFlow(userId)
    }

    suspend fun insertActivity(activity: UserActivity) {
        userActivityDao.insertActivity(activity)
    }

    suspend fun updateActivity(activity: UserActivity) {
        userActivityDao.updateActivity(activity)
    }

    suspend fun getTotalCompletedSessions(userId: String): Int {
        return userActivityDao.getTotalCompletedSessions(userId)
    }

    suspend fun getTotalMinutesSpent(userId: String): Int {
        return userActivityDao.getTotalMinutesSpent(userId)
    }
}