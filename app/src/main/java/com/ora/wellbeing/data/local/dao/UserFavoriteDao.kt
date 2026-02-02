package com.ora.wellbeing.data.local.dao

import androidx.room.*
import com.ora.wellbeing.data.local.entities.UserFavorite
import kotlinx.coroutines.flow.Flow

@Dao
interface UserFavoriteDao {

    @Query("SELECT * FROM user_favorites WHERE userId = :userId")
    fun getUserFavorites(userId: String): Flow<List<UserFavorite>>

    @Query("SELECT * FROM user_favorites WHERE userId = :userId AND contentId = :contentId LIMIT 1")
    suspend fun getFavorite(userId: String, contentId: String): UserFavorite?

    @Query("SELECT EXISTS(SELECT 1 FROM user_favorites WHERE userId = :userId AND contentId = :contentId)")
    suspend fun isFavorite(userId: String, contentId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: UserFavorite)

    @Delete
    suspend fun deleteFavorite(favorite: UserFavorite)

    @Query("DELETE FROM user_favorites WHERE userId = :userId AND contentId = :contentId")
    suspend fun removeFavorite(userId: String, contentId: String)

    @Query("DELETE FROM user_favorites WHERE userId = :userId")
    suspend fun deleteUserFavorites(userId: String)
}