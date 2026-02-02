package com.ora.wellbeing.data.repository

import com.ora.wellbeing.data.local.dao.ContentDao
import com.ora.wellbeing.data.local.dao.UserActivityDao
import com.ora.wellbeing.data.local.entities.Content
import com.ora.wellbeing.data.local.entities.ContentType
import com.ora.wellbeing.data.local.entities.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val contentDao: ContentDao,
    private val userActivityDao: UserActivityDao
) {

    fun getAllContentFlow(): Flow<List<Content>> {
        return contentDao.getAllContentFlow()
    }

    fun getContentByTypeFlow(type: ContentType): Flow<List<Content>> {
        return contentDao.getContentByTypeFlow(type)
    }

    fun getContentByCategoryFlow(category: Category): Flow<List<Content>> {
        return contentDao.getContentByCategoryFlow(category)
    }

    suspend fun getContentById(contentId: String): Content? {
        return contentDao.getContentById(contentId)
    }

    fun searchContentFlow(query: String): Flow<List<Content>> {
        return contentDao.searchContentFlow(query)
    }

    fun getUserFavoritesFlow(userId: String): Flow<List<Content>> {
        return contentDao.getUserFavoritesFlow(userId)
    }
}