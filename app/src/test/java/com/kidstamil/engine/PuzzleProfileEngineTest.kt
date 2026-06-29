package com.kidstamil.engine

import com.kidstamil.domain.model.Difficulty
import com.kidstamil.domain.model.GameStatus
import com.kidstamil.domain.model.KidsTamilGame
import com.kidstamil.domain.model.PuzzleProfileMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleProfileEngineTest {

    @Test
    fun buildProfile_afterCompletedGame_updatesMetrics() {
        val level = KidsTamilGenerator.generate(42L, 1, Difficulty.EASY)
        val completed = KidsTamilEngine.createInitialGame(level).copy(
            status = GameStatus.COMPLETED,
            moves = 10,
            hintsUsed = 0,
            elapsedSeconds = 30
        )
        val metrics = PuzzleProfileEngine.updateMetrics(PuzzleProfileMetrics(), completed)
        assertEquals(1, metrics.gamesAnalyzed)
        val profile = PuzzleProfileEngine.buildProfile(metrics)
        assertTrue(profile.archetype.name.isNotEmpty())
    }

    @Test
    fun adaptiveGenerationProfile_returnsModifierInRange() {
        val profile = PuzzleProfileEngine.buildProfile(
            PuzzleProfileMetrics(gamesAnalyzed = 5, perfectCompletions = 2)
        )
        val generation = PuzzleProfileEngine.adaptiveGenerationProfile(profile)
        assertTrue(generation.letterOffsetModifier in -1..2)
    }
}
