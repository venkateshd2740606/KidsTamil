package com.kidstamil.engine

import com.kidstamil.domain.model.Difficulty

object TutorialLevels {

    val all = (0 until 5).map { index ->
        KidsTamilGenerator.buildLevel(
            seed = index + 1L,
            levelNumber = index + 1,
            difficulty = Difficulty.BEGINNER,
            letterIndex = index,
            isTutorial = true
        )
    }

    fun getTutorialLevel(index: Int) = all.getOrNull(index)
}
