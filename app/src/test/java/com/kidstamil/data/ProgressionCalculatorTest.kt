package com.kidstamil.data

import com.kidstamil.domain.model.Difficulty
import com.kidstamil.domain.model.GameStatus
import com.kidstamil.engine.KidsTamilEngine
import com.kidstamil.engine.KidsTamilGenerator
import com.kidstamil.util.ProgressionCalculator
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionCalculatorTest {

    @Test
    fun xpForCompletedGame_isPositive() {
        val level = KidsTamilGenerator.generate(1L, 1, Difficulty.EASY)
        val game = KidsTamilEngine.createInitialGame(level).copy(status = GameStatus.COMPLETED)
        assertTrue(ProgressionCalculator.xpForGame(game) > 0)
    }

    @Test
    fun xpForGame_withHints_isLowerThanWithoutHints() {
        val level = KidsTamilGenerator.generate(1L, 1, Difficulty.EASY)
        val withHints = KidsTamilEngine.createInitialGame(level).copy(hintsUsed = 2, status = GameStatus.COMPLETED)
        val noHints = KidsTamilEngine.createInitialGame(level).copy(hintsUsed = 0, status = GameStatus.COMPLETED)
        assertTrue(ProgressionCalculator.xpForGame(noHints) >= ProgressionCalculator.xpForGame(withHints))
    }
}
