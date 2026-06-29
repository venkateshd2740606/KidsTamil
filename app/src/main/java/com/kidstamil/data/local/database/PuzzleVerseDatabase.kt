package com.kidstamil.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kidstamil.data.local.database.dao.AchievementDao
import com.kidstamil.data.local.database.dao.ChallengeDao
import com.kidstamil.data.local.database.dao.EconomyDao
import com.kidstamil.data.local.database.dao.GameDao
import com.kidstamil.data.local.database.dao.ProfileDao
import com.kidstamil.data.local.database.dao.StatsDao
import com.kidstamil.data.local.database.entity.ProfileEntity
import com.kidstamil.data.local.database.entity.AchievementEntity
import com.kidstamil.data.local.database.entity.ChallengeEntity
import com.kidstamil.data.local.database.entity.EconomyEntity
import com.kidstamil.data.local.database.entity.GameEntity
import com.kidstamil.data.local.database.entity.StatsEntity

@Database(
    entities = [
        GameEntity::class,
        StatsEntity::class,
        AchievementEntity::class,
        ChallengeEntity::class,
        EconomyEntity::class,
        ProfileEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class KidsTamilDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun statsDao(): StatsDao
    abstract fun achievementDao(): AchievementDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun economyDao(): EconomyDao
    abstract fun profileDao(): ProfileDao
}
