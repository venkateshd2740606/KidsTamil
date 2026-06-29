package com.kidstamil.engine

import com.kidstamil.domain.model.Difficulty
import org.junit.Assert.*
import org.junit.Test

class KidsTamilEngineTest {

    @Test fun tutorialLevel_isValid() {
        val level = TutorialLevels.getTutorialLevel(0)!!
        assertTrue(KidsTamilEngine.validateLevel(level))
    }

    @Test fun nextStep_advancesFromLearnToTrace() {
        val level = TutorialLevels.getTutorialLevel(0)!!
        var game = KidsTamilEngine.createInitialGame(level)
        assertEquals(0, game.currentStepIndex)
        game = KidsTamilEngine.nextStep(game)
        assertEquals(1, game.currentStepIndex)
    }

    @Test fun completeTrace_allowsNextStep() {
        val level = TutorialLevels.getTutorialLevel(0)!!
        var game = KidsTamilEngine.createInitialGame(level)
        game = KidsTamilEngine.nextStep(game)
        game = KidsTamilEngine.completeTrace(game)
        assertTrue(game.traceCompleted)
        assertTrue(KidsTamilEngine.canNextStep(game))
    }

    @Test fun selectQuizAnswer_correctCompletesLevel() {
        val level = TutorialLevels.getTutorialLevel(0)!!
        var game = KidsTamilEngine.createInitialGame(level)
        game = KidsTamilEngine.nextStep(game)
        game = KidsTamilEngine.completeTrace(game)
        game = KidsTamilEngine.nextStep(game)
        game = KidsTamilEngine.selectQuizAnswer(game, level.quizCorrectIndex)
        assertTrue(game.isCompleted)
        assertEquals(1, game.score)
    }

    @Test fun addTracePoint_autoCompletesWhenEnoughPointsNearOutline() {
        val level = TutorialLevels.getTutorialLevel(0)!!
        var game = KidsTamilEngine.createInitialGame(level)
        game = KidsTamilEngine.nextStep(game)
        repeat(KidsTamilEngine.TRACE_MIN_POINTS) { game = KidsTamilEngine.addTracePoint(game, 0.5f, 0.5f) }
        assertTrue(game.traceCompleted)
    }

    @Test fun generatedLevel_isValid() {
        val level = KidsTamilGenerator.generate(12345L, 1, Difficulty.EASY)
        assertTrue(KidsTamilEngine.validateLevel(level))
    }

    @Test fun generator_sameSeed_producesSameQuizOptions() {
        val a = KidsTamilGenerator.generate(999L, 5, Difficulty.MEDIUM)
        val b = KidsTamilGenerator.generate(999L, 5, Difficulty.MEDIUM)
        assertEquals(a.quizOptions, b.quizOptions)
        assertEquals(a.letterIndex, b.letterIndex)
    }

    @Test fun applyHint_eliminatesWrongOption() {
        val level = TutorialLevels.getTutorialLevel(0)!!
        var game = KidsTamilEngine.createInitialGame(level)
        game = KidsTamilEngine.nextStep(game)
        game = KidsTamilEngine.completeTrace(game)
        game = KidsTamilEngine.nextStep(game)
        assertTrue(KidsTamilEngine.canApplyHint(game))
        val hinted = KidsTamilEngine.applyHint(game)
        assertTrue(hinted.eliminatedQuizOptions.isNotEmpty())
        assertFalse(hinted.eliminatedQuizOptions.contains(level.quizCorrectIndex))
    }
}
