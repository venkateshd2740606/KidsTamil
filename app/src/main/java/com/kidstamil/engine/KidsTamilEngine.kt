package com.kidstamil.engine

import com.kidstamil.domain.model.GameStatus
import com.kidstamil.domain.model.KidsTamilGame
import com.kidstamil.domain.model.KidsTamilLevel
import com.kidstamil.domain.model.LearningStepMode
import com.kidstamil.domain.model.TracePoint

object KidsTamilEngine {

    const val TRACE_MIN_POINTS = 6
    const val TRACE_NEAR_OUTLINE_MIN = 4
    const val LETTER_REGION_MIN_X = 0.22f
    const val LETTER_REGION_MAX_X = 0.78f
    const val LETTER_REGION_MIN_Y = 0.18f
    const val LETTER_REGION_MAX_Y = 0.82f

    fun createInitialGame(level: KidsTamilLevel): KidsTamilGame = KidsTamilGame(level = level)

    fun validateLevel(level: KidsTamilLevel): Boolean {
        if (level.letterIndex !in 0..11) return false
        if (level.quizOptions.size != KidsTamilLevel.OPTIONS_PER_QUIZ) return false
        if (level.quizCorrectIndex !in 0 until KidsTamilLevel.OPTIONS_PER_QUIZ) return false
        return level.quizOptions[level.quizCorrectIndex] == level.entry.letter
    }

    fun currentStep(game: KidsTamilGame): LearningStepMode? = game.currentStep

    fun canNextStep(game: KidsTamilGame): Boolean {
        if (game.isCompleted || game.awaitingAdvance) return false
        return when (game.currentStep) {
            LearningStepMode.LEARN -> true
            LearningStepMode.TRACE -> game.traceCompleted
            LearningStepMode.QUIZ -> false
            null -> false
        }
    }

    fun nextStep(game: KidsTamilGame): KidsTamilGame {
        if (!canNextStep(game)) return game
        val now = System.currentTimeMillis()
        val nextIndex = game.currentStepIndex + 1
        if (nextIndex >= game.level.steps.size) {
            return game.copy(
                status = GameStatus.COMPLETED,
                completedAt = now,
                lastPlayedAt = now,
                moves = game.moves + 1
            )
        }
        return game.copy(
            currentStepIndex = nextIndex,
            tracePoints = emptyList(),
            traceCompleted = false,
            quizSelectedIndex = null,
            quizAnswered = false,
            lastAnswerCorrect = null,
            moves = game.moves + 1,
            lastPlayedAt = now
        )
    }

    fun addTracePoint(game: KidsTamilGame, x: Float, y: Float): KidsTamilGame {
        if (game.isCompleted || game.currentStep != LearningStepMode.TRACE || game.traceCompleted) {
            return game
        }
        val clamped = TracePoint(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))
        val updated = game.copy(
            tracePoints = game.tracePoints + clamped,
            lastPlayedAt = System.currentTimeMillis()
        )
        return if (isTraceSufficient(updated)) updated.copy(traceCompleted = true) else updated
    }

    fun completeTrace(game: KidsTamilGame): KidsTamilGame {
        if (game.isCompleted || game.currentStep != LearningStepMode.TRACE) return game
        return game.copy(
            traceCompleted = true,
            moves = game.moves + 1,
            lastPlayedAt = System.currentTimeMillis()
        )
    }

    fun canAnswerQuiz(game: KidsTamilGame): Boolean =
        !game.isCompleted &&
            game.currentStep == LearningStepMode.QUIZ &&
            !game.quizAnswered &&
            !game.awaitingAdvance

    fun selectQuizAnswer(game: KidsTamilGame, index: Int): KidsTamilGame {
        if (!canAnswerQuiz(game)) return game
        if (index !in 0 until KidsTamilLevel.OPTIONS_PER_QUIZ) return game
        if (index in game.eliminatedQuizOptions) return game

        val correct = index == game.level.quizCorrectIndex
        val newScore = game.score + if (correct) 1 else 0
        val newStreak = if (correct) game.streak + 1 else 0
        val newBestStreak = maxOf(game.bestStreak, newStreak)
        val now = System.currentTimeMillis()

        return if (correct) {
            game.copy(
                quizSelectedIndex = index,
                quizAnswered = true,
                score = newScore,
                streak = newStreak,
                bestStreak = newBestStreak,
                awaitingAdvance = true,
                lastAnswerCorrect = true,
                moves = game.moves + 1,
                status = GameStatus.COMPLETED,
                completedAt = now,
                lastPlayedAt = now
            )
        } else {
            game.copy(
                quizSelectedIndex = index,
                score = newScore,
                streak = newStreak,
                bestStreak = newBestStreak,
                awaitingAdvance = true,
                lastAnswerCorrect = false,
                moves = game.moves + 1,
                lastPlayedAt = now
            )
        }
    }

    fun advanceAfterQuiz(game: KidsTamilGame): KidsTamilGame {
        if (!game.awaitingAdvance || game.lastAnswerCorrect != false) return game
        return game.copy(
            awaitingAdvance = false,
            quizSelectedIndex = null,
            lastAnswerCorrect = null,
            lastPlayedAt = System.currentTimeMillis()
        )
    }

    fun isWon(game: KidsTamilGame): Boolean = game.isCompleted
    fun optimalScore(game: KidsTamilGame): Int = 1

    fun canApplyHint(game: KidsTamilGame): Boolean {
        if (game.currentStep != LearningStepMode.QUIZ || !canAnswerQuiz(game)) return false
        val wrongOptions = (0 until KidsTamilLevel.OPTIONS_PER_QUIZ)
            .filter { it != game.level.quizCorrectIndex && it !in game.eliminatedQuizOptions }
        return wrongOptions.isNotEmpty()
    }

    fun applyHint(game: KidsTamilGame): KidsTamilGame {
        if (!canApplyHint(game)) return game
        val wrongToEliminate = (0 until KidsTamilLevel.OPTIONS_PER_QUIZ)
            .first { it != game.level.quizCorrectIndex && it !in game.eliminatedQuizOptions }
        return game.copy(
            eliminatedQuizOptions = game.eliminatedQuizOptions + wrongToEliminate,
            hintsUsed = game.hintsUsed + 1,
            lastPlayedAt = System.currentTimeMillis()
        )
    }

    fun isTraceSufficient(game: KidsTamilGame): Boolean {
        if (game.tracePoints.size < TRACE_MIN_POINTS) return false
        val nearCount = game.tracePoints.count { isNearLetterOutline(it) }
        return nearCount >= TRACE_NEAR_OUTLINE_MIN
    }

    fun isNearLetterOutline(point: TracePoint): Boolean =
        point.x in LETTER_REGION_MIN_X..LETTER_REGION_MAX_X &&
            point.y in LETTER_REGION_MIN_Y..LETTER_REGION_MAX_Y

    fun formatP2PMove(action: String, payload: String = ""): String =
        if (payload.isEmpty()) action else "$action:$payload"

    fun parseP2PMove(payload: String): Pair<String, String>? {
        val parts = payload.split(":", limit = 2)
        return when (parts.size) {
            1 -> parts[0] to ""
            2 -> parts[0] to parts[1]
            else -> null
        }
    }

    fun applyRemoteMove(game: KidsTamilGame, payload: String): KidsTamilGame {
        val (action, data) = parseP2PMove(payload) ?: return game
        return when (action) {
            "next" -> nextStep(game)
            "trace" -> {
                val coords = data.split(",")
                if (coords.size != 2) return game
                val x = coords[0].toFloatOrNull() ?: return game
                val y = coords[1].toFloatOrNull() ?: return game
                addTracePoint(game, x, y)
            }
            "traceDone" -> completeTrace(game)
            "quiz" -> {
                val index = data.toIntOrNull() ?: return game
                selectQuizAnswer(game, index)
            }
            else -> game
        }
    }

    fun botQuizAnswer(game: KidsTamilGame, accuracy: Float = 0.7f): Int? {
        if (!canAnswerQuiz(game)) return null
        val available = (0 until KidsTamilLevel.OPTIONS_PER_QUIZ)
            .filter { it !in game.eliminatedQuizOptions }
        if (available.isEmpty()) return null
        return if (kotlin.random.Random.nextFloat() < accuracy) {
            game.level.quizCorrectIndex
        } else {
            available.filter { it != game.level.quizCorrectIndex }.randomOrNull()
                ?: game.level.quizCorrectIndex
        }
    }
}
