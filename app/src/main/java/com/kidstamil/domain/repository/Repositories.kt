package com.kidstamil.domain.repository

import com.kidstamil.domain.model.Achievement
import com.kidstamil.domain.model.ChallengeRecord
import com.kidstamil.domain.model.ChallengeType
import com.kidstamil.domain.model.KidsTamilGame
import com.kidstamil.domain.model.KidsTamilLevel
import com.kidstamil.domain.model.Difficulty
import com.kidstamil.domain.model.EconomyState
import com.kidstamil.domain.model.PuzzleProfile
import com.kidstamil.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    suspend fun createNewGame(difficulty: Difficulty, levelNumber: Int): KidsTamilGame
    suspend fun createGameFromSeed(seed: Long, levelNumber: Int, difficulty: Difficulty): KidsTamilGame
    suspend fun createTutorialGame(tutorialIndex: Int): KidsTamilGame?
    suspend fun createEndlessGame(wave: Int): KidsTamilGame
    suspend fun saveGame(game: KidsTamilGame): Long
    suspend fun getGame(gameId: Long): KidsTamilGame?
    suspend fun getInProgressGame(): KidsTamilGame?
    fun observeInProgressGame(): Flow<KidsTamilGame?>
    suspend fun completeGame(game: KidsTamilGame): KidsTamilGame
    suspend fun abandonGame(gameId: Long)
    suspend fun getLevel(seed: Long, levelNumber: Int, difficulty: Difficulty): KidsTamilLevel
}

interface ChallengeRepository {
    suspend fun getChallenge(type: ChallengeType, key: String): ChallengeRecord?
    suspend fun createChallenge(type: ChallengeType, key: String, difficulty: Difficulty): ChallengeRecord
    suspend fun resolveActiveChallenge(type: ChallengeType): ChallengeRecord
    fun observeActiveChallenge(type: ChallengeType): Flow<ChallengeRecord?>
    suspend fun completeChallenge(record: ChallengeRecord, timeSeconds: Long, moves: Int): ChallengeRecord
    fun observeChallengeHistory(type: ChallengeType): Flow<List<ChallengeRecord>>
    suspend fun getCurrentStreak(type: ChallengeType): Int
    suspend fun getChallengeGame(record: ChallengeRecord): KidsTamilGame
}

interface ProgressionRepository {
    fun observeStats(): Flow<UserStats>
    suspend fun getStats(): UserStats
    suspend fun updateStatsAfterGame(game: KidsTamilGame)
    suspend fun grantChallengeRewards(rewardCoins: Int, rewardXp: Int)
    fun observePuzzleProfile(): Flow<PuzzleProfile>
    suspend fun getPuzzleProfile(): PuzzleProfile
    fun observeAchievements(): Flow<List<Achievement>>
    suspend fun checkAndUnlockAchievements(
        game: KidsTamilGame,
        sameDevicePlayed: Boolean = false
    ): List<Achievement>
    fun observeEconomy(): Flow<EconomyState>
    suspend fun getEconomy(): EconomyState
    suspend fun spendCoins(amount: Int): Boolean
    suspend fun earnCoins(amount: Int)
    suspend fun unlockTheme(themeId: String): Boolean
}

interface PreferencesRepository {
    fun getUserPreferences(): Flow<com.kidstamil.domain.model.UserPreferences>
    suspend fun updatePreferences(transform: (com.kidstamil.domain.model.UserPreferences) -> com.kidstamil.domain.model.UserPreferences)
    suspend fun getCampaignLevel(difficulty: Difficulty): Int
    suspend fun advanceCampaignLevel(difficulty: Difficulty): Int
}
