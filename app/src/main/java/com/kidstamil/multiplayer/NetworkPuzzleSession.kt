package com.kidstamil.multiplayer

import com.kidstamil.domain.model.KidsTamilGame
import com.kidstamil.domain.model.Difficulty
import com.kidstamil.domain.model.MultiplayerMode
import com.kidstamil.domain.model.MultiplayerSession
import com.kidstamil.domain.model.P2PRole
import com.kidstamil.engine.KidsTamilEngine
import com.kidstamil.engine.KidsTamilGenerator
import com.kidstamil.network.P2PMessage
import com.kidstamil.network.P2PSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkPuzzleSession @Inject constructor(
    private val p2pSessionManager: P2PSessionManager
) {
    private val _session = MutableStateFlow<MultiplayerSession?>(null)
    val session: StateFlow<MultiplayerSession?> = _session.asStateFlow()

    private var sharedGame: KidsTamilGame? = null
    private var localName = "Player 1"
    private var remoteName = "Player 2"
    private var isMyTurn = false

    val isLocalTurn: Boolean get() = isMyTurn

    suspend fun startAsHost(localPlayer: String, remotePlayer: String, difficulty: Difficulty) {
        localName = localPlayer
        remoteName = remotePlayer
        val seed = System.currentTimeMillis()
        val level = KidsTamilGenerator.generate(seed, 1, difficulty)
        val game = KidsTamilEngine.createInitialGame(level)
        sharedGame = game
        isMyTurn = true
        publishSession(difficulty, seed, isActive = true)
        p2pSessionManager.send(
            P2PMessage.gameStart(
                levelSeed = seed,
                levelNumber = 1,
                hostName = localName,
                difficulty = difficulty.name
            )
        )
    }

    fun getGame(): KidsTamilGame? = sharedGame

    suspend fun applyLocalAction(action: (KidsTamilGame) -> KidsTamilGame): KidsTamilGame? {
        if (!isMyTurn) return null
        val game = sharedGame ?: return null
        val updated = action(game)
        if (updated == game) return updated
        sharedGame = updated
        isMyTurn = false
        publishSession(updated.level.difficulty, updated.level.seed, isActive = true)
        p2pSessionManager.send(P2PMessage.move(encodeMove(updated, game)))
        return updated
    }

    private fun encodeMove(updated: KidsTamilGame, previous: KidsTamilGame): String = when {
        updated.currentStepIndex > previous.currentStepIndex -> "next"
        updated.traceCompleted && !previous.traceCompleted -> "traceDone"
        updated.tracePoints.size > previous.tracePoints.size -> {
            val point = updated.tracePoints.last()
            "trace:${point.x},${point.y}"
        }
        updated.quizSelectedIndex != null -> "quiz:${updated.quizSelectedIndex}"
        else -> "sync"
    }

    suspend fun onRemoteMessage(message: P2PMessage): KidsTamilGame? {
        return when (message.type) {
            P2PMessage.TYPE_GAME_START -> {
                val seed = message.levelSeed ?: return null
                val levelNumber = message.levelNumber ?: 1
                val difficulty = message.difficulty?.let {
                    runCatching { Difficulty.valueOf(it) }.getOrNull()
                } ?: Difficulty.MEDIUM
                val level = KidsTamilGenerator.generate(seed, levelNumber, difficulty)
                val game = KidsTamilEngine.createInitialGame(level)
                sharedGame = game
                isMyTurn = false
                remoteName = message.playerName ?: remoteName
                publishSession(difficulty, seed, isActive = true)
                game
            }
            P2PMessage.TYPE_MOVE -> {
                if (isMyTurn) return sharedGame
                val payload = message.movePayload ?: return null
                val game = sharedGame ?: return null
                val updated = KidsTamilEngine.applyRemoteMove(game, payload)
                sharedGame = updated
                isMyTurn = true
                publishSession(updated.level.difficulty, updated.level.seed, isActive = true)
                updated
            }
            P2PMessage.TYPE_RESIGN -> {
                isMyTurn = false
                sharedGame
            }
            else -> sharedGame
        }
    }

    suspend fun resign() {
        p2pSessionManager.send(P2PMessage.resign())
    }

    fun onRoundWon(localWon: Boolean) {
        val session = _session.value ?: return
        val newLocal = session.localScore + if (localWon) 1 else 0
        val newRemote = session.remoteScore + if (localWon) 0 else 1
        val newLevel = KidsTamilGenerator.generate(
            session.seed + newLocal + newRemote,
            newLocal + newRemote + 1,
            session.difficulty
        )
        val newGame = KidsTamilEngine.createInitialGame(newLevel)
        sharedGame = newGame
        isMyTurn = if (localWon) {
            p2pSessionManager.role.value != P2PRole.HOST
        } else {
            p2pSessionManager.role.value == P2PRole.HOST
        }
        _session.value = session.copy(
            localScore = newLocal,
            remoteScore = newRemote,
            activePlayerName = if (isMyTurn) localName else remoteName
        )
    }

    fun end() {
        _session.value = null
        sharedGame = null
        isMyTurn = false
    }

    private fun publishSession(difficulty: Difficulty, seed: Long, isActive: Boolean) {
        _session.value = MultiplayerSession(
            mode = MultiplayerMode.LOCAL_P2P,
            localPlayerName = localName,
            remotePlayerName = remoteName,
            activePlayerName = if (isMyTurn) localName else remoteName,
            localScore = _session.value?.localScore ?: 0,
            remoteScore = _session.value?.remoteScore ?: 0,
            isActive = isActive,
            seed = seed,
            difficulty = difficulty
        )
    }
}
