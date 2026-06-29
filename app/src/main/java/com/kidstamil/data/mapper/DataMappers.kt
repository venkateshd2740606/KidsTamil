package com.kidstamil.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kidstamil.data.local.database.entity.AchievementEntity
import com.kidstamil.data.local.database.entity.ChallengeEntity
import com.kidstamil.data.local.database.entity.EconomyEntity
import com.kidstamil.data.local.database.entity.GameEntity
import com.kidstamil.data.local.database.entity.ProfileEntity
import com.kidstamil.data.local.database.entity.StatsEntity
import com.kidstamil.domain.model.TamilAlphabetEntry
import com.kidstamil.domain.model.PuzzleArchetype
import com.kidstamil.domain.model.PuzzleProfile
import com.kidstamil.domain.model.PuzzleProfileMetrics
import com.kidstamil.domain.model.SkillCategory
import com.kidstamil.domain.model.Achievement
import com.kidstamil.domain.model.ChallengeRecord
import com.kidstamil.domain.model.ChallengeType
import com.kidstamil.domain.model.KidsTamilGame
import com.kidstamil.domain.model.KidsTamilLevel
import com.kidstamil.domain.model.LearningStepMode
import com.kidstamil.domain.model.TracePoint
import com.kidstamil.domain.model.Difficulty
import com.kidstamil.domain.model.EconomyState
import com.kidstamil.domain.model.GameStatus
import com.kidstamil.domain.model.UserStats

object DataMappers {
    private val gson = Gson()
    private val tracePointListType = object : TypeToken<List<TracePoint>>() {}.type
    private val stepModeListType = object : TypeToken<List<LearningStepMode>>() {}.type
    private val stringListType = object : TypeToken<List<String>>() {}.type

    fun toEntity(game: KidsTamilGame): GameEntity {
        val gameState = GameStateJson(
            currentStepIndex = game.currentStepIndex,
            tracePoints = game.tracePoints,
            traceCompleted = game.traceCompleted,
            quizSelectedIndex = game.quizSelectedIndex,
            quizAnswered = game.quizAnswered,
            eliminatedQuizOptions = game.eliminatedQuizOptions.toList(),
            score = game.score,
            streak = game.streak,
            bestStreak = game.bestStreak,
            awaitingAdvance = game.awaitingAdvance,
            lastAnswerCorrect = game.lastAnswerCorrect
        )
        return GameEntity(
            id = game.id,
            seed = game.level.seed,
            levelNumber = game.level.levelNumber,
            difficulty = game.level.difficulty.name,
            status = game.status.name,
            tubeStateJson = gson.toJson(gameState),
            selectedTubeId = game.currentStepIndex,
            moves = game.moves,
            hintsUsed = game.hintsUsed,
            elapsedSeconds = game.elapsedSeconds,
            createdAt = game.createdAt,
            lastPlayedAt = game.lastPlayedAt,
            completedAt = game.completedAt,
            isTutorial = game.level.isTutorial,
            isEndless = game.level.isEndless,
            challengeType = game.level.challengeType?.name,
            challengeKey = game.level.challengeKey,
            levelJson = gson.toJson(toLevelJson(game.level)),
            coinsEarned = game.coinsEarned,
            xpEarned = game.xpEarned
        )
    }

    fun fromEntity(entity: GameEntity): KidsTamilGame {
        val levelJson = gson.fromJson(entity.levelJson, LevelJson::class.java)
        val gameState = runCatching {
            gson.fromJson(entity.tubeStateJson, GameStateJson::class.java)
        }.getOrNull()
        val level = fromLevelJson(entity, levelJson)

        return if (gameState != null) {
            KidsTamilGame(
                id = entity.id,
                level = level,
                status = GameStatus.valueOf(entity.status),
                currentStepIndex = gameState.currentStepIndex,
                tracePoints = gameState.tracePoints,
                traceCompleted = gameState.traceCompleted,
                quizSelectedIndex = gameState.quizSelectedIndex,
                quizAnswered = gameState.quizAnswered,
                eliminatedQuizOptions = gameState.eliminatedQuizOptions.toSet(),
                score = gameState.score,
                streak = gameState.streak,
                bestStreak = gameState.bestStreak,
                awaitingAdvance = gameState.awaitingAdvance,
                lastAnswerCorrect = gameState.lastAnswerCorrect,
                hintsUsed = entity.hintsUsed,
                moves = entity.moves,
                elapsedSeconds = entity.elapsedSeconds,
                createdAt = entity.createdAt,
                lastPlayedAt = entity.lastPlayedAt,
                completedAt = entity.completedAt,
                coinsEarned = entity.coinsEarned,
                xpEarned = entity.xpEarned
            )
        } else {
            KidsTamilGame(
                id = entity.id,
                level = level,
                status = GameStatus.valueOf(entity.status),
                currentStepIndex = entity.selectedTubeId.coerceAtLeast(0),
                hintsUsed = entity.hintsUsed,
                moves = entity.moves,
                elapsedSeconds = entity.elapsedSeconds,
                createdAt = entity.createdAt,
                lastPlayedAt = entity.lastPlayedAt,
                completedAt = entity.completedAt,
                coinsEarned = entity.coinsEarned,
                xpEarned = entity.xpEarned
            )
        }
    }

    private fun toLevelJson(level: KidsTamilLevel): LevelJson = LevelJson(
        letterIndex = level.letterIndex,
        entry = level.entry,
        steps = level.steps,
        quizOptions = level.quizOptions,
        quizCorrectIndex = level.quizCorrectIndex,
        quizPrompt = level.quizPrompt,
        isReview = level.isReview
    )

    private fun fromLevelJson(entity: GameEntity, levelJson: LevelJson?): KidsTamilLevel {
        if (levelJson == null) {
            return com.kidstamil.engine.KidsTamilGenerator.generate(
                entity.seed,
                entity.levelNumber,
                Difficulty.valueOf(entity.difficulty)
            )
        }
        return KidsTamilLevel(
            id = entity.id,
            seed = entity.seed,
            levelNumber = entity.levelNumber,
            difficulty = Difficulty.valueOf(entity.difficulty),
            letterIndex = levelJson.letterIndex,
            entry = levelJson.entry,
            steps = levelJson.steps,
            quizOptions = levelJson.quizOptions,
            quizCorrectIndex = levelJson.quizCorrectIndex,
            quizPrompt = levelJson.quizPrompt,
            isTutorial = entity.isTutorial,
            isEndless = entity.isEndless,
            isReview = levelJson.isReview,
            challengeType = entity.challengeType?.let { ChallengeType.valueOf(it) },
            challengeKey = entity.challengeKey
        )
    }

    fun toStatsEntity(stats: UserStats): StatsEntity = StatsEntity(
        gamesPlayed = stats.gamesPlayed,
        gamesWon = stats.gamesWon,
        gamesAbandoned = stats.gamesAbandoned,
        totalPlayTimeSeconds = stats.totalPlayTimeSeconds,
        fastestTimeBeginner = stats.fastestTimeBeginner,
        fastestTimeEasy = stats.fastestTimeEasy,
        fastestTimeMedium = stats.fastestTimeMedium,
        fastestTimeHard = stats.fastestTimeHard,
        fastestTimeExpert = stats.fastestTimeExpert,
        fastestTimeMaster = stats.fastestTimeMaster,
        currentStreak = stats.currentStreak,
        longestStreak = stats.longestStreak,
        lastPlayedDate = stats.lastPlayedDate,
        xpPoints = stats.xpPoints,
        level = stats.level,
        hintsUsedTotal = stats.hintsUsedTotal,
        perfectGames = stats.perfectGames,
        poursTotal = stats.poursTotal,
        endlessHighScore = stats.endlessHighScore
    )

    fun fromStatsEntity(entity: StatsEntity?): UserStats {
        if (entity == null) return UserStats()
        return UserStats(
            gamesPlayed = entity.gamesPlayed,
            gamesWon = entity.gamesWon,
            gamesAbandoned = entity.gamesAbandoned,
            totalPlayTimeSeconds = entity.totalPlayTimeSeconds,
            fastestTimeBeginner = entity.fastestTimeBeginner,
            fastestTimeEasy = entity.fastestTimeEasy,
            fastestTimeMedium = entity.fastestTimeMedium,
            fastestTimeHard = entity.fastestTimeHard,
            fastestTimeExpert = entity.fastestTimeExpert,
            fastestTimeMaster = entity.fastestTimeMaster,
            currentStreak = entity.currentStreak,
            longestStreak = entity.longestStreak,
            lastPlayedDate = entity.lastPlayedDate,
            xpPoints = entity.xpPoints,
            level = entity.level,
            hintsUsedTotal = entity.hintsUsedTotal,
            perfectGames = entity.perfectGames,
            poursTotal = entity.poursTotal,
            endlessHighScore = entity.endlessHighScore
        )
    }

    fun toChallengeEntity(record: ChallengeRecord): ChallengeEntity = ChallengeEntity(
        key = record.key,
        type = record.type.name,
        seed = record.seed,
        difficulty = record.difficulty.name,
        isCompleted = record.isCompleted,
        completionTime = record.completionTime,
        moves = record.moves,
        rewardCoins = record.rewardCoins,
        rewardXp = record.rewardXp,
        streakDay = record.streakDay
    )

    fun fromChallengeEntity(entity: ChallengeEntity): ChallengeRecord = ChallengeRecord(
        key = entity.key,
        type = ChallengeType.valueOf(entity.type),
        seed = entity.seed,
        difficulty = Difficulty.valueOf(entity.difficulty),
        isCompleted = entity.isCompleted,
        completionTime = entity.completionTime,
        moves = entity.moves,
        rewardCoins = entity.rewardCoins,
        rewardXp = entity.rewardXp,
        streakDay = entity.streakDay
    )

    fun toEconomyEntity(state: EconomyState): EconomyEntity = EconomyEntity(
        coins = state.coins,
        totalCoinsEarned = state.totalCoinsEarned,
        totalCoinsSpent = state.totalCoinsSpent,
        unlockedThemes = gson.toJson(state.unlockedThemeIds.toList())
    )

    fun fromEconomyEntity(entity: EconomyEntity?): EconomyState {
        if (entity == null) return EconomyState()
        val type = object : TypeToken<List<String>>() {}.type
        val unlocked: List<String> = gson.fromJson(entity.unlockedThemes, type) ?: emptyList()
        return EconomyState(
            coins = entity.coins,
            totalCoinsEarned = entity.totalCoinsEarned,
            totalCoinsSpent = entity.totalCoinsSpent,
            unlockedThemeIds = unlocked.toSet()
        )
    }

    fun mergeAchievement(def: Achievement, entity: AchievementEntity?): Achievement =
        def.copy(
            isUnlocked = entity?.isUnlocked ?: false,
            unlockedAt = entity?.unlockedAt,
            progress = entity?.progress ?: 0
        )

    fun toProfileEntity(profile: PuzzleProfile): ProfileEntity = ProfileEntity(
        gamesAnalyzed = profile.metrics.gamesAnalyzed,
        totalSolveTimeSeconds = profile.metrics.totalSolveTimeSeconds,
        totalMoves = profile.metrics.totalMoves,
        totalOptimalMoves = profile.metrics.totalOptimalMoves,
        totalHintsUsed = profile.metrics.totalHintsUsed,
        fastCompletions = profile.metrics.fastCompletions,
        slowCompletions = profile.metrics.slowCompletions,
        perfectCompletions = profile.metrics.perfectCompletions,
        complexChainWins = profile.metrics.complexChainWins,
        inefficientWins = profile.metrics.inefficientWins,
        hintHeavyWins = profile.metrics.hintHeavyWins,
        archetype = profile.archetype.name,
        strength = profile.strength.name,
        weakness = profile.weakness.name,
        adaptiveColorModifier = profile.adaptiveColorModifier
    )

    fun fromProfileEntity(entity: ProfileEntity?): PuzzleProfile {
        if (entity == null) return PuzzleProfile()
        val metrics = PuzzleProfileMetrics(
            gamesAnalyzed = entity.gamesAnalyzed,
            totalSolveTimeSeconds = entity.totalSolveTimeSeconds,
            totalMoves = entity.totalMoves,
            totalOptimalMoves = entity.totalOptimalMoves,
            totalHintsUsed = entity.totalHintsUsed,
            fastCompletions = entity.fastCompletions,
            slowCompletions = entity.slowCompletions,
            perfectCompletions = entity.perfectCompletions,
            complexChainWins = entity.complexChainWins,
            inefficientWins = entity.inefficientWins,
            hintHeavyWins = entity.hintHeavyWins
        )
        return PuzzleProfile(
            metrics = metrics,
            archetype = runCatching { PuzzleArchetype.valueOf(entity.archetype) }
                .getOrDefault(PuzzleArchetype.EXPLORER),
            strength = runCatching { SkillCategory.valueOf(entity.strength) }
                .getOrDefault(SkillCategory.PATTERN_RECOGNITION),
            weakness = runCatching { SkillCategory.valueOf(entity.weakness) }
                .getOrDefault(SkillCategory.TIME_PRESSURE),
            adaptiveColorModifier = entity.adaptiveColorModifier
        )
    }

    data class LevelJson(
        val letterIndex: Int,
        val entry: TamilAlphabetEntry,
        val steps: List<LearningStepMode>,
        val quizOptions: List<String>,
        val quizCorrectIndex: Int,
        val quizPrompt: String,
        val isReview: Boolean = false
    )

    data class GameStateJson(
        val currentStepIndex: Int = 0,
        val tracePoints: List<TracePoint> = emptyList(),
        val traceCompleted: Boolean = false,
        val quizSelectedIndex: Int? = null,
        val quizAnswered: Boolean = false,
        val eliminatedQuizOptions: List<Int> = emptyList(),
        val score: Int = 0,
        val streak: Int = 0,
        val bestStreak: Int = 0,
        val awaitingAdvance: Boolean = false,
        val lastAnswerCorrect: Boolean? = null
    )
}
