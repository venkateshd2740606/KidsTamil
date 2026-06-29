package com.kidstamil.engine

import com.kidstamil.domain.model.TamilAlphabetEntry
import com.kidstamil.domain.model.Difficulty
import com.kidstamil.domain.model.GenerationProfile
import com.kidstamil.domain.model.KidsTamilLevel
import kotlin.random.Random

object KidsTamilGenerator {

    fun generate(
        seed: Long,
        levelNumber: Int,
        difficulty: Difficulty,
        generationProfile: GenerationProfile = GenerationProfile()
    ): KidsTamilLevel {
        val letterIndex = resolveLetterIndex(levelNumber, generationProfile)
        return buildLevel(seed, levelNumber, difficulty, letterIndex, isEndless = difficulty == Difficulty.ENDLESS)
    }

    fun generateForChallenge(seed: Long, levelNumber: Int, difficulty: Difficulty): KidsTamilLevel =
        generate(seed, levelNumber, difficulty)

    fun seedFromLevelNumber(levelNumber: Int, difficulty: Difficulty): Long {
        val difficultyOffset = difficulty.ordinal * 100_000L
        return levelNumber.toLong() * 9973L + difficultyOffset + 42L
    }

    fun formatShareText(seed: Long, levelNumber: Int, difficulty: Difficulty): String =
        "Kids Tamil Level\nSeed: $seed\nLevel: $levelNumber\nDifficulty: ${difficulty.name}"

    fun buildLevel(
        seed: Long,
        levelNumber: Int,
        difficulty: Difficulty,
        letterIndex: Int,
        isTutorial: Boolean = false,
        isEndless: Boolean = false,
        isReview: Boolean = false
    ): KidsTamilLevel {
        val entry = TamilAlphabetBank.byIndex(letterIndex)
        val random = Random(seed)
        val quiz = buildQuiz(entry, random)
        return KidsTamilLevel(
            seed = seed,
            levelNumber = levelNumber,
            difficulty = difficulty,
            letterIndex = letterIndex,
            entry = entry,
            quizOptions = quiz.options,
            quizCorrectIndex = quiz.correctIndex,
            quizPrompt = "Which letter does ${entry.exampleWord} start with?",
            isTutorial = isTutorial,
            isEndless = isEndless,
            isReview = isReview
        )
    }

    fun buildQuiz(entry: TamilAlphabetEntry, random: Random): QuizBuild {
        val distractors = entry.quizDistractors
            .filter { it != entry.letter }
            .distinct()
            .take(3)
        val options = (listOf(entry.letter) + distractors).take(KidsTamilLevel.OPTIONS_PER_QUIZ)
        val shuffledIndices = options.indices.shuffled(random)
        val shuffledOptions = shuffledIndices.map { options[it] }
        val correctIndex = shuffledOptions.indexOf(entry.letter)
        return QuizBuild(shuffledOptions, correctIndex.coerceAtLeast(0))
    }

    private fun resolveLetterIndex(levelNumber: Int, profile: GenerationProfile): Int {
        val base = (levelNumber - 1).coerceAtLeast(0) % TamilAlphabetBank.all.size
        return (base + profile.letterOffsetModifier).coerceIn(0, TamilAlphabetBank.all.lastIndex)
    }

    data class QuizBuild(val options: List<String>, val correctIndex: Int)
}
