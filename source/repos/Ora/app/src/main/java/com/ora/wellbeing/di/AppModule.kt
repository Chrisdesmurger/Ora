package com.ora.wellbeing.di

import android.content.Context
import androidx.work.WorkManager
import com.ora.wellbeing.data.player.VideoPlayerManager
import com.ora.wellbeing.data.repository.OraRepository
import com.ora.wellbeing.data.repository.OraRepositoryImpl
import com.ora.wellbeing.data.worker.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideVideoPlayerManager(@ApplicationContext context: Context): VideoPlayerManager {
        return VideoPlayerManager(context)
    }

    @Provides
    @Singleton
    fun provideReminderScheduler(@ApplicationContext context: Context): ReminderScheduler {
        return ReminderScheduler(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class OraRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOraRepository(
        oraRepositoryImpl: OraRepositoryImpl
    ): OraRepository
}