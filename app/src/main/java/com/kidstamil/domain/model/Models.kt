package com.kidstamil.domain.model

enum class Difficulty(val stars: Int, val xpMultiplier: Float) {
    BEGINNER(1, 0.5f),
    EASY(2, 1.0f),
    MEDIUM(3, 1.5f),
    HARD(4, 2.0f),
    EXPERT(5, 2.5f),
    MASTER(6, 3.0f),
    ENDLESS(0, 1.0f);

    val displayName: String get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class GameStatus { IN_PROGRESS, COMPLETED, FAILED, PAUSED }

enum class AppTheme(val displayName: String) {
    SYSTEM("System"), LIGHT("Light"), DARK("Dark"), AMOLED("AMOLED"),
    NEON("Neon"), CYBER("Cyber"), SPACE("Space"), NATURE("Nature")
}

enum class ColorBlindMode { NONE, DEUTERANOPIA, PROTANOPIA, TRITANOPIA }
enum class ChallengeType { DAILY, WEEKLY, MONTHLY }

enum class PlayerRank(val minLevel: Int, val title: String) {
    NOVICE(1, "Novice"), APPRENTICE(5, "Apprentice"), PUZZLER(10, "Puzzler"),
    STRATEGIST(20, "Strategist"), EXPERT(35, "Expert"), MASTER(50, "Master"),
    GRANDMASTER(75, "Grandmaster"), LEGEND(100, "Legend");

    companion object {
        fun fromLevel(level: Int): PlayerRank =
            entries.filter { level >= it.minLevel }.maxByOrNull { it.minLevel } ?: NOVICE
    }
}

enum class MultiplayerMode { SAME_DEVICE, LOCAL_P2P }

enum class P2PConnectionType(val displayName: String) {
    NEARBY("Nearby (Bluetooth + Wi-Fi)"),
    WIFI_LAN("Same Wi-Fi"),
    WIFI_DIRECT("Wi-Fi Direct")
}

enum class P2PRole { HOST, CLIENT }

enum class P2PSessionState {
    IDLE, ADVERTISING, DISCOVERING, CONNECTING, CONNECTED, ERROR
}

enum class LearningStepMode { LEARN, TRACE, QUIZ }

enum class PuzzleArchetype(val title: String, val description: String) {
    ARCHITECT("The Architect", "You plan ahead and learn steadily."),
    SPRINTER("The Sprinter", "You move quickly through lessons."),
    ANALYST("The Analyst", "You take your time and use hints when needed."),
    EXPLORER("The Explorer", "You explore each letter with curiosity."),
    STRATEGIST("The Strategist", "You excel across letters and quiz challenges.")
}

enum class SkillCategory(val label: String) {
    PATTERN_RECOGNITION("Pattern Recognition"), PLANNING("Planning"), SPEED("Speed"),
    ACCURACY("Accuracy"), COMPLEX_CHAINS("Complex Chains"), TIME_PRESSURE("Time Pressure")
}

data class PuzzleProfileMetrics(
    val gamesAnalyzed: Int = 0,
    val totalSolveTimeSeconds: Long = 0,
    val totalMoves: Int = 0,
    val totalOptimalMoves: Int = 0,
    val totalHintsUsed: Int = 0,
    val fastCompletions: Int = 0,
    val slowCompletions: Int = 0,
    val perfectCompletions: Int = 0,
    val complexChainWins: Int = 0,
    val inefficientWins: Int = 0,
    val hintHeavyWins: Int = 0
)

data class PuzzleProfile(
    val metrics: PuzzleProfileMetrics = PuzzleProfileMetrics(),
    val archetype: PuzzleArchetype = PuzzleArchetype.EXPLORER,
    val strength: SkillCategory = SkillCategory.PATTERN_RECOGNITION,
    val weakness: SkillCategory = SkillCategory.TIME_PRESSURE,
    val adaptiveColorModifier: Int = 0
)

data class GenerationProfile(val letterOffsetModifier: Int = 0)

data class TracePoint(val x: Float, val y: Float)

data class TamilAlphabetEntry(
    val letter: String,
    val romanization: String,
    val exampleWord: String,
    val meaning: String,
    val emoji: String,
    val quizDistractors: List<String>
)

data class KidsTamilLevel(
    val id: Long = 0,
    val seed: Long,
    val levelNumber: Int,
    val difficulty: Difficulty,
    val letterIndex: Int,
    val entry: TamilAlphabetEntry,
    val steps: List<LearningStepMode> = listOf(
        LearningStepMode.LEARN, LearningStepMode.TRACE, LearningStepMode.QUIZ
    ),
    val quizOptions: List<String>,
    val quizCorrectIndex: Int,
    val quizPrompt: String,
    val isTutorial: Boolean = false,
    val isEndless: Boolean = false,
    val isReview: Boolean = false,
    val challengeType: ChallengeType? = null,
    val challengeKey: String? = null
) {
    val letter: String get() = entry.letter
    val stepCount: Int get() = steps.size

    companion object {
        const val CAMPAIGN_LEVELS = 26
        const val OPTIONS_PER_QUIZ = 4
        const val LETTER_COUNT = 12
    }
}

data class KidsTamilGame(
    val id: Long = 0,
    val level: KidsTamilLevel,
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val currentStepIndex: Int = 0,
    val tracePoints: List<TracePoint> = emptyList(),
    val traceCompleted: Boolean = false,
    val quizSelectedIndex: Int? = null,
    val quizAnswered: Boolean = false,
    val eliminatedQuizOptions: Set<Int> = emptySet(),
    val score: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val hintsUsed: Int = 0,
    val moves: Int = 0,
    val elapsedSeconds: Long = 0,
    val awaitingAdvance: Boolean = false,
    val lastAnswerCorrect: Boolean? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val coinsEarned: Int = 0,
    val xpEarned: Int = 0
) {
    val isCompleted: Boolean get() = status == GameStatus.COMPLETED
    val currentStep: LearningStepMode? get() = level.steps.getOrNull(currentStepIndex)
    val completionTimeFormatted: String get() {
        val mins = elapsedSeconds / 60
        val secs = elapsedSeconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}

data class UserStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val gamesAbandoned: Int = 0,
    val totalPlayTimeSeconds: Long = 0,
    val fastestTimeBeginner: Long = Long.MAX_VALUE,
    val fastestTimeEasy: Long = Long.MAX_VALUE,
    val fastestTimeMedium: Long = Long.MAX_VALUE,
    val fastestTimeHard: Long = Long.MAX_VALUE,
    val fastestTimeExpert: Long = Long.MAX_VALUE,
    val fastestTimeMaster: Long = Long.MAX_VALUE,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastPlayedDate: String = "",
    val xpPoints: Long = 0,
    val level: Int = 1,
    val hintsUsedTotal: Int = 0,
    val perfectGames: Int = 0,
    val poursTotal: Int = 0,
    val endlessHighScore: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val bestQuizScore: Int = 0
) {
    val winRate: Float get() = if (gamesPlayed > 0) gamesWon.toFloat() / gamesPlayed else 0f
    val rank: PlayerRank get() = PlayerRank.fromLevel(level)
    val accuracyRate: Float get() = if (poursTotal > 0) totalCorrectAnswers.toFloat() / poursTotal else 0f
}

data class Achievement(
    val id: String, val titleRes: Int, val descriptionRes: Int, val iconName: String,
    val xpReward: Int, val coinReward: Int = 0, val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null, val progress: Int = 0, val maxProgress: Int = 1
)

data class ChallengeRecord(
    val key: String, val type: ChallengeType, val seed: Long, val difficulty: Difficulty,
    val isCompleted: Boolean = false, val completionTime: Long? = null, val moves: Int = 0,
    val rewardCoins: Int = 0, val rewardXp: Int = 0, val streakDay: Int = 0
)

data class UserPreferences(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val hapticFeedback: Boolean = true,
    val soundEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val highContrastMode: Boolean = false,
    val colorBlindMode: ColorBlindMode = ColorBlindMode.NONE,
    val fontScale: Float = 1.0f,
    val timerVisible: Boolean = true,
    val adsEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val consentGiven: Boolean = false,
    val analyticsEnabled: Boolean = true,
    val personalizedAds: Boolean = false,
    val language: String = "system",
    val unlockedThemes: Set<String> = setOf(AppTheme.SYSTEM.name, AppTheme.LIGHT.name, AppTheme.DARK.name)
)

data class EconomyState(
    val coins: Int = 100,
    val totalCoinsEarned: Int = 100,
    val totalCoinsSpent: Int = 0,
    val unlockableThemes: List<String> = listOf(
        AppTheme.AMOLED.name, AppTheme.NEON.name, AppTheme.CYBER.name,
        AppTheme.SPACE.name, AppTheme.NATURE.name
    ),
    val unlockedThemeIds: Set<String> = emptySet()
)

data class MultiplayerSession(
    val mode: MultiplayerMode,
    val localPlayerName: String,
    val remotePlayerName: String? = null,
    val activePlayerName: String = localPlayerName,
    val localScore: Int = 0,
    val remoteScore: Int = 0,
    val isActive: Boolean = false,
    val seed: Long = 0L,
    val difficulty: Difficulty = Difficulty.EASY
)
