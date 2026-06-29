package com.kidstamil.di

import android.content.Context
import androidx.room.Room
import com.kidstamil.data.local.database.KidsTamilDatabase
import com.kidstamil.data.local.database.dao.AchievementDao
import com.kidstamil.data.local.database.dao.ChallengeDao
import com.kidstamil.data.local.database.dao.EconomyDao
import com.kidstamil.data.local.database.dao.GameDao
import com.kidstamil.data.local.database.dao.ProfileDao
import com.kidstamil.data.local.database.dao.StatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KidsTamilDatabase =
        Room.databaseBuilder(context, KidsTamilDatabase::class.java, "kidstamil.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideGameDao(db: KidsTamilDatabase): GameDao = db.gameDao()
    @Provides fun provideStatsDao(db: KidsTamilDatabase): StatsDao = db.statsDao()
    @Provides fun provideAchievementDao(db: KidsTamilDatabase): AchievementDao = db.achievementDao()
    @Provides fun provideChallengeDao(db: KidsTamilDatabase): ChallengeDao = db.challengeDao()
    @Provides fun provideEconomyDao(db: KidsTamilDatabase): EconomyDao = db.economyDao()
    @Provides fun provideProfileDao(db: KidsTamilDatabase): ProfileDao = db.profileDao()
}
