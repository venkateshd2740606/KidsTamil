package com.kidstamil.multiplayer

import com.kidstamil.domain.model.KidsTamilGame
import com.kidstamil.domain.model.Difficulty
import com.kidstamil.domain.model.LearningStepMode
import com.kidstamil.domain.model.MultiplayerMode
import com.kidstamil.domain.model.MultiplayerSession
import com.kidstamil.engine.KidsTamilEngine
import com.kidstamil.engine.KidsTamilGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PuzzleBotSession @Inject constructor() {
    private val _session = MutableStateFlow<MultiplayerSession?>(null)
    val session: StateFlow<MultiplayerSession?> = _session.asStateFlow()

    private var playerGame: KidsTamilGame? = null
    private var botGame: KidsTamilGame? = null
    private var playerName = "You"
    private val botName = "AI Bot"

    fun start(player: String, difficulty: Difficulty, seed: Long = System.currentTimeMillis()) {
        playerName = player
        val level = KidsTamilGenerator.generate(seed, 1, difficulty)
        val game = KidsTamilEngine.createInitialGame(level)
        playerGame = game
        botGame = game
        _session.value = MultiplayerSession(
            mode = MultiplayerMode.SAME_DEVICE,
            localPlayerName = playerName,
            remotePlayerName = botName,
            activePlayerName = playerName,
            isActive = true,
            seed = seed,
            difficulty = difficulty
        )
    }

    fun getPlayerGame(): KidsTamilGame? = playerGame

    fun applyPlayerAction(game: KidsTamilGame, action: (KidsTamilGame) -> KidsTamilGame): KidsTamilGame? {
        val current = playerGame ?: return null
        if (current != game) return null
        val updated = action(current)
        playerGame = updated
        botGame = updated
        return updated
    }

    fun applyBotMove(): KidsTamilGame? {
        var game = botGame ?: return null
        game = when (game.currentStep) {
            LearningStepMode.LEARN -> KidsTamilEngine.nextStep(game)
            LearningStepMode.TRACE -> {
                var traced = game
                repeat(8) {
                    traced = KidsTamilEngine.addTracePoint(traced, 0.5f, 0.5f)
                }
                KidsTamilEngine.completeTrace(traced)
            }
            LearningStepMode.QUIZ -> {
                val answer = KidsTamilEngine.botQuizAnswer(game) ?: return game
                KidsTamilEngine.selectQuizAnswer(game, answer)
            }
            null -> game
        }
        playerGame = game
        botGame = game
        val session = _session.value
        if (session != null && game.isCompleted) {
            _session.value = session.copy(
                remoteScore = session.remoteScore + 1,
                activePlayerName = playerName
            )
        }
        return game
    }

    fun onPlayerWon() {
        val session = _session.value ?: return
        _session.value = session.copy(
            localScore = session.localScore + 1,
            activePlayerName = playerName
        )
        startNewRound(session)
    }

    fun onBotWon() {
        val session = _session.value ?: return
        _session.value = session.copy(
            remoteScore = session.remoteScore + 1,
            activePlayerName = playerName
        )
        startNewRound(session)
    }

    private fun startNewRound(session: MultiplayerSession) {
        val newSeed = session.seed + session.localScore + session.remoteScore
        val level = KidsTamilGenerator.generate(newSeed, session.localScore + session.remoteScore + 1, session.difficulty)
        val game = KidsTamilEngine.createInitialGame(level)
        playerGame = game
        botGame = game
    }

    fun end() {
        _session.value = null
        playerGame = null
        botGame = null
    }
}
